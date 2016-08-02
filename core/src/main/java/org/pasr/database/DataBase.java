package org.pasr.database;


import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import org.pasr.asr.dictionary.Dictionary;
import org.pasr.database.processes.LanguageModelProcess;
import org.pasr.prep.corpus.Corpus;
import org.pasr.prep.corpus.WordSequence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DataBase {
    static{
        String databaseDirectoryPath = Configuration.getInstance().getDatabaseDirectoryPath();

        CORPUS_DIRECTORY_PATH = databaseDirectoryPath + "corpora/";
        INDEX_PATH = databaseDirectoryPath + "corpora/index.json";
    }

    private DataBase () {
        Gson gson = new Gson();

        try {
            index_ = gson.fromJson(new InputStreamReader(
                new FileInputStream(INDEX_PATH)), Index.class
            );
        } catch (FileNotFoundException e) {
            index_ = new Index();
        }
    }

    public void newCorpusEntry (Corpus corpus, Dictionary dictionary){
        File corpusDirectory = new File(CORPUS_DIRECTORY_PATH);

        int newCorpusID = index_.getNumberOfEntries() + 1;

        File newCorpusDirectory = new File(corpusDirectory, String.valueOf(newCorpusID));
        if(!newCorpusDirectory.mkdir()){
            throw new RuntimeException("Could not create the directory to save the new corpus");
        }

        try {
            saveCorpusToDirectory(corpus, newCorpusDirectory);
        } catch (FileNotFoundException e) {
            // TODO
            e.printStackTrace();
        }

        try {
            saveDictionaryToDirectory(dictionary, newCorpusDirectory);
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }

        // Create language model for this corpus
        try {
            new LanguageModelProcess(
                Paths.get(newCorpusDirectory.getPath(), "sentences.txt"),
                Paths.get(newCorpusDirectory.getPath(), "language_model.lm"),
                3
            ).startAndWaitFor();
        } catch (IOException | InterruptedException e) {
            // TODO
            e.printStackTrace();
        }

        index_.addEntry(new Index.Entry(newCorpusID, corpus.getName()));
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

    public int getNumberOfCorpora(){
        return index_.getNumberOfEntries();
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

    public void close(){
        PrintWriter printWriter;
        try {
            printWriter = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(INDEX_PATH))
            );
        } catch (FileNotFoundException e) {
            // TODO Could not save database. Act appropriately
            e.printStackTrace();
            return;
        }

        new Gson().toJson(index_, printWriter);

        printWriter.close();
    }

    public static DataBase getInstance () {
        return instance_;
    }

    private static final String CORPUS_DIRECTORY_PATH;
    private static final String INDEX_PATH;

    private static DataBase instance_ = new DataBase();

    private Index index_;

}
