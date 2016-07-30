package org.pasr.prep.corpus;


public class Document {
    public Document(int id, String content){
        id_ = id;
        content_ = content;
    }

    public int getID(){
        return id_;
    }

    public String getContent(){
        return content_;
    }

    private final int id_;
    private final String content_;

}
