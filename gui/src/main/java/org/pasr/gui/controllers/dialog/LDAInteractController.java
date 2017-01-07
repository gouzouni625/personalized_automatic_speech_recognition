package org.pasr.gui.controllers.dialog;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.pasr.database.DataBase;
import org.pasr.gui.console.Console;
import org.pasr.gui.dialog.LDAInteractDialog;
import org.pasr.gui.lda.InteractPane;
import org.pasr.gui.lda.Interactable;
import org.pasr.model.text.Document;
import org.pasr.external.lda.LDA;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @class LDAInteractController
 * @brief Controller for LDAInteractDialog
 */
public class LDAInteractController extends Controller<MultiValuedMap<String, List<Long>>> {

    /**
     * @brief Constructor
     *
     * @param dialog
     *     The Dialog of this Controller
     * @param lda
     *     The LDA algorithm implementation
     */
    public LDAInteractController (LDAInteractDialog dialog, LDA lda) {
        super(dialog);

        lda_ = lda;
    }

    @FXML
    public void initialize () {
        List<List<String>> topWords = lda_.getTopWords(10);

        List<InteractPane> interactPaneList = new ArrayList<>();
        for (int i = 0, n = topWords.size(); i < n; i++) {
            interactPaneList.add(new InteractPane(
                "topic " + i + ": " + String.join(" ", topWords.get(i)),
                "corpus " + String.valueOf(DataBase.getInstance().getCorpusEntryList().nextId() + i)
            ));
        }

        List<Document> documentList = lda_.getDocuments();

        int[] documentTopicArray;
        try {
            documentTopicArray = lda_.getDocumentTopic();
        } catch (IOException e) {
            Console.getInstance().postMessage("There appears to be a problem with LDA.\n" +
                "Please, refrain from using it.");

            dialog_.hide();
            return;
        }

        for (int i = 0, n = documentTopicArray.length; i < n; i++) {
            interactPaneList.get(documentTopicArray[i])
                .addChild(new Interactable(documentList.get(i)));
        }

        vBox.getChildren().addAll(interactPaneList);

        button.setOnAction(this :: buttonOnAction);
    }

    private void buttonOnAction (ActionEvent actionEvent) {
        MultiValuedMap<String, List<Long>> map = new ArrayListValuedHashMap<>();

        for (Node interactPaneNode : vBox.getChildren()) {
            InteractPane interactPane = (InteractPane) interactPaneNode;

            String corpusName = interactPane.getName();

            ArrayList<Long> documentIds = new ArrayList<>();
            for (Node interactableNode : interactPane.getDocumentNodeList()) {
                Interactable interactable = (Interactable) interactableNode;

                documentIds.add(interactable.getDocument().getId());
            }

            if (! documentIds.isEmpty()) {
                map.put(corpusName, documentIds);
            }
        }

        dialog_.setValue(map);
    }

    public void terminate () {
        button.fire();
    }

    @FXML
    private VBox vBox;

    @FXML
    private Button button;

    private final LDA lda_;

}
