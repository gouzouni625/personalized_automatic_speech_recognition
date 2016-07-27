package org.pasr.prep.corpus;


import org.pasr.asr.dictionary.Dictionary;
import org.pasr.prep.email.fetchers.Email;

import org.pasr.utilities.NumberSpeller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.ListUtils.longestCommonSubsequence;


public class Corpus implements Iterable<WordSequence> {
    public Corpus(){
        text_ = "";
        sentences_ = new ArrayList<>();
        name_ = "";
    }

    public Corpus(String text){
        text_ = text;
        sentences_ = new ArrayList<>();
        name_ = "";
    }

    public Corpus(List<Email> emails){
        StringBuilder stringBuilder = new StringBuilder();

        for(Email email : emails){
            stringBuilder.append(email.getBody()).append(" ");
        }

        text_ = stringBuilder.toString().trim();
        sentences_ = new ArrayList<>();
        name_ = "";
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

    public String getText(){
        return text_;
    }

    public List<WordSequence> getSentences() {
        return sentences_;
    }

    public String getName(){
        return name_;
    }

    private List<Word> getWords(){
        ArrayList<Word> words = new ArrayList<>();

        for (WordSequence sentence : sentences_) {
            words.addAll(sentence.getWords());
        }

        return words;
    }

    public void setName(String name){
        name_ = name;
    }

    public synchronized void append(String text){
        if(!text_.isEmpty()){
            text_ += " ";
        }
        text_ += text;
    }

    public Dictionary process(Dictionary dictionary) {
        processNumbers();
        createSentences();

        Dictionary reducedDictionary = new Dictionary();

        for(Word word : getWords()){
            String wordText = word.getText();

            Map<String, String> entries = dictionary.getEntriesByKey(wordText);

            if(entries.size() == 0){
                reducedDictionary.addUnknownWord(wordText);
            }
            else{
                reducedDictionary.addAll(entries);
            }
        }

        return reducedDictionary;
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
            text_ = text_.replaceAll(spelled + " dollars", " " + spelled + " dollars ");
        }
        matches.clear();

        matcher = Pattern.compile("([0-9]+)\\$").matcher(text_);
        while(matcher.find()){
            matches.add(matcher.group(1));
        }
        Collections.sort(matches, (s1, s2) -> s2.length() - s1.length());
        for(String match : matches){
            String spelled = speller.spell(Integer.valueOf(match));
            text_ = text_.replaceAll(match + "\\$", " " + spelled + " dollars ");
        }
        matches.clear();

        matcher = Pattern.compile("([0-9]+)").matcher(text_);
        while(matcher.find()){
            matches.add(matcher.group(1));
        }
        Collections.sort(matches, (s1, s2) -> s2.length() - s1.length());
        for(String match : matches){
            String spelled = speller.spell(Integer.valueOf(match), NumberSpeller.Types.DATE);
            text_ = text_.replaceAll(match, " " + spelled + " ");
        }
    }

    private void createSentences(){
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

        String[] sentencesText = text_.split(" ?\\. ?");

        for(String sentenceText : sentencesText){
            sentences_.add(new WordSequence(sentenceText, " "));
        }
    }

    public boolean contains(WordSequence wordSequence){
        for(WordSequence sentence : sentences_){
            if(sentence.getText().contains(wordSequence.getText())){
                return true;
            }
        }

        return false;
    }

    public List<WordSequence> longestCommonSubSequences (WordSequence wordSequence){
        ArrayList<WordSequence> lCSS = new ArrayList<>();

        List<Word> words = wordSequence.getWords();
        for(WordSequence sentence : sentences_){
            List<Word> candidate = longestCommonSubsequence(sentence.getWords(), words);

            if(candidate.size() > 0) {
                lCSS.add(new WordSequence(candidate, " "));
            }
        }

        return lCSS;
    }

    public List<WordSequence> matchAsCommonSubSequence(WordSequence wordSequence){
        int size = wordSequence.size();

        return longestCommonSubSequences(wordSequence).stream()
            .filter(subSequence -> subSequence.size() == size)
            .collect(Collectors.toList());
    }

    public void saveToFile(File file) throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(file);

        sentences_.forEach(printWriter:: println);

        printWriter.close();
    }

    public Iterator<WordSequence> iterator(){
        return sentences_.iterator();
    }

    private String text_;
    private List<WordSequence> sentences_;

    private String name_;

}
