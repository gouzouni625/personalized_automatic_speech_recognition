package org.pasr.gui.dialog;


import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import org.pasr.gui.controllers.dialog.YesNoController;

import java.io.IOException;
import java.net.URL;

import static org.pasr.utilities.Utilities.getResource;


public class YesNoDialog extends Dialog<Boolean> {
    public YesNoDialog(boolean defaultValue, String promptText) throws IOException {
        super(defaultValue);

        URL location = getResource("/fxml/dialog/yes_no.fxml");

        if(location == null){
            throw new IOException("getResource(\"/fxml/dialog/yes_no.fxml\") returned null");
        }

        FXMLLoader loader = new FXMLLoader(location);
        YesNoController controller = new YesNoController(this, promptText);
        loader.setController(controller);

        initModality(Modality.APPLICATION_MODAL);

        setScene(new Scene(loader.load()));
    }

}
