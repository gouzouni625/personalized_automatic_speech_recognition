package org.pasr.gui.controllers.scene;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import org.pasr.gui.email.tree.EmailTree;
import org.pasr.gui.email.tree.EmailTreeItem;
import org.pasr.prep.email.fetchers.Email;
import org.pasr.prep.email.fetchers.Folder;
import org.pasr.prep.email.fetchers.EmailFetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


public class EmailListController extends Controller implements Observer{
    public EmailListController (Controller.API api) {
        super(api);
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

        // Start fetching the messages only after all the views have been initialized
        EmailFetcher emailFetcher = ((API) api_).getEmailFetcher();
        emailFetcher.addObserver(this);
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

        stringBuilder.append("On: ").append(email.getReceivedDate()).append("\n");

        subjectTextArea.setText(stringBuilder.toString());
    }

    private void updateBodyTextArea(Email email){
        bodyTextArea.setText(email.getBody());
    }

    private void backButtonOnAction(ActionEvent actionEvent){
        ((API) api_).initialScene();
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
        if(arg == null){
            // This means that the email fetcher has finished fetching
            progressIndicator.setVisible(false);
        }
        else {
            emailTree.add((Folder) arg);
        }
    }

    public interface API extends Controller.API{
        EmailFetcher getEmailFetcher();
        void initialScene();
        void processEmail(List<Email> corpus);
    }

    @FXML
    private EmailTree emailTree;

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
