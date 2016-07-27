package org.pasr.gui.controllers;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import org.pasr.gui.email.tree.EmailTree;
import org.pasr.gui.email.tree.EmailTreeItem;
import org.pasr.prep.email.fetchers.Email;
import org.pasr.prep.email.fetchers.Folder;
import org.pasr.prep.email.fetchers.EmailFetcher;
import org.pasr.prep.email.fetchers.GMailFetcher;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


public class EmailListSceneController extends Controller implements Observer{
    public EmailListSceneController(org.pasr.gui.controllers.Controller.API api) {
        super(api);

        try {
            emailFetcher_ = new GMailFetcher(((API) api_).getEmailAddress(),
                ((API) api_).getPassword());
        } catch (IOException e) {
            // TODO Act appropriately
            e.printStackTrace();
        } catch (MessagingException e) {
            // TODO Act appropriately
            e.printStackTrace();
        }

        emailFetcher_.addObserver(this);

        try {
            emailFetcher_.fetch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        EmailTreeItem root = new EmailTreeItem(new Folder("/E-mails", new ArrayList<>()));

        emailTree.setRoot(root);
        emailTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        emailTree.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                EmailTreeItem selectedEmailTreeItem = (EmailTreeItem) observable.getValue();

                if (selectedEmailTreeItem != null && !selectedEmailTreeItem.isFolder()) {
                    updateSubjectTextArea(selectedEmailTreeItem.getEmail());
                    updateBodyTextArea(selectedEmailTreeItem.getEmail());

                }
            }
        );

        backButton.setOnAction(this :: backButtonOnAction);
        doneButton.setOnAction(this :: doneButtonOnAction);
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

        stringBuilder.append("On: ").append(email.getReceivedDate()).append("\n");

        subjectTextArea.setText(stringBuilder.toString());
    }

    private void updateBodyTextArea(Email email){
        bodyTextArea.setText(email.getBody());
    }

    private void backButtonOnAction(ActionEvent actionEvent){
        ((API) api_).back();
    }

    private void doneButtonOnAction(ActionEvent actionEvent){
        ((API) api_).processEmail(getChosenEmails());
    }

    private List<Email> getChosenEmails () {
        ArrayList<Email> emails = new ArrayList<>();

        for(TreeItem<String> treeItem : emailTree.getSelectionModel().getSelectedItems()){
            EmailTreeItem emailTreeItem = (EmailTreeItem) treeItem;

            emails.addAll(emailTreeItem.getEmails());
        }

        return emails;
    }

    @Override
    public void update (Observable o, Object arg) {
        emailTree.add((Folder) arg);
    }

    @Override
    public void terminate() throws Exception {
        emailFetcher_.close();
    }

    public interface API extends org.pasr.gui.controllers.Controller.API{
        String getEmailAddress();
        String getPassword();
        void back();
        void processEmail(List<Email> corpus);
    }

    @FXML
    private EmailTree emailTree;

    @FXML
    private TextArea subjectTextArea;

    @FXML
    private TextArea bodyTextArea;

    @FXML
    private Button backButton;

    @FXML
    private Button doneButton;

    private EmailFetcher emailFetcher_;

}
