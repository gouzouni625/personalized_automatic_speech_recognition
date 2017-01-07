package org.pasr.external.pos;


import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import org.pasr.model.text.Word;
import org.pasr.model.text.WordSequence;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.pasr.utilities.Utilities.getResourceStream;

public class POSTagger {

    public POSTagger () throws IOException {
        InputStream inputStream = getResourceStream("/pos/en-pos-perceptron.bin");

        if (inputStream == null) {
            throw new FileNotFoundException(
                    "getResourceStream(\"/pos/en-pos-perceptron.bin\")" +
                            " returned null.");
        }

        tagger = new POSTaggerME(new POSModel(inputStream));
    }

    /**
     * @brief Tags a WordSequence
     *
     * @param wordSequence
     *     The WordSequence to tag
     *
     * @return A List with the corresponding Tag for each Word in the given WordSequence
     */
    public List<POSTagger.Tags> tag (WordSequence wordSequence) {
        return POSTagger.Tags.tag(
                tagger.tag(
                        wordSequence.stream()
                                .map(Word:: toString)
                                .toArray(String[] ::new)
                )
        );
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

    private POSTaggerME tagger;

}
