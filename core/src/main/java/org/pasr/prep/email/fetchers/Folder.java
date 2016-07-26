package org.pasr.prep.email.fetchers;


public class Folder {
    public Folder (String path, Email[] emails){
        path_ = path;
        emails_ = emails;

        String[] tokens = path.split("/");
        name_ = tokens[tokens.length - 1];
    }

    public String getPath(){
        return path_;
    }

    public String getName(){
        return name_;
    }

    public Email[] getEmails(){
        return emails_;
    }

    private String path_;
    private String name_;
    private Email[] emails_;

}
