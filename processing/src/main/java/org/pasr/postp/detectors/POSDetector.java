package org.pasr.postp.detectors;

import org.apache.commons.collections4.ListUtils;
import org.pasr.external.pos.POSTagger;
import org.pasr.model.text.Corpus;
import org.pasr.model.text.Word;
import org.pasr.model.text.WordSequence;
import org.pasr.utilities.LevenshteinMatrix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * @class POSDetector
 * @brief Implements an error word Detector that is based on the Part of Speech (POS) patterns
 *        and the Levenshtein Distance between word sequences
 */
public class POSDetector implements Detector {

    /**
     * @brief Constructor
     *
     * @param corpus
     *     The Corpus to be used
     *
     * @throws IOException If the Apache Open NLP Tag model cannot be loaded
     */
    public POSDetector (Corpus corpus) throws IOException {
        if (corpus == null) {
            throw new IllegalArgumentException("corpus must not be null!");
        }

        tagger = new POSTagger();

        createPOSPatterns(corpus);
    }

    /**
     * @brief Creates a POS pattern for each sentence in the Corpus
     *
     * @param corpus
     *     The Corpus to be used
     */
    private void createPOSPatterns (Corpus corpus) {
        corpusMap_ = new HashMap<>();

        for (WordSequence wordSequence : corpus) {
            corpusMap_.put(wordSequence.getWordTextList(), tagger.tag(wordSequence));
        }
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
        if (wordSequence == null) {
            throw new IllegalArgumentException("wordSequence must not be null!");
        }

        if (wordSequence.isEmpty()) {
            new ArrayList<>();
        }

        List<POSTagger.Tags> bestPattern = getBestPattern(wordSequence);

        if (bestPattern == null) {
            return new ArrayList<>();
        }

        return findErrorWords(bestPattern, tagger.tag(wordSequence)).stream()
            .map(wordSequence:: get)
            .collect(Collectors.toList());
    }

    /**
     * @brief Finds the best POS pattern in the corpus that matches the given WordSequence
     *        The scoring is done based on the matching of the two POS patterns and also on the
     *        distance between the two corresponding sentences.
     *
     * @param wordSequence
     *     The WordSequence
     *
     * @return The best matching Tag List
     */
    private List<POSTagger.Tags> getBestPattern (WordSequence wordSequence) {
        List<POSTagger.Tags> wordSequenceTagList = tagger.tag(wordSequence);
        List<String> wordSequenceWordTextList = wordSequence.getWordTextList();

        double minDistance = Double.POSITIVE_INFINITY;
        List<POSTagger.Tags> bestPattern = null;

        double currentDistance;
        for (Map.Entry<List<String>, List<POSTagger.Tags>> corpusMapEntry : corpusMap_.entrySet()) {
            currentDistance = LevenshteinMatrix.getDistance(
                wordSequenceTagList,
                corpusMapEntry.getValue()
            );

            currentDistance = 0.5 * currentDistance + 0.5 * LevenshteinMatrix.getDistance(
                wordSequenceWordTextList,
                corpusMapEntry.getKey()
            );

            if (currentDistance < minDistance) {
                minDistance = currentDistance;
                bestPattern = corpusMapEntry.getValue();
            }
        }

        return bestPattern;
    }

    /**
     * @brief Returns a List with the indices of the error words
     *        The two given patterns are aligned and each Tag of the hypothesis pattern that does
     *        not match any of the Tags of the reference pattern is considered an error.
     *
     * @param referencePattern
     *     The reference pattern considered as the target
     * @param hypothesisPattern
     *     The hypothesis pattern
     *
     * @return A List with the indices of the error words
     */
    private List<Integer> findErrorWords (List<POSTagger.Tags> referencePattern,
                                          List<POSTagger.Tags> hypothesisPattern) {
        List<POSTagger.Tags> matchingTags = ListUtils.longestCommonSubsequence(referencePattern, hypothesisPattern);

        if (matchingTags == null || matchingTags.size() == 0) {
            return IntStream.range(0, hypothesisPattern.size())
                .boxed()
                .collect(Collectors.toList());
        }

        List<Integer> errorWordIndexList = new ArrayList<>();

        int matchingTagsSize = matchingTags.size();
        int hypothesisPatternSize = hypothesisPattern.size();

        int matchingTagsIndex = 0;
        int hypothesisIndex = 0;

        for (; hypothesisIndex < hypothesisPatternSize; hypothesisIndex++) {
            if (hypothesisPattern.get(hypothesisIndex) == matchingTags.get(matchingTagsIndex)) {
                matchingTagsIndex++;

                if (matchingTagsIndex == matchingTagsSize) {
                    hypothesisIndex++;
                    break;
                }
            }
            else {
                errorWordIndexList.add(hypothesisIndex);
            }
        }
        for (int i = hypothesisIndex; i < hypothesisPatternSize; i++) {
            errorWordIndexList.add(i);
        }

        return errorWordIndexList;
    }

    private Map<List<String>, List<POSTagger.Tags>> corpusMap_; //!< Contains an entry for each sentence in
                                                      //!< the given corpus. The key is a list with
                                                      //!< every word in the corpus and the value is
                                                      //!< a list with the corresponding tags

    private POSTagger tagger;

}
