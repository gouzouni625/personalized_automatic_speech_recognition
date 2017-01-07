package org.pasr.gui.controllers.scene;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;


/**
 * @class IntermediateController
 * @brief Controller for the intermediate scene of this application
 *        This scene is a loading scene shown when transitioning from a scene another. This is done
 *        when instantiation of a scene and its components takes time.
 */
public class IntermediateController extends Controller {

    /**
     * @brief Constructor
     *
     * @param api
     *     The implementation of the API of this Controller
     */
    public IntermediateController (Controller.API api) {
        super(api);
    }

    @FXML
    public void initialize () {
        label.setText(((API) api_).getMessage());
    }

    public interface API extends Controller.API {

        String getMessage ();
    }

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Label label;

}
