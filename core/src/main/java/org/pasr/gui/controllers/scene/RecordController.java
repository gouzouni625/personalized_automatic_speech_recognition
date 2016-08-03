package org.pasr.gui.controllers.scene;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;


public class RecordController extends Controller{
    public RecordController(Controller.API api){
        super(api);
    }

    @FXML
    public void initialize(){

    }

    public interface API extends Controller.API{
        int getCorpusID();
    }

    @FXML
    private ListView<String> corpusListView;

    @FXML
    private ListView<String> externalListView;

    @FXML
    private Label sentenceLabel;

    @FXML
    private ProgressBar leftProgressBar;

    @FXML
    private ProgressBar rightProgressBar;

    @FXML
    private Button eraseButton;

    @FXML
    private ToggleButton recordToggleButton;

    @FXML
    private Button stopButton;

    @FXML
    private ToggleButton pauseToggleButton;

    @FXML
    private ToggleButton playToggleButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button backButton;

    @FXML
    private Button doneButton;

}
