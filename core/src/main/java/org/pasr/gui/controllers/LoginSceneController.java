package org.pasr.gui.controllers;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import javax.mail.MessagingException;
import java.io.IOException;


public class LoginSceneController {

    @FXML
    private Button submitButton;

    @FXML
    private TextField emailAddress;

    @FXML
    private PasswordField password;

    @FXML
    public void initialize(){
        // At the moment initialize is called, the views are not yet ready to handle focus so
        // runLater is used.
        Platform.runLater(() -> submitButton.requestFocus());
    }

    @FXML
    private void submitButtonClicked() throws IOException, MessagingException {
        authenticator_.authenticate(emailAddress.getText(), password.getText());
    }

    public interface Authenticator{
        void authenticate(String username, String password) throws IOException, MessagingException;
    }

    public void setAuthenticator(Authenticator authenticator){
        authenticator_ = authenticator;
    }

    private Authenticator authenticator_;

}
