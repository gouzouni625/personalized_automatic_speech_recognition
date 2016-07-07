package org.pasr.asr.dictionary;

import org.pasr.corpus.Word;
import org.pasr.corpus.WordSequence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;


public class Dictionary implements Iterable<Map.Entry<String, String>>{
    public static Dictionary createFromStream (InputStream inputStream) throws FileNotFoundException {
        LinkedHashMap<String, String> wordsToPhonesTable = new LinkedHashMap<>();

        Scanner scanner = new Scanner(inputStream);
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();

            int indexOfSeparation = line.indexOf(" ");

            wordsToPhonesTable.put(line.substring(0, indexOfSeparation),
                    line.substring(indexOfSeparation + 1));
        }
        scanner.close();

        return new Dictionary(wordsToPhonesTable);
    }

    public Dictionary(){
        wordsToPhonesTable_ = new LinkedHashMap<>();

        unknownWords_ = new HashSet<>();
    }

    private Dictionary(Map<String, String> wordsToPhonesTable) {
        wordsToPhonesTable_ = wordsToPhonesTable;

        unknownWords_ = new HashSet<>();
    }

    public String getPhones(String word){
        if(word.equals("")){
            return "";
        }

        return wordsToPhonesTable_.get(word);
    }

    public Map<String, String> getEntriesByKey(String key){
        return wordsToPhonesTable_.entrySet().stream().
            filter(entry -> entry.getKey().equals(key) ||
                entry.getKey().matches(key + "\\([0-9]+\\)")).
            collect(Collectors.toMap(Map.Entry:: getKey, Map.Entry:: getValue));
    }

    public String[] getPhones(WordSequence wordSequence){
        Word[] words = wordSequence.getWords();

        if(words.length == 0){
            return new String[] {};
        }

        int numberOfWords = words.length;

        String[] phones = new String[numberOfWords];
        for(int i = 0;i < numberOfWords;i++){
            phones[i] = getPhones(words[i].getText());
        }

        return phones;
    }

    public void remove(String key){
        if(wordsToPhonesTable_.remove(key) == null){
            return;
        }

        int index = 2;
        while(wordsToPhonesTable_.remove(key + "(" + index + ")") != null){
            index++;
        }
    }

    public void saveToFile(File file) throws FileNotFoundException {
        // Sort the entries of the dictionary based on the key length. This will enusre that
        // "the(1)" is below "the" when the dictionary is printed.
        List<Map.Entry<String, String>> entries = new ArrayList<>();
        entries.addAll(wordsToPhonesTable_.entrySet());

        Collections.sort(entries, (e1, e2) -> e1.getKey().length() - e2.getKey().length());

        PrintWriter printWriter = new PrintWriter(file);

        for (Map.Entry<String, String> entry : entries) {
            printWriter.write(entry.getKey() + " " + entry.getValue() + "\n");
        }

        printWriter.close();
    }

    public void add(Map.Entry<String, String> entry){
        add(entry.getKey(), entry.getValue());
    }

    public void add(String key, String value){
        if(!wordsToPhonesTable_.containsKey(key)) {
            wordsToPhonesTable_.put(key, value);

            return;
        }

        int index = 1;
        while(wordsToPhonesTable_.containsKey(key + "(" + index + ")")){
            // if the given value already exists inside the dictionary, don't add it again
            if(wordsToPhonesTable_.get(key + "(" + index + ")").equals(value)){
                return;
            }

            index++;
        }

        wordsToPhonesTable_.put(key + "(" + index + ")", value);
    }

    public void addAll(Map<String, String> entries){
        for(Map.Entry<String, String> entry : entries.entrySet()){
            this.add(entry.getKey(), entry.getValue());
        }
    }

    public void addUnknownWord(String word){
        unknownWords_.add(word);
    }

    public HashSet<String> getUnknownWords(){
        return unknownWords_;
    }

    public int size(){
        return wordsToPhonesTable_.size();
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator () {
        return wordsToPhonesTable_.entrySet().iterator();
    }

    private final Map<String, String> wordsToPhonesTable_;
    private final HashSet<String> unknownWords_;

}
