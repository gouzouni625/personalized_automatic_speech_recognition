package org.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Scanner;


public class Dictionary {
    public Dictionary(String dictionaryPath) throws FileNotFoundException {
        this(dictionaryPath, " ");
    }

    public Dictionary(String dictionaryPath, String wordPhoneSeparator) throws FileNotFoundException {
        wordsToPhonesTable_ = new Hashtable<String, String>();

        Scanner scanner = new Scanner(new File(dictionaryPath));
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();

            int indexOfSpace = line.indexOf(wordPhoneSeparator);

            wordsToPhonesTable_.put(line.substring(0, indexOfSpace), line.substring(indexOfSpace + 1));
        }
        scanner.close();
    }

    public Hashtable<String, String> getWordsToPhonesTable(){
        return new Hashtable<String, String>(wordsToPhonesTable_);
    }

    public String getPhones(String word){
        return wordsToPhonesTable_.get(word);
    }

    public String[] getPhones(String[] words){
        int numberOfWords = words.length;

        String[] phones = new String[numberOfWords];
        for(int i = 0;i < numberOfWords;i++){
            phones[i] = getPhones(words[i]);
        }

        return phones;
    }

    private final Hashtable<String, String> wordsToPhonesTable_;

}
