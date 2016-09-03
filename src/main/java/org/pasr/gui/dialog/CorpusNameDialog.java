package org.pasr.gui.dialog;


import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import org.pasr.gui.controllers.dialog.CorpusNameController;

import java.io.IOException;
import java.net.URL;

import static org.pasr.utilities.Utilities.getResource;


public class CorpusNameDialog extends Dialog<String> {
    public CorpusNameDialog (String defaultName) throws IOException {
        super(defaultName);

        URL location = getResource("/fxml/dialog/corpus_name.fxml");

        if(location == null){
            throw new IOException("getResource(\"/fxml/dialog/corpus_name.fxml\") returned null");
        }

        FXMLLoader loader = new FXMLLoader(location);
        CorpusNameController controller = new CorpusNameController(this);
        loader.setController(controller);

        initModality(Modality.APPLICATION_MODAL);

        setScene(new Scene(loader.load()));
    }

}
