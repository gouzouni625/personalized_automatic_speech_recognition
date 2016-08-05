package org.pasr.database.audio;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.pasr.database.Configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;


public class Index extends ArrayList<Index.Entry> {
    static{
        try {
            instance_ = new Gson().fromJson(new InputStreamReader(new FileInputStream(
                Configuration.getInstance().getAudioIndexPath()
            )), Index.class);
        } catch (FileNotFoundException e) {
            instance_ = new Index();
        }
    }

    private Index() {}

    public static class Entry{
        public Entry(String filename, String sentence, int corpusID){
            this.filename = filename;
            this.sentence = sentence;
            this.corpusID = corpusID;
        }

        public String getFilename () {
            return filename;
        }

        public String getSentence () {
            return sentence;
        }

        public int getCorpusID () {
            return corpusID;
        }

        private final String filename;
        private final String sentence;
        private final int corpusID;
    }

    public void save () throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
            Configuration.getInstance().getAudioIndexPath()
        )));

        new GsonBuilder().setPrettyPrinting().create().toJson(this, printWriter);

        printWriter.close();
    }

    public static Index getInstance(){
        return instance_;
    }

    private static Index instance_;

}
