package org.pasr.gui.controllers;


import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import org.pasr.prep.email.EmailTree;
import org.pasr.prep.email.EmailTreeItem;
import org.pasr.prep.email.Folder;
import org.pasr.prep.email.fetchers.EmailFetcher;
import org.pasr.prep.email.fetchers.GMailFetcher;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;


public class EmailListSceneController extends Controller implements Observer{
    public EmailListSceneController(org.pasr.gui.controllers.Controller.API api) {
        super(api);

        try {
            emailFetcher_ = new GMailFetcher(((API) api_).getEmailAddress(),
                ((API) api_).getPassword());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
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
        TreeItem<String> root = EmailTreeItem.createFolder("E-mails");

        emailTree.setRoot(root);
        emailTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    // @FXML
    // private void submitButtonClicked() throws Exception{
    //     // // Create corpus.
    //     // ObservableList<TreeItem<String>> list = treeView.getSelectionModel().getSelectedItems();
    //     //
    //     // Corpus corpus = new Corpus();
    //     //
    //     // list.forEach(item -> {
    //     //     EmailTreeItem emailTreeItem = (EmailTreeItem) item;
    //     //
    //     //     if(!emailTreeItem.isFolder()){
    //     //         corpus.append(emailTreeItem.getBody());
    //     //     }
    //     // });
    //     //
    //     // corpus.process(Dictionary.getDefaultDictionary());
    //     // hasCorpus_.setCorpus(corpus);
    // }


    @Override
    public void update (Observable o, Object arg) {
        emailTree.add((Folder) arg);
    }

    public void close() throws Exception {
        emailFetcher_.close();
    }

    public interface API extends org.pasr.gui.controllers.Controller.API{
        String getEmailAddress();
        String getPassword();
    }

    @FXML
    private EmailTree emailTree;

    private EmailFetcher emailFetcher_;

}
