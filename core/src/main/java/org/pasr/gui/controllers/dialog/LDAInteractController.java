package org.pasr.gui.controllers.dialog;


import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import org.apache.commons.collections4.MultiValuedMap;
import org.pasr.gui.dialog.LDAInteractDialog;
import org.pasr.gui.lda.InteractPane;
import org.pasr.gui.lda.Interactable;
import org.pasr.prep.corpus.Document;
import org.pasr.prep.lda.LDA;

import java.util.ArrayList;
import java.util.List;


public class LDAInteractController extends Controller<MultiValuedMap<String, List<Long>>>{
    public LDAInteractController(LDAInteractDialog dialog, LDA lda){
        super(dialog);

        lda_ = lda;
    }

    @FXML
    public void initialize(){
        List<List<String>> topWords = lda_.getTopWords(10);

        List<InteractPane> interactPaneList = new ArrayList<>();
        for(int i = 0, n = topWords.size();i < n;i++){
            interactPaneList.add(new InteractPane(
                "topic " + i + ": " + String.join(" ", topWords.get(i))
            ));
        }

        List<Document> documentList = lda_.getDocuments();
        int[] documentTopicArray = lda_.getDocumentTopic();
        for(int i = 0, n = documentTopicArray.length;i < n;i++){
            interactPaneList.get(documentTopicArray[i])
                .addChild(new Interactable(documentList.get(i)));
        }

        vBox.getChildren().addAll(interactPaneList);
    }

    @FXML
    private VBox vBox;

    private final LDA lda_;

}
