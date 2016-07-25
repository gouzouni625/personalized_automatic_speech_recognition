package org.pasr.gui.controllers;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

import java.io.File;
import java.io.IOException;


public class MainSceneController {
    public MainSceneController(){}

    @FXML
    public void initialize() throws IOException {
        // At the moment initialize is called,
        // the views are not yet ready to handle
        // focus so runLater is used.
        Platform.runLater(() -> emailAddress.requestFocus());

        File corpusDirectory = new File("corpora");
        File acousticModelDirectory = new File("acoustic_models");
        if(!corpusDirectory.exists()){
            if(!corpusDirectory.mkdir()){
                throw new IOException("Failed to create corpus directory");
            }
        }

        if(!acousticModelDirectory.exists()){
            if(!acousticModelDirectory.mkdir()){
                throw new IOException("Failed to create acoustic model directory");
            }
        }

        if(corpusDirectory.list().length == 0 || acousticModelDirectory.list().length == 0){
            dictateButton.setDisable(true);
            dictateButtonLabel.setVisible(true);
            dictateButtonLabel.setText(DictateButtonMessages.DISABLED.getMessage());
        }
        else{
            dictateButton.setTooltip(
                new Tooltip(DictateButtonMessages.ENABLED.getMessage())
            );
        }
    }

    @FXML
    private ListView<String> corpusList;

    @FXML
    private ListView<String> acousticModelList;

    @FXML
    private TextField emailAddress;

    @FXML
    private PasswordField password;

    @FXML
    private Button newCorpusButton;

    @FXML
    private Button dictateButton;

    @FXML
    private Label dictateButtonLabel;

    private enum DictateButtonMessages{
        DISABLED("Note: You should create at least one corpus\n " +
            "and one acoustic model in order to dictate"),
        ENABLED("Use the chosen corpus and acoustic model during speech recognition");

        DictateButtonMessages(String message){
            message_ = message;
        }

        public String getMessage(){
            return message_;
        }

        private String message_;
    }

}
