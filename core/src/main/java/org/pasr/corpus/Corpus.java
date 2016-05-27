package org.pasr.corpus;


import org.pasr.utilities.ArrayIterable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;


public class Corpus implements Iterable<WordSequence> {
    public Corpus(){
        text_ = "";
    }

    public Corpus(String text){
        text_ = text;
    }

    public synchronized void append(String text){
        text_ += text;
    }

    public void process() {
        createSentences();
    }

    private void createSentences(){
        sentences_ = tokenize(text_);
    }

    public static Corpus createFromStream(InputStream inputStream){
        StringBuilder stringBuilder = new StringBuilder();

        Scanner scanner = new Scanner(inputStream);
        while(scanner.hasNextLine()){
            stringBuilder.append(scanner.nextLine());
        }
        scanner.close();

        return new Corpus(stringBuilder.toString());
    }

    private WordSequence[] tokenize(String text){
        String[] sentences = text.
            replaceAll("<.*>", " ").            // Remove links
            replaceAll("\\[(.*)]", "$1 .").     // Remove brackets but keep the text inside
            replaceAll("\\((.*)\\)", "$1 .").   // Remove parentheses but keep the text inside
            replaceAll("[_\\-',:\"]+", " ").    // Remove special characters
            replaceAll("!", ".").               // Remove punctuation marks
            replaceAll("\\r\\n", " ").          // Remove end of line
            replaceAll(" +", " ").              // Trim repeating spaces
            toLowerCase().split(" ?\\. ?");     // Split based on full stops

        int numberOfSentences = sentences.length;
        WordSequence[] wordSequences = new WordSequence[numberOfSentences];
        for(int i = 0;i < numberOfSentences;i++){
            wordSequences[i] = new WordSequence(sentences[i], " ");
        }

        return wordSequences;
    }

    public WordSequence[] getSentences() {
        return sentences_;
    }

    public String getText(){
        return text_;
    }

    public static Corpus merge(Corpus corpus1, Corpus corpus2){
        String text = corpus1.getText();
        text += " " + corpus2.getText();

        return new Corpus(text);
    }

    public void saveToFile(File file) throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(file);

        for(WordSequence sentence : sentences_){
            printWriter.write("<s> " + sentence + " </s>\n");
        }

        printWriter.close();
    }

    public boolean containsText(String text){
        for(WordSequence sequence_ : sentences_){
            if(sequence_.containsText(text)){
                return true;
            }
        }

        return false;
    }

    public Iterator<WordSequence> iterator(){
        return (new ArrayIterable<>(sentences_).iterator());
    }

    private String text_;
    private WordSequence[] sentences_;

}
