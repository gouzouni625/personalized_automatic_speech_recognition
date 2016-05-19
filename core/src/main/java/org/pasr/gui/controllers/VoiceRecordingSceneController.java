package org.pasr.gui.controllers;


import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.pasr.corpus.Corpus;
import org.pasr.corpus.WordSequence;
import org.pasr.prep.record.Microphone;

import javax.sound.sampled.LineUnavailableException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;


// TODO add voice input visualizer
public class VoiceRecordingSceneController {
    @SuppressWarnings ("ResultOfMethodCallIgnored")
    public VoiceRecordingSceneController(Corpus corpus) throws IOException {
        corpus_ = corpus;

        sentenceIterator_ = corpus_.iterator();

        microphone_ = new Microphone();

        fileids_ = new File("acoustic_model_adaptation/records.fileids");
        if(fileids_.exists()){
            fileids_.delete();
            fileids_.createNewFile();
        }
        fileidsWriter_ = new PrintWriter(new FileOutputStream(fileids_), true);
        transcription_ = new File("acoustic_model_adaptation/records.transcription");
        if(transcription_.exists()){
            transcription_.delete();
            transcription_.createNewFile();
        }
        transcriptionWriter_ = new PrintWriter(new FileOutputStream(transcription_), true);
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

        String currentFileName = microphone_.getCurrentFileName().split("\\.")[0];

        fileidsWriter_.println(currentFileName);
        transcriptionWriter_.println("<s> " + sentenceToRead.getText() + " </s>" + " (" + currentFileName + ")");

        if(sentenceIterator_.hasNext()) {
            sentenceToRead.setText(sentenceIterator_.next().getText());
        }
    }

    @FXML
    private void doneButtonClicked() throws IOException {
        // Move to asr
        fileidsWriter_.close();
        transcriptionWriter_.close();

        new ProcessBuilder("./adapt_acoustic_model.sh").start();
    }

    private Microphone microphone_;

    private Corpus corpus_;

    private Iterator<WordSequence> sentenceIterator_;

    private File fileids_;
    private PrintWriter fileidsWriter_;
    private File transcription_;
    private PrintWriter transcriptionWriter_;

}
