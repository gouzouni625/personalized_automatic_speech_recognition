package org.pasr.gui.controllers;


import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.pasr.prep.email.EmailFetcher.RecentFolder.Email;
import org.pasr.prep.email.EmailFetcher.RecentFolder;
import org.pasr.prep.email.EmailFetcher;
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

        list.forEach(item -> {
            System.out.println(((EmailTreeItem)(item)).isFolder());
        });
    }

    @FXML
    public void initialize() throws MessagingException, IOException {
        TreeItem<String> root = new TreeItem<>("E-mails");

        treeView.setRoot(root);
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private EmailFetcher emailFetcher_;

    @SuppressWarnings ("SuspiciousMethodCalls")
    @Override
    public void update (Observable o, Object arg) {
        RecentFolder recentFolder = (RecentFolder) arg;

        String[] folders = recentFolder.getPath().split("/");
        int numberOfFolders = folders.length;

        EmailTreeItem newFolder = EmailTreeItem.createFolder(folders[numberOfFolders - 1]);
        for(Email email : recentFolder.getEmails()){
            newFolder.getChildren().add(EmailTreeItem.createEmail(email.getSubject(), email.getBody()));
        }

        int depth = 0;
        TreeItem<String> currentFolder = treeView.getRoot();
        while(depth < numberOfFolders){
            TreeItem<String> existingSubFolder = containsAsFolder(currentFolder, folders[depth]);
            if (existingSubFolder != null){
                currentFolder = existingSubFolder;
                depth++;

                if(depth == numberOfFolders - 1){
                    currentFolder.getChildren().add(newFolder);
                    break;
                }
            }
            else{
                break;
            }
        }
        if(depth < numberOfFolders - 1 || depth == 0){
            for(int i = depth, n = numberOfFolders - 1;i < n;i++){
                EmailTreeItem parentFolder = EmailTreeItem.createFolder(folders[i]);
                currentFolder.getChildren().add(parentFolder);

                currentFolder = parentFolder;
            }

            currentFolder.getChildren().add(newFolder);
        }
    }

    private TreeItem<String> containsAsFolder(TreeItem<String> item, String value){
        for(TreeItem<String> child : item.getChildren()){
            if(child.getValue().equals(value) &&
                ((EmailTreeItem)(child)).isFolder()){
                return child;
            }
        }

        return null;
    }

    public void close() throws Exception {
        emailFetcher_.close();
    }

    private static class EmailTreeItem extends TreeItem<String>{
        private EmailTreeItem(String name){
            super(name);

            isFolder_ = true;

            name_ = name;
            subject_ = null;
            body_ = null;
        }

        private EmailTreeItem(String subject, String body){
            super(subject);

            isFolder_ = false;

            name_ = null;

            subject_ = subject;
            body_ = body;
        }

        static EmailTreeItem createFolder(String name){
            return new EmailTreeItem(name);
        }

        static EmailTreeItem createEmail(String subject, String body){
            return new EmailTreeItem(subject, body);
        }

        @Override
        public String toString(){
            if(isFolder()){
                return name_;
            }
            else{
                return subject_;
            }
        }

        boolean isFolder(){
            return isFolder_;
        }

        String getName(){
            return name_;
        }

        String getSubject(){
            return subject_;
        }

        String getBody(){
            return body_;
        }

        private final String name_;

        private final String subject_;
        private final String body_;

        private final boolean isFolder_;

    }

}
