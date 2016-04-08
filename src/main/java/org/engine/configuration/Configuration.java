package org.engine.configuration;

public class Configuration {
    private Configuration() {
    }

    public static Configuration getInstance() {
        return instance_;
    }

    public String getWordSeparator(){
        return wordSeparator_;
    }

    public void setWordSeparator(String wordSeparator){
        wordSeparator_ = wordSeparator;
    }

    public String getSentenceSeparator(){
        return sentenceSeparator_;
    }

    public void setSentenceSeparator(String sentenceSeparator){
        sentenceSeparator_ = sentenceSeparator;
    }

    public String getNewLineDelimiter(){
        return newLineDelimiter_;
    }

    public void setNewLineDelimiter(String newLineDelimiter){
        newLineDelimiter_ = newLineDelimiter;
    }

    public boolean arePunctuationMarksRemoved(){
        return removePunctuationMarks_;
    }

    public void setRemovePunctuationMarks(boolean removePunctuationMarks){
        removePunctuationMarks_ = removePunctuationMarks;
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

    private static Configuration instance_ = new Configuration();

    private String wordSeparator_ = " ";
    private String sentenceSeparator_ = ".";
    private String newLineDelimiter_ = System.getProperty("line.separator"); //!< The system
                                                                             //!< independent line
                                                                             //!< separator
    private boolean removePunctuationMarks_ = true;

}
