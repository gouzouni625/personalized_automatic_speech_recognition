package org.pasr.gui;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.pasr.corpus.Corpus;
import org.pasr.gui.controllers.ASRSceneController;
import org.pasr.gui.controllers.EmailListSceneController;
import org.pasr.gui.controllers.LoginSceneController;
import org.pasr.gui.controllers.LoginSceneController.Authenticator;
import org.pasr.gui.controllers.EmailListSceneController.HasCorpus;
import org.pasr.gui.controllers.VoiceRecordingSceneController;
import org.pasr.gui.controllers.VoiceRecordingSceneController.HasASR;
import org.pasr.postp.dictionary.Dictionary;
import org.pasr.utilities.Utilities;

import java.io.File;


public class MainView extends Application implements Authenticator, HasCorpus, HasASR {

    private final Rectangle2D screenSize_ = Screen.getPrimary().getVisualBounds();

    private Stage primaryStage_;

    private LoginSceneController loginSceneController_;
    private EmailListSceneController emailListSceneController_;
    private VoiceRecordingSceneController voiceRecordingSceneController_;
    private ASRSceneController asrSceneController_;

    private Corpus corpus_;

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start (Stage primaryStage) throws Exception {
        primaryStage_ = primaryStage;

        primaryStage_.setTitle("Personalized Automatic Speech Recognition");

        FXMLLoader loginNodeLoader = new FXMLLoader(getClass().getResource("/fxml/login_scene.fxml"));
        loginSceneController_ = new LoginSceneController(this);
        loginNodeLoader.setController(loginSceneController_);
        Parent loginNode = loginNodeLoader.load();

        primaryStage_.setScene(new Scene(loginNode, screenSize_.getWidth(), screenSize_.getHeight()));

        primaryStage_.show();
    }

    @Override
    public void authenticate (String username, String password) throws Exception {
        FXMLLoader emailListNodeLoader = new FXMLLoader(getClass().getResource("/fxml/email_list_scene.fxml"));
        emailListSceneController_ = new EmailListSceneController(this, username, password);
        emailListNodeLoader.setController(emailListSceneController_);
        Parent emailListNode = emailListNodeLoader.load();

        primaryStage_.setScene(new Scene(emailListNode, screenSize_.getWidth(), screenSize_.getHeight()));
    }

    @Override
    public void setCorpus (Corpus corpus) throws Exception {
        corpus_ = corpus;

        // Stop the email fetcher from fetching more email
        if(emailListSceneController_ != null) {
            emailListSceneController_.close();
        }

        // Create language model
        corpus_.saveToFile(new File("cmuclmtk-0.7/language_model.txt"));
        new ProcessBuilder("./generate_language_model.sh", "language_model.txt",
            "language_model.lm", "3").start();

        // Move to recording scene
        FXMLLoader voiceRecordingNodeLoader = new FXMLLoader(getClass().getResource("/fxml/voice_recording_scene.fxml"));
        voiceRecordingSceneController_ = new VoiceRecordingSceneController(this, corpus_);
        voiceRecordingNodeLoader.setController(voiceRecordingSceneController_);
        Parent voiceRecordingNode = voiceRecordingNodeLoader.load();

        primaryStage_.setScene(new Scene(voiceRecordingNode, screenSize_.getWidth(), screenSize_.getHeight()));
    }

    @Override
    public void stop() throws Exception {
        if(emailListSceneController_ != null) {
            emailListSceneController_.close();
        }
    }

    @Override
    public void startASR () throws Exception {
        FXMLLoader asrNodeLoader = new FXMLLoader(getClass().getResource("/fxml/asr_scene.fxml"));
        asrSceneController_ = new ASRSceneController(corpus_, Dictionary.createFromInputStream(Utilities.getResourceStream("/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict")));
        asrNodeLoader.setController(asrSceneController_);
        primaryStage_.setScene(new Scene(asrNodeLoader.load(), screenSize_.getWidth(), screenSize_.getHeight()));
    }

}
