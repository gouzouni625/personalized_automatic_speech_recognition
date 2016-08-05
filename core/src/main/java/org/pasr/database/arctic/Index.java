package org.pasr.database.arctic;


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


public class Index extends ArrayList<Index.Entry>{
    static{
        try {
            instance_ = new Gson().fromJson(new InputStreamReader(new FileInputStream(
                Configuration.getInstance().getArcticIndexPath()
            )), Index.class);
        } catch (FileNotFoundException e) {
            instance_ = new Index();
        }
    }

    private Index(){}

    public static class Entry{
        public Entry(String sentence, boolean used){
            this.sentence = sentence;
            this.used = used;
        }

        public String getSentence(){
            return sentence;
        }

        public boolean isUsed(){
            return used;
        }

        public void setUsed(boolean used){
            this.used = used;
        }

        private final String sentence;
        private boolean used;
    }

    public void save () throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
            Configuration.getInstance().getArcticIndexPath()
        )));

        new GsonBuilder().setPrettyPrinting().create().toJson(this, printWriter);

        printWriter.close();
    }

    public static Index getInstance(){
        return instance_;
    }

    private static Index instance_;

}
