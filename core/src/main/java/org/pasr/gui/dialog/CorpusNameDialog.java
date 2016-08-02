package org.pasr.gui.dialog;


import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import org.pasr.gui.controllers.dialog.CorpusNameDialogController;

import java.io.IOException;

import static org.pasr.utilities.Utilities.getResource;


public class CorpusNameDialog extends Dialog<String> {
    public CorpusNameDialog (String defaultName) throws IOException {
        super(defaultName);

        FXMLLoader loader = new FXMLLoader(getResource("/fxml/dialog/corpus_name.fxml"));
        CorpusNameDialogController controller = new CorpusNameDialogController(this);
        loader.setController(controller);

        stage_.setScene(new Scene(loader.load()));
    }

}
