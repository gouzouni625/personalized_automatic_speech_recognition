package org.pasr.gui.controllers;


import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.pasr.corpus.Corpus;
import org.pasr.corpus.WordSequence;
import org.pasr.prep.record.Microphone;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.util.Iterator;


// TODO add voice input visualizer
public class VoiceRecordingSceneController {
    public VoiceRecordingSceneController(Corpus corpus) {
        corpus_ = corpus;

        sentenceIterator_ = corpus_.iterator();

        microphone_ = new Microphone();
    }

    @FXML
    private TextArea sentenceToRead;

    @FXML
    public void initialize(){
        if(sentenceIterator_.hasNext()) {
            sentenceToRead.setText(sentenceIterator_.next().getText());
        }
    }

    @FXML
    private void recordButtonClicked() throws LineUnavailableException {
        microphone_.record();
    }

    @FXML
    private void recordDoneButtonClicked() throws InterruptedException, IOException {
        microphone_.stop();

        if(sentenceIterator_.hasNext()) {
            sentenceToRead.setText(sentenceIterator_.next().getText());
        }
    }

    @FXML
    private void doneButtonClicked(){
        // Move to asr
    }

    private Microphone microphone_;

    private Corpus corpus_;

    private Iterator<WordSequence> sentenceIterator_;

}
