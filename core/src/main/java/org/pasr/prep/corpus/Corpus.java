package org.pasr.prep.corpus;


import org.pasr.asr.dictionary.Dictionary;
import org.pasr.utilities.NumberSpeller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.ListUtils.longestCommonSubsequence;


public class Corpus implements Iterable<WordSequence> {
    public Corpus(List<Document> documents){
        documents_ = documents;

        sentences_ = new ArrayList<>();

        name_ = "";
    }

    public String getName(){
        return name_;
    }

    private List<Word> getUniqueWords (){
        HashSet<Word> uniqueWords = new HashSet<>();

        for (WordSequence sentence : sentences_) {
            uniqueWords.addAll(sentence.getWords());
        }

        return new ArrayList<>(uniqueWords);
    }

    public Dictionary process(Dictionary dictionary) {
        for(Document document : documents_){
            String processedContent = processNumbers(document.getContent());
            sentences_.addAll(createSentences(processedContent, document.getID()));
        }

        Dictionary reducedDictionary = new Dictionary();

        for(Word word : getUniqueWords()){
            String wordText = word.getText();

            Map<String, String> entries = dictionary.getEntriesByKey(wordText);

            if(entries.size() == 0){
                reducedDictionary.addUnknownWord(wordText);
            }
            else{
                reducedDictionary.addAll(entries);
            }
        }

        // Release the documents resources
        documents_ = null;

        return reducedDictionary;
    }

    /**
     * @brief Replaces number with their literal representation
     *        There are two ways that a number is pronounced. The first is for dates (e.g. 1942 ->
     *        nineteen forty two) and the second is for amounts (e.g. 1942 dollars -> one thousand
     *        nine hundred forty two dollars).
     */
    private String processNumbers(String document) {
        NumberSpeller speller = NumberSpeller.getInstance();

        ArrayList<String> matches = new ArrayList<>();

        Matcher matcher = Pattern.compile("([0-9]+) dollars").matcher(document);
        while(matcher.find()){
            matches.add(matcher.group(1));
        }
        // Sort the matches by descending length so that longer numbers get spelled first. If the
        // matches are sorted by ascending length, then a long number, say 1942 will be spelled as
        // one nine four two instead of nineteen forty two.
        Collections.sort(matches, (s1, s2) -> s2.length() - s1.length());
        for(String match : matches){
            String spelled = speller.spell(Integer.valueOf(match));
            document = document.replaceAll(spelled + " dollars", " " + spelled + " dollars ");
        }
        matches.clear();

        matcher = Pattern.compile("([0-9]+)\\$").matcher(document);
        while(matcher.find()){
            matches.add(matcher.group(1));
        }
        Collections.sort(matches, (s1, s2) -> s2.length() - s1.length());
        for(String match : matches){
            String spelled = speller.spell(Integer.valueOf(match));
            document = document.replaceAll(match + "\\$", " " + spelled + " dollars ");
        }
        matches.clear();

        matcher = Pattern.compile("([0-9]+)").matcher(document);
        while(matcher.find()){
            matches.add(matcher.group(1));
        }
        Collections.sort(matches, (s1, s2) -> s2.length() - s1.length());
        for(String match : matches){
            String spelled = speller.spell(Integer.valueOf(match), NumberSpeller.Types.DATE);
            document = document.replaceAll(match, " " + spelled + " ");
        }

        return document;
    }

    private List<WordSequence> createSentences(String document, int documentID){
        document = document.
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

        String[] sentencesText = document.split(" ?\\. ?");

        ArrayList<WordSequence> sentences = new ArrayList<>();
        for(String sentenceText : sentencesText){
            if(!sentenceText.isEmpty()) {
                sentences.add(new WordSequence(sentenceText, documentID));
            }
        }

        return sentences;
    }

    public List<String> getDocumentsText(){
        List<Integer> documentIDs = sentences_.stream()
            .map(WordSequence :: getDocumentID)
            .collect(Collectors.toList());

        ArrayList<String> documentsText = new ArrayList<>();
        for(int i : documentIDs){
            documentsText.add(sentences_.stream()
                .filter(sentence -> sentence.getDocumentID() == i)
                .map(WordSequence :: getText)
                .collect(Collectors.joining(" ")));
        }

        return documentsText;
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
                lCSS.add(new WordSequence(candidate, sentence.getDocumentID()));
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

    public void replaceWordText(String oldText, String newText){
        // If new text is empty then the words should be removed instead of having their text
        // replaced
        if(newText.isEmpty()){
            removeWordByText(oldText);
        }
        else {
            for (WordSequence sentence : sentences_) {
                sentence.replaceWordText(oldText, newText);
            }
        }
    }

    public void removeWordByText(String text){
        ArrayList<WordSequence> emptySentences = new ArrayList<>();

        for(WordSequence sentence : sentences_){
            sentence.removeByText(text);

            if(sentence.size() == 0){
                emptySentences.add(sentence);
            }
        }

        for(WordSequence emptySentence : emptySentences){
            sentences_.remove(emptySentence);
        }
    }

    public void setSentences(List<WordSequence> sentences){
        sentences_ = sentences;
    }

    public void setName(String name){
        name_ = name;
    }

    public Iterator<WordSequence> iterator(){
        return sentences_.iterator();
    }

    @Override
    public void forEach (Consumer<? super WordSequence> action) {
        sentences_.forEach(action);
    }

    private List<Document> documents_;
    private List<WordSequence> sentences_;

    private String name_;

}
