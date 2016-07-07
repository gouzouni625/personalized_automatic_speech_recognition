package org.pasr.prep.corpus;


import org.apache.commons.collections4.Equator;
import org.pasr.postp.engine.POSTagger;
import org.pasr.postp.engine.POSTagger.Tags;


public class Word {
    public Word(String text, WordSequence wordSequence, int index){
        text_ = text.toLowerCase();
        wordSequence_ = wordSequence;
        index_ = index;

        pOSPattern_ = POSTagger.getInstance().tag(this);
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

    public Tags getPOSPattern(){
        return pOSPattern_;
    }

    public static final Equator<Word> textEquator_ = new Equator<Word>() {
        public boolean equate(Word word1, Word word2) {
            return word1.getText().equals(word2.getText());
        }

        public int hash(Word word) {
            return 0;
        }
    };

    private final String text_;
    private final WordSequence wordSequence_;
    private final int index_;

    private final Tags pOSPattern_;
}
