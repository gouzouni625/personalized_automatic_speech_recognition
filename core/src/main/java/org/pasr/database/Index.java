package org.pasr.database;


import java.util.ArrayList;
import java.util.List;


public class Index {
    Index(){
        entryList = new ArrayList<>();
    }

    void addEntry(Entry entry){
        entryList.add(entry);
    }

    static class Entry{
        Entry(int id, String name){
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

    int getNumberOfEntries(){
        return entryList.size();
    }

    private List<Entry> entryList;

}
