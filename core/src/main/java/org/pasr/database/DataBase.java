package org.pasr.database;


import org.pasr.asr.dictionary.Dictionary;
import org.pasr.database.corpus.Index;
import org.pasr.database.processes.AcousticModelProcess;
import org.pasr.database.processes.LanguageModelProcess;
import org.pasr.prep.corpus.Corpus;
import org.pasr.prep.corpus.WordSequence;
import org.pasr.prep.recorder.Recorder;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class DataBase {
    private DataBase () throws IOException {
        configuration_ = Configuration.create();
        corpusIndex_ = org.pasr.database.corpus.Index.getInstance();
        arcticIndex_ = org.pasr.database.arctic.Index.getInstance();
        audioIndex_ = org.pasr.database.audio.Index.getInstance();
    }

    public static void create() throws IOException {
        if(instance_ == null) {
            instance_ = new DataBase();
        }
    }

    public List<org.pasr.database.corpus.Index.Entry> getCorpusEntryList (){
        return corpusIndex_;
    }

    public int getNumberOfCorpora(){
        return corpusIndex_.size();
    }

    public List<org.pasr.database.audio.Index.Entry> getAudioEntryList() {
        return audioIndex_;
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

            corpus.setID(corpusID);
        } catch (FileNotFoundException e) {
            // TODO
            e.printStackTrace();
        }

        return corpus;
    }

    private Corpus loadCorpusFromDirectory (File directory) throws FileNotFoundException {
        Map<Long, String> documentTitleMap = new HashMap<>();

        Scanner documentTitleScanner = new Scanner(new File(directory, "document_titles.txt"));
        while(documentTitleScanner.hasNextLine()){
            Matcher matcher = Pattern.compile("([0-9]+) (.+)")
                .matcher(documentTitleScanner.nextLine());

            if(matcher.matches()){
                documentTitleMap.put(Long.parseLong(matcher.group(1)), matcher.group(2));
            }
        }
        documentTitleScanner.close();

        ArrayList<WordSequence> sentences = new ArrayList<>();

        Pattern sentencePattern = Pattern.compile("<s> (.*) </s>");

        Scanner sentencesScanner = new Scanner(new File(directory, "sentences.txt"));
        Scanner documentIDSScanner = new Scanner(new File(directory, "document_ids.txt"));

        while(sentencesScanner.hasNextLine()){
            Matcher matcher = sentencePattern.matcher(sentencesScanner.nextLine());

            if(matcher.find()){
                long documentID;
                if(documentIDSScanner.hasNextLine()){
                    documentID = Long.parseLong(documentIDSScanner.nextLine());
                }
                else{
                    documentID = -1;
                }

                if(documentTitleMap.containsKey(documentID)) {
                    sentences.add(new WordSequence(
                        matcher.group(1), documentID, documentTitleMap.get(documentID)
                    ));
                }
                else {
                    sentences.add(new WordSequence(matcher.group(1), documentID, ""));
                }
            }
        }
        sentencesScanner.close();
        documentIDSScanner.close();

        Corpus corpus = new Corpus(null);
        corpus.setWordSequences(sentences);

        return corpus;
    }

    public String getDictionaryPathByID(int id){
        return configuration_.getCorpusDirectoryPath() + String.valueOf(id) + "/dictionary.dict";
    }

    public String getLanguageModelPathByID(int id){
        return configuration_.getCorpusDirectoryPath() + String.valueOf(id) + "/language_model.lm";
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

    public int newCorpusEntry (Corpus corpus, Dictionary dictionary){
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

        return newCorpusID;
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

        PrintWriter documentTitlesPrintWriter = new PrintWriter(
            new File(directory, "document_titles.txt")
        );

        corpus.stream()
            .collect(Collectors.toMap(
                WordSequence :: getDocumentID, WordSequence :: getDocumentTitle,
                (title1, title2) -> {
                    if(!title1.equals(title2)) {
                        logger_.warning(
                            "Found 2 documents with the same id but different title.\n" +
                                "This should not happen since a document is an e-mail," +
                                " the title is the subject of the e-mail and the id is the" +
                                " unix time stamp of the received date.");
                    }
                    return title1;
                }
            ))
            .entrySet()
            .forEach(
                entry -> documentTitlesPrintWriter.println(entry.getKey() + " " + entry.getValue())
            );

        documentTitlesPrintWriter.close();
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

    public void newAudioEntry(byte[] audioData, String sentence, int corpusID){
        int newEntryID = audioIndex_.size() + 1;

        File newEntryFile = new File(configuration_.getAudioDirectoryPath(), newEntryID + ".wav");

        long numberOfFrames = (long)(audioData.length / Recorder.AUDIO_FORMAT.getFrameSize());

        AudioInputStream audioInputStream = new AudioInputStream(
            new ByteArrayInputStream(audioData, 0, audioData.length),
            Recorder.AUDIO_FORMAT,
            numberOfFrames
        );

        try {
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, newEntryFile);
            audioInputStream.close();
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }

        audioIndex_.add(new org.pasr.database.audio.Index.Entry(
            newEntryFile.getName(), sentence, corpusID
        ));
    }

    public void newAcousticModel(){
        try {
            new AcousticModelProcess().startAndWaitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
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
            logger_.log(Level.SEVERE, "Could not save to " + configuration_.getCorpusIndexPath() +
                "\nThe following information need to be copied in the above file:\n\n" +
                corpusIndex_.toJson() + "\n\n" +
                "You should check your permissions on this file.\n" +
                e.getMessage());
        }

        try {
            arcticIndex_.save();
        } catch (FileNotFoundException e) {
            logger_.log(Level.SEVERE, "Could not save to " + configuration_.getArcticIndexPath() +
                "\nThe following information need to be copied in the above file:\n\n" +
                arcticIndex_.toJson() + "\n\n" +
                "You should check your permissions on this file.\n" +
                e.getMessage());
        }

        try {
            audioIndex_.save();
        } catch (FileNotFoundException e) {
            logger_.log(Level.SEVERE, "Could not save to " + configuration_.getAudioIndexPath() +
                "\nThe following information need to be copied in the above file:\n\n" +
                audioIndex_.toJson() + "\n\n" +
                "You should check your permissions on this file.\n" +
                e.getMessage());
        }
    }

    public static DataBase getInstance () {
        return instance_;
    }

    private final Configuration configuration_;
    private final org.pasr.database.corpus.Index corpusIndex_;
    private final org.pasr.database.arctic.Index arcticIndex_;
    private final org.pasr.database.audio.Index audioIndex_;

    private static DataBase instance_;

    private final Logger logger_ = Logger.getLogger(DataBase.class.getName());

}
