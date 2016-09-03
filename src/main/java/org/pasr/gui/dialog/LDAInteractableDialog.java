package org.pasr.gui.dialog;


import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import org.apache.commons.lang3.ObjectUtils;
import org.pasr.gui.controllers.dialog.LDAInteractableController;

import java.io.IOException;
import java.net.URL;

import static org.pasr.utilities.Utilities.getResource;


public class LDAInteractableDialog extends Dialog<ObjectUtils.Null> {
    public LDAInteractableDialog(String title, String content) throws IOException {
        super(ObjectUtils.NULL);

        URL location = getResource("/fxml/dialog/lda_interactable.fxml");

        if(location == null){
            throw new IOException(
                "getResource(\"/fxml/dialog/lda_interactable.fxml\") returned null"
            );
        }

        FXMLLoader loader = new FXMLLoader(location);
        LDAInteractableController controller = new LDAInteractableController(this, title, content);
        loader.setController(controller);

        setScene(new Scene(loader.load()));
    }

}
