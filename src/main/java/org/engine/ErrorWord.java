package org.engine;

import org.corpus.TextLine;

public class ErrorWord {
    public ErrorWord(String word, TextLine contextLine, int index){
        word_ = word;
        contextLine_ = contextLine;
        index_ = index;

        int maxIndex = contextLine_.tokenize().length - 1;

        if(index_ == 0){
            wordOnTheLeftIndex_ = index_;

            if(maxIndex == 0) {
                wordOnTheRightIndex_ = 0;
            }
            else{
                wordOnTheRightIndex_ = 1;
            }
        }
        else if(index_ == maxIndex){
            wordOnTheLeftIndex_ = maxIndex - 1;
            wordOnTheRightIndex_ = index_;
        }
        else{
            wordOnTheLeftIndex_ = index_ - 1;
            wordOnTheRightIndex_ = index_ + 1;
        }
    }

    public String getWord(){
        return word_;
    }

    public TextLine getContextLine(){
        return contextLine_;
    }

    public int getIndex(){
        return index_;
    }

    public int getWordOnTheLeftIndex(){
        return wordOnTheLeftIndex_;
    }

    public int getWordOnTheRightIndex(){
        return wordOnTheRightIndex_;
    }

    private final String word_;
    private final TextLine contextLine_;
    private final int index_;

    private final int wordOnTheLeftIndex_;
    private final int wordOnTheRightIndex_;

}
