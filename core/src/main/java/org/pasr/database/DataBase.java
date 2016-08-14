package org.pasr.database;


import org.apache.commons.io.FileUtils;
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

    public static DataBase create() throws IOException {
        if(instance_ == null) {
            instance_ = new DataBase();
        }

        return instance_;
    }

    public Configuration getConfiguration(){
        return configuration_;
    }

    public org.pasr.database.corpus.Index getCorpusEntryList (){
        return corpusIndex_;
    }

    public int getNumberOfCorpora(){
        return corpusIndex_.size();
    }

    public List<org.pasr.database.audio.Index.Entry> getAudioEntryList() {
        return audioIndex_;
    }

    public Corpus getCorpusById (int corpusId) throws IOException {
        if(! corpusIndex_.containsId(corpusId)){
            throw new IllegalArgumentException("Id does not exist.");
        }

        Corpus corpus;
        try {
            corpus = loadCorpusFromDirectory(
                new File(configuration_.getCorpusDirectoryPath(), String.valueOf(corpusId))
            );

            corpus.setId(corpusId);

            return corpus;
        } catch (FileNotFoundException e) {
            corpusIndex_.removeById(corpusId);

            throw new FileNotFoundException("Error while loading corpus with id: " + corpusId +
                "\nCould not find file: " + e.getMessage());
        } catch (IOException e) {
            corpusIndex_.removeById(corpusId);

            throw new IOException("Error while loading corpus with id: " + corpusId + "\n" +
                "Exception Message: " + e.getMessage());
        }
    }

    private Corpus loadCorpusFromDirectory (File directory) throws IOException {
        Map<Long, String> documentTitleMap = new HashMap<>();

        try {
            Scanner documentTitleScanner = new Scanner(new File(directory, "document_titles.txt"));
            while(documentTitleScanner.hasNextLine()){
                Matcher matcher = Pattern.compile("([0-9]+) (.+)")
                    .matcher(documentTitleScanner.nextLine());

                if(matcher.matches()){
                    documentTitleMap.put(Long.parseLong(matcher.group(1)), matcher.group(2));
                }
            }
            documentTitleScanner.close();
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("document_titles.txt");
        }

        ArrayList<WordSequence> sentences = new ArrayList<>();

        Pattern sentencePattern = Pattern.compile("<s> (.*) </s>");

        Scanner sentencesScanner;
        try {
            sentencesScanner = new Scanner(new File(directory, "sentences.txt"));
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("sentences.txt");
        }
        Scanner documentIdsScanner;
        try {
            documentIdsScanner = new Scanner(new File(directory, "document_ids.txt"));
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("document_ids.txt");
        }

        while(sentencesScanner.hasNextLine()){
            Matcher matcher = sentencePattern.matcher(sentencesScanner.nextLine());

            if(matcher.matches()){
                long documentId;
                if(documentIdsScanner.hasNextLine()){
                    documentId = Long.parseLong(documentIdsScanner.nextLine());
                }
                else{
                    throw new IOException("Malformed file: document_ids.txt");
                }

                if(documentTitleMap.containsKey(documentId)) {
                    sentences.add(new WordSequence(
                        matcher.group(1), documentId, documentTitleMap.get(documentId)
                    ));
                }
                else {
                    throw new IOException("Malformed file: document_titles.txt");
                }
            }
        }

        sentencesScanner.close();
        documentIdsScanner.close();

        Corpus corpus = new Corpus(null);
        corpus.setWordSequences(sentences);

        return corpus;
    }

    public String getDictionaryPathById (int id) throws IOException {
        String path = configuration_.getCorpusDirectoryPath() +
            String.valueOf(id) + "/dictionary.dict";

        if(!(new File(path).isFile())){
            // TODO Maybe don't throw an exception but first, try creating a new dictionary
            throw new IOException("Dictionary doesn't exist.");
        }

        return path;
    }

    public String getLanguageModelPathById (int id) throws IOException {
        String path = configuration_.getCorpusDirectoryPath() +
            String.valueOf(id) + "/language_model.lm";

        if((!new File(path).isFile())){
            // TODO Maybe don't throw an exception but first, try creating a new language model
            throw new IOException("Language Model doesn't exist.");
        }

        return path;
    }

    public String getAcousticModelPath() throws IOException {
        String path = configuration_.getAcousticModelPath();

        if(!new File(path).isDirectory()){
            // TODO Maybe don't throw an exception but first, try creating a new acoustic model
            throw new IOException("Acoustic Model doesn't exist.");
        }

        return path;
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

    public int newCorpusEntry (Corpus corpus, Dictionary dictionary) throws IOException {
        File corpusDirectory = new File(configuration_.getCorpusDirectoryPath());

        int newCorpusId = corpusIndex_.nextId();
        corpus.setId(newCorpusId);

        File newCorpusDirectory = new File(corpusDirectory, String.valueOf(newCorpusId));
        if(newCorpusDirectory.exists()){
            if(newCorpusDirectory.isFile()){
                if(!newCorpusDirectory.delete()){
                    throw new IOException("Could not delete file: " + newCorpusDirectory.getPath());
                }
            }
            else if(newCorpusDirectory.isDirectory()){
                try {
                    FileUtils.deleteDirectory(newCorpusDirectory);
                } catch (IOException e) {
                    throw new IOException(
                        "Could not delete directory: " + newCorpusDirectory.getPath()
                    );
                }
            }
            else{
                throw new IOException("Unknown file type: " + newCorpusDirectory.getPath());
            }
        }

        if(!newCorpusDirectory.mkdir()){
            throw new IOException("Could not create directory: " + newCorpusDirectory.getPath());
        }

        saveCorpusToDirectory(corpus, newCorpusDirectory);

        saveDictionaryToDirectory(dictionary, newCorpusDirectory);

        // Create language model for this corpus
        try {
            new LanguageModelProcess(
                Paths.get(newCorpusDirectory.getPath(), "sentences.txt"),
                Paths.get(newCorpusDirectory.getPath(), "language_model.lm"),
                3
            ).startAndWaitFor();
        } catch (IOException | InterruptedException e) {
            throw new IOException("Could not create language model.\n" +
                "Exception Message: " + e.getMessage());
        }

        corpusIndex_.add(new Index.Entry(newCorpusId, corpus.getName()));

        return newCorpusId;
    }

    private void saveCorpusToDirectory(Corpus corpus, File directory) throws FileNotFoundException {
        PrintWriter sentencesPrintWriter;
        try {
            sentencesPrintWriter = new PrintWriter(new File(directory, "sentences.txt"));
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("sentences.txt");
        }

        PrintWriter documentIdSPrintWriter;
        try {
            documentIdSPrintWriter = new PrintWriter(
                new File(directory, "document_ids.txt")
            );
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("document_ids.txt");
        }

        corpus.forEach(sentence -> {
            sentencesPrintWriter.println("<s> " + sentence + " </s>");
            documentIdSPrintWriter.println(sentence.getDocumentId());
        });

        sentencesPrintWriter.close();
        documentIdSPrintWriter.close();

        PrintWriter documentTitlesPrintWriter;
        try {
            documentTitlesPrintWriter = new PrintWriter(
                new File(directory, "document_titles.txt")
            );
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("document_titles.txt");
        }

        corpus.stream()
            .collect(Collectors.toMap(
                WordSequence :: getDocumentId, WordSequence :: getDocumentTitle,
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

        try {
            FileOutputStream dictionaryOutputStream = new FileOutputStream(
                new File(directory, "dictionary.dict")
            );

            dictionary.exportToStream(dictionaryOutputStream);
            dictionaryOutputStream.close();
        } catch (IOException e) {
            throw new IOException("dictionary.dict");
        }

        try {
            PrintWriter unknownWordPrintWriter = new PrintWriter(
                new File(directory, "unknown_words.txt")
            );

            dictionary.getUnknownWords().forEach(unknownWordPrintWriter :: println);
            unknownWordPrintWriter.close();
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("unknown_words.txt");
        }
    }

    public void newAudioEntry(byte[] audioData, String sentence, int corpusId) throws IOException {
        int newEntryId = audioIndex_.size() + 1;

        File newEntryFile = new File(configuration_.getAudioDirectoryPath(), newEntryId + ".wav");

        long numberOfFrames = (long)(audioData.length / Recorder.AUDIO_FORMAT.getFrameSize());

        AudioInputStream audioInputStream = new AudioInputStream(
            new ByteArrayInputStream(audioData, 0, audioData.length),
            Recorder.AUDIO_FORMAT,
            numberOfFrames
        );

        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, newEntryFile);
        audioInputStream.close();

        audioIndex_.add(new org.pasr.database.audio.Index.Entry(
            newEntryFile.getName(), sentence, corpusId
        ));
    }

    public void newAcousticModel() throws IOException, InterruptedException {
        new AcousticModelProcess().startAndWaitFor();
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
