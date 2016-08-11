package org.pasr.database.corpus;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.pasr.database.Configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class Index extends ArrayList<Index.Entry>{
    private static final Logger logger_ = Logger.getLogger(Index.class.getName());

    static{
        Index instance;
        try {
            instance = new Gson().fromJson(new InputStreamReader(new FileInputStream(
                Configuration.getInstance().getCorpusIndexPath()
            )), Index.class);
        } catch (FileNotFoundException | JsonIOException | JsonSyntaxException e) {
            logger_.warning("Could not load Index.");
            instance = new Index();
        }

        instance_ = instance == null ? new Index() : instance;
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

        @Override
        public String toString(){
            return id + ". " + name;
        }

        private final int id;
        private final String name;
    }

    public int nextId(){
        Optional<Integer> maxId = stream()
            .map(Entry :: getId)
            .reduce(BinaryOperator.maxBy(Integer :: compare));

        if(maxId.isPresent()){
            return maxId.get() + 1;
        }
        else{
            return 1;
        }
    }

    public boolean containsId (int id){
        for(Entry entry : this){
            if(entry.getId() == id){
                return true;
            }
        }

        return false;
    }

    public String toJson(){
        return serializer_.toJson(this);
    }

    public void removeById(int id){
        removeAll(stream()
            .filter(entry -> entry.getId() == id)
            .collect(Collectors.toList()));
    }

    public void save () throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
            Configuration.getInstance().getCorpusIndexPath()
        )));

        serializer_.toJson(this, printWriter);

        printWriter.close();
    }

    public static Index getInstance(){
        return instance_;
    }

    private static Index instance_;

    private static Gson serializer_ = new GsonBuilder().setPrettyPrinting().create();

}
