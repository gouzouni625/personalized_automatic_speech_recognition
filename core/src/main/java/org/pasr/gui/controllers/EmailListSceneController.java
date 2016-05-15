package org.pasr.gui.controllers;


import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.pasr.prep.email.EmailFetcher;
import org.pasr.prep.email.EmailFolder;
import org.pasr.prep.email.GMailFetcher;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;


public class EmailListSceneController implements Observer{

    public EmailListSceneController(String username, String password) throws IOException, MessagingException {
        emailFetcher_ = new GMailFetcher(username, password);
        emailFetcher_.addObserver(this);
        emailFetcher_.fetch();
    }

    @FXML
    private TreeView<String> treeView;

    @FXML
    private void submitButtonClicked(){
        // Create corpus.
        ObservableList<TreeItem<String>> list = treeView.getSelectionModel().getSelectedItems();

        list.forEach(System.out:: println);
    }

    @FXML
    public void initialize() throws MessagingException, IOException {
        TreeItem<String> root = new TreeItem<>("E-mails");

        treeView.setRoot(root);
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private EmailFetcher emailFetcher_;

    @Override
    public void update (Observable o, Object arg) {
        EmailFolder emailFolder = (EmailFolder) arg;

        TreeItem<String> folderItem = new TreeItem<>(emailFolder.getName());
        for(String child : emailFolder.getEmails()){
            folderItem.getChildren().add(new TreeItem<>(child));
        }

        treeView.getRoot().getChildren().add(folderItem);
    }

}
