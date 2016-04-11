package org.corpus;

import org.utilities.ArrayIterable;
import org.utilities.Margin;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


public class WordSequence implements Iterable<Word> {
    public WordSequence(String text, String wordSeparator) {
        text_ = text.toLowerCase();

        wordSeparator_ = wordSeparator.toLowerCase();

        words_ = tokenize(text_);
    }

    public WordSequence(Word[] words, String wordSeparator){
        words_ = words;
        int numberOfWords = words.length;

        wordSeparator_ = wordSeparator.toLowerCase();

        if(numberOfWords == 0){
            text_ = "";

            return;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0;i < numberOfWords - 1;i++){
            stringBuilder.append(words[i].getText()).append(wordSeparator_);
        }
        // Avoid appending a word separator at the end
        stringBuilder.append(words[numberOfWords - 1]);

        text_ = stringBuilder.toString();
    }

    public WordSequence(List<Word> words, String wordSeparator){
        this(words.toArray(new Word[words.size()]), wordSeparator);
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

    public Margin longestContinuousSubSequence(){
        int numberOfWords = words_.length;

        if(numberOfWords == 0){
            return null;
        }
        else if(numberOfWords == 1){
            return new Margin(0, 1);
        }

        int maxLength = 1;
        int maxStartingIndex = 0;
        int currentLength = 1;
        int startingIndex = 0;
        int previousIndex = words_[0].getIndex();
        for(int i = 1, n = words_.length;i < n;i++){
            if(words_[i].getIndex() == previousIndex + 1){
                currentLength++;
            }
            else{
                if(currentLength > maxLength){
                    maxLength = currentLength;
                    maxStartingIndex = startingIndex;
                }

                startingIndex = i;
                currentLength = 1;
            }

            previousIndex = words_[i].getIndex();
        }
        if(currentLength > maxLength){
            maxLength = currentLength;
            maxStartingIndex = startingIndex;
        }

        return new Margin(maxStartingIndex, maxStartingIndex + maxLength);
    }

    public boolean equals(String text){
        return text_.equals(text.toLowerCase());
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
