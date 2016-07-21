package org.pasr.prep.corpus;

import org.pasr.postp.engine.POSTagger;
import org.pasr.postp.engine.POSTagger.Tags;
import org.pasr.utilities.ArrayIterable;

import java.util.ArrayList;
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
        if(endIndex > words_.length){
            endIndex = words_.length - 1;
        }

        if(beginIndex < 0){
            beginIndex = 0;
        }

        if(beginIndex >= endIndex){
            return new WordSequence("", wordSeparator_);
        }

        if(words_.length == 0){
            return new WordSequence("", wordSeparator_);
        }

        return new WordSequence(Arrays.copyOfRange(words_, beginIndex, endIndex), wordSeparator_);
    }

    public WordSequence subSequence(int beginIndex){
        return subSequence(beginIndex, words_.length);
    }

    public List<WordSequence> continuousSubSequences(){
        ArrayList<WordSequence> subSequences = new ArrayList<>();

        int numberOfWords = words_.length;

        if(numberOfWords <= 1){
            subSequences.add(this);

            return subSequences;
        }

        int startingIndex = 0;
        int previousIndex = words_[0].getIndex();
        for(int i = 1;i < numberOfWords;i++){
            if(words_[i].getIndex() != previousIndex + 1){
                subSequences.add(subSequence(startingIndex, i));

                startingIndex = i;
            }

            previousIndex = words_[i].getIndex();
        }
        subSequences.add(subSequence(startingIndex));

        return subSequences;
    }

    public boolean equals(String text){
        return text_.equals(text.toLowerCase());
    }

    public WordSequence[] split(WordSequence wordSequence){
        // Not using text_ because this and wordSequence might have a different word separator.
        String thisText = String.join(" ", (CharSequence[]) getWordsText());
        String wordSequenceText = String.join(" ", (CharSequence[]) wordSequence.getWordsText());

        String[] tokens = thisText.split(wordSequenceText);

        // We know that there are going to be only 2 tokens
        if(tokens.length == 2) {
            return new WordSequence[] {new WordSequence(tokens[0], getWordSeparator()),
                new WordSequence(tokens[1], getWordSeparator())};
        }
        else{
            if(wordSequence.getFirstWord().getText().equals(getFirstWord().getText())){
                return new WordSequence[] {new WordSequence("", getWordSeparator()),
                    new WordSequence(tokens[0], getWordSeparator())};
            }
            else{
                return new WordSequence[] {new WordSequence(tokens[0], getWordSeparator()),
                    new WordSequence("", getWordSeparator())};
            }
        }
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof WordSequence){
            String thisText = getText();
            String objectText = ((WordSequence) o).getText();

            return thisText.equals(objectText);
        }
        else{
            return false;
        }
    }

    @Override
    public int hashCode(){
        return getText().hashCode();
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

    public String getWordSeparator(){
        return wordSeparator_;
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