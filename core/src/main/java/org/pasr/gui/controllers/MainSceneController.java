package org.pasr.gui.controllers;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;


public class MainSceneController {
    public MainSceneController(){}

    @FXML
    public void initialize(){
        // At the moment initialize is called,
        // the views are not yet ready to handle
        // focus so runLater is used.
        Platform.runLater(() -> emailAddress.requestFocus());
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

}
