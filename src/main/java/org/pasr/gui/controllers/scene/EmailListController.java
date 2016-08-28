package org.pasr.gui.controllers.scene;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import org.pasr.gui.console.Console;
import org.pasr.gui.email.tree.EmailTreeView;
import org.pasr.gui.email.tree.EmailValue;
import org.pasr.gui.email.tree.Value;
import org.pasr.prep.email.fetchers.Email;
import org.pasr.prep.email.fetchers.EmailFetcher;

import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;


public class EmailListController extends Controller implements Observer{
    public EmailListController (Controller.API api) {
        super(api);
    }

    @FXML
    public void initialize() {
        EmailFetcher emailFetcher = ((API) api_).getEmailFetcher();
        emailFetcher.addObserver(this);

        emailTreeView.init(emailFetcher);
        emailTreeView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    Value value = newValue.getValue();

                    if(value.isEmail()) {
                        updateSubjectTextArea(((EmailValue)value).getEmail());
                        updateBodyTextArea(((EmailValue)value).getEmail());
                    }
                }
            }
        );
        emailTreeView.addAll(emailFetcher.getFolderPaths());

        backButton.setOnAction(this :: backButtonOnAction);
        doneButton.setOnAction(this :: doneButtonOnAction);

        emailFetcher.fetch();
    }

    private void updateSubjectTextArea(Email email){
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Subject: ").append(email.getSubject()).append("\n");

        String[] senders = email.getSenders();
        if(senders.length != 0){
            stringBuilder.append("From: ")
                .append(String.join(", ", (CharSequence[]) senders))
                .append("\n");
        }

        String[] tORecipients = email.getRecipients(Email.RecipientType.TO);
        if(tORecipients.length != 0){
            stringBuilder.append("To: ")
                .append(String.join(", ", (CharSequence[]) tORecipients))
                .append("\n");
        }

        String[] cCRecipients = email.getRecipients(Email.RecipientType.CC);
        if(cCRecipients.length != 0){
            stringBuilder.append("CC: ")
                .append(String.join(", ", (CharSequence[]) cCRecipients))
                .append("\n");
        }

        String[] bCCRecipients = email.getRecipients(Email.RecipientType.BCC);
        if(bCCRecipients.length != 0){
            stringBuilder.append("BCC: ")
                .append(String.join(", ", (CharSequence[]) bCCRecipients))
                .append("\n");
        }

        stringBuilder.append("On: ").append(new Date(email.getReceivedDate())).append("\n");

        subjectTextArea.setText(stringBuilder.toString());
    }

    private void updateBodyTextArea(Email email){
        bodyTextArea.setText(email.getBody());
    }

    private void backButtonOnAction(ActionEvent actionEvent){
        ((API) api_).initialScene();
    }

    private void doneButtonOnAction(ActionEvent actionEvent){
        Set<Email> selectedEmailSet = emailTreeView.getSelectedEmails();

        if(selectedEmailSet.size() > 0) {
            ((API) api_).processEmail(selectedEmailSet);
        }
        else{
            Console.getInstance().postMessage("You must choose at least one e-mail before moving" +
                " on!");
        }
    }

    @Override
    public void update (Observable o, Object arg) {
        if(arg instanceof EmailFetcher.Stage){
            switch ((EmailFetcher.Stage) arg){
                case STARTED_FETCHING:
                    progressIndicator.setVisible(true);
                    break;
                case STOPPED_FETCHING:
                    progressIndicator.setVisible(false);
                    break;
            }
        }
    }

    public interface API extends Controller.API{
        EmailFetcher getEmailFetcher();
        void initialScene();
        void processEmail(Set<Email> emailList);
    }

    @FXML
    private EmailTreeView emailTreeView;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private TextArea subjectTextArea;

    @FXML
    private TextArea bodyTextArea;

    @FXML
    private Button backButton;

    @FXML
    private Button doneButton;

}
