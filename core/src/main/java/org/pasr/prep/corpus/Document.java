package org.pasr.prep.corpus;


public class Document {
    public Document(int id, String content){
        id_ = id;
        content_ = content;
    }

    int getID(){
        return id_;
    }

    String getContent(){
        return content_;
    }

    private final int id_;
    private final String content_;

}
