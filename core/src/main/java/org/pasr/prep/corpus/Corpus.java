package org.pasr.prep.corpus;


import org.pasr.asr.dictionary.Dictionary;
import org.pasr.utilities.NumberSpeller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.Observable;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.collections4.ListUtils.longestCommonSubsequence;


public class Corpus extends Observable implements Iterable<WordSequence> {
    public Corpus(List<Document> documents){
        documentList_ = documents;

        wordSequenceList_ = new ArrayList<>();

        name_ = "";
        id_ = - 1;
    }

    public Stream<WordSequence> stream(){
        return wordSequenceList_.stream();
    }

    public int getID(){
        return id_;
    }

    public String getName(){
        return name_;
    }

    public int size(){
        return wordSequenceList_.size();
    }

    public int numberOfDocuments(){
        return wordSequenceList_.stream()
            .map(WordSequence :: getDocumentID)
            .collect(Collectors.toSet()).size();
    }

    private List<Word> getUniqueWords (){
        HashSet<Word> uniqueWords = new HashSet<>();

        for (WordSequence wordSequence : wordSequenceList_) {
            uniqueWords.addAll(wordSequence.getWords());
        }

        return new ArrayList<>(uniqueWords);
    }

    public Dictionary process(Dictionary dictionary) {
        cancelProcess_ = false;

        for(int i = 0, n = documentList_.size();i < n;i++){
            if(cancelProcess_){
                return new Dictionary();
            }

            String processedContent = processNumbers(documentList_.get(i).getContent());
            wordSequenceList_.addAll(createWordSequences(
                processedContent, documentList_.get(i).getID(), documentList_.get(i).getTitle())
            );

            setChanged();
            notifyObservers(((double) (i + 1)) / (2 * n));
        }

        Dictionary reducedDictionary = new Dictionary();

        List<Word> uniqueWords = getUniqueWords();

        for(int i = 0, n = uniqueWords.size();i < n;i++){
            if(cancelProcess_){
                return reducedDictionary;
            }

            String wordText = uniqueWords.get(i).getText();

            Map<String, String> entries = dictionary.getEntriesByKey(wordText);

            if(entries.size() == 0){
                reducedDictionary.addUnknownWord(wordText);
            }
            else{
                reducedDictionary.addAll(entries);
            }

            setChanged();
            notifyObservers(0.5 + ((double) (i + 1)) / (2 * n));
        }

        // Release the documents resources
        documentList_ = null;

        return reducedDictionary;
    }

    public void cancelProcess(){
        cancelProcess_ = true;
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

    private List<WordSequence> createWordSequences (String document, long documentID,
                                                    String documentTitle){
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

        String[] wordSequenceTextArray = document.split(" ?\\. ?");

        ArrayList<WordSequence> wordSequences = new ArrayList<>();
        for(String wordSequenceText : wordSequenceTextArray){
            if(!wordSequenceText.isEmpty()) {
                wordSequences.add(new WordSequence(wordSequenceText, documentID, documentTitle));
            }
        }

        return wordSequences;
    }

    public List<Document> getDocuments () {
        return wordSequenceList_.stream()
            .collect(Collectors.groupingBy(WordSequence:: getDocumentID, Collectors.toList()))
            .values().stream()
            .map(WordSequenceList -> {
                Optional<WordSequence> r = WordSequenceList.stream()
                    .reduce(WordSequence:: appendSequence);

                if (r.isPresent()) {
                    return r.get();
                }
                else {
                    return null;
                }
            })
            .filter(wordSequence -> wordSequence != null)
            .map(wordSequence -> new Document(wordSequence.getDocumentID(),
                wordSequence.getDocumentTitle(), wordSequence.getText()
            ))
            .collect(Collectors.toList());
    }

    public String getText(){
        StringBuilder stringBuilder = new StringBuilder();

        for(WordSequence wordSequence : wordSequenceList_){
            stringBuilder.append(wordSequence.getText()).append("\n");
        }

        return stringBuilder.toString();
    }

    public boolean contains(WordSequence wordSequence){
        for(WordSequence wordSequence_ : wordSequenceList_){
            if(wordSequence_.getText().contains(wordSequence.getText())){
                return true;
            }
        }

        return false;
    }

    public List<WordSequence> longestCommonSubSequences (WordSequence wordSequence){
        ArrayList<WordSequence> lCSS = new ArrayList<>();

        List<Word> words = wordSequence.getWords();
        for(WordSequence wordSequence_ : wordSequenceList_){
            List<Word> candidate = longestCommonSubsequence(wordSequence_.getWords(), words);

            if(candidate.size() > 0) {
                lCSS.add(new WordSequence(
                    candidate, wordSequence_.getDocumentID(), wordSequence_.getDocumentTitle()
                ));
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

    public String getRandomSubSequence(Random random){
        return wordSequenceList_.get(random.nextInt(size())).getRandomSubsequence(random);
    }

    public void replaceWordText(String oldText, String newText){
        // If new text is empty then the words should be removed instead of having their text
        // replaced
        if(newText.isEmpty()){
            removeWordByText(oldText);
        }
        else {
            for (WordSequence wordSequence : wordSequenceList_) {
                wordSequence.replaceWordText(oldText, newText);
            }
        }
    }

    public void removeWordByText(String text){
        ArrayList<WordSequence> emptyWordSequences = new ArrayList<>();

        for(WordSequence wordSequence : wordSequenceList_){
            wordSequence.removeByText(text);

            if(wordSequence.size() == 0){
                emptyWordSequences.add(wordSequence);
            }
        }

        for(WordSequence emptyWordSequence : emptyWordSequences){
            wordSequenceList_.remove(emptyWordSequence);
        }
    }

    public void setWordSequences (List<WordSequence> wordSequences){
        wordSequenceList_ = wordSequences;
    }

    public void setID(int id){
        id_ = id;
    }

    public void setName(String name){
        name_ = name;
    }

    public Iterator<WordSequence> iterator(){
        return wordSequenceList_.iterator();
    }

    @Override
    public void forEach (Consumer<? super WordSequence> action) {
        wordSequenceList_.forEach(action);
    }

    private List<Document> documentList_;
    private List<WordSequence> wordSequenceList_;

    private int id_;
    private String name_;

    private volatile boolean cancelProcess_;

}
