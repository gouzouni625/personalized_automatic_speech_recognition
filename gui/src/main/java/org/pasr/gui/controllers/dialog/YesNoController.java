package org.pasr.gui.controllers.dialog;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.pasr.gui.dialog.YesNoDialog;


/**
 * @class YesNoController
 * @brief Controller for YesNoDialog
 */
public class YesNoController extends Controller<Boolean> {

    /**
     * @brief Constructor
     *
     * @param dialog
     *     The Dialog of this Controller
     * @param promptText
     *     The text to be shown to the user
     */
    public YesNoController (YesNoDialog dialog, String promptText) {
        super(dialog);

        promptText_ = promptText;
    }

    @FXML
    public void initialize () {
        label.setText(promptText_);

        yesButton.setOnAction(this :: yesButtonOnAction);
        noButton.setOnAction(this :: noButtonOnAction);
    }

    private void yesButtonOnAction (ActionEvent actionEvent) {
        dialog_.setValue(true);
    }

    private void noButtonOnAction (ActionEvent actionEvent) {
        dialog_.setValue(false);
    }

    @FXML
    private Label label;

    @FXML
    private Button yesButton;

    @FXML
    private Button noButton;

    private String promptText_;

}
