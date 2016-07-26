package org.pasr.prep.email;


public class Folder {
    public Folder (String path, Email[] emails){
        path_ = path;
        emails_ = emails;
    }

    public String getPath(){
        return path_;
    }

    public Email[] getEmails(){
        return emails_;
    }

    private String path_;
    private Email[] emails_;

}
