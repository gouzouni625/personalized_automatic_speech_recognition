package org.pasr.prep.corpus;


public class Word {
    public Word(String text, WordSequence wordSequence, int index){
        text_ = text.toLowerCase();
        parent_ = wordSequence;
        index_ = index;
    }

    public String getText(){
        return text_;
    }

    public WordSequence getParent (){
        return parent_;
    }

    public int getIndex(){
        return index_;
    }

    public boolean equals(String text){
        return text_.equals(text.toLowerCase());
    }

    public void setText(String text){
        text_ = text.toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Word && text_.equals(((Word) o).getText());
    }

    @Override
    public int hashCode(){
        return text_.hashCode();
    }

    @Override
    public String toString(){
        return text_;
    }

    private String text_;
    private final WordSequence parent_;
    private final int index_;

}
