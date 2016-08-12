package org.pasr.prep.corpus;


public class Document {
    public Document(long id, String title, String content){
        id_ = id;
        title_ = title;
        content_ = content;
    }

    public long getID(){
        return id_;
    }

    public String getTitle(){
        return title_;
    }

    public String getContent(){
        return content_;
    }

    @Override
    public String toString(){
        return getContent();
    }

    private final long id_;
    private final String title_;
    private final String content_;

}
