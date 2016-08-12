package org.pasr.gui.lda;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import static org.pasr.utilities.Utilities.getResource;


public class InteractPane extends AnchorPane {
    public InteractPane(String title){
        title_ = title;

        try {
            URL location = getResource("/fxml/lda/interact_pane.fxml");

            if (location == null) {
                throw new IOException(
                    "getResource(\"/fxml/corpus/interact_pane.fxml\") returned null"
                );
            }

            FXMLLoader loader = new FXMLLoader(location);
            loader.setRoot(this);
            loader.setController(this);

            loader.load();
        } catch (IOException e) {
            logger_.severe("Could not load resource:/fxml/corpus/interact_pane.fxml\n" +
                "The file might be missing or be corrupted.\n" +
                "Application will terminate.\n" +
                "Exception Message: " + e.getMessage());
            Platform.exit();
        }
    }

    @FXML
    public void initialize(){
        label.setText(title_);
    }

    public void addChild(Interactable interactable){
        vBox.getChildren().add(interactable);
    }

    @FXML
    private Label label;
    private String title_;

    @FXML
    private VBox vBox;

    @FXML
    private TextField textField;

    private final Logger logger_ = Logger.getLogger(getClass().getName());

}
