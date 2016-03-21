package org.postp;


public class TextLine {
    public TextLine(String line) {
        line_ = line;
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

    public void setWordSeparator(String wordSeparator) {
        wordSeparator_ = wordSeparator;
    }

    public String getWordSeparator() {
        return wordSeparator_;
    }

    public enum PunctuationMarks {
        FULL_STOP('.'),
        COMMA(','),
        EXCLAMATION_MARK('!'),
        QUESTION_MARK('?');

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

    private String line_;

    private String wordSeparator_ = " ";

}
