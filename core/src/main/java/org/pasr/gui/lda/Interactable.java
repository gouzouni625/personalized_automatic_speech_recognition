package org.pasr.gui.lda;


import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import org.pasr.gui.console.Console;
import org.pasr.gui.dialog.LDAInteractableDialog;
import org.pasr.prep.corpus.Document;

import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;


public class Interactable extends ToggleButton {
    public Interactable (Document document){
        document_ = document;

        String text = "subject: " + document.getTitle() + "\n" +
            "date: " + new Date(document.getID());

        setText(text);
        setPrefHeight(50);

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

        setOnDragDetected(this :: onDragDetected);
        setOnDragDone(this :: onDragDone);
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

    private void onDragDetected(MouseEvent mouseEvent){
        logger_.fine("onDragDetected called");

        if(isSelected()){
            fire();
        }

        Dragboard dragboard;

        if(mouseEvent.getButton() == MouseButton.PRIMARY) {
            dragboard = startDragAndDrop(TransferMode.MOVE);
        } else if(mouseEvent.getButton() == MouseButton.SECONDARY){
            dragboard = startDragAndDrop(TransferMode.COPY);
        }
        else{
            return;
        }

        // Could not serialize Interactable itself, so will serialize the document
        Map<DataFormat, Object> content = new Hashtable<>();
        content.put(Document.DATA_FORMAT, document_);
        dragboard.setContent(content);

        mouseEvent.consume();
    }

    private void onDragDone(DragEvent dragEvent){
        logger_.fine("onDragDone called");

        if(dragEvent.getTransferMode() == TransferMode.MOVE){
            Parent parent = getParent();

            if (parent != null && parent instanceof Pane) {
                ((Pane) parent).getChildren().remove(this);
            }
        }
        dragEvent.consume();
    }

    private final LDAInteractableDialog dialog_;

    private final Document document_;

    private final Logger logger_ = Logger.getLogger(getClass().getName());

}
