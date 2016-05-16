package org.pasr.gui;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.pasr.corpus.Corpus;
import org.pasr.gui.controllers.EmailListSceneController;
import org.pasr.gui.controllers.LoginSceneController;
import org.pasr.gui.controllers.LoginSceneController.Authenticator;
import org.pasr.gui.controllers.EmailListSceneController.HasCorpus;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;


public class MainView extends Application implements Authenticator, HasCorpus {

    private final Rectangle2D screenSize_ = Screen.getPrimary().getVisualBounds();

    private Stage primaryStage_;

    private EmailListSceneController emailListSceneController_;

    private Corpus corpus_;

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start (Stage primaryStage) throws IOException {
        primaryStage_ = primaryStage;

        primaryStage_.setTitle("Personalized Automatic Speech Recognition");

        FXMLLoader loginNodeLoader = new FXMLLoader(getClass().getResource("/fxml/login_scene.fxml"));
        Parent loginNode = loginNodeLoader.load();
        ((LoginSceneController)loginNodeLoader.getController()).setAuthenticator(this);

        primaryStage_.setScene(new Scene(loginNode, screenSize_.getWidth(), screenSize_.getHeight()));

        primaryStage_.show();
    }

    @Override
    public void authenticate (String username, String password) throws IOException, MessagingException {
        FXMLLoader emailListNodeLoader = new FXMLLoader(getClass().getResource("/fxml/email_list_scene.fxml"));
        emailListSceneController_ = new EmailListSceneController(username, password);
        emailListSceneController_.setHasCorpus(this);
        emailListNodeLoader.setController(emailListSceneController_);
        Parent emailListNode = emailListNodeLoader.load();

        primaryStage_.setScene(new Scene(emailListNode, screenSize_.getWidth(), screenSize_.getHeight()));
    }

    @Override
    public void setCorpus (Corpus corpus) throws IOException {
        corpus_ = corpus;

        // Create language model
        corpus_.saveToFile(new File("cmuclmtk-0.7/language_model.txt"));
        new ProcessBuilder("./generate_language_model.sh", "cmuclmtk-0.7/language_model.txt",
            "cmuclmtk-0.7/language_model.lm", "3").start();

        // Move to recording scene
    }

    @Override
    public void stop() throws Exception {
        if(emailListSceneController_ != null) {
            emailListSceneController_.close();
        }
    }
}