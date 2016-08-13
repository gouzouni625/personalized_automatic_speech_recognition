package org.pasr.gui.controllers.scene;


import edu.cmu.pocketsphinx.Hypothesis;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.pasr.asr.recognizers.StreamSpeechRecognizer;
import org.pasr.database.DataBase;
import org.pasr.gui.corpus.CorpusPane;
import org.pasr.prep.corpus.Corpus;

import java.io.IOException;

import static org.pasr.utilities.Utilities.getResourceStream;


public class DictateController extends Controller{
    public DictateController(Controller.API api){
        super(api);

        int corpusId = ((API) api_).getCorpus().getId();

        DataBase dataBase = DataBase.getInstance();

        org.pasr.asr.Configuration recognizerConfiguration = new org.pasr.asr.Configuration();
        recognizerConfiguration.setDictionaryPath(dataBase.getDictionaryPathById(corpusId));
        recognizerConfiguration.setLanguageModelPath(dataBase.getLanguageModelPathById(corpusId));
        recognizerConfiguration.setAcousticModelPath(
            org.pasr.database.Configuration.getInstance().getAcousticModelPath()
        );

        try {
            recognizer_ = new StreamSpeechRecognizer(recognizerConfiguration);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize(){
        corpusPane.selectCorpus(((API) api_).getCorpus().getId());

        dictateToggleButton.setGraphic(dictateToggleButtonDefaultGraphic);
        dictateToggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            dictateToggleButton.setGraphic(
                newValue ? dictateToggleButtonSelectedGraphic : dictateToggleButtonDefaultGraphic
            );
        });

        dictateToggleButton.setOnAction(this :: dictateToggleButtonOnAction);

        backButton.setOnAction(this :: backButtonOnAction);

        recognizer_.addObserver(
            (o, arg) -> {
                aSRResultTextArea.setText(aSRResultTextAreaText_ + ((Hypothesis) arg).getHypstr());
                aSRResultTextArea.setScrollTop(Double.MAX_VALUE);
            });
    }

    private void dictateToggleButtonOnAction(ActionEvent actionEvent){
        if(dictateToggleButton.isSelected()){
            recognizer_.startRecognition();
        }
        else{
            recognizer_.stopRecognition();
            aSRResultTextArea.appendText("\n");
            aSRResultTextAreaText_ = aSRResultTextArea.getText();
        }
    }

    private void backButtonOnAction(ActionEvent actionEvent){
        ((API) api_).initialScene();
    }

    @Override
    public void terminate(){
        try {
            recognizer_.terminate();
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
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
    private String aSRResultTextAreaText_ = "";

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

    private StreamSpeechRecognizer recognizer_;

}
