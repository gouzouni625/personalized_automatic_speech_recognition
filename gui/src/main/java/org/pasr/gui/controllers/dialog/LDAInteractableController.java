package org.pasr.gui.controllers.dialog;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.apache.commons.lang3.ObjectUtils;
import org.pasr.gui.dialog.LDAInteractableDialog;


/**
 * @class LDAInteractableController
 * @brief Controller for LDAInteractableDialog
 */
public class LDAInteractableController extends Controller<ObjectUtils.Null> {

    /**
     * @brief Constructor
     *
     * @param dialog
     *     The Dialog of this Controller
     * @param title
     *     The title of the Dialog
     * @param content
     *     The content of the Dialog
     */
    public LDAInteractableController (LDAInteractableDialog dialog, String title, String content) {
        super(dialog);

        title_ = title;
        content_ = content;
    }

    @FXML
    public void initialize () {
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
