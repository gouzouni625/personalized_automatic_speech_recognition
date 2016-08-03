package org.pasr.gui.controllers.scene;


import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import static org.pasr.utilities.Utilities.getResourceStream;


public class RecordController extends Controller{
    public RecordController(Controller.API api){
        super(api);
    }

    @FXML
    public void initialize(){
        eraseButton.setGraphic(eraseButtonDefaultGraphic);
        eraseButton.pressedProperty().addListener((observable, oldValue, newValue) -> {
            eraseButton.setGraphic(
                newValue ? eraseButtonPressedGraphic : eraseButtonDefaultGraphic
            );
        });

        recordToggleButton.setGraphic(recordToggleButtonDefaultGraphic);
        recordToggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            recordToggleButton.setGraphic(
                newValue ? recordToggleButtonSelectedGraphic : recordToggleButtonDefaultGraphic
            );
        });

        stopButton.setGraphic(stopButtonDefaultGraphic);
        stopButton.pressedProperty().addListener((observable, oldValue, newValue) -> {
            stopButton.setGraphic(
                newValue ? stopButtonPressedGraphic : stopButtonDefaultGraphic
            );
        });

        pauseToggleButton.setGraphic(pauseToggleButtonDefaultGraphic);
        pauseToggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            pauseToggleButton.setGraphic(
                newValue ? pauseToggleButtonSelectedGraphic : pauseToggleButtonDefaultGraphic
            );
        });

        playToggleButton.setGraphic(playToggleButtonDefaultGraphic);
        playToggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            playToggleButton.setGraphic(
                newValue ? playToggleButtonSelectedGraphic : playToggleButtonDefaultGraphic
            );
        });

        saveButton.setGraphic(saveButtonDefaultGraphic);
        saveButton.pressedProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setGraphic(
                newValue ? saveButtonPressedGraphic : saveButtonDefaultGraphic
            );
        });
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
    private static final Node eraseButtonDefaultGraphic = new ImageView(
        new Image(getResourceStream("/icons/bin_black.png")));
    private static final Node eraseButtonPressedGraphic = new ImageView(
        new Image(getResourceStream("/icons/bin_green.png")));

    @FXML
    private ToggleButton recordToggleButton;
    private static final Node recordToggleButtonDefaultGraphic = new ImageView(
        new Image(getResourceStream("/icons/microphone_black.png")));
    private static final Node recordToggleButtonSelectedGraphic = new ImageView(
        new Image(getResourceStream("/icons/microphone_green.png")));

    @FXML
    private Button stopButton;
    private static final Node stopButtonDefaultGraphic = new ImageView(
        new Image(getResourceStream("/icons/stop_black.png")));
    private static final Node stopButtonPressedGraphic = new ImageView(
        new Image(getResourceStream("/icons/stop_green.png")));

    @FXML
    private ToggleButton pauseToggleButton;
    private static final Node pauseToggleButtonDefaultGraphic = new ImageView(
        new Image(getResourceStream("/icons/pause_black.png")));
    private static final Node pauseToggleButtonSelectedGraphic = new ImageView(
        new Image(getResourceStream("/icons/pause_green.png")));

    @FXML
    private ToggleButton playToggleButton;
    private static final Node playToggleButtonDefaultGraphic = new ImageView(
        new Image(getResourceStream("/icons/play_black.png")));
    private static final Node playToggleButtonSelectedGraphic = new ImageView(
        new Image(getResourceStream("/icons/play_green.png")));

    @FXML
    private Button saveButton;
    private static final Node saveButtonDefaultGraphic = new ImageView(
        new Image(getResourceStream("/icons/save_black.png")));
    private static final Node saveButtonPressedGraphic = new ImageView(
        new Image(getResourceStream("/icons/save_green.png")));

    @FXML
    private Button backButton;

    @FXML
    private Button doneButton;

}
