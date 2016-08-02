package org.pasr.gui.dialog;


import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import org.pasr.gui.controllers.dialog.YesNoDialogController;

import java.io.IOException;

import static org.pasr.utilities.Utilities.getResource;


public class YesNoDialog extends Dialog<Boolean> {
    public YesNoDialog(boolean defaultValue, String promptText) throws IOException {
        super(defaultValue);

        FXMLLoader loader = new FXMLLoader(getResource("/fxml/dialog/yes_no.fxml"));
        YesNoDialogController controller = new YesNoDialogController(this, promptText);
        loader.setController(controller);

        stage_.setScene(new Scene(loader.load()));
    }

}
