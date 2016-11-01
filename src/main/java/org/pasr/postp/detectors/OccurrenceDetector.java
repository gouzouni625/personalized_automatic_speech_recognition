package org.pasr.postp.detectors;

import org.pasr.prep.corpus.Corpus;
import org.pasr.prep.corpus.Word;
import org.pasr.prep.corpus.WordSequence;
import org.pasr.utilities.SortedMapEntryList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Integer.max;


/**
 * @class OccurrenceDetector
 * @brief Implements an error word Detector that is based on statistical analysis and word
 *        occurrence frequency to detect error words
 */
public class OccurrenceDetector implements Detector {

    /**
     * @brief Constructor
     *
     * @param corpus
     *     The Corpus to be used
     */
    public OccurrenceDetector (Corpus corpus) {
        createOccurrenceMap(corpus);
    }

    /**
     * @brief Creates the occurrence map between all the words of the Corpus
     *
     * @param corpus
     *     The Corpus to be used
     */
    // TODO Should be parallel. Maybe use Apache Lucene
    private void createOccurrenceMap (Corpus corpus) {
        // Contains a map for each word of the corpus. The map of a word, contains every other word
        // of the corpus that occurs on the same sentence with the word and also the number of times
        // this occurrence happens.
        Map<String, Map<String, Double>> occurrenceMap = new HashMap<>();

        // Contains each word of the corpus and the number that is occurs inside all the corpus
        // sentences.
        Map<String, Integer> wordOccurrenceCount = new HashMap<>();

        for (WordSequence wordSequence : corpus) {
            for (int i = 0, n = wordSequence.size(); i < n; i++) {
                String wordText = wordSequence.get(i).toString();

                if (wordOccurrenceCount.containsKey(wordText)) {
                    wordOccurrenceCount.put(wordText, wordOccurrenceCount.get(wordText) + 1);
                }
                else {
                    wordOccurrenceCount.put(wordText, 1);
                }

                if (! occurrenceMap.containsKey(wordText)) {
                    occurrenceMap.put(wordText, new HashMap<>());
                }

                for (int j = i + 1; j < n; j++) {
                    String nextWordText = wordSequence.get(j).toString();
                    if (! occurrenceMap.containsKey(nextWordText)) {
                        occurrenceMap.put(nextWordText, new HashMap<>());
                    }

                    // Update the wordMap for the wordText
                    Map<String, Double> wordMap = occurrenceMap.get(wordText);
                    if (wordMap.containsKey(nextWordText)) {
                        wordMap.put(nextWordText, wordMap.get(nextWordText) + 1);
                    }
                    else {
                        wordMap.put(nextWordText, 1.0d);
                    }

                    // Update the wordMap for the nextWordText so that, j needs to count from i + 1
                    // and not from 0.
                    Map<String, Double> nextWordMap = occurrenceMap.get(nextWordText);
                    if (nextWordMap.containsKey(wordText)) {
                        nextWordMap.put(wordText, nextWordMap.get(wordText) + 1);
                    }
                    else {
                        nextWordMap.put(wordText, 1.0d);
                    }
                }
            }
        }

        // Transform the number of co-occurrence of two words into a probability. Concretely, if
        // wordA has a co-occurrence number with wordB equal to M, divide M with N where N is the
        // number of occurrences of wordA in the whole corpus.
        for (String key : occurrenceMap.keySet()) {
            Map<String, Double> map = occurrenceMap.get(key);

            int occurrenceCount = wordOccurrenceCount.get(key);

            for (String innerKey : map.keySet()) {
                map.put(innerKey, map.get(innerKey) / occurrenceCount);
            }
        }

        // Create final occurrence map keeping only the top WORD_LIST_SIZE co-occurrences with one
        // of them being the word itself.
        occurrenceMap_ = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> entry : occurrenceMap.entrySet()) {
            occurrenceMap_.put(entry.getKey(), getTopValues(entry.getValue()));

            occurrenceMap_.get(entry.getKey()).add(entry.getKey());
        }
    }

    /**
     * @brief Returns the top WORD_LIST_SIZE - 1 keys of the given map based on their values
     *
     * @param map
     *     The map
     * @return The top WORD_LIST_SIZE - 1 keys of the given map based on their values
     */
    private List<String> getTopValues (Map<String, Double> map) {
        SortedMapEntryList<String, Double> sortedList = new SortedMapEntryList<>(
            WORD_LIST_SIZE - 1, false
        );

        sortedList.addAll(map.entrySet());

        return sortedList.keyList();
    }

    /**
     * @brief Returns a List with the error words from the given WordSequence
     *
     * @param wordSequence
     *     The WordSequence
     *
     * @return A List with the error words from the given WordSequence
     */
    @Override
    public List<Word> detect (WordSequence wordSequence) {
        List<String> wordList = wordSequence.getWordTextList();
        int numberOfWords = wordList.size();

        // Calculate individual scores for each word in the given WordSequence
        double[][] scoreBoard = new double[numberOfWords][numberOfWords];
        for (int i = 0; i < numberOfWords; i++) {
            for (int j = i; j < numberOfWords; j++) {
                if (i != j) {
                    List<String> list1 = occurrenceMap_.get(wordList.get(i));
                    List<String> list2 = occurrenceMap_.get(wordList.get(j));

                    scoreBoard[i][j] = (
                        ((double) intersectionSize(list1, list2))
                            / max(list1.size(), list2.size())
                    );
                    scoreBoard[j][i] = scoreBoard[i][j];
                }
                else {
                    scoreBoard[i][j] = 1;
                }
            }
        }

        // Reduce each word's individual scores to a final score and check if the score is above the
        // threshold.
        List<String> errorCandidateList = new ArrayList<>();
        for (int i = 0; i < numberOfWords; i++) {
            double score = 0;
            for (int j = 0; j < numberOfWords; j++) {
                score += scoreBoard[i][j];
            }
            score /= numberOfWords;

            if (score < lowScoreThreshold_) {
                errorCandidateList.add(wordList.get(i));
            }
        }

        return wordSequence.stream()
            .filter(word -> errorCandidateList.contains(word.toString()))
            .collect(Collectors.toList());
    }

    /**
     * @brief Returns the number of common String objects in the two List objects
     *
     * @param wordList1
     *     The first List
     * @param wordList2
     *     The second List
     *
     * @return The number of common String objects in the two List objects
     */
    private int intersectionSize (List<String> wordList1, List<String> wordList2) {
        int size = 0;
        for (String word : wordList1) {
            if (wordList2.contains(word)) {
                size++;
            }
        }

        return size;
    }

    /**
     * @brief Returns the low score threshold for error words
     *
     * @return The low score threshold for error words
     */
    public double getLowScoreThreshold () {
        return lowScoreThreshold_;
    }

    /**
     * @brief Sets the low score threshold for error words
     *
     * @param lowScoreThreshold
     *     The new value of the low score threshold
     */
    public void setLowScoreThreshold (double lowScoreThreshold) {
        lowScoreThreshold_ = lowScoreThreshold;
    }

    private Map<String, List<String>> occurrenceMap_; //!< The word co-occurrence map

    private static final int WORD_LIST_SIZE = 10;
    private double lowScoreThreshold_ = 0.10; //!< Score threshold to detect error words. Any word
                                              //!< with a score below this threshold is considered
                                              //!< an error word

}
