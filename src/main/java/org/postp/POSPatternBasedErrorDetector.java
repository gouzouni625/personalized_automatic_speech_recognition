package org.postp;

import static java.lang.Integer.min;
import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;
import static org.utilities.Utilities.collectionToArray;
import static org.utilities.Utilities.objectCollectionToPrimitiveArray;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;


public class POSPatternBasedErrorDetector {
    public POSPatternBasedErrorDetector(String pOSTaggerModelPath, String textCorpusPath) throws IOException {
        pOSTagger_ = new POSTaggerME(new POSModel(new FileInputStream(pOSTaggerModelPath)));

        // Use a HashSet to save the text lines to avoid saving identical lines twice.
        HashSet<String> lines = new HashSet<String>();

        Scanner scanner = new Scanner(new File(textCorpusPath));
        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }
        scanner.close();

        // Use a Hashtable to save POS Patterns to avoid saving identical patterns twice.
        // HashSet could find identical String arrays since they weren't the same object, so a hash is needed to compare
        // String arrays with one another.
        Hashtable<Integer, Tags[]> pOSPatterns = new Hashtable<Integer, Tags[]>();

        for (String line : lines) {
            Tags[] taggedLine = tag(line);
            pOSPatterns.put(Arrays.hashCode(taggedLine), taggedLine);
        }

        // Move Hashtable data to array.
        pOSPatterns_ = new Tags[pOSPatterns.size()][];
        collectionToArray(pOSPatterns.values(), pOSPatterns_);
    }

    public int[] process(String aSROutput) {
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
        return getErrorWordCandidates(abbreviatedASROutputPOSPattern,
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

    private Tags[] tag(String line) {
        return Tags.createFromTagsStringArray(pOSTagger_.tag(new TextLine(line).split(true)));
    }

    private int[] getErrorWordCandidates(String source, String destination) {
        int sourceLength = source.length();
        int destinationLength = destination.length();

        // Initialize the Levenshtein matrix.
        int[][] levenshteinMatrix = new int[destinationLength + 1][sourceLength + 1];
        for (int i = 0; i <= destinationLength; i++) {
            for (int j = 0; j <= sourceLength; j++) {
                if (j == 0 && i == 0) {
                    levenshteinMatrix[0][0] = 0;
                } else if (i == 0) {
                    levenshteinMatrix[0][j] = j;
                } else if (j == 0) {
                    levenshteinMatrix[i][0] = i;
                } else {
                    levenshteinMatrix[i][j] = 0;
                }
            }
        }

        // Build the Levenshtein matrix.
        int substitutionCost;
        for (int j = 1; j <= sourceLength; j++) {
            for (int i = 1; i <= destinationLength; i++) {
                if (destination.charAt(i - 1) == source.charAt(j - 1)) {
                    substitutionCost = 0;
                } else {
                    substitutionCost = 1;
                }

                levenshteinMatrix[i][j] = min(
                        levenshteinMatrix[i - 1][j] + 1, min(
                                levenshteinMatrix[i][j - 1] + 1,
                                levenshteinMatrix[i - 1][j - 1] + substitutionCost
                        )
                );
            }
        }

        HashSet<Integer> errorCandidateWords = new HashSet<Integer>();
        int currentScore = levenshteinMatrix[destinationLength][sourceLength];

        int row = destinationLength;
        int column = sourceLength;

        int leftValue;
        int aboveValue;
        int diagonalValue;

        int minValue;
        while (currentScore > 0) {
            leftValue = levenshteinMatrix[row][column - 1];
            aboveValue = levenshteinMatrix[row - 1][column];
            diagonalValue = levenshteinMatrix[row - 1][column - 1];

            minValue = min(leftValue, min(aboveValue, diagonalValue));
            if (currentScore != minValue) {
                // Note that in Levenshtein matrix, columns start counting from 1 not zero.
                errorCandidateWords.add(column - 1);
            }

            if (minValue == diagonalValue) {
                row--;
                column--;
            } else if (minValue == leftValue) {
                column--;
            } else {
                row--;
            }

            currentScore = minValue;
        }

        int[] errorCandidateWordsArray = new int[errorCandidateWords.size()];
        objectCollectionToPrimitiveArray(errorCandidateWords, errorCandidateWordsArray);
        return errorCandidateWordsArray;
    }

    private POSTaggerME pOSTagger_;

    Tags[][] pOSPatterns_;

}
