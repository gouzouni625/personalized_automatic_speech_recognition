package org.corpus;

import org.utilities.ArrayIterable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;


public class WordSequence implements Iterable<Word> {
    public WordSequence(String text, String wordSeparator) {
        text_ = text;

        wordSeparator_ = wordSeparator;

        words_ = tokenize(text);
    }

    public WordSequence(Word[] words, String wordSeparator){
        words_ = words;
        int numberOfWords = words.length;

        wordSeparator_ = wordSeparator;

        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0;i < numberOfWords - 1;i++){
            stringBuilder.append(words[i].getText()).append(wordSeparator_);
        }
        // Avoid appending a word separator at the end
        stringBuilder.append(words[numberOfWords - 1]);

        text_ = stringBuilder.toString();
    }

    private Word[] tokenize(String text) {
        String[] wordStrings = text.split(wordSeparator_);
        int numberOfWords = wordStrings.length;

        Word[] words = new Word[numberOfWords];
        for(int i = 0;i < numberOfWords;i++){
            words[i] = new Word(wordStrings[i], this, i);
        }

        return words;
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
    public WordSequence subSequence(int beginIndex, int endIndex){
        if(beginIndex >= endIndex){
            return new WordSequence("", wordSeparator_);
        }

        return new WordSequence(Arrays.copyOfRange(words_, beginIndex, endIndex), wordSeparator_);
    }

    public boolean containsText(String text){
        return text_.contains(text);
        // int startingPosition = Collections.indexOfSubList(
        //         Arrays.asList(words_),
        //         Arrays.asList(wordSequence.getWords())
        // );
        //
        // return (startingPosition > -1);
    }

    @Override
    public String toString() {
        return getText();
    }

    public String getText() {
        return text_;
    }

    public Word[] getWords(){
        return words_;
    }

    public Iterator<Word> iterator(){
        return (new ArrayIterable<Word>(words_).iterator());
    }

    private final String text_;
    private final Word[] words_;

    private final String wordSeparator_;

}
