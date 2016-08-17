package org.pasr.prep.corpus;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


public class WordSequence extends ArrayList<Word> {
    public WordSequence(String text){
        this(text, -1, "");
    }

    public WordSequence(String text, long documentID, String documentTitle) {
        documentID_ = documentID;
        documentTitle_ = documentTitle;

        text = escape(text);

        String[] words = text.split(" ");
        int index = 0;
        for(String word : words){
            if(!word.isEmpty()){
                add(new Word(word, this, index));
                index++;
            }
        }
    }

    private String escape(String text){
        return text.toLowerCase().trim();
    }

    public WordSequence(List<Word> words, long documentID, String documentTitle){
        documentID_ = documentID;
        documentTitle_ = documentTitle;

        addAll(words);
    }

    public long getDocumentId (){
        return documentID_;
    }

    public String getDocumentTitle(){
        return documentTitle_;
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();

        for(Word word : this){
            stringBuilder.append(word).append(" ");
        }

        return stringBuilder.toString().trim();
    }

    public List<String> getWordTextList () {
        return stream()
            .map(Word:: toString)
            .collect(Collectors.toList());
    }

    public boolean contains(String string){
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
    public WordSequence subSequence(int beginIndex, int endIndex){
        return new WordSequence(subList(beginIndex, endIndex), documentID_, documentTitle_);
    }

    public WordSequence subSequence(int beginIndex){
        return subSequence(beginIndex, size());
    }

    String getRandomSubsequence(Random random){
        int size = size();

        if(size <= 5){
            return toString();
        }

        int subSequenceSize;
        do{
            // Note that nextInt argument is exclusive, that is why +1 is added
            subSequenceSize = random.nextInt(size + 1);
        } while(subSequenceSize == 0);

        // Note that nextInt argument is exclusive, that is why +1 is added
        int beginIndex = random.nextInt(size - subSequenceSize + 1);

        return subSequence(beginIndex, beginIndex + subSequenceSize).toString();
    }

    void replaceWordText(String oldText, String newText){
        // If new text is empty then the words should be removed instead of having their text
        // replaced
        if(newText.isEmpty()){
            removeByText(oldText);
        }
        else {
            stream()
                .filter(word -> word.toString().equals(oldText))
                .forEach(word -> word.setText(newText));
        }
    }

    void removeByText(String text){
        removeAll(stream()
            .filter(word -> word.toString().equals(text))
            .collect(Collectors.toList())
        );
    }

    private final long documentID_;
    private final String documentTitle_;

}
