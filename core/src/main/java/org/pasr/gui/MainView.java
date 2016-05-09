package org.pasr.gui;


import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.pasr.gui.controllers.LoginSceneController;
import org.pasr.gui.controllers.LoginSceneController.Authenticator;
import org.pasr.prep.email.EmailFetcher;
import org.pasr.prep.email.GMailFetcher;

import javax.mail.MessagingException;
import java.io.IOException;


public class MainView extends Application implements Authenticator{

    private final Rectangle2D screenSize_ = Screen.getPrimary().getVisualBounds();

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start (Stage primaryStage) throws Exception {
        primaryStage.setTitle("Personalized Automatic Speech Recognition");

        FXMLLoader loginNodeLoader = new FXMLLoader(getClass().getResource("/fxml/login_scene.fxml"));
        Parent loginNode = loginNodeLoader.load();
        ((LoginSceneController)loginNodeLoader.getController()).setAuthenticator(this);

        primaryStage.setScene(new Scene(loginNode, screenSize_.getWidth(), screenSize_.getHeight()));

        primaryStage.show();
    }

    @Override
    public void authenticate (String username, String password) {
        System.out.println(username);
        System.out.println(password);
    }
}
