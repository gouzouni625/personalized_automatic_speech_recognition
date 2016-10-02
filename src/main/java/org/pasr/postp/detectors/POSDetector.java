package org.pasr.postp.detectors;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import org.pasr.prep.corpus.Corpus;
import org.pasr.prep.corpus.Word;
import org.pasr.prep.corpus.WordSequence;
import org.pasr.utilities.LevenshteinMatrix;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.collections4.ListUtils.longestCommonSubsequence;
import static org.pasr.utilities.Utilities.getResourceStream;


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

        InputStream inputStream = getResourceStream("/detectors/pos/en-pos-perceptron.bin");

        if (inputStream == null) {
            throw new FileNotFoundException(
                "getResourceStream(\"/detectors/pos/en-pos-perceptron.bin\")" +
                    " returned null.");
        }

        tagger_ = new POSTaggerME(new POSModel(inputStream));

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
            corpusMap_.put(wordSequence.getWordTextList(), tag(wordSequence));
        }
    }

    /**
     * @brief Tags a WordSequence
     *
     * @param wordSequence
     *     The WordSequence to tag
     *
     * @return A List with the corresponding Tag for each Word in the given WordSequence
     */
    private List<Tags> tag (WordSequence wordSequence) {
        return Tags.tag(
            tagger_.tag(
                wordSequence.stream()
                    .map(Word:: toString)
                    .toArray(String[] ::new)
            )
        );
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

        List<Tags> bestPattern = getBestPattern(wordSequence);

        if (bestPattern == null) {
            return new ArrayList<>();
        }

        return findErrorWords(bestPattern, tag(wordSequence)).stream()
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
    private List<Tags> getBestPattern (WordSequence wordSequence) {
        List<Tags> wordSequenceTagList = tag(wordSequence);
        List<String> wordSequenceWordTextList = wordSequence.getWordTextList();

        double minDistance = Double.POSITIVE_INFINITY;
        List<Tags> bestPattern = null;

        double currentDistance;
        for (Map.Entry<List<String>, List<Tags>> corpusMapEntry : corpusMap_.entrySet()) {
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
    private List<Integer> findErrorWords (List<Tags> referencePattern,
                                          List<Tags> hypothesisPattern) {
        List<Tags> matchingTags = longestCommonSubsequence(referencePattern, hypothesisPattern);

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

    /**
     * @class Tags
     * @brief Holds the different tags that the Apache OpenNLP Tagger produces
     *
     * @see <a href="http://www.cis.upenn.edu/~treebank/">http://www.cis.upenn.edu/~treebank/</a>
     */
    public enum Tags {
        CC("Coordinating conjunction"),
        CD("Cardinal number"),
        DT("Determiner"),
        EX("Existential there"),
        FW("Foreign word"),
        IN("Preposition or subordinating conjunction"),
        JJ("Adjective"),
        JJR("Adjective comparative"),
        JJS("Adjective superlative"),
        LS("List item marker"),
        MD("Modal verb"),
        NN("Noun singular or mass"),
        NNS("Noun plural"),
        NNP("Proper noun singular"),
        NNPS("Proper noun plural"),
        PDT("Predeterminer"),
        POS("Possessive ending"),
        PRP("Personal pronoun"),
        PRP_DS("Possessive pronoun"),
        RB("Adverb"),
        RBR("Adverb comparative"),
        RBS("Adverb superlative"),
        RP("Particle"),
        SYM("Symbol"),
        TO("to"),
        UH("Interjection"),
        VB("Verb base form"),
        VBD("Verb past tense"),
        VBG("Verb gerund or present participle"),
        VBN("Verb past participle"),
        VBP("Verb non-3rd person singular present"),
        VBZ("Verb 3rd person singular present"),
        WDT("Wh-determiner"),
        WP("Wh-pronoun"),
        WP_DS("Possessive wh-pronoun"),
        WRB("Wh-adverb"),
        UNK("Unknown");

        /**
         * @brief Constructor
         *
         * @param partOfSpeech
         *     The part of speech the tags represents
         */
        Tags (String partOfSpeech) {
            partOfSpeech_ = partOfSpeech;
        }

        /**
         * @brief Returns the part of speech this tag represents
         *
         * @return The part of speech this tag represents
         */
        public String getPartOfSpeech () {
            return partOfSpeech_;
        }

        /**
         * @brief Maps a String to its Tag object
         *
         * @param tagString
         *     The String
         *
         * @return The Tag object this String maps to
         */
        public static Tags tag (String tagString) {
            for (Tags tag : Tags.values()) {
                if (tag.toString().equals(tagString)) {
                    return tag;
                }
            }

            // PRP_DS is a replacement for PRP$ to avoid using the $ (Dollar Symbol).
            // WP_DS is a replacement for WP$ to avoid using the $ (Dollar Symbol).
            if (tagString.equals("PRP$")) {
                return PRP_DS;
            }
            else if (tagString.equals("WP$")) {
                return WP_DS;
            }

            return UNK;
        }

        /**
         * @brief Maps an array of String objects to their corresponding Tag objects
         *
         * @param tagStringArray
         *     The array of String objects
         *
         * @return The corresponding Tag objects
         */
        public static List<Tags> tag (String[] tagStringArray) {
            return Arrays.stream(tagStringArray)
                .map(Tags:: tag)
                .collect(Collectors.toList());
        }

        private final String partOfSpeech_; //!< The part of speech this Tag represents
    }

    private Map<List<String>, List<Tags>> corpusMap_; //!< Contains an entry for each sentence in
                                                      //!< the given corpus. The key is a list with
                                                      //!< every word in the corpus and the value is
                                                      //!< a list with the corresponding tags

    private POSTaggerME tagger_; //!< The Apache OpenNLP POS Tagger of this Detector

}
