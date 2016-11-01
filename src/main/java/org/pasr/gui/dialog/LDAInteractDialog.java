package org.pasr.gui.dialog;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.pasr.gui.controllers.dialog.LDAInteractController;
import org.pasr.prep.lda.LDA;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.pasr.utilities.Utilities.getResource;


// Return a Map with keys the corpus name and values the list of documents for each corpus

/**
 * @class LDAInteractDialog
 * @brief Implementation of the Dialog used to interact with LDA results
 *        Returns a Map with entry keys a corpus name and values a List of Document objects for the
 *        corresponding corpus.
 */
public class LDAInteractDialog extends Dialog<MultiValuedMap<String, List<Long>>> {

    /**
     * @brief Constructor
     *
     * @param lda
     *     The LDA algorithm implementation
     *
     * @throws IOException If the fxml file of this Dialog cannot be found
     */
    public LDAInteractDialog (LDA lda) throws IOException {
        super(new ArrayListValuedHashMap<>());

        if (lda == null || ! lda.hasRun()) {
            throw new IllegalArgumentException("Null or not started LDA.");
        }

        URL location = getResource("/fxml/dialog/lda_interact.fxml");

        if (location == null) {
            throw new IOException("getResource(\"/fxml/dialog/lda_interact.fxml\") returned null.");
        }

        FXMLLoader loader = new FXMLLoader(location);
        LDAInteractController controller = new LDAInteractController(this, lda);
        loader.setController(controller);

        initModality(Modality.APPLICATION_MODAL);

        setScene(new Scene(loader.load()));

        setOnCloseRequest(event -> controller.terminate());
    }

}
