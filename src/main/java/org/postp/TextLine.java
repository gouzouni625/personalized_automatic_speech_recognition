package org.postp;

import org.postp.Configuration.PunctuationMarks;

public class TextLine {
    public TextLine(String line) {
        line_ = configuration_.arePunctuationMarksRemoved() ? removePunctuationMarks(line) : line;
    }

    private String removePunctuationMarks(String line) {
        StringBuilder stringBuilder = new StringBuilder();
        for (char ch : line.toCharArray()) {
            if (!PunctuationMarks.isPunctuationMark(ch)) {
                stringBuilder.append(ch);
            }
            else{
                stringBuilder.append(configuration_.getWordSeparator());
            }
        }

        return stringBuilder.toString().replaceAll(configuration_.getWordSeparator() + "{2,}",
                configuration_.getWordSeparator());
    }

    public String[] tokenize() {
        return line_.split(configuration_.getWordSeparator());
    }

    // beginIndex is inclusive
    // endIndex is exclusive
    public TextLine subLine(int beginIndex, int endIndex){
        if(beginIndex >= endIndex){
            return new TextLine("");
        }

        String[] words = tokenize();

        StringBuilder stringBuilder = new StringBuilder();
        for(int i = beginIndex;i < endIndex - 1;i++){
            stringBuilder.append(words[i]).append(configuration_.getWordSeparator());
        }
        stringBuilder.append(words[endIndex - 1]); // Avoid adding a word separator at the end

        return new TextLine(stringBuilder.toString());
    }

    @Override
    public String toString() {
        return getLine();
    }

    public String getLine() {
        return line_;
    }

    private final String line_;

    private final Configuration configuration_ = Configuration.getInstance();

}
