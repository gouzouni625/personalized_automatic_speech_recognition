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
import org.pasr.gui.console.Console;
import org.pasr.prep.corpus.Corpus;
import org.pasr.prep.recorder.BufferedRecorder;

import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.logging.Level;

import static org.pasr.utilities.Utilities.getResourceStream;


public class RecordController extends Controller implements Observer{
    public RecordController(Controller.API api){
        super(api);

        corpus_ = ((API) api_).getCorpus();
        corpusSentences_ = FXCollections.observableArrayList();
        fillCorpusSentences();

        arcticSentences_ = FXCollections.observableArrayList();
        fillArcticSentences();

        try {
            recorder_ = new BufferedRecorder();
            recorder_.addObserver(this);
        } catch (LineUnavailableException e) {
            Console.getInstance().postMessage("Could not open the Microphone.\n" +
                "Maybe it is being used by another application.\n" +
                "You will not be able to record your voice so it is advised you go back.");

            getLogger().log(Level.WARNING, "Could not create a BufferedRecorder.", e);
        }
    }

    private void fillCorpusSentences () {
        if(corpus_ == null){
            return;
        }

        if(fillCorpusSentencesThread_ == null || !fillCorpusSentencesThread_.isAlive()){
            fillCorpusSentencesThread_ = new FillCorpusSentencesThread();
            fillCorpusSentencesThread_.start();
        }
    }

    private class FillCorpusSentencesThread extends Thread{
        FillCorpusSentencesThread(){
            setDaemon(true);
        }

        @Override
        public void run(){
            int currentSize = corpusSentences_.size();
            if (currentSize == corpusSentencesMaxSize_) {
                return;
            }

            ArrayList<String> randomSentences = new ArrayList<>();

            Random random = new Random(System.currentTimeMillis());
            for (int i = currentSize; i < corpusSentencesMaxSize_; i++) {
                if(stop_){
                    return;
                }

                randomSentences.add(corpus_.getRandomSubSequence(random));
            }

            Platform.runLater(() -> corpusSentences_.addAll(randomSentences));
        }

        void terminate(){
            stop_ = true;
        }


        private boolean stop_ = false;
    }

    private void fillArcticSentences () {
        if(fillArcticSentencesThread_ == null || !fillArcticSentencesThread_.isAlive()) {
            fillArcticSentencesThread_ = new Thread(fillArcticSentencesRunnable_);
            fillArcticSentencesThread_.setDaemon(true);
            fillArcticSentencesThread_.start();
        }
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
        saveButton.setOnAction(this :: saveButtonOnAction);

        backButton.setOnAction(this :: backButtonOnAction);
        doneButton.setOnAction(this :: doneButtonOnAction);

        corpusListView.setItems(corpusSentences_);
        corpusListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if(newValue != null){
                    sentenceLabel.setText(newValue);

                    arcticListView.getSelectionModel().clearSelection();
                }
                else{
                    if(arcticListView.getSelectionModel().getSelectedIndex() == -1){
                        sentenceLabel.setText("");
                    }
                }
        });

        arcticListView.setItems(arcticSentences_);
        arcticListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if(newValue != null){
                    sentenceLabel.setText(newValue);

                    corpusListView.getSelectionModel().clearSelection();
                }
                else{
                    if(corpusListView.getSelectionModel().getSelectedIndex() == -1){
                        sentenceLabel.setText("");
                    }
                }
        });
    }

    private void eraseButtonOnAction(ActionEvent actionEvent){
        if(recordToggleButton.isSelected()){
            recordToggleButton.fire();
        }

        if(playToggleButton.isSelected()){
            playToggleButton.fire();
        }

        recorder_.flush();
    }

    private void recordToggleButtonOnAction(ActionEvent actionEvent){
        if(recordToggleButton.isSelected()){
            if(playToggleButton.isSelected()){
                playToggleButton.fire();
            }

            recorder_.startRecording();
        }
        else{
            recorder_.stopRecording();
            setProgressBarLevel(0);
        }
    }

    private void playToggleButtonOnAction(ActionEvent actionEvent){
        if(playToggleButton.isSelected()){
            if(recordToggleButton.isSelected()){
                recordToggleButton.fire();
            }

            try {
                clip_ = recorder_.getClip();
                clip_.addLineListener(clipLineListener_);
                clip_.start();
            } catch (LineUnavailableException e) {
                Console.getInstance().postMessage("Could not get the recording of your voice.\n" +
                    "Maybe your microphone is being used by another application.\n" +
                    "Try recording again.");
                playToggleButton.setSelected(false);
            }
        }
        else{
            clip_.stop();
            setProgressBarLevel(0);
        }
    }

    private void saveButtonOnAction(ActionEvent actionEvent){
        if(recordToggleButton.isSelected()){
            recordToggleButton.fire();
        }

        if(playToggleButton.isSelected()) {
            playToggleButton.fire();
        }

        String sentence = sentenceLabel.getText();

        if(sentence.isEmpty()){
            Console.getInstance().postMessage("You should choose a sentence from the two lists" +
                "on the write as a transcription of your recording.");
            return;
        }

        int index;
        int corpusId;
        if((index = corpusListView.getSelectionModel().getSelectedIndex()) != -1){
            corpusListView.getItems().remove(index);
            fillCorpusSentences();
            corpusId = corpus_.getId();
        }
        else{
            index = arcticListView.getSelectionModel().getSelectedIndex();

            arcticListView.getItems().remove(index);
            DataBase.getInstance().setArcticSentenceAsUsed(sentence);
            fillArcticSentences();
            corpusId = 0;
        }

        try {
            DataBase.getInstance().newAudioEntry(recorder_.getData(), sentence, corpusId);
        } catch (IOException e) {
            Console.getInstance().postMessage("Could not save your recording.\n" +
                "You should check your user permissions inside the directory " +
                DataBase.getInstance().getConfiguration().getDataBaseDirectoryPath());

            return;
        }

        eraseButton.fire();
    }

    private void backButtonOnAction(ActionEvent actionEvent){
        terminate();

        ((API) api_).initialScene();
    }

    private void doneButtonOnAction(ActionEvent actionEvent){
        terminate();

        ((API) api_).dictate(corpus_ == null ? -1 : corpus_.getId());
    }

    @Override
    public void update (Observable o, Object arg) {
        setProgressBarLevel((Double) arg);
    }

    private void setProgressBarLevel(double level){
        leftProgressBar.setProgress(level);
        rightProgressBar.setProgress(level);
    }

    @Override
    public void terminate() {
        if(clip_ != null && clip_.isOpen()) {
            clip_.close();
        }

        if(recorder_ != null) {
            recorder_.terminate();
        }

        if(fillCorpusSentencesThread_ != null && fillCorpusSentencesThread_.isAlive()) {
            fillCorpusSentencesThread_.terminate();
            try {
                // Don't wait forever on this thread since it is a daemon and will not block the JVM
                // from shutting down
                fillCorpusSentencesThread_.join(3000);
            } catch (InterruptedException e) {
                getLogger().warning("Interrupted while joining fillCorpusSentencesThread.");
            }
        }

        if(fillArcticSentencesThread_ != null && fillArcticSentencesThread_.isAlive()) {
            try {
                // Don't wait forever on this thread since it is a daemon and will not block the JVM
                // from shutting down
                fillArcticSentencesThread_.join(3000);
            } catch (InterruptedException e) {
                getLogger().warning("Interrupted while joining fillArcticSentencesThread.");
            }
        }
    }

    public interface API extends Controller.API{
        Corpus getCorpus();
        void initialScene();
        void dictate(int corpusId);
    }

    @FXML
    private ListView<String> corpusListView;
    private ObservableList<String> corpusSentences_;
    private static final int corpusSentencesMaxSize_ = 20;
    private FillCorpusSentencesThread fillCorpusSentencesThread_;

    @FXML
    private ListView<String> arcticListView;
    private ObservableList<String> arcticSentences_;
    private static final int arcticSentencesMaxSize_ = 20;
    private Thread fillArcticSentencesThread_;
    private final Runnable fillArcticSentencesRunnable_ = () -> {
        int currentSize = arcticSentences_.size();
        if (currentSize == arcticSentencesMaxSize_) {
            return;
        }

        List<String> newArcticSentences = DataBase.getInstance().getUnUsedArcticSentences(
            arcticSentencesMaxSize_ - currentSize
        );

        Platform.runLater(() -> arcticSentences_.addAll(newArcticSentences));
    };

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
    private final LineListener clipLineListener_ = event -> {
        if(event.getType() == LineEvent.Type.STOP){
            Platform.runLater(() -> playToggleButton.setSelected(false));
        }
    };

}
