package org.pasr.corpus;

import org.pasr.postp.engine.POSTagger;
import org.pasr.postp.engine.POSTagger.Tags;
import org.pasr.utilities.ArrayIterable;

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
        if(text.equals("")){
            return new Word[] {};
        }

        String[] wordStrings = text.split(wordSeparator_);
        int numberOfWords = wordStrings.length;

        Word[] words = new Word[numberOfWords];
        for(int i = 0;i < numberOfWords;i++){
            words[i] = new Word(wordStrings[i], this, i);
        }

        return words;
    }

    public Tags[] getPOSPattern(){
        return POSTagger.getInstance().tag(this);
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

        if(words_.length == 0){
            return new WordSequence("", wordSeparator_);
        }

        if(endIndex > words_.length){
            endIndex = words_.length - 1;
        }

        return new WordSequence(Arrays.copyOfRange(words_, beginIndex, endIndex), wordSeparator_);
    }

    public WordSequence subSequence(int beginIndex){
        return subSequence(beginIndex, words_.length);
    }

    public WordSequence longestContinuousSubSequence(){
        int numberOfWords = words_.length;

        if(numberOfWords <= 1){
            return this;
        }

        int maxLength = 1;
        int maxStartingIndex = 0;
        int currentLength = 1;
        int startingIndex = 0;
        int previousIndex = words_[0].getIndex();
        for(int i = 1;i < numberOfWords;i++){
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

        return subSequence(maxStartingIndex, maxStartingIndex + maxLength);
    }

    public boolean equals(String text){
        return text_.equals(text.toLowerCase());
    }

    public WordSequence[] split(WordSequence wordSequence){
        Word[] words = wordSequence.getWords();

        Word firstWord = words[0];
        Word lastWord = words[words.length - 1];

        int firstWordIndex = -1;
        int lastWordIndex = -1;

        for(int i = 0, n = words_.length;i < n;i++){
            if((firstWordIndex == -1) && words_[i].getText().equals(firstWord.getText())){
                firstWordIndex = i;

                if(lastWordIndex != -1){
                    break;
                }
            }

            if((lastWordIndex == -1) && words_[i].getText().equals(lastWord.getText())){
                lastWordIndex = i;

                if(firstWordIndex != -1){
                    break;
                }
            }
        }

        WordSequence wordSequenceOnTheLeft = subSequence(0, firstWordIndex);
        WordSequence wordSequenceOnTheRight = subSequence(lastWordIndex + 1);

        return new WordSequence[] {wordSequenceOnTheLeft, wordSequenceOnTheRight};
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

    public String[] getWordsText(){
        int numberOfWords = words_.length;

        String[] wordsText = new String[numberOfWords];

        for(int i = 0;i < numberOfWords;i++){
            wordsText[i] = words_[i].getText();
        }

        return wordsText;
    }

    public Word getWord(int index){
        return words_[index];
    }

    public void appendWord (Word word){
        int numberOfWords = words_.length;

        Word[] newWords = new Word[numberOfWords + 1];

        System.arraycopy(words_, 0, newWords, 0, numberOfWords);

        newWords[numberOfWords] = word;

        words_ = newWords;

        if(!text_.isEmpty()) {
            text_ += wordSeparator_;
        }
        text_ += word.getText();
    }

    public void prependWord(Word word){
        int numberOfWords = words_.length;

        Word[] newWords = new Word[numberOfWords + 1];

        System.arraycopy(words_, 0, newWords, 1, numberOfWords);

        newWords[0] = word;

        words_ = newWords;

        if(! text_.isEmpty()){
            text_ = word.getText() + wordSeparator_ + text_;
        }
        else{
            text_ = word.getText();
        }
    }

    public Word getFirstWord(){
        return words_[0];
    }

    public Word getLastWord(){
        return words_[words_.length - 1];
    }

    public int numberOfWords(){
        return words_.length;
    }

    public int indexOf(String word){
        return Arrays.asList(getWordsText()).indexOf(word);
    }

    public Iterator<Word> iterator(){
        return (new ArrayIterable<>(words_).iterator());
    }

    private String text_;
    private Word[] words_;

    private final String wordSeparator_;

}
