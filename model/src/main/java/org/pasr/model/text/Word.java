package org.pasr.model.text;

import lombok.Getter;
import lombok.Setter;


/**
 * @class Word
 * @brief Implements a word inside a WordSequence wrapping a String
 */
public class Word {

    /**
     * @brief Constructor
     *
     * @param text
     *     The String of this Word
     * @param wordSequence
     *     The WordSequence containing this Word
     * @param index
     *     The index of this Word inside the WordSequence
     */
    public Word (String text, WordSequence wordSequence, int index) {
        this.text = escape(text);
        this.parent = wordSequence;
        this.index = index;
    }

    /**
     * @brief Escapes a String
     *        Escaping is done to ensure that all characters are in lower case in order to be
     *        compatible with CMU Sphinx.
     *
     * @param text
     *     The String to escape
     *
     * @return The escaped String
     */
    private String escape (String text) {
        return text.toLowerCase().replaceAll(" {2,}", " ").trim();
    }

    /**
     * @brief Returns the String of this Word
     *
     * @return The String of this Word
     */
    @Override
    public String toString () {
        return text;
    }

    @Getter
    @Setter
    private String text; //!< The String of this Word

    @Getter
    private final WordSequence parent; //!< The WordSequence containing this Word

    @Getter
    private final int index; //!< The index of this Word inside the WordSequence

}
