package org.pasr.gui.controllers;


import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;


public class ASRSceneController {
    @FXML
    private TextArea asrResult;

    public ASRSceneController() throws Exception {
        Configuration configuration = new Configuration();

        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration.setLanguageModelPath("cmuclmtk-0.7/language_model.lm");

        recognizer_ = new LiveSpeechRecognizer(configuration);
        // recognizer_.loadTransform("acoustic_model_adaptation/mllr_matrix", 1);
    }

    @FXML
    public void initialize(){
        recognizer_.startRecognition(true);

        new Thread(() -> {
            SpeechResult result;
            while ((result = recognizer_.getResult()) != null) {
                asrResult.setText(asrResult.getText() + "\n" + result.getHypothesis());
            }

            recognizer_.stopRecognition();
        }).start();
    }

    private LiveSpeechRecognizer recognizer_;

}
