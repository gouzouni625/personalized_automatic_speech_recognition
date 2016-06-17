package org.pasr.corpus;


import org.pasr.postp.dictionary.Dictionary;
import org.pasr.utilities.ArrayIterable;
import org.pasr.utilities.NumberSpeller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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

    public Dictionary process(Dictionary dictionary) {
        processNumbers();
        createSentences();

        Dictionary reducedDictionary = new Dictionary();

        for(String word : getWords()){
            Map<String, String> entries = dictionary.getEntriesByKey(word);

            if(entries.size() == 0){
                reducedDictionary.addUnknownWord(word);
            }
            else{
                reducedDictionary.addAll(entries);
            }
        }

        return reducedDictionary;
    }

    private void createSentences(){
        sentences_ = tokenize();
    }

    /**
     * @brief Replaces number with their literal representation
     *        There are two ways that a number is pronounced. The first is for dates (e.g. 1942 ->
     *        nineteen forty two) and the second is for amounts (e.g. 1942 dollars -> one thousand
     *        nine hundred forty two dollars).
     */
    private void processNumbers() {
        NumberSpeller speller = NumberSpeller.getInstance();

        ArrayList<String> matches = new ArrayList<>();

        Matcher matcher = Pattern.compile("([0-9]+) dollars").matcher(text_);
        while(matcher.find()){
            matches.add(matcher.group(1));
        }
        // Sort the matches by descending length so that longer numbers get spelled first. If the
        // matches are sorted by ascending length, then a long number, say 1942 will be spelled as
        // one nine four two instead of nineteen forty two.
        Collections.sort(matches, (s1, s2) -> s2.length() - s1.length());
        for(String match : matches){
            String spelled = speller.spell(Integer.valueOf(match));
            text_ = text_.replaceAll(spelled + " dollars", spelled + " dollars");
        }
        matches.clear();

        matcher = Pattern.compile("([0-9]+)\\$").matcher(text_);
        while(matcher.find()){
            matches.add(matcher.group(1));
        }
        Collections.sort(matches, (s1, s2) -> s2.length() - s1.length());
        for(String match : matches){
            String spelled = speller.spell(Integer.valueOf(match));
            text_ = text_.replaceAll(match + "\\$", spelled + " dollars");
        }
        matches.clear();

        matcher = Pattern.compile("([0-9]+)").matcher(text_);
        while(matcher.find()){
            matches.add(matcher.group(1));
        }
        Collections.sort(matches, (s1, s2) -> s2.length() - s1.length());
        for(String match : matches){
            String spelled = speller.spell(Integer.valueOf(match), NumberSpeller.Types.DATE);
            text_ = text_.replaceAll(match, spelled);
        }
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

    private WordSequence[] tokenize(){
        text_ = text_.
            replaceAll("\\(", " ").
            replaceAll("\\)", " . ").
            replaceAll("\\[", " ").
            replaceAll("]", " . ").
            replaceAll("[!?;]", ".").
            replaceAll("[_\\-,:/\"<>|#@\\\\=+~*]+", " ").
            replaceAll("\\r\\n", " ").
            replaceAll("\\t", " ").
            replaceAll(" +", " ").
            toLowerCase();

        String[] sentences = text_.split(" ?\\. ?");

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

    private HashSet<String> getWords(){
        HashSet<String> words = new HashSet<>();

        for (WordSequence wordSequence : sentences_) {
            Collections.addAll(words, wordSequence.getWordsText());
        }

        return words;
    }

    public Iterator<WordSequence> iterator(){
        return (new ArrayIterable<>(sentences_).iterator());
    }

    private String text_;
    private WordSequence[] sentences_;

}
