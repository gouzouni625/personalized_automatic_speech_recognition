package org.corpus;

import org.utilities.ArrayIterable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Scanner;


public class Corpus implements Iterable<WordSequence> {
    public static Corpus createFromFile(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);

        StringBuilder stringBuilder = new StringBuilder();

        while(scanner.hasNextLine()){
            stringBuilder.append(scanner.nextLine());
        }

        return new Corpus(stringBuilder.toString());
    }

    private Corpus(String text){
        text_ = text;

        sentences_ = tokenize(text);
    }

    private WordSequence[] tokenize(String text){
        String[] sentences = text.replaceAll("[_\\-()',:\"]+", " ").
                replaceAll("[!\\?]", ".").
                replaceAll("\\[.\\]", " ").
                replaceAll("\\n", " ").
                replaceAll(" +", " ").
                toLowerCase().split(" ?\\. ?");

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

    public void saveToFile(File file) throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(file);

        for(WordSequence sentence : sentences_){
            printWriter.write("<s> " + sentence + " </s>\n");
        }

        printWriter.close();
    }

    public Iterator<WordSequence> iterator(){
        return (new ArrayIterable<WordSequence>(sentences_).iterator());
    }

    private final String text_;
    private final WordSequence[] sentences_;

}
