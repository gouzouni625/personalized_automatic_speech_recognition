package org.postp;


public class TextLine {
    public TextLine(String line) {
        line_ = line;
    }

    public TextLine(String line, boolean removePunctuationMarks) {
        if (removePunctuationMarks) {
            line_ = removePunctuationMarks(line, wordSeparator_);
        } else {
            line_ = line;
        }
    }

    public String[] split(boolean... flags) {
        return split(line_, wordSeparator_, flags);
    }

    public String[] split(String wordSeparator, boolean... flags) {
        return split(line_, wordSeparator, flags);
    }

    public static String[] split(String line, String wordSeparator, boolean... flags) {
        boolean removePunctuationMarks = false;
        if (flags.length > 0) {
            removePunctuationMarks = flags[0];
        }

        StringBuilder stringBuilder = new StringBuilder();

        for (char ch : line.toCharArray()) {
            if (PunctuationMarks.isPunctuationMark(ch)) {
                if (!removePunctuationMarks) {
                    stringBuilder.append(wordSeparator).append(ch).append(wordSeparator);
                }
            } else {
                stringBuilder.append(ch);
            }
        }

        return stringBuilder.toString().split(wordSeparator + "+");
    }

    public static String removePunctuationMarks(String line, String wordSeparator) {
        StringBuilder stringBuilder = new StringBuilder();
        for (char ch : line.toCharArray()) {
            if (!PunctuationMarks.isPunctuationMark(ch)) {
                stringBuilder.append(ch);
            }
        }

        return stringBuilder.toString().replaceAll(wordSeparator + "{2,}", wordSeparator);
    }

    public enum PunctuationMarks {
        FULL_STOP('.'),
        COMMA(','),
        EXCLAMATION_MARK('!'),
        QUESTION_MARK('?'),
        APOSTROPHE('\'');

        PunctuationMarks(char symbol) {
            symbol_ = symbol;
        }

        public char getSymbol() {
            return symbol_;
        }

        public static boolean isPunctuationMark(char ch) {
            for (PunctuationMarks mark : PunctuationMarks.values()) {
                if (ch == mark.getSymbol()) {
                    return true;
                }
            }

            return false;
        }

        private char symbol_;
    }

    // start is inclusive.
    // end is exclusive.
    public TextLine subLine(int beginIndex, int endIndex){
        if(beginIndex >= endIndex){
            TextLine subLine = new TextLine("");
            subLine.setWordSeparator(wordSeparator_);

            return subLine;
        }

        // No need to think about removing punctuation marks. If this TextLine has them removed, then the new line
        // will also have them removed. Using this method, new TextLines can be created that have the same word separators
        // as the Text Lines provided by the user. That make the recognizer agnostic as far as the word separator is
        // concerned.
        String[] words = split();

        StringBuilder stringBuilder = new StringBuilder();
        for(int i = beginIndex;i < endIndex;i++){
            stringBuilder.append(words[i]).append(wordSeparator_);
        }
        String subString = stringBuilder.toString();

        // Note that the last word separator is removed.
        TextLine subLine = new TextLine(subString.substring(0, subString.length() - wordSeparator_.length()));
        subLine.setWordSeparator(wordSeparator_);

        return subLine;
    }

    @Override
    public String toString() {
        return line_;
    }

    public String getLine() {
        return line_;
    }

    public void setWordSeparator(String wordSeparator) {
        wordSeparator_ = wordSeparator;
    }

    public String getWordSeparator() {
        return wordSeparator_;
    }

    private final String line_;

    private String wordSeparator_ = " ";

}
