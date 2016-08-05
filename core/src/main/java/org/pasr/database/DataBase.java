package org.pasr.database;


import com.google.gson.Gson;
import org.pasr.asr.dictionary.Dictionary;
import org.pasr.database.corpus.Index;
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
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class DataBase {
    static{
        configuration_ = Configuration.getInstance();
        corpusIndex_ = org.pasr.database.corpus.Index.getInstance();
        arcticIndex_ = org.pasr.database.arctic.Index.getInstance();
    }

    private DataBase () {}

    public void newCorpusEntry (Corpus corpus, Dictionary dictionary){
        File corpusDirectory = new File(configuration_.getCorpusDirectoryPath());

        int newCorpusID = corpusIndex_.size() + 1;
        corpus.setID(newCorpusID);

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

        corpusIndex_.add(new Index.Entry(newCorpusID, corpus.getName()));
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
        return corpusIndex_.size();
    }

    public Corpus getCorpusByID(int corpusID){
        if(! corpusIndex_.containsID(corpusID)){
            return null;
        }

        Corpus corpus = null;

        try {
            corpus = loadCorpusFromDirectory(
                new File(configuration_.getCorpusDirectoryPath(), String.valueOf(corpusID))
            );
        } catch (FileNotFoundException e) {
            // TODO
            e.printStackTrace();
        }

        return corpus;
    }

    private Corpus loadCorpusFromDirectory (File directory) throws FileNotFoundException {
        ArrayList<WordSequence> sentences = new ArrayList<>();

        Pattern sentencePattern = Pattern.compile("<s> (.*) </s>");

        Scanner sentencesScanner = new Scanner(new File(directory, "sentences.txt"));
        Scanner documentIDSScanner = new Scanner(new File(directory, "document_ids.txt"));

        while(sentencesScanner.hasNextLine()){
            Matcher matcher = sentencePattern.matcher(sentencesScanner.nextLine());

            if(matcher.find()){
                int documentID;
                if(documentIDSScanner.hasNextLine()){
                    documentID = Integer.parseInt(documentIDSScanner.nextLine());
                }
                else{
                    documentID = -1;
                }

                sentences.add(new WordSequence(matcher.group(1), documentID));
            }
        }
        sentencesScanner.close();
        documentIDSScanner.close();

        Corpus corpus = new Corpus(null);
        corpus.setSentences(sentences);

        return corpus;
    }

    public List<String> getUnUsedArcticSentences(int count){
        List<String> sentences = arcticIndex_.stream()
            .filter(entry -> !entry.isUsed())
            .map(org.pasr.database.arctic.Index.Entry :: getSentence)
            .collect(Collectors.toList());

        Collections.shuffle(sentences);

        if(count < 0 || count > sentences.size()){
            return sentences;
        }
        else{
            return sentences.subList(0, count);
        }
    }

    public void setArcticSentenceAsUsed(String sentence){
        arcticIndex_.stream()
            .filter(entry -> entry.getSentence().equals(sentence))
            .forEach(entry -> entry.setUsed(true));
    }

    public void close(){
        try {
            corpusIndex_.save();
        } catch (FileNotFoundException e) {
            // TODO could not save database
            e.printStackTrace();
        }

        try {
            arcticIndex_.save();
        } catch (FileNotFoundException e) {
            // TODO could not save database
            e.printStackTrace();
        }
    }

    public static DataBase getInstance () {
        return instance_;
    }

    private static final Configuration configuration_;
    private static final org.pasr.database.corpus.Index corpusIndex_;
    private static final org.pasr.database.arctic.Index arcticIndex_;

    private static DataBase instance_ = new DataBase();

}
