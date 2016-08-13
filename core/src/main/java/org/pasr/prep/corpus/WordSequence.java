package org.pasr.prep.corpus;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


public class WordSequence implements Iterable<Word> {
    public WordSequence(String text, long documentID, String documentTitle) {
        documentID_ = documentID;
        documentTitle_ = documentTitle;

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

    public WordSequence(List<Word> words, long documentID, String documentTitle){
        this(StringUtils.join(words, " "), documentID, documentTitle);
    }

    public long getDocumentId (){
        return documentID_;
    }

    public String getDocumentTitle(){
        return documentTitle_;
    }

    public String getText() {
        StringBuilder stringBuilder = new StringBuilder();

        for(Word word : words_){
            stringBuilder.append(word).append(" ");
        }

        return stringBuilder.toString().trim();
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

        return new WordSequence(words_.subList(beginIndex, endIndex), documentID_, documentTitle_);
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
                tokens.add(new WordSequence(token, documentID_, documentTitle_));
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

    public String getRandomSubsequence(Random random){
        int size = size();

        if(size <= 5){
            return getText();
        }

        int subSequenceSize;
        do{
            // Note that nextInt argument is exclusive, that is why +1 is added
            subSequenceSize = random.nextInt(size + 1);
        } while(subSequenceSize == 0);

        // Note that nextInt argument is exclusive, that is why +1 is added
        int beginIndex = random.nextInt(size - subSequenceSize + 1);

        return subSequence(beginIndex, beginIndex + subSequenceSize).getText();
    }

    private void appendWord (Word word){
        words_.add(word);
    }

    private void prependWord(Word word){
        words_.add(0, word);
    }

    public WordSequence appendSequence(WordSequence wordSequence){
        for(Word word : wordSequence){
            appendWord(word);
        }

        return this;
    }

    public WordSequence prependSequence(WordSequence wordSequence){
        List<Word> words = wordSequence.getWords();

        for(int i = words.size() - 1;i >= 0;i--){
            prependWord(words.get(i));
        }

        return this;
    }

    public boolean equals(String text){
        return getText().equals(text.toLowerCase());
    }

    public void replaceWordText(String oldText, String newText){
        // If new text is empty then the words should be removed instead of having their text
        // replaced
        if(newText.isEmpty()){
            removeByText(oldText);
        }
        else {
            words_.stream()
                .filter(word -> word.equals(oldText))
                .forEach(word -> word.setText(newText));
        }
    }

    public void remove(Word word){
        words_.remove(word);
    }

    public void remove(List<Word> words){
        words.forEach(this :: remove);
    }

    public void removeByText(String text){
        remove(words_.stream().filter(word -> word.equals(text)).collect(Collectors.toList()));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof WordSequence && getText().equals(((WordSequence) o).getText());
    }

    @Override
    public int hashCode(){
        return getText().hashCode();
    }

    @Override
    public String toString() {
        return getText();
    }

    public Iterator<Word> iterator(){
        return words_.iterator();
    }

    private final long documentID_;
    private final String documentTitle_;

    private List<Word> words_;

}
