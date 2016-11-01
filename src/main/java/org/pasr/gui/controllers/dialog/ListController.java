package org.pasr.gui.controllers.dialog;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.pasr.gui.dialog.ListDialog;

import java.util.List;


/**
 * @class ListController
 * @brief Controller for ListDialog
 *
 * @param <T>
 *     The value type of the items of the List
 */
public class ListController<T> extends Controller<T> {

    /**
     * @brief Constructor
     *
     * @param dialog
     *     The Dialog of this Controller
     * @param promptText
     *     The text to be shown to the user
     * @param list
     *     The List of this Controller
     */
    public ListController (ListDialog<T> dialog, String promptText, List<T> list) {
        super(dialog);

        promptText_ = promptText;
        list_ = list;
    }

    @FXML
    public void initialize () {
        label.setText(promptText_);

        listView.getItems().addAll(list_);

        button.setOnAction(this :: buttonOnAction);
    }

    @FXML
    private void buttonOnAction (ActionEvent actionEvent) {
        if (listView.getSelectionModel().getSelectedIndex() != - 1) {
            dialog_.setValue(listView.getSelectionModel().getSelectedItem());
        }
    }

    @FXML
    private Label label;

    @FXML
    private ListView<T> listView;

    @FXML
    private Button button;

    private String promptText_;
    private List<T> list_;

}
