package org.pasr.prep.corpus;


public class Word {
    public Word(String text, WordSequence wordSequence, int index){
        text_ = escape(text);
        parent_ = wordSequence;
        index_ = index;
    }

    private String escape(String text){
        return text.toLowerCase().trim();
    }

    @Override
    public String toString(){
        return text_;
    }

    WordSequence getParent (){
        return parent_;
    }

    public int getIndex(){
        return index_;
    }

    public void setText(String text){
        text_ = escape(text);
    }

    private String text_;
    private final WordSequence parent_;
    private final int index_;

}
