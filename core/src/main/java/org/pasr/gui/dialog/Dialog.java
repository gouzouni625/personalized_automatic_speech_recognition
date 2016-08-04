package org.pasr.gui.dialog;


import javafx.stage.Modality;
import javafx.stage.Stage;


public abstract class Dialog<T> {
    Dialog (T defaultValue) {
        value_ = defaultValue;

        stage_ = new Stage();
    }

    public void showAndWait(){
        // Make sure that events aren't delivered to other stages.
        stage_.initModality(Modality.APPLICATION_MODAL);

        stage_.showAndWait();
    }

    public void setValue(T value){
        value_ = value;

        stage_.close();
    }

    public T getValue(){
        return value_;
    }

    Stage stage_;

    private T value_;

}
