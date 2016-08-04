package org.pasr.gui.controllers.scene;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.pasr.database.DataBase;
import org.pasr.prep.corpus.Corpus;
import org.pasr.prep.recorder.BufferedRecorder;

import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static org.pasr.utilities.Utilities.getResourceStream;


public class RecordController extends Controller{
    public RecordController(Controller.API api){
        super(api);

        corpus_ = DataBase.getInstance().getCorpusByID(((API) api_).getCorpusID());
        corpusSentences_ = FXCollections.observableArrayList();
        fillCorpusSentences();

        arcticSentences_ = FXCollections.observableArrayList();
        fillArcticSentences();

        try {
            recorder_ = new BufferedRecorder();
            new Thread(recorder_).start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        timer_ = new Timer();
    }

    private void fillCorpusSentences () {
        new Thread(() -> {
            int currentSize = corpusSentences_.size();
            if (currentSize == corpusSentencesMaxSize_) {
                return;
            }

            Random random = new Random(System.currentTimeMillis());
            for (int i = currentSize; i < corpusSentencesMaxSize_; i++) {
                corpusSentences_.add(corpus_.getRandomSubSequence(random));
            }
        }).start();
    }

    private void fillArcticSentences () {
        new Thread(() -> {
            int currentSize = arcticSentences_.size();
            if (currentSize == arcticSentencesMaxSize_) {
                return;
            }

            arcticSentences_.addAll(DataBase.getInstance().getUnUsedArcticSentences(
                arcticSentencesMaxSize_ - currentSize
            ));
        }).start();
    }

    @FXML
    public void initialize(){
        eraseButton.setGraphic(eraseButtonDefaultGraphic);
        eraseButton.pressedProperty().addListener((observable, oldValue, newValue) -> {
            eraseButton.setGraphic(
                newValue ? eraseButtonPressedGraphic : eraseButtonDefaultGraphic
            );
        });
        eraseButton.setOnAction(this :: eraseButtonOnAction);

        recordToggleButton.setGraphic(recordToggleButtonDefaultGraphic);
        recordToggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            recordToggleButton.setGraphic(
                newValue ? recordToggleButtonSelectedGraphic : recordToggleButtonDefaultGraphic
            );
        });
        recordToggleButton.setOnAction(this :: recordToggleButtonOnAction);

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
        playToggleButton.setOnAction(this :: playToggleButtonOnAction);

        saveButton.setGraphic(saveButtonDefaultGraphic);
        saveButton.pressedProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setGraphic(
                newValue ? saveButtonPressedGraphic : saveButtonDefaultGraphic
            );
        });

        corpusListView.setItems(corpusSentences_);
        corpusListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if(newValue != null){
                    sentenceLabel.setText(newValue);

                    arcticListView.getSelectionModel().clearSelection();
                }
        });

        arcticListView.setItems(arcticSentences_);
        arcticListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if(newValue != null){
                    sentenceLabel.setText(newValue);

                    corpusListView.getSelectionModel().clearSelection();
                }
        });

        timer_.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run () {
                Platform.runLater(() -> updateProgressBars());
            }
        }, 0, 100);
    }

    private void eraseButtonOnAction(ActionEvent actionEvent){
        recorder_.flush();
    }

    private void recordToggleButtonOnAction(ActionEvent actionEvent){
        if(recordToggleButton.isSelected()){
            recorder_.startRecording();
        }
        else{
            recorder_.stopRecording();
        }
    }

    private void playToggleButtonOnAction(ActionEvent actionEvent){
        if(playToggleButton.isSelected()){
            try {
                clip_ = recorder_.getClip();
            } catch (LineUnavailableException e) {
                // TODO
                e.printStackTrace();
            }

            clip_.start();
        }
        else{
            clip_.stop();
        }
    }

    private void updateProgressBars(){
        double level = recorder_.getLevel();

        leftProgressBar.setProgress(level);
        rightProgressBar.setProgress(level);

        System.out.println(level);
    }

    @Override
    public void terminate(){
        timer_.cancel();
    }

    public interface API extends Controller.API{
        int getCorpusID();
    }

    @FXML
    private ListView<String> corpusListView;
    private ObservableList<String> corpusSentences_;
    private static final int corpusSentencesMaxSize_ = 20;

    @FXML
    private ListView<String> arcticListView;
    private ObservableList<String> arcticSentences_;
    private static final int arcticSentencesMaxSize_ = 20;

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

    private Corpus corpus_;

    private BufferedRecorder recorder_;
    private Clip clip_;

    private Timer timer_;

}
