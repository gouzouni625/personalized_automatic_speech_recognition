package org.pasr.prep.email.fetchers;


import java.util.ArrayList;


public class Folder extends ArrayList<Email> {
    public Folder (String path){
        path_ = path;

        String[] tokens = path.split("/");
        name_ = tokens[tokens.length - 1];
    }

    public String getPath(){
        return path_;
    }

    public String getName(){
        return name_;
    }

    private String path_;
    private String name_;

}
