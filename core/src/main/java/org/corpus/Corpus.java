package org.corpus;

import org.utilities.ArrayIterable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

import static org.apache.commons.collections4.ListUtils.longestCommonSubsequence;


public class Corpus implements Iterable<WordSequence> {
    public static Corpus createFromFile(File file) throws FileNotFoundException {
        StringBuilder stringBuilder = new StringBuilder();

        Scanner scanner = new Scanner(file);
        while(scanner.hasNextLine()){
            stringBuilder.append(scanner.nextLine());
        }
        scanner.close();

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

        int maximumLength = Collections.max(subSequences, new Comparator<WordSequence>() {
            public int compare(WordSequence wordSequence1, WordSequence wordSequence2) {
                return wordSequence1.getWords().length - wordSequence2.getWords().length;
            }
        }).getWords().length;

        ArrayList<WordSequence> longestSubSequences = new ArrayList<WordSequence>();

        for(WordSequence subSequence : subSequences){
            if(subSequence.getWords().length == maximumLength){
                longestSubSequences.add(subSequence);
            }
        }

        return longestSubSequences;
    }

    public Iterator<WordSequence> iterator(){
        return (new ArrayIterable<WordSequence>(sentences_).iterator());
    }

    private final String text_;
    private final WordSequence[] sentences_;

}
