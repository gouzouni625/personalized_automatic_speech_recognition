package org.pasr.gui.controllers;


import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.pasr.corpus.Corpus;
import org.pasr.postp.dictionary.Dictionary;
import org.pasr.postp.engine.Corrector;

import java.io.IOException;


public class ASRSceneController {
    @FXML
    private TextArea asrResult;

    @FXML
    private TextArea asrCorrected;

    public ASRSceneController(Corpus corpus, Dictionary dictionary) {
        corrector_ = new Corrector(corpus, dictionary);
    }

    @FXML
    public void initialize(){
        new Thread(() -> {
            Configuration configuration = new Configuration();

            configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
            configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
            configuration.setLanguageModelPath("cmuclmtk-0.7/language_model.lm");

            try {
                recognizer_ = new LiveSpeechRecognizer(configuration);
                // recognizer_.loadTransform("acoustic_model_adaptation/mllr_matrix", 1);

                recognizerRunning_ = true;

                recognizer_.startRecognition(true);

            } catch (IOException e) {
                e.printStackTrace();
            }

            SpeechResult result;
            while ((result = recognizer_.getResult()) != null && recognizerRunning_) {
                String hypothesis = result.getHypothesis();
                asrResult.setText(asrResult.getText() + "\n" + hypothesis);
                asrCorrected.setText(corrector_.correct(hypothesis));
            }

            recognizer_.stopRecognition();
        }).start();
    }

    public void close(){
        recognizerRunning_ = false;
    }

    private LiveSpeechRecognizer recognizer_;
    private Corrector corrector_;

    private boolean recognizerRunning_ = false;

}
