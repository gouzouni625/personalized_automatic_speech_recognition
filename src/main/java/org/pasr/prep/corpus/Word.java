package org.pasr.prep.corpus;


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
        text_ = escape(text);
        parent_ = wordSequence;
        index_ = index;
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
        return text_;
    }

    /**
     * @brief Returns the WordSequence of this Word
     *
     * @return The WordSequence of this Word
     */
    WordSequence getParent () {
        return parent_;
    }

    /**
     * @brief Returns the index of this Word inside the WordSequence
     *
     * @return The index of this Word inside the WordSequence
     */
    public int getIndex () {
        return index_;
    }

    /**
     * @brief Sets the String of this Word
     *
     * @param text
     *     The new String of this Word
     */
    public void setText (String text) {
        text_ = escape(text);
    }

    private String text_; //!< The String of this Word
    private final WordSequence parent_; //!< The WordSequence containing this Word
    private final int index_; //!< The index of this Word inside the WordSequence

}
