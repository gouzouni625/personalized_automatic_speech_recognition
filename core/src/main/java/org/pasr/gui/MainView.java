package org.pasr.gui;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.pasr.gui.controllers.EmailListSceneController;
import org.pasr.gui.controllers.LoginSceneController;
import org.pasr.gui.controllers.LoginSceneController.Authenticator;

import javax.mail.MessagingException;
import java.io.IOException;


public class MainView extends Application implements Authenticator{

    private final Rectangle2D screenSize_ = Screen.getPrimary().getVisualBounds();

    private Stage primaryStage_;

    private EmailListSceneController emailListSceneController_;

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start (Stage primaryStage) throws Exception {
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
        emailListNodeLoader.setController(emailListSceneController_);
        Parent emailListNode = emailListNodeLoader.load();

        primaryStage_.setScene(new Scene(emailListNode, screenSize_.getWidth(), screenSize_.getHeight()));
    }

    @Override
    public void stop() throws MessagingException {
        emailListSceneController_.close();
    }
}
