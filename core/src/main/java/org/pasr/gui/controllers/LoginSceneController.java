package org.pasr.gui.controllers;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;


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
    private void submitButtonClicked() throws Exception {
        authenticator_.authenticate(emailAddress.getText(), password.getText());
    }

    public interface Authenticator{
        void authenticate(String username, String password) throws Exception;
    }

    public void setAuthenticator(Authenticator authenticator){
        authenticator_ = authenticator;
    }

    private Authenticator authenticator_;

}
