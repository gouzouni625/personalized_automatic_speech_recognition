package org.pasr.gui.controllers.scene;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.pasr.asr.dictionary.Dictionary;
import org.pasr.asr.recognizers.RealTimeSpeechRecognizer;
import org.pasr.asr.recognizers.StreamSpeechRecognizer;
import org.pasr.asr.recognizers.RealTimeSpeechRecognizer.Stage;
import org.pasr.database.DataBase;
import org.pasr.gui.console.Console;
import org.pasr.gui.corpus.CorpusPane;
import org.pasr.postp.correctors.Corrector;
import org.pasr.postp.detectors.POSDetector;
import org.pasr.prep.corpus.Corpus;
import org.pasr.prep.recorder.BufferedRecorder;

import javax.sound.sampled.LineUnavailableException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.pasr.utilities.Utilities.getResourceStream;


public class DictateController extends Controller implements Observer{
    public DictateController(Controller.API api){
        super(api);
    }

    @FXML
    public void initialize(){
        corpusPane.addSelectionListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                setCorpus(newValue.getId());
            }
        });

        Corpus corpus = ((API) api_).getCorpus();
        if(corpus != null){
            corpusPane.selectCorpus(corpus.getId());
        }

        aSRResultTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            aSRResultTextArea.setScrollTop(Double.MAX_VALUE);
        });

        correctedTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            correctedTextArea.setScrollTop(Double.MAX_VALUE);
        });

        dictateToggleButton.setGraphic(dictateToggleButtonDefaultGraphic);
        dictateToggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            dictateToggleButton.setGraphic(
                newValue ? dictateToggleButtonSelectedGraphic : dictateToggleButtonDefaultGraphic
            );
        });

        dictateToggleButton.setOnAction(this :: dictateToggleButtonOnAction);

        ToggleGroup toggleGroup = new ToggleGroup();
        batchRadioButton.setToggleGroup(toggleGroup);
        batchRadioButton.setSelected(true);
        streamRadioButton.setToggleGroup(toggleGroup);
        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == batchRadioButton){
                recognitionMode_ = RecognitionMode.BATCH;
            }
            else{
                recognitionMode_ = RecognitionMode.STREAM;
            }
        });

        useDefaultAcousticModelCheckBox.setOnAction(
            this :: useDefaultAcousticModelCheckBoxOnAction
        );

        backButton.setOnAction(this :: backButtonOnAction);
    }

    private void setCorpus(int id){
        if(dictateToggleButton.isSelected()){
            dictateToggleButton.fire();
        }

        currentCorpusId_ = id;

        dictatePane.setDisable(true);
        corpusPane.setDisable(true);
        dictationDisabledLabel.setText(dictationDisabledLabelMessages.WAITING.getMessage());
        dictationDisabledLabel.setVisible(true);

        startSetCorpusThread(id);
    }

    private void startSetCorpusThread(int id){
        if(setCorpusThread_ == null || !setCorpusThread_.isAlive()){
            setCorpusThread_ = new SetCorpusThread(id);
            setCorpusThread_.start();
        }
    }

    private class SetCorpusThread extends Thread{
        SetCorpusThread(int id){
            id_ = id;

            setDaemon(true);
        }

        @Override
        public void run(){
            logger_.info("SetCorpusThread started!");

            if(!run_){
                onFailure();
                beforeExit();
                return;
            }

            org.pasr.asr.Configuration recognizerConfiguration = new org.pasr.asr.Configuration();

            try {
                recognizerConfiguration.setDictionaryPath(dataBase_.getDictionaryPathById(id_));
            } catch (FileNotFoundException e) {
                Console.getInstance().postMessage("Could not load the dictionary of the selected" +
                    " corpus.");
                onFailure();
                beforeExit();
                return;
            }

            if(!run_){
                onFailure();
                beforeExit();
                return;
            }

            try {
                recognizerConfiguration.setLanguageModelPath(
                    dataBase_.getLanguageModelPathById(id_)
                );
            } catch (FileNotFoundException e) {
                Console.getInstance().postMessage("Could not load the language model of the" +
                    " selected corpus");
                onFailure();
                beforeExit();
                return;
            }

            if(!run_){
                onFailure();
                beforeExit();
                return;
            }

            boolean acousticModelLoaded = false;
            if (!useDefaultAcousticModelCheckBox.isSelected()) {
                try {
                    recognizerConfiguration.setAcousticModelPath(dataBase_.getAcousticModelPath());

                    acousticModelLoaded = true;
                } catch (FileNotFoundException e) {
                    Console.getInstance().postMessage("Could not load the acoustic model.\n" +
                        "Will load the default acoustic model instead.");
                    useDefaultAcousticModelCheckBox.setSelected(true);
                }
            }

            if(!acousticModelLoaded){
                try {
                    recognizerConfiguration.setAcousticModelPath(getDefaultAcousticModelPath());
                } catch (FileNotFoundException e) {
                    Console.getInstance().postMessage("Could not load the default acoustic model.");
                    onFailure();
                    beforeExit();
                    return;
                }
            }

            if(!run_){
                onFailure();
                beforeExit();
                return;
            }

            // Setup the recognizers
            try {
                if(realTimeSpeechRecognizer_ != null) {
                    realTimeSpeechRecognizer_.terminate();
                }

                realTimeSpeechRecognizer_ = new RealTimeSpeechRecognizer(recognizerConfiguration);
                realTimeSpeechRecognizer_.addObserver(DictateController.this);

                if(bufferedRecorder_ == null){
                    bufferedRecorder_ = new BufferedRecorder();
                }

                streamSpeechRecognizer_ = new StreamSpeechRecognizer(recognizerConfiguration);
            } catch (IOException e) {
                logger_.log(Level.SEVERE, "Missing native library.\n" +
                    "Application will terminate.", e);
                Platform.exit();
                beforeExit();
                return;
            } catch (LineUnavailableException e) {
                Console.getInstance().postMessage("Could not open the Microphone.\n" +
                    "Maybe it is being used by another application.\n" +
                    "You will not be able to record your voice so it is advised you go back.");
                onFailure();
                beforeExit();
                return;
            }

            // Setup the corrector
            Dictionary dictionary;
            try {
                dictionary = dataBase_.getDictionaryById(id_);
            } catch (FileNotFoundException e) {
                Console.getInstance().postMessage("Could not load the dictionary of the selected" +
                    " corpus.\nWill load the default dictionary.");

                try {
                    dictionary = Dictionary.getDefaultDictionary();
                } catch (FileNotFoundException e1) {
                    Console.getInstance().postMessage("Could not load the default dictionary" +
                        " model.\nCorrector will not be available.");
                    onFailure();
                    beforeExit();
                    return;
                }
            }

            try {
                Corpus corpus = dataBase_.getCorpusById(id_);

                corrector_ = new Corrector(corpus, dictionary);
                try {
                    corrector_.addDetector(new POSDetector(corpus));
                } catch (IOException e) {
                    logger_.log(Level.SEVERE, "Missing POSDetector model.\n" +
                        "Application will terminate.", e);
                    Platform.exit();
                    beforeExit();
                    return;
                }
                // TODO Improve OccurrenceDetector before using it
                // corrector_.addDetector(new OccurrenceDetector(corpus));
            } catch (IOException e) {
                Console.getInstance().postMessage("Could not load the selected corpus.\n" +
                    "Corrector will not be available.");
                onFailure();
                beforeExit();
                return;
            }

            onSuccess();
            beforeExit();
        }

        private void onFailure(){
            Platform.runLater(() -> {
                dictationDisabledLabel.setText(dictationDisabledLabelMessages.FAILED.getMessage());
                corpusPane.setDisable(false);
            });
        }

        private String getDefaultAcousticModelPath() throws FileNotFoundException {
            String path = org.pasr.asr.Configuration.getDefaultConfiguration()
                .getAcousticModelPath();

            if(!(new File(path).isDirectory())){
                throw new FileNotFoundException("Language Model doesn't exist.");
            }

            return path;
        }

        private void onSuccess(){
            Platform.runLater(() -> {
                dictationDisabledLabel.setVisible(false);
                dictatePane.setDisable(false);
                corpusPane.setDisable(false);
            });
        }

        private void beforeExit(){
            logger_.info("SetCorpusThread shut down gracefully!");
        }

        public void terminate(){
            run_ = false;
        }

        private int id_;

        private volatile boolean run_ = true;

        private Logger logger_ = Logger.getLogger(getClass().getName());
    }

    private void dictateToggleButtonOnAction(ActionEvent actionEvent){
        if(dictateToggleButton.isSelected()){
            switch (recognitionMode_){
                case BATCH:
                    bufferedRecorder_.startRecording();
                    break;
                case STREAM:
                    realTimeSpeechRecognizer_.startRecognition();
                    break;
            }

            batchRadioButton.setDisable(true);
            streamRadioButton.setDisable(true);
            useDefaultAcousticModelCheckBox.setDisable(true);
            backButton.setDisable(true);
        }
        else{
            switch (recognitionMode_){
                case BATCH:
                    bufferedRecorder_.stopRecording();
                    try {
                        outputManager_.start();
                        outputManager_.process(
                            streamSpeechRecognizer_.recognize(
                                new ByteArrayInputStream(
                                    bufferedRecorder_.getData()
                                )
                            )
                        );
                        outputManager_.stop();
                    } catch (IOException e) {
                        Console.getInstance().postMessage("Could not record your voice.\n" +
                            "Please try again.");
                    }
                    break;
                case STREAM:
                    realTimeSpeechRecognizer_.stopRecognition();
                    break;
            }

            bufferedRecorder_.flush();

            batchRadioButton.setDisable(false);
            streamRadioButton.setDisable(false);
            useDefaultAcousticModelCheckBox.setDisable(false);
            backButton.setDisable(false);
        }
    }

    private void useDefaultAcousticModelCheckBoxOnAction(ActionEvent actionEvent){
        setCorpus(currentCorpusId_);
    }

    private void backButtonOnAction(ActionEvent actionEvent){
        if(dictateToggleButton.isSelected()){
            dictateToggleButton.fire();
        }

        terminate();

        ((API) api_).initialScene();
    }

    @Override
    public void update (Observable o, Object arg) {
        if(arg instanceof Stage){
            if(arg == Stage.STARTED){
                outputManager_.start();
            }
            else if(arg == Stage.STOPPED){
                outputManager_.stop();
            }
        } else if(arg instanceof String){
            outputManager_.process((String) arg);
        }
    }

    private class OutputManager{
        OutputManager(){}

        void start(){}

        void stop(){
            aSRTextAreaText_ += aSROutput_ + "\n";
            correctedTextAreaText_ += corrected_ + "\n";
        }

        void process(String aSROutput){
            if(corrector_ == null){
                aSROutput_ = aSROutput;
                updateASRTextArea();

                return;
            }

            aSROutput_ = aSROutput;
            corrected_ = corrector_.correct(aSROutput_);

            updateASRTextArea();
            updateCorrectedTextArea();
        }

        private void updateASRTextArea(){
            aSRResultTextArea.setText(aSRTextAreaText_ + aSROutput_);
        }

        private void updateCorrectedTextArea(){
            correctedTextArea.setText(correctedTextAreaText_ + corrected_);
        }

        private String aSRTextAreaText_ = "";
        private String correctedTextAreaText_ = "";

        private String aSROutput_ = "";
        private String corrected_ = "";
    }

    @Override
    public void terminate(){
        setCorpusThread_.terminate();

        try {
            // Don't wait forever on this thread since it is a daemon and will not block the JVM
            // from shutting down
            setCorpusThread_.join(3000);
        } catch (InterruptedException e) {
            logger_.warning("Interrupted while joining setCorpusThread.");
        }

        realTimeSpeechRecognizer_.terminate();
        bufferedRecorder_.terminate();
    }

    public interface API extends Controller.API{
        Corpus getCorpus();
        void initialScene();
    }

    @FXML
    private CorpusPane corpusPane;

    @FXML
    private AnchorPane dictatePane;

    @FXML
    private TextArea aSRResultTextArea;

    @FXML
    private TextArea correctedTextArea;

    @FXML
    private Label dictationDisabledLabel;
    private enum dictationDisabledLabelMessages{
        WAITING("Please wait while the recognition system is setup..."),
        FAILED("There has been an error while setting up the recognition system. Please select" +
            " another corpus.");

        dictationDisabledLabelMessages(String message){
            message_ = message;
        }

        public String getMessage(){
            return message_;
        }

        private String message_;
    }

    @FXML
    private ToggleButton dictateToggleButton;
    private static final Node dictateToggleButtonDefaultGraphic = new ImageView(
        new Image(getResourceStream("/icons/microphone_black.png")));
    private static final Node dictateToggleButtonSelectedGraphic = new ImageView(
        new Image(getResourceStream("/icons/microphone_green.png")));

    @FXML
    private RadioButton batchRadioButton;

    @FXML
    private RadioButton streamRadioButton;

    @FXML
    private CheckBox useDefaultAcousticModelCheckBox;

    @FXML
    private Button backButton;

    private StreamSpeechRecognizer streamSpeechRecognizer_;
    private RealTimeSpeechRecognizer realTimeSpeechRecognizer_;
    private RecognitionMode recognitionMode_ = RecognitionMode.BATCH;
    private enum RecognitionMode{
        STREAM,
        BATCH
    }

    private BufferedRecorder bufferedRecorder_;

    private Corrector corrector_;

    private SetCorpusThread setCorpusThread_;

    private int currentCorpusId_ = -1;

    private OutputManager outputManager_ = new OutputManager();

    private DataBase dataBase_ = DataBase.getInstance();

    private final Logger logger_ = Logger.getLogger(getClass().getName());

}
