package org.corpus;


import org.apache.commons.collections4.Equator;

public class Word {
    public Word(String text, WordSequence wordSequence, int index){
        text_ = text.toLowerCase();
        wordSequence_ = wordSequence;
        index_ = index;
    }

    public String getText(){
        return text_;
    }

    @Override
    public String toString(){
        return getText();
    }

    public WordSequence getWordSequence(){
        return wordSequence_;
    }

    public int getIndex(){
        return index_;
    }

    public boolean isWrong(){
        return wrong_;
    }

    public void setWrong(boolean wrong){
        wrong_ = wrong;
    }

    private final String text_;
    private final WordSequence wordSequence_;
    private final int index_;

    private boolean wrong_ = false;

    public static final Equator<Word> textEquator = new Equator<Word>() {
        public boolean equate(Word word1, Word word2) {
            return word1.getText().equals(word2.getText());
        }

        public int hash(Word word) {
            return 0;
        }
    };

}
