package org.postp;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;
import static org.utilities.Utilities.collectionToArray;
import static org.utilities.Utilities.objectCollectionToPrimitiveArray;

import opennlp.tools.postag.POSTaggerME;
import org.prep.Corpus;

import java.util.HashSet;


public class ErrorDetector {
    public ErrorDetector(Corpus corpus, POSTaggerME pOSTagger) {
        pOSTagger_ = pOSTagger;
        corpus_ = corpus;

        createPOSPatterns(corpus);
    }

    private void createPOSPatterns(Corpus corpus){
        HashSet<String> pOSPatterns = new HashSet<String>();

        TextLine[] corpusSentences = corpus.getSentences();
        for (TextLine sentence : corpusSentences) {
            pOSPatterns.add(Tags.tagArrayToAbbreviatedString(tag(sentence)));
        }

        pOSPatterns_ = new String[pOSPatterns.size()];
        collectionToArray(pOSPatterns, pOSPatterns_);
    }

    public ErrorWord[] process(TextLine aSROutput) {
        // If the ASR output exists inside the corpus, then there are no errors
        for(TextLine sentence : corpus_){
            if(sentence.getLine().contains(aSROutput.getLine())){
                return new ErrorWord[] {};
            }
        }

        // Tag the ASR output.
        String abbreviatedASROutputPOSPattern = Tags.tagArrayToAbbreviatedString(tag(aSROutput));

        // Find the POS pattern that is closer to the POST patter of the ASR output.
        int selectedPOSPattern = 0;
        int currentScore;
        int bestScore = getLevenshteinDistance(abbreviatedASROutputPOSPattern, pOSPatterns_[0]);
        for (int i = 1, n = pOSPatterns_.length; i < n; i++) {
            currentScore = getLevenshteinDistance(abbreviatedASROutputPOSPattern, pOSPatterns_[i]);

            if (currentScore < bestScore) {
                bestScore = currentScore;
                selectedPOSPattern = i;
            }
        }

        // Find the error candidate words from the ASR output based on the selected POS pattern.
        // The ASR output is not obligated to include all the tags that the selected POST patterns includes.
        int[] errorWordIndices = getErrorCandidateWords(abbreviatedASROutputPOSPattern,
                pOSPatterns_[selectedPOSPattern]);

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

        HashSet<Integer> uniqueErrorWords = new HashSet<Integer>();

        for(int[] step : levenshteinPath){
            uniqueErrorWords.add(step[1]);
        }

        int numberOfErrorWords = uniqueErrorWords.size();
        int[] errorCandidateWords = new int[numberOfErrorWords];

        objectCollectionToPrimitiveArray(uniqueErrorWords, errorCandidateWords);

        return errorCandidateWords;
    }

    public void setCorpus(Corpus corpus){
        corpus_ = corpus;

        createPOSPatterns(corpus);
    }

    public Corpus getCorpus(){
        return corpus_;
    }

    private final POSTaggerME pOSTagger_;

    private Corpus corpus_;

    String[] pOSPatterns_;

}
