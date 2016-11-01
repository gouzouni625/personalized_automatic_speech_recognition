package org.pasr.gui.dialog;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import org.pasr.gui.controllers.dialog.ListController;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.pasr.utilities.Utilities.getResource;


/**
 * @class ListDialog
 * @brief Implements a Dialog with a List of selectable items
 *
 * @param <T>
 *     The value type of the items of the List
 */
public class ListDialog<T> extends Dialog<T> {

    /**
     * @brief Constructor
     *
     * @param defaultValue
     *     The default value of this Dialog
     * @param promptText
     *     The text to be shown to the user
     * @param list
     *     The List of this Dialog
     *
     * @throws IOException If the fxml file of this Dialog cannot be found
     */
    public ListDialog (T defaultValue, String promptText, List<T> list) throws IOException {
        super(defaultValue);

        URL location = getResource("/fxml/dialog/list.fxml");

        if (location == null) {
            throw new IOException("getResource(\"/fxml/dialog/list.fxml\") returned null");
        }

        FXMLLoader loader = new FXMLLoader(location);
        ListController<T> controller = new ListController<>(this, promptText, list);
        loader.setController(controller);

        initModality(Modality.APPLICATION_MODAL);

        setScene(new Scene(loader.load()));
    }

}
