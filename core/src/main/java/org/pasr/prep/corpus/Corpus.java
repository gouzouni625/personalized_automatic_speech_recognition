package org.pasr.prep.corpus;


import org.pasr.asr.dictionary.Dictionary;
import org.pasr.utilities.NumberSpeller;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class Corpus extends ArrayList<WordSequence> {
    public Corpus(){
        this(null);
    }

    public Corpus(List<WordSequence> wordSequenceList){
        if(wordSequenceList != null) {
            addAll(wordSequenceList);
        }

        name_ = "";
        id_ = - 1;

        progress_ = new Progress();
    }

    public int getId (){
        return id_;
    }

    public String getName(){
        return name_;
    }

    public Observable getProgress(){
        return progress_;
    }

    public int numberOfDocuments(){
        return stream()
            .map(WordSequence :: getDocumentId)
            .collect(Collectors.toSet()).size();
    }

    private Set<String> getUniqueWords (){
        HashSet<String> uniqueWords = new HashSet<>();

        for (WordSequence wordSequence : this) {
            uniqueWords.addAll(wordSequence.stream()
                .map(Word :: toString)
                .collect(Collectors.toSet()));
        }

        return uniqueWords;
    }

    public Dictionary process(Dictionary dictionary) {
        cancelProcess_ = false;

        if(documentList_ != null && documentList_.size() > 0) {
            for (int i = 0, n = documentList_.size(); i < n; i++) {
                if (cancelProcess_) {
                    return new Dictionary();
                }

                String processedContent = processNumbers(documentList_.get(i).getContent());
                addAll(createWordSequences(
                    processedContent, documentList_.get(i).getId(), documentList_.get(i).getTitle())
                );

                progress_.setValue(((double) (i + 1)) / (2 * n));
            }
        }
        else{
            progress_.setValue(0.50);
        }

        Dictionary reducedDictionary = new Dictionary();

        Set<String> uniqueWords = getUniqueWords();
        int n = uniqueWords.size();
        int i = 0;

        for(String uniqueWord : uniqueWords){
            if(cancelProcess_){
                return reducedDictionary;
            }

            Map<String, String> entries = dictionary.getEntriesByKey(uniqueWord);

            if(entries.size() == 0){
                reducedDictionary.addUnknownWord(uniqueWord);
            }
            else{
                reducedDictionary.addAll(entries);
            }

            i++;
            progress_.setValue(0.5 + ((double) (i + 1)) / (2 * n));
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
        return stream()
            .map(wordSequence -> new Document(wordSequence.getDocumentId(),
                wordSequence.getDocumentTitle(), wordSequence.toString()
            ))
            .collect(Collectors.groupingBy(Document:: getId, Collectors.toList()))
            .values().stream()
            .map(documentList -> {
                Optional<Document> r = documentList.stream()
                    .reduce((document1, document2) -> new Document(
                        document1.getId(), document1.getTitle(),
                        document1.getContent() + ".\n" + document2.getContent()
                    ));

                if (r.isPresent()) {
                    return r.get();
                }
                else {
                    return null;
                }
            })
            .filter(wordSequence -> wordSequence != null)
            .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        for(WordSequence wordSequence : this){
            stringBuilder.append(wordSequence.toString()).append(".");
        }

        return stringBuilder.toString();
    }

    public String toPrettyString(){
        return toString().replaceAll("\\.", ".\n");
    }

    public boolean contains(String string){
        for(WordSequence wordSequence : this){
            if(wordSequence.contains(string)){
                return true;
            }
        }

        return false;
    }

    public String getRandomSubSequence(Random random){
        return get(random.nextInt(size())).getRandomSubsequence(random);
    }

    public void replaceWordText(String oldText, String newText){
        // If new text is empty then the words should be removed instead of having their text
        // replaced
        if(newText.isEmpty()){
            removeWordByText(oldText);
        }
        else {
            for (WordSequence wordSequence : this) {
                wordSequence.replaceWordText(oldText, newText);
            }
        }
    }

    public void removeWordByText(String text){
        ArrayList<WordSequence> emptyWordSequences = new ArrayList<>();

        for(WordSequence wordSequence : this){
            wordSequence.removeByText(text);

            if(wordSequence.size() == 0){
                emptyWordSequences.add(wordSequence);
            }
        }

        emptyWordSequences.forEach(this :: remove);
    }

    public void setId (int id){
        id_ = id;
    }

    public void setName(String name){
        name_ = name;
    }

    public void setDocuments(List<Document> documentList){
        documentList_ = documentList;
    }

    private List<Document> documentList_;

    private int id_;
    private String name_;

    private Progress progress_;
    private volatile boolean cancelProcess_;

}
