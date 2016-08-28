package org.pasr.gui.dialog;


import javafx.stage.Stage;


public abstract class Dialog<T> extends Stage {
    Dialog (T defaultValue) {
        value_ = defaultValue;
    }

    public void setValue(T value){
        value_ = value;

        close();
    }

    public T getValue(){
        return value_;
    }

    private T value_;

}
