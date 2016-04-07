package org.corpus;

import org.engine.Configuration;
import org.engine.Configuration.PunctuationMarks;
import org.utilities.ArrayIterable;

import java.util.Arrays;
import java.util.Iterator;


public class WordSequence implements Iterable<Word> {
    public WordSequence(String sequence) {
        sequence_ = configuration_.arePunctuationMarksRemoved() ? removePunctuationMarks(
                sequence) : sequence;

        words_ = tokenize(sequence);
    }

    public WordSequence(Word[] words){
        words_ = words;
        int numberOfWords = words.length;

        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0;i < numberOfWords - 1;i++){
            stringBuilder.append(words[i].getWord()).append(configuration_.getWordSeparator());
        }
        // Avoid appending a word separator at the end
        stringBuilder.append(words[numberOfWords - 1]);

        sequence_ = stringBuilder.toString();
    }

    private String removePunctuationMarks(String sequence) {
        StringBuilder stringBuilder = new StringBuilder();
        for (char ch : sequence.toCharArray()) {
            if (!PunctuationMarks.isPunctuationMark(ch)) {
                stringBuilder.append(ch);
            }
            else{
                stringBuilder.append(configuration_.getWordSeparator());
            }
        }

        return stringBuilder.toString().replaceAll(configuration_.getWordSeparator() + "{2,}",
                configuration_.getWordSeparator());
    }

    private Word[] tokenize(String sequence) {
        String[] wordsString = sequence.split(configuration_.getWordSeparator());
        int numberOfWords = wordsString.length;

        Word[] words = new Word[numberOfWords];
        for(int i = 0;i < numberOfWords;i++){
            words[i] = new Word(wordsString[i], this, i);
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
            return new WordSequence("");
        }

        return new WordSequence(Arrays.copyOfRange(words_, beginIndex, endIndex));
    }

    @Override
    public String toString() {
        return getSequence();
    }

    public String getSequence() {
        return sequence_;
    }

    public Word[] getWords(){
        return words_;
    }

    public Iterator<Word> iterator(){
        return (new ArrayIterable<Word>(words_).iterator());
    }

    private final String sequence_;
    private final Word[] words_;

    private final Configuration configuration_ = Configuration.getInstance();

}
