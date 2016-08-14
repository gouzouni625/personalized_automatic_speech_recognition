package org.pasr.gui.controllers.scene;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.pasr.asr.recognizers.StreamSpeechRecognizer;
import org.pasr.asr.recognizers.StreamSpeechRecognizer.Stage;
import org.pasr.database.DataBase;
import org.pasr.gui.console.Console;
import org.pasr.gui.corpus.CorpusPane;
import org.pasr.prep.corpus.Corpus;

import javax.sound.sampled.LineUnavailableException;
import java.io.File;
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

        aSRResultTextArea.textProperty().addListener(event -> {
            aSRResultTextArea.setScrollTop(Double.MAX_VALUE);
        });

        correctedTextArea.textProperty().addListener(event -> {
            correctedTextArea.setScrollTop(Double.MAX_VALUE);
        });

        dictateToggleButton.setGraphic(dictateToggleButtonDefaultGraphic);
        dictateToggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            dictateToggleButton.setGraphic(
                newValue ? dictateToggleButtonSelectedGraphic : dictateToggleButtonDefaultGraphic
            );
        });

        dictateToggleButton.setOnAction(this :: dictateToggleButtonOnAction);

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
            if(!run_){
                onFailure();
                return;
            }

            org.pasr.asr.Configuration recognizerConfiguration = new org.pasr.asr.Configuration();

            try {
                recognizerConfiguration.setDictionaryPath(dataBase_.getDictionaryPathById(id_));
            } catch (IOException e) {
                Console.getInstance().postMessage("Could not load the dictionary of the selected" +
                    " corpus.");
                onFailure();
                return;
            }

            if(!run_){
                onFailure();
                return;
            }

            try {
                recognizerConfiguration.setLanguageModelPath(dataBase_.getLanguageModelPathById(id_));
            } catch (IOException e) {
                Console.getInstance().postMessage("Could not load the language model of the" +
                    " selected corpus");
                onFailure();
                return;
            }

            if(!run_){
                onFailure();
                return;
            }

            boolean acousticModelLoaded = false;
            if (!useDefaultAcousticModelCheckBox.isSelected()) {
                try {
                    recognizerConfiguration.setAcousticModelPath(dataBase_.getAcousticModelPath());

                    acousticModelLoaded = true;
                } catch (IOException e) {
                    Console.getInstance().postMessage("Could not load the acoustic model.\n" +
                        "Will load the default acoustic model instead.");
                    useDefaultAcousticModelCheckBox.setSelected(true);
                }
            }

            if(!acousticModelLoaded){
                try {
                    recognizerConfiguration.setAcousticModelPath(getDefaultAcousticModelPath());
                } catch (IOException e) {
                    Console.getInstance().postMessage("Could not load the default acoustic model.");
                    onFailure();
                    return;
                }
            }

            if(!run_){
                onFailure();
                return;
            }

            try {
                if(recognizer_ != null) {
                    recognizer_.terminate();
                }

                recognizer_ = new StreamSpeechRecognizer(recognizerConfiguration);
                recognizer_.addObserver(DictateController.this);
            } catch (IOException e) {
                logger_.log(Level.SEVERE, "Missing native library.\n" +
                    "Application will terminate.", e);
                Platform.exit();
                return;
            } catch (LineUnavailableException e) {
                Console.getInstance().postMessage("Could not open the Microphone.\n" +
                    "Maybe it is being used by another application.\n" +
                    "You will not be able to record your voice so it is advised you go back.");
                onFailure();
                return;
            }

            onSuccess();
        }

        private void onFailure(){
            Platform.runLater(() -> {
                dictationDisabledLabel.setText(dictationDisabledLabelMessages.FAILED.getMessage());
                corpusPane.setDisable(false);
            });
        }

        private String getDefaultAcousticModelPath() throws IOException {
            String path = org.pasr.asr.Configuration.getDefaultConfiguration()
                .getAcousticModelPath();

            if(!(new File(path).isDirectory())){
                throw new IOException("Language Model doesn't exist.");
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

        public void terminate(){
            run_ = false;
        }

        private int id_;

        private volatile boolean run_ = true;
    }

    private void dictateToggleButtonOnAction(ActionEvent actionEvent){
        if(dictateToggleButton.isSelected()){
            recognizer_.startRecognition();
        }
        else{
            recognizer_.stopRecognition();
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
            if(arg == Stage.STARTED && !aSRResultTextAreaPreviousText_.isEmpty()){
                aSRResultTextAreaPreviousText_ += ".\n\n";
            }
            else if(arg == Stage.STOPPED){
                aSRResultTextAreaPreviousText_ = aSRResultTextArea.getText();
            }
        } else if(arg instanceof String){
            aSRResultTextArea.setText(aSRResultTextAreaPreviousText_ + arg);
        }
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

        recognizer_.terminate();
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
    private String aSRResultTextAreaPreviousText_ = "";

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
    private CheckBox useDefaultAcousticModelCheckBox;

    @FXML
    private Button backButton;

    private StreamSpeechRecognizer recognizer_;

    private SetCorpusThread setCorpusThread_;

    private int currentCorpusId_ = -1;

    private DataBase dataBase_ = DataBase.getInstance();

    private final Logger logger_ = Logger.getLogger(getClass().getName());

}
