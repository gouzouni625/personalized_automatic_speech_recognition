package org.corpus;

import org.utilities.ArrayIterable;
import org.utilities.Margin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

import static org.apache.commons.collections4.ListUtils.longestCommonSubsequence;


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

    public boolean containsText(String text){
        for(WordSequence sequence_ : sentences_){
            if(sequence_.containsText(text)){
                return true;
            }
        }

        return false;
    }

    /**
     * @brief Matches a given String against the sentences of this Corpus
     *
     * @param text
     *     The String to match against the sentences of this Corpus
     *
     * @return The longest, continuous sub-sequence of Words inside this Corpus that matches the
     *         given String
     */
    public WordSequence matchText(String text){
        // Get the words of the given String
        Word[] textWords = new WordSequence(text.toLowerCase(), " ").getWords();

        ArrayList<WordSequence> subSequences = new ArrayList<WordSequence>();

        // Apache Commons longestCommonSubsequence returns the objects of its first argument. That
        // means that the Words added in subSequences are the actual Words that exists inside this
        // Corpus.
        for(WordSequence sentence : sentences_){
            subSequences.add(new WordSequence(
                    longestCommonSubsequence(
                            Arrays.asList(sentence.getWords()),
                            Arrays.asList(textWords),
                            Word.textEquator
                    ), " ")
            );
        }

        // TODO Handle the case where there are two sub-sequences of the same length

        Margin chosenMargin = subSequences.get(0).longestContinuousSubSequence();
        Margin currentMargin;
        int index = 0;
        for(int i = 1, n = subSequences.size();i < n;i++){
            currentMargin = subSequences.get(i).longestContinuousSubSequence();

            if(currentMargin.length_ > chosenMargin.length_){
                chosenMargin = currentMargin;
                index = i;
            }
        }

        return subSequences.get(index).subSequence(chosenMargin.leftIndex_,
                chosenMargin.rightIndex_);
    }

    public Iterator<WordSequence> iterator(){
        return (new ArrayIterable<WordSequence>(sentences_).iterator());
    }

    private final String text_;
    private final WordSequence[] sentences_;

}
