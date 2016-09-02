package org.pasr.gui.email.tree;


import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.apache.commons.io.FilenameUtils;
import org.pasr.prep.email.fetchers.Email;
import org.pasr.prep.email.fetchers.EmailFetcher;

import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;


public class EmailTreeView extends TreeView<Value> implements Observer {
    public void init(EmailFetcher emailFetcher){
        emailFetcher_ = emailFetcher;
        emailFetcher_.addObserver(this);

        setRoot(createFolderTreeItem("E-mails", false));

        emailFetcher.getFolderPaths().forEach(this :: addFolder);
    }

    private void addFolder(String path){
        add(FilenameUtils.getFullPathNoEndSeparator(path), createFolderTreeItem(path));
    }

    private void addEmail (Email email) {
        add(email.getPath(), new EmailTreeItem(new EmailValue(email)));
    }

    private void add (String path, TreeItem<Value> treeItem) {
        if(path == null){
            throw new IllegalArgumentException("path must not be null!");
        }

        if(treeItem == null){
            throw new IllegalArgumentException("treeItem must not be null!");
        }

        if(path.startsWith("/")){
            throw new IllegalArgumentException("path must not start with /!");
        }

        if(path.endsWith("/")){
            throw new IllegalArgumentException("path must not end with /!");
        }

        if(path.isEmpty()){
            if(treeItem.getValue().isEmail()) {
                throw new IllegalArgumentException("path must not be empty for an email!");
            }
            else{
                // Top folders will be added here
                getRoot().getChildren().add(treeItem);
                return;
            }
        }

        String[] folders = path.split("/");
        int numberOfFolders = folders.length;

        int depth = 0;
        TreeItem<Value> currentFolder = getRoot();
        while (depth < numberOfFolders) {
            TreeItem<Value> existingSubFolder = containsAsFolder(currentFolder, folders[depth]);
            if (existingSubFolder != null) {
                currentFolder = existingSubFolder;
                depth++;
            }
            else {
                break;
            }
        }
        if(depth < numberOfFolders){
            for (int i = depth; i < numberOfFolders; i++) {
                TreeItem<Value> parentFolder = createFolderTreeItem(
                    String.join("/", (CharSequence[]) Arrays.copyOfRange(folders, 0, i + 1))
                );

                currentFolder.getChildren().add(parentFolder);

                currentFolder = parentFolder;
            }

            currentFolder.getChildren().add(treeItem);
        }
        if(depth == numberOfFolders){
            if(!currentFolder.getChildren().contains(treeItem)) {
                currentFolder.getChildren().add(treeItem);
            }
        }
    }

    private TreeItem<Value> containsAsFolder (TreeItem<Value> item, String value) {
        for (TreeItem<Value> child : item.getChildren()) {
            Value childValue = child.getValue();

            if (childValue.isFolder() && childValue.toString().equals(value)) {
                return child;
            }
        }

        return null;
    }

    public Set<Email> getSelectedEmails (){
        return ((EmailTreeItem) getRoot()).getSelectedEmails();
    }

    private EmailTreeItem createFolderTreeItem(String folderPath){
        return createFolderTreeItem(folderPath, true);
    }

    private EmailTreeItem createFolderTreeItem(String folderPath, boolean canDownload){
        EmailTreeItem treeItem = new EmailTreeItem();

        FolderValue folderValue = new FolderValue(folderPath, treeItem, emailFetcher_, canDownload);

        treeItem.setValue(folderValue);

        return treeItem;
    }

    @Override
    public void update (Observable o, Object arg) {
        if(arg instanceof Email){
            addEmail((Email) arg);
        }
    }

    private EmailFetcher emailFetcher_;

}
