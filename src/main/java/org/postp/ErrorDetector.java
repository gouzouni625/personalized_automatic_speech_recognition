package org.postp;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;
import static org.utilities.Utilities.collectionToArray;

import opennlp.tools.postag.POSTaggerME;
import org.prep.Corpus;

import java.util.*;


public class ErrorDetector {
    public ErrorDetector(Corpus corpus, POSTaggerME pOSTagger) {
        pOSTagger_ = pOSTagger;

        createPOSPatterns(corpus);
    }

    private void createPOSPatterns(Corpus corpus){
        Hashtable<Integer, Tags[]> pOSPatterns = new Hashtable<Integer, Tags[]>();

        TextLine[] corpusSentences = corpus.getSentences();
        for (TextLine sentence : corpusSentences) {
            Tags[] taggedSentence = tag(sentence);
            pOSPatterns.put(Arrays.hashCode(taggedSentence), taggedSentence);
        }

        // Move Hashtable data to array.
        pOSPatterns_ = new Tags[pOSPatterns.size()][];
        collectionToArray(pOSPatterns.values(), pOSPatterns_);
    }

    public ErrorWord[] process(TextLine aSROutput) {
        // Tag the ASR output.
        String abbreviatedASROutputPOSPattern = Tags.tagArrayToAbbreviatedString(tag(aSROutput));

        // Find the POS pattern that is closer to the POST patter of the ASR output.
        int selectedPOSPattern = 0;
        int currentScore;
        int bestScore = getLevenshteinDistance(abbreviatedASROutputPOSPattern,
                Tags.tagArrayToAbbreviatedString(pOSPatterns_[0]));
        for (int i = 1, n = pOSPatterns_.length; i < n; i++) {
            currentScore = getLevenshteinDistance(abbreviatedASROutputPOSPattern,
                    Tags.tagArrayToAbbreviatedString(pOSPatterns_[i]));

            if (currentScore < bestScore) {
                bestScore = currentScore;
                selectedPOSPattern = i;
            }
        }

        // Find the error candidate words from the ASR output based on the selected POS pattern.
        // The ASR output is not obligated to include all the tags that the selected POST patterns includes.
        int[] errorWordIndices = getErrorCandidateWords(abbreviatedASROutputPOSPattern,
                Tags.tagArrayToAbbreviatedString(pOSPatterns_[selectedPOSPattern]));

        String[] words = aSROutput.split();

        ErrorWord[] errorWords = new ErrorWord[errorWordIndices.length];
        int index = 0;
        for(int errorWordIndex : errorWordIndices){
            errorWords[index] = new ErrorWord(words[errorWordIndex], aSROutput, errorWordIndex);

            index++;
        }

        return errorWords;
    }

    // TODO Add part of speech for each tag.
    public enum Tags {
        CC("", "0"),
        CD("", "1"),
        DT("", "2"),
        EX("", "3"),
        FW("", "4"),
        IN("", "5"),
        JJ("", "6"),
        JJR("", "7"),
        JJS("", "8"),
        LS("", "9"),
        MD("", "A"),
        NN("", "B"),
        NNS("", "C"),
        NNP("", "D"),
        NNPS("", "E"),
        PDT("", "F"),
        POS("", "G"),
        PRP("", "H"),
        PRP_DS("", "I"),
        RB("", "J"),
        RBR("", "K"),
        RBS("", "L"),
        RP("", "M"),
        SYM("", "N"),
        TO("", "O"),
        UH("", "P"),
        VB("", "Q"),
        VBD("", "R"),
        VBG("", "S"),
        VBN("", "T"),
        VBP("", "U"),
        VBZ("", "V"),
        WDT("", "W"),
        WP("", "X"),
        WP_DS("", "Y"),
        WRB("", "Z");

        Tags(String partOfSpeech, String abbreviation) {
            partOfSpeech_ = partOfSpeech;
            abbreviation_ = abbreviation;
        }

        public static Tags createFromTagString(String tagString) {
            for (Tags tag : Tags.values()) {
                if (tagString.equals(tag.toString())) {
                    return tag;
                }
            }

            // PRP_DS is a replacement for PRP$ to avoid using the $ (Dollar Symbol).
            // WP_DS is a replacement for WP$ to avoid using the $ (Dollar Symbol).
            if (tagString.equals("PRP$")) {
                return PRP_DS;
            } else if (tagString.equals("WP$")) {
                return WP_DS;
            }

            return null;
        }

        public static Tags[] createFromTagsStringArray(String[] tagStrings) {
            Tags[] tagArray = new Tags[tagStrings.length];

            for (int i = 0, n = tagArray.length; i < n; i++) {
                tagArray[i] = createFromTagString(tagStrings[i]);
            }

            return tagArray;
        }

        public static String tagArrayToAbbreviatedString(Tags[] tagArray) {
            StringBuilder stringBuilder = new StringBuilder();

            for (Tags tag : tagArray) {
                stringBuilder.append(tag.getAbbreviation());
            }

            return stringBuilder.toString();
        }

        public String getPartOfSpeech() {
            return partOfSpeech_;
        }

        public String getAbbreviation() {
            return abbreviation_;
        }

        private String partOfSpeech_;
        private String abbreviation_;
    }

    private Tags[] tag(TextLine line) {
        return Tags.createFromTagsStringArray(pOSTagger_.tag(line.split()));
    }

    private int[] getErrorCandidateWords(String source, String destination) {
        int[][] levenshteinPath = new LevenshteinMatrix(source, destination).getPath();

        int pathLength = levenshteinPath.length;

        int[] errorCandidateWords = new int[pathLength];
        for (int i = 0; i < pathLength; i++) {
            errorCandidateWords[i] = levenshteinPath[i][1];
        }

        return errorCandidateWords;
    }

    private final POSTaggerME pOSTagger_;

    Tags[][] pOSPatterns_;

}
