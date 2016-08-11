package org.pasr.gui.controllers.scene;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import org.pasr.gui.console.Console;
import org.pasr.gui.corpus.CorpusPane;


public class MainController extends Controller{
    public MainController (Controller.API api){
        super(api);
    }

    @FXML
    public void initialize() {
        corpusPane.addSelectionListener((observable, oldValue, newValue) -> {
            if(newValue == null){
                dictateButtonAccessHandling(false);
            }
            else{
                dictateButtonAccessHandling(true);
            }
        });
        dictateButtonAccessHandling(false);

        String emailAddress = ((API) api_).getEmailAddress();
        String password = ((API) api_).getPassword();

        if(emailAddress != null && password != null){
            emailAddressTextField.setText(emailAddress);
            passwordField.setText(password);
        }

        // At the moment initialize is called the views are not yet ready to handle focus so
        // runLater is used
        Platform.runLater(() -> emailAddressTextField.requestFocus());

        newCorpusButton.setTooltip(new Tooltip(NewCorpusButtonMessages.TOOLTIP.getMessage()));
        newCorpusButton.setOnAction(this :: newCorpusButtonOnAction);

        dictateButton.setOnAction(this :: dictateButtonOnAction);
    }

    private void dictateButtonAccessHandling (boolean corpusSelected){
        if(corpusSelected){
            dictateButton.setDisable(false);
            dictateButton.setTooltip(new Tooltip(DictateButtonMessages.ENABLED.getMessage()));
            dictateLabel.setVisible(false);
        }
        else{
            dictateButton.setDisable(true);
            dictateLabel.setVisible(true);
            dictateLabel.setText(DictateButtonMessages.DISABLED.getMessage());
        }
    }

    private void newCorpusButtonOnAction (ActionEvent actionEvent){
        String emailAddressText = emailAddressTextField.getText();
        String passwordText = passwordField.getText();

        Console console = Console.getInstance();

        if(emailAddressText.isEmpty() && passwordText.isEmpty()){
            console.postMessage(NewCorpusButtonMessages.NO_INPUT.getMessage());
        }
        else if(emailAddressText.isEmpty()){
            console.postMessage(NewCorpusButtonMessages.NO_EMAIL_ADDRESS.getMessage());
        }
        else if(passwordText.isEmpty()){
            console.postMessage(NewCorpusButtonMessages.NO_PASSWORD.getMessage());
        }
        else{
            ((API) api_).newCorpus(emailAddressText, passwordText);
        }
    }

    private void dictateButtonOnAction (ActionEvent actionEvent){
        // There is no need to check if the provided id is valid since dictateButton can be fired
        // only when a valid corpus is chosen
        ((API) api_).dictate(corpusPane.getSelectedCorpusID());
    }

    public interface API extends Controller.API{
        String getEmailAddress();
        String getPassword();
        void newCorpus(String emailAddress, String password);
        void dictate(int corpusID);
    }

    @FXML
    private CorpusPane corpusPane;

    @FXML
    private TextField emailAddressTextField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button newCorpusButton;
    private enum NewCorpusButtonMessages{
        TOOLTIP("Enter the address and the password of your e-mail to fetch your e-mails" +
            " and create a corpus."),
        NO_INPUT("Please enter the address and the password of your e-mail!"),
        NO_EMAIL_ADDRESS("Please enter the address of your e-mail!"),
        NO_PASSWORD("Please enter the password of your e-mail!");

        NewCorpusButtonMessages(String message){
            message_ = message;
        }

        public String getMessage(){
            return message_;
        }

        private String message_;
    }

    @FXML
    private Button dictateButton;

    @FXML
    private Label dictateLabel;
    private enum DictateButtonMessages{
        DISABLED("Note: You must create and select a corpus in order to dictate."),
        ENABLED("Use the chosen corpus for speech recognition.");

        DictateButtonMessages(String message){
            message_ = message;
        }

        public String getMessage(){
            return message_;
        }

        private String message_;
    }

}
