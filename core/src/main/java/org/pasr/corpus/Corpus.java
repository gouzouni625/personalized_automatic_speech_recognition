package org.pasr.corpus;

import org.pasr.utilities.ArrayIterable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;

import static org.apache.commons.collections4.ListUtils.longestCommonSubsequence;


public class Corpus implements Iterable<WordSequence> {
    public Corpus(){
        text_ = "";
    }

    public void append(String text){
        text_ += text;
    }

    public void process(){
        sentences_ = tokenize(text_);
    }

    public static Corpus createFromStream(InputStream inputStream) throws FileNotFoundException {
        StringBuilder stringBuilder = new StringBuilder();

        Scanner scanner = new Scanner(inputStream);
        while(scanner.hasNextLine()){
            stringBuilder.append(scanner.nextLine());
        }
        scanner.close();

        return new Corpus(stringBuilder.toString());
    }

    public Corpus(String text){
        text_ = text;
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

    public List<WordSequence> matchWordSequence(WordSequence wordSequence){
        // Get the words of the given String
        Word[] words = wordSequence.getWords();

        ArrayList<WordSequence> subSequences = new ArrayList<WordSequence>();

        // Apache Commons longestCommonSubsequence returns the objects of its first argument. That
        // means that the Words added in subSequences are the actual Words that exists inside this
        // Corpus.
        for(WordSequence sentence : sentences_){
            subSequences.add(new WordSequence(
                    longestCommonSubsequence(
                            Arrays.asList(sentence.getWords()),
                            Arrays.asList(words),
                            Word.textEquator
                    ), " ").longestContinuousSubSequence()
            );
        }

        int maximumLength = Collections.max(
            subSequences,
            (wordSequence1, wordSequence2) -> wordSequence1.getWords().length -
                wordSequence2.getWords().length).
            getWords().length;

        ArrayList<WordSequence> longestSubSequences = new ArrayList<>();

        for(WordSequence subSequence : subSequences){
            if(subSequence.getWords().length == maximumLength){
                longestSubSequences.add(subSequence);
            }
        }

        return longestSubSequences;
    }

    public Iterator<WordSequence> iterator(){
        return (new ArrayIterable<>(sentences_).iterator());
    }

    private String text_;
    private WordSequence[] sentences_;

}
