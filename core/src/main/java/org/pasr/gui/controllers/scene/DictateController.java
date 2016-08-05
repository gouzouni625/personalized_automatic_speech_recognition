package org.pasr.gui.controllers.scene;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import org.pasr.gui.corpus.CorpusView;


public class DictateController extends Controller{
    public DictateController(Controller.API api){
        super(api);
    }

    @FXML
    public void initialize(){
        corpusView.selectCorpus(((API) api_).getCorpusID());

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
    private ToggleButton dictateButton;

    @FXML
    private Button backButton;

}
