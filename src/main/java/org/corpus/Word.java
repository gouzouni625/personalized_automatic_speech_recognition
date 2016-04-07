package org.corpus;


public class Word {
    public Word(String word, WordSequence sequence, int index){
        word_ = word;
        sequence_ = sequence;
        index_ = index;
    }

    public String getWord(){
        return word_;
    }

    @Override
    public String toString(){
        return getWord();
    }

    public WordSequence getSequence(){
        return sequence_;
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

    private final String word_;
    private final WordSequence sequence_;
    private final int index_;

    private boolean wrong_ = false;

}
