package org.pasr.model.text;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


/**
 * @class WordSequence
 * @brief Implements a List of Word objects backed by an ArrayList
 */
public class WordSequence extends ArrayList<Word> {

    /**
     * @brief Constructor
     *
     * @param text
     *     The text of this WordSequence
     */
    public WordSequence (String text) {
        this(text, - 1, "");
    }

    /**
     * @brief Constructor
     *
     * @param text
     *     The text of this WordSequence
     * @param documentId
     *     The id of the Document that this WordSequence belongs to
     * @param documentTitle
     *     The title of the Document that this WordSequence belongs to
     */
    public WordSequence (String text, long documentId, String documentTitle) {
        this.documentId = documentId;
        this.documentTitle = documentTitle;

        text = escape(text);

        String[] words = text.split(" ");
        int index = 0;
        for (String word : words) {
            if (! word.isEmpty()) {
                add(new Word(word, this, index));
                index++;
            }
        }
    }

    /**
     * @brief Constructor
     *
     * @param words
     *     The Word objects of this WordSequence
     * @param documentId
     *     The id of the Document that this WordSequence belongs to
     * @param documentTitle
     *     The title of the Document that this WordSequence belongs to
     */
    public WordSequence (List<Word> words, long documentId, String documentTitle) {
        this.documentId = documentId;
        this.documentTitle = documentTitle;

        addAll(words);
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
     * @brief Returns the String of this WordSequence
     *
     * @return The String of this WordSequence
     */
    @Override
    public String toString () {
        StringBuilder stringBuilder = new StringBuilder();

        for (Word word : this) {
            stringBuilder.append(word).append(" ");
        }

        return stringBuilder.toString().trim();
    }

    /**
     * @brief Returns a List containing the Word objects of this WordSequence
     *
     * @return A List containing the Word objects of this WordSequence
     */
    public List<String> getWordTextList () {
        return stream()
            .map(Word:: toString)
            .collect(Collectors.toList());
    }

    /**
     * @brief Returns true if and only if the String of this WordSequence contains the given String
     *
     * @param string
     *     The String to test whether or not this WordSequence contains
     *
     * @return True if and only if the String of this WordSequence contains the given String
     */
    public boolean contains (String string) {
        return toString().contains(string);
    }

    /**
     * @brief Returns a new WordSequence that is a sub-sequence of this WordSequence
     *
     * @param beginIndex
     *     The beginning index inclusive
     * @param endIndex
     *     The ending index exclusive
     *
     * @return A new WordSequence that is a sub-sequence of this WordSequence
     */
    public WordSequence subSequence (int beginIndex, int endIndex) {
        return new WordSequence(subList(beginIndex, endIndex), documentId, documentTitle);
    }

    /**
     * @brief Returns a new WordSequence that is a sub-sequence of this WordSequence
     *
     * @param beginIndex
     *     The beginning index inclusive
     *
     * @return A new WordSequence that is a sub-sequence of this WordSequence
     */
    public WordSequence subSequence (int beginIndex) {
        return subSequence(beginIndex, size());
    }

    /**
     * @brief Returns a random sub-String of this WordSequence String
     *
     * @param random
     *     The Random number generator to be used
     *
     * @return A random sub-String of this WordSequence String
     */
    String getRandomSubsequence (Random random) {
        int size = size();

        if (size <= 5) {
            return toString();
        }

        int subSequenceSize;
        do {
            // Note that nextInt argument is exclusive, that is why +1 is added
            subSequenceSize = random.nextInt(size + 1);
        } while (subSequenceSize == 0);

        // Note that nextInt argument is exclusive, that is why +1 is added
        int beginIndex = random.nextInt(size - subSequenceSize + 1);

        return subSequence(beginIndex, beginIndex + subSequenceSize).toString();
    }

    /**
     * @brief Replaces the String of each Word of this WordSequence
     *
     * @param oldText
     *     The text to be replaced
     * @param newText
     *     The text to be placed
     */
    void replaceWordText (String oldText, String newText) {
        // If new text is empty then the words should be removed instead of having their text
        // replaced
        if (newText.isEmpty()) {
            removeByText(oldText);
        }
        else {
            stream()
                .filter(word -> word.toString().equals(oldText))
                .forEach(word -> word.setText(newText));
        }
    }

    /**
     * @brief Removes any Word objects inside this WordSequence that their String matches the given
     *
     * @param text
     *     The String to match upon
     */
    void removeByText (String text) {
        removeAll(stream()
            .filter(word -> word.toString().equals(text))
            .collect(Collectors.toList())
        );
    }

    @Getter
    private final long documentId; //!< The id of the Document that this WordSequence belongs to

    @Getter
    private final String documentTitle; //!< The title of the Document that this WordSequence
                                         //!< belongs to

}
