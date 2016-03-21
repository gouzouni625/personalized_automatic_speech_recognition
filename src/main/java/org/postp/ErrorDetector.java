package org.postp;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;
import static org.utilities.Utilities.collectionToArray;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;


public class ErrorDetector {
    public ErrorDetector(String pOSTaggerModelPath, String textCorpusPath) throws IOException {
        pOSTagger_ = new POSTaggerME(new POSModel(new FileInputStream(pOSTaggerModelPath)));

        // Use a Hashtable to save POS Patterns to avoid saving identical patterns twice.
        // HashSet could find identical String arrays since they weren't the same object, so a hash is needed to compare
        // String arrays with one another.
        Hashtable<Integer, Tags[]> pOSPatterns = new Hashtable<Integer, Tags[]>();

        Scanner scanner = new Scanner(new File(textCorpusPath));
        while (scanner.hasNextLine()) {
            Tags[] taggedLine = tag(scanner.nextLine());
            pOSPatterns.put(Arrays.hashCode(taggedLine), taggedLine);
        }
        scanner.close();

        // Move Hashtable data to array.
        pOSPatterns_ = new Tags[pOSPatterns.size()][];
        collectionToArray(pOSPatterns.values(), pOSPatterns_);
    }

    public int[] process(TextLine aSROutput) {
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
        return getErrorCandidateWords(abbreviatedASROutputPOSPattern,
                Tags.tagArrayToAbbreviatedString(pOSPatterns_[selectedPOSPattern]));
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
        // TODO For now, the Error Detector removes punctuation marks from both the corpus and the ASR output.
        // TODO This should change in the future as the Error Detector should include punctuation marks.
        return Tags.createFromTagsStringArray(pOSTagger_.tag(line.split(true)));
    }

    private Tags[] tag(String line){
        return tag(new TextLine(line));
    }

    private int[] getErrorCandidateWords(String source, String destination) {
        int[][] levenshteinPath = new LevenshteinMatrix(source, destination).getPath();

        int pathLength = levenshteinPath.length;

        int[] errorCandidateWords = new int[pathLength];
        for(int i = 0;i < pathLength;i++){
            errorCandidateWords[i] = levenshteinPath[i][1];
        }

        return errorCandidateWords;
    }

    private POSTaggerME pOSTagger_;

    Tags[][] pOSPatterns_;

}
