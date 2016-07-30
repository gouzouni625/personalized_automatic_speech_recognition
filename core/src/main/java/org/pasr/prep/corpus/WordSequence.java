package org.pasr.prep.corpus;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;


public class WordSequence implements Iterable<Word> {
    public WordSequence(String text, int documentID) {
        documentID_ = documentID;

        text = text.toLowerCase();

        words_ = new ArrayList<>();

        String[] words = text.split(" ");
        int index = 0;
        for(String word : words){
            if(!word.isEmpty()){
                words_.add(new Word(word, this, index));
                index++;
            }
        }
    }

    public WordSequence(List<Word> words, int documentID){
        this(StringUtils.join(words, " "), documentID);
    }

    public int getDocumentID(){
        return documentID_;
    }

    public String getText() {
        return buildText();
    }

    public List<Word> getWords(){
        return words_;
    }

    public int size (){
        return words_.size();
    }

    public Word getWord(int index){
            return words_.get(index);
    }

    public Word getFirstWord(){
        return words_.get(0);
    }

    public Word getLastWord(){
        return words_.get(words_.size() - 1);
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
        if(beginIndex < 0){
            throw new IndexOutOfBoundsException("beginIndex should not be negative");
        }

        if(endIndex < 0){
            throw new IndexOutOfBoundsException("endIndex should not be negative");
        }

        if(endIndex > words_.size()){
            throw new IndexOutOfBoundsException("endIndex should not be greater than size()");
        }

        if(beginIndex > endIndex){
            throw new IndexOutOfBoundsException("beginIndex should not be greater than endIndex");
        }

        return new WordSequence(words_.subList(beginIndex, endIndex), documentID_);
    }

    public WordSequence subSequence (int beginIndex){
        return subSequence(beginIndex, words_.size());
    }

    public List<WordSequence> split(WordSequence wordSequence){
        // Not using text_ because this and wordSequence might have a different word separator.
        String thisText = StringUtils.join(words_, " ");
        String wordSequenceText = StringUtils.join(wordSequence, " ");

        String[] tokensText = thisText.split(wordSequenceText);

        ArrayList<WordSequence> tokens = new ArrayList<>();
        for(String token : tokensText){
            if(!token.isEmpty()){
                tokens.add(new WordSequence(token, documentID_));
            }
        }

        return tokens;
    }

    /**
     * If a WordSequence is the result of merging many WordSequence, this method will return the
     * groups of Words (as WordSequences) that belong to the same WordSequence and are found
     * successively in their parent WordSequence (based on their indices).
     * @return
     */
    public List<WordSequence> continuousSubSequences(){
        ArrayList<WordSequence> subSequences = new ArrayList<>();

        int numberOfWords = words_.size();

        if(numberOfWords <= 1){
            subSequences.add(this);

            return subSequences;
        }

        int startingIndex = 0;
        Word previousWord = words_.get(0);
        for(int i = 1;i < numberOfWords;i++){
            Word currentWord = words_.get(i);

            if(currentWord.getIndex() != previousWord.getIndex() ||
                !currentWord.getParent().equals(previousWord.getParent())){
                subSequences.add(subSequence(startingIndex, i));

                startingIndex = i;
            }

            previousWord = currentWord;
        }
        subSequences.add(subSequence(startingIndex));

        return subSequences;
    }

    public List<String> getWordsText(){
        return words_.stream()
            .map(Word:: getText)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    private void appendWord (Word word){
        words_.add(word);
    }

    private void prependWord(Word word){
        words_.add(0, word);
    }

    public void appendSequence(WordSequence wordSequence){
        for(Word word : wordSequence){
            appendWord(word);
        }
    }

    public void prependSequence(WordSequence wordSequence){
        List<Word> words = wordSequence.getWords();

        for(int i = words.size() - 1;i >= 0;i--){
            prependWord(words.get(i));
        }
    }

    public boolean equals(String text){
        return buildText().equals(text.toLowerCase());
    }

    public void replaceWordText(String oldText, String newText){
        words_.stream().filter(word -> word.equals(oldText)).forEach(word -> word.setText(newText));
    }

    public void remove(Word word){
        words_.remove(word);
    }

    private String buildText (){
        StringBuilder stringBuilder = new StringBuilder();

        for(Word word : words_){
            stringBuilder.append(word).append(" ");
        }

        return stringBuilder.toString().trim();
    }

    public void remove(List<Word> words){
        words.forEach(this :: remove);
    }

    public void removeByText(String text){
        remove(words_.stream().filter(word -> word.equals(text)).collect(Collectors.toList()));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof WordSequence && buildText().equals(((WordSequence) o).getText());
    }

    @Override
    public int hashCode(){
        return buildText().hashCode();
    }

    @Override
    public String toString() {
        return buildText();
    }

    public Iterator<Word> iterator(){
        return words_.iterator();
    }

    private final int documentID_;

    private List<Word> words_;

}
