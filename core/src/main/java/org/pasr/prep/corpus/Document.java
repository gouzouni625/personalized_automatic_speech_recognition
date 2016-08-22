package org.pasr.prep.corpus;


import javafx.scene.input.DataFormat;

import java.io.Serializable;


public class Document implements Serializable {
    public Document(long id, String title, String content){
        id_ = id;
        title_ = title;
        content_ = content;
    }

    public long getId (){
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

    // Used for drag and drop
    public static final DataFormat DATA_FORMAT = new DataFormat("org.pasr.prep.corpus.Document");

}
