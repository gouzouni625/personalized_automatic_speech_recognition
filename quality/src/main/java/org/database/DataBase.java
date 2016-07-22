package org.database;


import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Spliterator;
import java.util.function.Consumer;


public class DataBase implements Iterable<DataBase.Entry>{
    private DataBase(List<Entry> entries, Configuration configuration){
        entries_ = entries;
        configuration_ = configuration;
    }

    public static DataBase create(Configuration configuration) throws FileNotFoundException {
        ArrayList<Entry> entries = new ArrayList<>();

        String directory = configuration.getDirectory();
        if(!directory.endsWith("/")){
            directory += "/";
        }

        Scanner fileIdsScanner = new Scanner(new File(directory + configuration.getFilesIds()));
        Scanner transcriptionsScanner = new Scanner(new File(
            directory + configuration.getTranscriptions()
        ));

        while(fileIdsScanner.hasNextLine()){
            String soundFilePath = directory + fileIdsScanner.nextLine();
            if(!soundFilePath.endsWith(".wav")){
                soundFilePath += ".wav";
            }
            File soundFile = new File(soundFilePath);

            String transcription = "";
            if(transcriptionsScanner.hasNextLine()){
                transcription = transcriptionsScanner.nextLine();
            }

            entries.add(new Entry(soundFile, transcription));
        }

        fileIdsScanner.close();
        transcriptionsScanner.close();

        return new DataBase(
            entries,
            new Configuration(
                directory, configuration.getFilesIds(), configuration.getTranscriptions()
            )
        );
    }

    public static class Entry{
        Entry(File soundFile, String transcription){
            soundFile_ = soundFile;
            transcription_ = transcription;

            id_ = FilenameUtils.getBaseName(soundFile_.getName());
        }

        public String getId(){
            return id_;
        }

        public File getSoundFile(){
            return soundFile_;
        }

        public String getTranscription(){
            return transcription_;
        }

        private final String transcription_;
        private final File soundFile_;
        private final String id_;
    }

    @Override
    public Iterator<Entry> iterator () {
        return entries_.iterator();
    }

    @Override
    public void forEach (Consumer<? super Entry> action) {
        entries_.forEach(action);
    }

    @Override
    public Spliterator<Entry> spliterator () {
        return entries_.spliterator();
    }

    public List<Entry> entries(){
        return entries_;
    }

    public Configuration getConfiguration(){
        return configuration_;
    }

    private List<Entry> entries_;
    private Configuration configuration_;

}
