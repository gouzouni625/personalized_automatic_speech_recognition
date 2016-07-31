package org.pasr.database;


import org.pasr.asr.dictionary.Dictionary;
import org.pasr.prep.corpus.Corpus;
import org.pasr.prep.corpus.WordSequence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DataBase {
    private DataBase () {}

    public void newCorpusEntry (Corpus corpus, Dictionary dictionary){
        int newCorpusIndex;
        File[] files = corpusDirectory_.listFiles();
        if(files == null){
            newCorpusIndex = 1;
        }
        else{
            newCorpusIndex = files.length + 1;
        }

        File newCorpusDirectory = new File(corpusDirectory_, String.valueOf(newCorpusIndex));
        if(!newCorpusDirectory.mkdir()){
            throw new RuntimeException("Could not create the directory to save the new corpus");
        }

        try {
            saveCorpusToDirectory(corpus, newCorpusDirectory);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            saveDictionaryToDirectory(dictionary, newCorpusDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveCorpusToDirectory(Corpus corpus, File directory) throws FileNotFoundException {
        PrintWriter sentencesPrintWriter = new PrintWriter(new File(directory, "sentences.txt"));
        PrintWriter documentIDSPrintWriter = new PrintWriter(
            new File(directory, "document_ids.txt")
        );

        corpus.forEach(sentence -> {
            sentencesPrintWriter.println("<s> " + sentence + " </s>");
            documentIDSPrintWriter.println(sentence.getDocumentID());
        });

        sentencesPrintWriter.close();
        documentIDSPrintWriter.close();
    }

    private void saveDictionaryToDirectory(Dictionary dictionary, File directory)
        throws IOException {

        FileOutputStream dictionaryOutputStream = new FileOutputStream(
            new File(directory, "dictionary.dict")
        );
        dictionary.exportToStream(dictionaryOutputStream);
        dictionaryOutputStream.close();

        PrintWriter unknownWordPrintWriter = new PrintWriter(
            new File(directory, "unknown_words.txt")
        );
        dictionary.getUnknownWords().forEach(unknownWordPrintWriter :: println);
        unknownWordPrintWriter.close();
    }

    // public static Corpus loadFromDirectory (File directory) throws FileNotFoundException {
    //     ArrayList<WordSequence> sentences = new ArrayList<>();
    //
    //     Pattern sentencePattern = Pattern.compile("<s> (.*) </s>");
    //
    //     Scanner sentencesScanner = new Scanner(new File(directory, "sentences.txt"));
    //     Scanner documentIDSScanner = new Scanner(new File(directory, "document_ids.txt"));
    //
    //     while(sentencesScanner.hasNextLine()){
    //         Matcher matcher = sentencePattern.matcher(sentencesScanner.nextLine());
    //
    //         if(matcher.find()){
    //             int documentID;
    //             if(documentIDSScanner.hasNextLine()){
    //                 documentID = Integer.parseInt(documentIDSScanner.nextLine());
    //             }
    //             else{
    //                 documentID = -1;
    //             }
    //
    //             sentences.add(new WordSequence(matcher.group(1), documentID));
    //         }
    //     }
    //     sentencesScanner.close();
    //     documentIDSScanner.close();
    //
    //     Corpus corpus = new Corpus(null);
    //     corpus.setSentences(sentences);
    //
    //     return corpus;
    // }

    public static DataBase getInstance () {
        return instance_;
    }

    private static final File corpusDirectory_ = new File("database/corpora/");

    private static DataBase instance_ = new DataBase();

}
