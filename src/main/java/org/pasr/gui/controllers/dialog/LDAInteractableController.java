package org.pasr.gui.controllers.dialog;


import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.apache.commons.lang3.ObjectUtils;
import org.pasr.gui.dialog.LDAInteractableDialog;


public class LDAInteractableController extends Controller<ObjectUtils.Null>{
    public LDAInteractableController(LDAInteractableDialog dialog, String title, String content){
        super(dialog);

        title_ = title;
        content_ = content;
    }

    @FXML
    public void initialize(){
        titleTextArea.setText(title_);
        contentTextArea.setText(content_);
    }

    @FXML
    private TextArea titleTextArea;
    private final String title_;

    @FXML
    private TextArea contentTextArea;
    private final String content_;

}
