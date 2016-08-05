package org.pasr.gui.controllers.scene;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.pasr.gui.corpus.CorpusView;

import static org.pasr.utilities.Utilities.getResourceStream;


public class DictateController extends Controller{
    public DictateController(Controller.API api){
        super(api);
    }

    @FXML
    public void initialize(){
        corpusView.selectCorpus(((API) api_).getCorpusID());

        dictateToggleButton.setGraphic(dictateToggleButtonDefaultGraphic);
        dictateToggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            dictateToggleButton.setGraphic(
                newValue ? dictateToggleButtonSelectedGraphic : dictateToggleButtonDefaultGraphic
            );
        });

        backButton.setOnAction(this :: backButtonOnAction);
    }

    private void backButtonOnAction(ActionEvent actionEvent){
        ((API) api_).initialScene();
    }

    public interface API extends Controller.API{
        int getCorpusID();
        void initialScene();
    }

    @FXML
    private CorpusView corpusView;

    @FXML
    private AnchorPane dictatePane;

    @FXML
    private TextArea aSRResultTextArea;

    @FXML
    private TextArea correctedTextArea;

    @FXML
    private ToggleButton dictateToggleButton;
    private static final Node dictateToggleButtonDefaultGraphic = new ImageView(
        new Image(getResourceStream("/icons/microphone_black.png")));
    private static final Node dictateToggleButtonSelectedGraphic = new ImageView(
        new Image(getResourceStream("/icons/microphone_green.png")));

    @FXML
    private Button backButton;

}
