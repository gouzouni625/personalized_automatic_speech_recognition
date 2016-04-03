package org.postp;


import java.util.ArrayList;

import static org.utilities.Utilities.collectionToArray;

public class WordSequencePattern {
    public WordSequencePattern(TextLine changeablePart, TextLine line, int index){
        changeablePart_ = changeablePart;
        line_ = line;
        index_ = index;

        calculateSequences();
    }

    public int getMinSurroundingWords(){
        return minSurroundingWords_;
    }

    public void setMinSurroundingWords(int minSurroundingWords){
        if(minSurroundingWords_ != minSurroundingWords){
            calculateSequences_ = true;
        }

        minSurroundingWords_ = minSurroundingWords;
    }

    public int getMaxSurroundingWords(){
        return maxSurroundingWords_;
    }

    public void setMaxSurroundingWords(int maxSurroundingWords){
        if(maxSurroundingWords_ != maxSurroundingWords){
            calculateSequences_ = true;
        }

        maxSurroundingWords_ = maxSurroundingWords;
    }

    private void calculateSequences(){
        if(!calculateSequences_){
            return;
        }

        ArrayList<TextLine> wordsOnTheLeft = new ArrayList<TextLine>();
        ArrayList<TextLine> wordsOnTheRight = new ArrayList<TextLine>();

        String[] words = line_.split();
        int numberOfWords = words.length;
        int changeablePartLength = changeablePart_.split().length;

        for(int i = minSurroundingWords_, length = i + changeablePartLength;i <= maxSurroundingWords_;i++, length++){
            for(int j = -i;j < 1;j++){
                int firstWordIndex = index_ + j;
                if(firstWordIndex < 0){
                    continue;
                }

                // The last word index is exclusive.
                int lastWordIndex = firstWordIndex + length;
                if(lastWordIndex >= numberOfWords){
                    lastWordIndex = numberOfWords - 1;
                }

                wordsOnTheLeft.add(line_.subLine(firstWordIndex, index_));
                wordsOnTheRight.add(line_.subLine(index_ + changeablePartLength, lastWordIndex));
            }
        }

        wordsOnTheLeft_ = new TextLine[wordsOnTheLeft.size()];
        collectionToArray(wordsOnTheLeft, wordsOnTheLeft_);

        wordsOnTheRight_ = new TextLine[wordsOnTheRight.size()];
        collectionToArray(wordsOnTheRight, wordsOnTheRight_);

        calculateSequences_ = false;
    }

    public String[] getRegularExpressionPatterns(int maxWordsForChangeablePart){
        String[] patterns = new String[wordsOnTheLeft_.length];

        // TODO
        // Using "second moment of (([a-zA-Z]+ ?){0,5}) the" as a regular expression to search in
        // "After the second moment of silence this is the third interval of speaking and the last one"
        // the following two cases arise:
        // 1) if you use 8 or less words ({0,8}) the result is "silence this is" which is the one wanted.
        // 2) if you use 9 or more words ({0,9}) the result is "silence this is the third interval of speaking and"
        //    because the second "the" show up. Find a way to deal with this situation so that getting back only
        //    the first result is possible.
        String pattern = "(([a-zA-Z]+" + line_.getWordSeparator() + "){0," + Integer.toString(maxWordsForChangeablePart - 1) + "}[a-zA-Z]+)";

        for(int i = 0, n = patterns.length;i < n;i++){
            patterns[i] = "";

            if(wordsOnTheLeft_[i].getLine().length() > 0){
                patterns[i] += wordsOnTheLeft_[i] + line_.getWordSeparator();
            }

            patterns[i] += pattern;

            if(wordsOnTheRight_[i].getLine().length() > 0){
                patterns[i] += line_.getWordSeparator() + wordsOnTheRight_[i];
            }
        }

        return patterns;
    }

    public String[] getPronunciationSequences(Dictionary dictionary){
        String[] pronunciationSequences = new String[wordsOnTheLeft_.length];

        StringBuilder stringBuilder = new StringBuilder();
        String[] phones = dictionary.getPhones(changeablePart_.split());
        for(String phone : phones){
            stringBuilder.append(phone);
        }

        String changeablePartPhones = stringBuilder.toString();
        for(int i = 0, n = pronunciationSequences.length;i < n;i++){
            stringBuilder = new StringBuilder();

            phones = dictionary.getPhones(wordsOnTheLeft_[i].split());
            for(String phone : phones){
                stringBuilder.append(phone);
            }

            stringBuilder.append(changeablePartPhones);

            phones = dictionary.getPhones(wordsOnTheRight_[i].split());
            for(String phone : phones){
                stringBuilder.append(phone);
            }

            pronunciationSequences[i] = stringBuilder.toString();
        }

        return pronunciationSequences;
    }

    private final TextLine changeablePart_;
    private final TextLine line_;
    private final int index_;

    private TextLine[] wordsOnTheLeft_;
    private TextLine[] wordsOnTheRight_;
    private boolean calculateSequences_ = true;

    private int minSurroundingWords_ = 3;
    private int maxSurroundingWords_ = 5;
}
