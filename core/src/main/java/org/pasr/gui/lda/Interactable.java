package org.pasr.gui.lda;


import javafx.event.ActionEvent;
import javafx.scene.control.ToggleButton;
import org.pasr.gui.console.Console;
import org.pasr.gui.dialog.LDAInteractableDialog;
import org.pasr.prep.corpus.Document;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;


public class Interactable extends ToggleButton {
    public Interactable (Document document){
        String text = "subject: " + document.getTitle() + "\n" +
            "date: " + new Date(document.getID());

        setText(text);

        LDAInteractableDialog dialog;
        try {
            dialog = new LDAInteractableDialog(text, document.getContent());
        } catch (IOException e) {
            dialog = null;

            Console.getInstance().postMessage("Could not create dialog for interactable " + text +
                ".\n Clicking on it will not have any effect.");

            logger_.severe("Could not load resource:/fxml/dialog/lda_interactable.fxml.\n" +
                "The file might be missing or be corrupted.\n" +
                "Exception Message: " + e.getMessage());
        }
        dialog_ = dialog;

        if(dialog_ != null){
            dialog_.setOnHidden(event -> setSelected(false));
        }

        setOnAction(this :: onAction);
    }

    private void onAction(ActionEvent actionEvent){
        if(dialog_ != null) {
            if (isSelected()) {
                dialog_.show();
            }
            else {
                dialog_.hide();
            }
        }
    }

    private final LDAInteractableDialog dialog_;

    private final Logger logger_ = Logger.getLogger(getClass().getName());

}
