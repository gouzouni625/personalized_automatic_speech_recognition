package org.pasr.postp.correctors;

import org.pasr.model.asr.dictionary.Dictionary;
import org.pasr.model.text.Corpus;
import org.pasr.model.text.Word;
import org.pasr.model.text.WordSequence;
import org.pasr.postp.detectors.Detector;
import org.pasr.utilities.LevenshteinMatrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.Integer.min;


/**
 * @class Corrector
 * @brief Implements a corrector using a Corpus and a Dictionary
 */
public class Corrector {

    /**
     * @brief Constructor
     *
     * @param corpus
     *     The Corpus to be used
     * @param dictionary
     *     The Dictionary to be used
     */
    public Corrector (Corpus corpus, Dictionary dictionary) {
        if (corpus == null) {
            throw new IllegalArgumentException("corpus must not be null!");
        }

        if (dictionary == null) {
            throw new IllegalArgumentException("dictionary must not be null!");
        }

        corpus_ = corpus;
        dictionary_ = dictionary;

        detectorList_ = new ArrayList<>();
    }

    /**
     * @brief Adds a Detector to this Corrector Detector list
     *
     * @param detector
     *     The Detector to be added
     */
    public void addDetector (Detector detector) {
        if (detector == null) {
            throw new IllegalArgumentException("detector must not be null!");
        }

        detectorList_.add(detector);
    }

    /**
     * @brief Corrects a given String
     *
     * @param input
     *     The String to correct
     *
     * @return The corrected String
     */
    public String correct (String input) {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null!");
        }

        return correct("", input);
    }

    /**
     * @brief Corrects a given String
     *
     * @param onTheLeft
     *     The String that precedes the input
     * @param input
     *     The String to correct
     *
     * @return The corrected String
     */
    public String correct (String onTheLeft, String input) {
        if (onTheLeft == null) {
            throw new IllegalArgumentException("onTheLeft must not be null!");
        }

        if (input == null) {
            throw new IllegalArgumentException("input must not be null!");
        }

        // If the input is contained inside the corpus as is, consider it correct.
        if (corpus_.contains(input)) {
            return input;
        }

        WordSequence onTheLeftWS = new WordSequence(onTheLeft);
        WordSequence inputWS = new WordSequence(input);

        List<Range> changeablePartIndexList = getChangeablePartList(inputWS);
        int size = changeablePartIndexList.size();

        if (size == 0) {
            return input;
        }

        // Score and replace first changeable part
        Range part = changeablePartIndexList.get(0);
        onTheLeftWS.addAll(inputWS.subSequence(0, part.getLeft()));

        WordSequence onTheRightWS;
        if (changeablePartIndexList.size() > 1) {
            onTheRightWS = inputWS.subSequence(part.getRight(),
                changeablePartIndexList.get(1).getLeft());
        }
        else {
            onTheRightWS = inputWS.subSequence(part.getRight());
        }

        WordSequence substitute = scoreAndReplace(
            onTheLeftWS,
            inputWS.subSequence(part.getLeft(), part.getRight()),
            onTheRightWS
        );

        onTheLeftWS.addAll(substitute);
        onTheLeftWS.addAll(onTheRightWS);

        if (size == 1) {
            String result = onTheLeftWS.toString();
            return checkResult(result) ? result : input;
        }

        int index = 1;
        while (index < size) {
            part = changeablePartIndexList.get(index);

            if (index < size - 1) {
                onTheRightWS = inputWS.subSequence(part.getRight(),
                    changeablePartIndexList.get(index + 1).getLeft());
            }
            else {
                onTheRightWS = inputWS.subSequence(part.getRight());
            }

            substitute = scoreAndReplace(
                onTheLeftWS,
                inputWS.subSequence(part.getLeft(), part.getRight()),
                onTheRightWS
            );

            onTheLeftWS.addAll(substitute);
            onTheLeftWS.addAll(onTheRightWS);

            index++;
        }

        String result = onTheLeftWS.toString();
        return checkResult(result) ? result : input;
    }

    /**
     * @brief Returns a List of changeable Range objects upon the given WordSequence
     *
     * @param wordSequence
     *     The given WordSequence
     *
     * @return A List of changeable Range objectsA upon the given WordSequence
     */
    private List<Range> getChangeablePartList (WordSequence wordSequence) {
        int size = wordSequence.size();

        Set<Integer> errorWordIndexSet = getErrorWordSet(wordSequence).stream()
            .map(wordSequence:: indexOf)
            .collect(Collectors.toSet());

        // If there are no error words, there are no changeable parts
        if (errorWordIndexSet.size() == 0) {
            return new ArrayList<>();
        }

        // For each error word, consider the one on its left and the one on its right also as error
        // words
        List<Integer> neighbourErrorWordIndexList = new ArrayList<>();
        for (int index : errorWordIndexSet) {
            if (index > 0) {
                neighbourErrorWordIndexList.add(index - 1);
            }

            if (index < size - 1) {
                neighbourErrorWordIndexList.add(index + 1);
            }
        }

        errorWordIndexSet.addAll(neighbourErrorWordIndexList);

        // Given the erorr word index set, create the error word ranges.
        List<Range> rangeList = new ArrayList<>();
        int currentLeft_ = 0;
        int currentRight_ = 0;
        for (int i = 0; i < size; i++) {
            if (errorWordIndexSet.contains(i)) {
                currentRight_++;
            }
            else {
                if (currentLeft_ < currentRight_) {
                    rangeList.add(new Range(currentLeft_, currentRight_));
                }
                currentLeft_ = i + 1;
                currentRight_ = currentLeft_;
            }
        }
        if (currentLeft_ < currentRight_) {
            rangeList.add(new Range(currentLeft_, currentRight_));
        }

        return rangeList;
    }

    /**
     * @brief Returns a Set of the error words inside the given WordSequence
     *        The detection of the error words is done using the detectors of this corrector.
     *
     * @param wordSequence
     *     The WordSequence
     *
     * @return A Set of the error words inside the given WordSequence
     */
    private Set<Word> getErrorWordSet (WordSequence wordSequence) {
        Set<Word> errorWordSet = new HashSet<>();

        for (Detector detector : detectorList_) {
            errorWordSet.addAll(detector.detect(wordSequence));
        }

        return errorWordSet;
    }

    /**
     * @brief Returns the replacing WordSequence for the given changeable part
     *
     * @param onTheLeft
     *     The WordSequence on the left of the changeable part
     * @param changeablePart
     *     The changeable part
     * @param onTheRight
     *     The WordSequence on the right of the changeable part
     *
     * @return The replacing WordSequence for the given changeable part
     */
    private WordSequence scoreAndReplace (WordSequence onTheLeft,
                                          WordSequence changeablePart,
                                          WordSequence onTheRight) {
        if (onTheLeft.size() == 0 && onTheRight.size() == 0) {
            return replaceWithoutContext(changeablePart);
        }

        String[] changeablePartPhones = dictionary_.getPhonesInLine(changeablePart).stream()
            .toArray(String[] ::new);

        // String: replacing part (part candidate to replace the changeable part)
        // String: regular expression
        // Double: sum of scores of the replacing part on the matched word patterns of the
        //         regular expression
        Map<String, Map<String, Double>> scoreMap = new HashMap<>();
        Map<String, Integer> contextMap = buildContextMap(onTheLeft, onTheRight);
        // Add the changeable part number of phones to each context entry number of phones.
        contextMap.entrySet().stream()
            .forEach(entry -> entry.setValue(entry.getValue() + changeablePartPhones.length));
        Map<String, Double> candidateScoreMap = new HashMap<>();

        // for each regular expression in the context map
        for (String regExp : contextMap.keySet()) {
            // for each sentence inside the corpus
            for (WordSequence wordSequence : corpus_) {
                // match the regular expression on the sentence
                Matcher matcher = Pattern.compile(regExp)
                    .matcher(wordSequence.toString());
                // score each match and store it
                while (matcher.find()) {
                    String candidate = matcher.group();

                    if (scoreMap.containsKey(candidate)) {
                        Map<String, Double> candidateMap = scoreMap.get(candidate);

                        if (candidateMap.containsKey(regExp)) {
                            candidateMap.put(
                                regExp,
                                candidateMap.get(regExp) + candidateScoreMap.get(candidate)
                            );
                        }
                        else {
                            candidateMap.put(regExp, candidateScoreMap.get(candidate));
                        }
                    }
                    else {
                        Map<String, Double> candidateMap = new HashMap<>();
                        double candidateScore = score(
                            new WordSequence(candidate),
                            changeablePartPhones
                        );
                        candidateMap.put(regExp, candidateScore);
                        candidateScoreMap.put(candidate, candidateScore);

                        scoreMap.put(candidate, candidateMap);
                    }
                }
            }
        }

        // Choose the best candidate based on its score
        double bestScore = Double.NEGATIVE_INFINITY;
        String chosenCandidate = "";
        for (Map.Entry<String, Map<String, Double>> candidateEntry : scoreMap.entrySet()) {
            Map<String, Double> candidateMap = candidateEntry.getValue();

            double candidateScore = 0;
            for (Map.Entry<String, Double> candidateMapEntry : candidateMap.entrySet()) {
                candidateScore += 1 -
                    (candidateScoreMap.get(candidateEntry.getKey()) / contextMap.get(candidateMapEntry.getKey()));
            }

            if (candidateScore > bestScore) {
                bestScore = candidateScore;
                chosenCandidate = candidateEntry.getKey();
            }
        }

        return chosenCandidate.isEmpty() ? changeablePart : new WordSequence(chosenCandidate);
    }

    /**
     * @brief Returns the replacing WordSequence for the given changeable part
     *
     * @param changeablePart
     *     The changeable part
     *
     * @return The replacing WordSequence for the given changeable part
     */
    private WordSequence replaceWithoutContext (WordSequence changeablePart) {
        if (changeablePart.isEmpty()) {
            return new WordSequence("");
        }

        List<String> changeablePartPhones = dictionary_.getPhonesInLine(changeablePart);

        int minDistance = Integer.MAX_VALUE;
        WordSequence bestMatch = null;

        // Check which sub-part of every sentence inside the corpus matches better with the given
        // changeable part.
        for (WordSequence wordSequence : corpus_) {
            List<List<String>> wordSequenceWordPhoneList = dictionary_.getPhones(wordSequence);

            for (int i = 0, n = wordSequenceWordPhoneList.size(); i < n; i++) {
                for (int j = i + 1; j <= n; j++) {
                    List<String> candidate = new ArrayList<>();
                    wordSequenceWordPhoneList.subList(i, j).stream()
                        .forEach(candidate:: addAll);

                    int currentDistance = LevenshteinMatrix.getDistance(
                        changeablePartPhones, candidate
                    );

                    if (currentDistance < minDistance) {
                        minDistance = currentDistance;

                        bestMatch = wordSequence.subSequence(i, j);
                    }
                }
            }
        }

        return bestMatch == null ? new WordSequence("") : bestMatch;
    }

    /**
     * @brief Builds the context map given the WordSequence on the left and the one on the right
     *        The context map contains all the regular expressions that should be matched on the
     *        corpus in order to find the best replacement for a changeable part. For example:
     *
     *        onTheLeft = wl1,wl2,wl3,wl4,wl5
     *        onTheRight = wr1,wr2,wr3,wr4,wr5
     *
     *        contextMap = {
     *            "(?<=wl1 )(.*)": numberOfPhones(wl1),
     *            "(?<=wl1wl2 )(.*)": numberOfPhones(wl1) + numberOfPhones(wl2),
     *            ...
     *            "(.*)(?= wr1)": numberOfPhones(wr1),
     *            "(.*)(?= wr1wr2)": numberOfPhones(wr1) + numberOfPhones(wr2),
     *            ...
     *            "(?<=wl1 )(.*)(?= wr1)": numberOfPhones(wl1) + numberOfPhones(wr1),
     *            "(?<=wl1wl2 )(.*)(?= wr1)": numberOfPhones(wl1) + numberOfPhones(wl2) + numberOfPhones(wr1),
     *            ...
     *        }
     *
     * @param onTheLeft
     *     The WordSequence on the left
     * @param onTheRight
     *     The wordSequence on the right
     *
     * @return The context map
     */
    private Map<String, Integer> buildContextMap (WordSequence onTheLeft, WordSequence onTheRight) {
        Map<String, Integer> contextMap = new HashMap<>();

        String[] onTheLeftWords = onTheLeft.stream()
            .map(Word:: toString)
            .toArray(String[] ::new);

        String[] onTheRightWords = onTheRight.stream()
            .map(Word:: toString)
            .toArray(String[] ::new);

        int sizeOnTheLeft = onTheLeft.size();
        int n = min(sizeOnTheLeft, REGULAR_EXPRESSION_SPAN);
        int m = min(onTheRight.size(), REGULAR_EXPRESSION_SPAN);

        if (n == 0 && m == 0) {
            return contextMap;
        }
        else if (n == 0) {
            for (int j = 1; j <= m; j++) {
                String arg2 = String.join(" ",
                    (CharSequence[]) Arrays.copyOfRange(onTheRightWords, 0, j)
                );

                String contextKey = REGULAR_EXPRESSION_TEMPLATE +
                    REGULAR_EXPRESSION_TEMPLATE_RIGHT.replace("ARG2", arg2);

                int contextValue = 0;
                contextValue += dictionary_.getPhonesInLine(new WordSequence(arg2)).stream()
                    .toArray(String[] ::new).length;

                contextMap.put(contextKey, contextValue);
            }

            return contextMap;
        }
        else if (m == 0) {
            for (int i = 1; i <= n; i++) {
                String arg1 = String.join(
                    " ",
                    (CharSequence[]) Arrays.copyOfRange(
                        onTheLeftWords, sizeOnTheLeft - i, sizeOnTheLeft
                    )
                );

                String contextKey = REGULAR_EXPRESSION_TEMPLATE_LEFT.replace("ARG1", arg1) +
                    REGULAR_EXPRESSION_TEMPLATE;

                int contextValue = 0;
                contextValue += dictionary_.getPhonesInLine(new WordSequence(arg1)).stream()
                    .toArray(String[] ::new).length;

                contextMap.put(contextKey, contextValue);
            }

            return contextMap;
        }
        else {
            for (int i = 0; i <= n; i++) {
                String arg1 = String.join(
                    " ",
                    (CharSequence[]) Arrays.copyOfRange(
                        onTheLeftWords, sizeOnTheLeft - i, sizeOnTheLeft
                    )
                );

                for (int j = 0; j <= m; j++) {
                    if (i == 0 && j == 0) {
                        continue;
                    }

                    String arg2 = String.join(" ",
                        (CharSequence[]) Arrays.copyOfRange(onTheRightWords, 0, j)
                    );

                    String contextKey = REGULAR_EXPRESSION_TEMPLATE_LEFT.replace("ARG1", arg1) +
                        REGULAR_EXPRESSION_TEMPLATE +
                        REGULAR_EXPRESSION_TEMPLATE_RIGHT.replace("ARG2", arg2);

                    int contextValue = 0;
                    contextValue += dictionary_.getPhonesInLine(new WordSequence(arg1)).stream()
                        .toArray(String[] ::new).length;
                    contextValue += dictionary_.getPhonesInLine(new WordSequence(arg2)).stream()
                        .toArray(String[] ::new).length;

                    contextMap.put(contextKey, contextValue);
                }
            }

            return contextMap;
        }
    }

    /**
     * @brief Scores a replacing candidate against the phones of a changeable part
     *
     * @param candidate
     *     The candidate to replace the changeable part
     * @param changeablePartPhoneArray
     *     The phone array of the changeable part
     *
     * @return The score of the candidate
     */
    private double score (WordSequence candidate, String[] changeablePartPhoneArray) {
        String[] candidatePhoneArray = dictionary_.getPhonesInLine(candidate).stream()
            .toArray(String[] ::new);

        return LevenshteinMatrix.getDistance(
            Arrays.asList(candidatePhoneArray),
            Arrays.asList(changeablePartPhoneArray)
        );
    }

    /**
     * @brief Returns True if the given String is a valid result
     *
     * @param result
     *     The String
     *
     * @return True if the given String is a valid result
     */
    private boolean checkResult (String result) {
        return ! result.trim().isEmpty();
    }

    /**
     * @class Range
     * @brief Implementation of a pair of integer values
     */
    public static class Range {
        /**
         * @brief Constructor
         * @param left
         *     The first value
         * @param right
         *     The second value
         */
        Range (int left, int right) {
            left_ = left;
            right_ = right;
        }

        /**
         * @brief Returns the first value of this Range
         *
         * @return The first value of this Range
         */
        int getLeft () {
            return left_;
        }

        /**
         * @brief Returns the second value of this Range
         *
         * @return The second value of this Range
         */
        int getRight () {
            return right_;
        }

        private final int left_; //!< The first value of this Range
        private final int right_; //!< The second value of this Range
    }

    private Corpus corpus_; //!< The corpus of this corrector
    private Dictionary dictionary_; //!< The dictionary of this corrector

    private List<Detector> detectorList_; //!< The List of detectors of this corrector

    private static final String REGULAR_EXPRESSION_TEMPLATE_LEFT = "(?<=ARG1 )";
    private static final String REGULAR_EXPRESSION_TEMPLATE = "(.*)";
    private static final String REGULAR_EXPRESSION_TEMPLATE_RIGHT = "(?= ARG2)";
    private static final int REGULAR_EXPRESSION_SPAN = 5;

}
