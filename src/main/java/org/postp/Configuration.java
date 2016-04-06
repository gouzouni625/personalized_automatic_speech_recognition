package org.postp;

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

    private static Configuration instance_ = new Configuration();

    private String wordSeparator_ = " ";
    private String sentenceSeparator_ = ".";
    private String newLineDelimiter_ = System.getProperty("line.separator"); //!< The system
                                                                             //!< independent line
                                                                             //!< separator

}
