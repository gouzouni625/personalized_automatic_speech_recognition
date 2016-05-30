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
            replaceAll("0", " zero ").
            replaceAll("1", " one ").
            replaceAll("2", " two ").
            replaceAll("3", " three ").
            replaceAll("4", " four ").
            replaceAll("5", " five ").
            replaceAll("6", " six ").
            replaceAll("7", " seven ").
            replaceAll("8", " eight ").
            replaceAll("9", " nine ").
            replaceAll("\\(", " ").
            replaceAll("\\)", " . ").
            replaceAll("\\[", " ").
            replaceAll("]", " . ").
            replaceAll("[!?]", ".").                         // Remove punctuation or question marks
            replaceAll("[_\\-',:/\"<>|#@\\\\=+$~*;]+", " "). // Remove special characters
            replaceAll("\\r\\n", " ").                       // Remove end of line
            replaceAll("\\t", " ").                          // Remove tabs
            replaceAll(" +", " ").                           // Trim repeating spaces
            toLowerCase().
            split(" ?\\. ?");                                // Split based on full stops

        ArrayList<String> usefulSentences = new ArrayList<>();
        for(String sentence : sentences){
            if(sentence.length() >= 4){
                usefulSentences.add(sentence);
            }
        }

        int numberOfSentences = usefulSentences.size();
        WordSequence[] wordSequences = new WordSequence[numberOfSentences];
        for(int i = 0;i < numberOfSentences;i++){
            wordSequences[i] = new WordSequence(usefulSentences.get(i), " ");
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
