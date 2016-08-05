package org.pasr.database.corpus;


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
                Configuration.getInstance().getCorpusIndexPath()
            )), Index.class);
        } catch (FileNotFoundException e) {
            instance_ = new Index();
        }
    }

    private Index(){}

    public static class Entry{
        public Entry(int id, String name){
            this.id = id;
            this.name = name;
        }

        public int getId(){
            return id;
        }

        public String getName(){
            return name;
        }

        private final int id;
        private final String name;
    }

    public boolean containsID(int id){
        for(Entry entry : this){
            if(entry.getId() == id){
                return true;
            }
        }

        return false;
    }

    public void save () throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
            Configuration.getInstance().getCorpusIndexPath()
        )));

        new GsonBuilder().setPrettyPrinting().create().toJson(this, printWriter);

        printWriter.close();
    }

    public static Index getInstance(){
        return instance_;
    }

    private static Index instance_;

}
