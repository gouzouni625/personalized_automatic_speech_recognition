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

        setRoot(createFolderTreeItem("E-mails", 0, true));

        emailFetcher.getFolderInfo().entrySet().forEach(
            entry -> addFolder(entry.getKey(), entry.getValue())
        );
    }

    private void addFolder(String path, int numberOfContainedEmails){
        add(
            FilenameUtils.getFullPathNoEndSeparator(path),
            createFolderTreeItem(path, numberOfContainedEmails)
        );
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
                // At this point, in order to add a folder of depth1, we should create a folder
                // of depth2 < depth1 but we don't known the number of contained Emails for the
                // second folder. Set the number of contained Emails to zero, for the second folder.
                TreeItem<Value> parentFolder = createFolderTreeItem(
                    String.join("/", (CharSequence[]) Arrays.copyOfRange(folders, 0, i + 1)), 0
                );

                currentFolder.getChildren().add(parentFolder);

                currentFolder = parentFolder;
            }

            currentFolder.getChildren().add(treeItem);
        }
        if(depth == numberOfFolders){
            if (! currentFolder.getChildren().contains(treeItem)) {
                currentFolder.getChildren().add(treeItem);
            }
            else{
                // Replace old folders with new ones since the new might have information regarding
                // the number of Emails that they contain. Note that, there is no need to replace
                // the TreeItem which would force us to move the children to the new TreeItem. We
                // only need to move the values.
                if(treeItem.getValue().isFolder()){
                    TreeItem<Value> oldFolder = currentFolder.getChildren().get(
                        currentFolder.getChildren().indexOf(treeItem)
                    );

                    oldFolder.setValue(treeItem.getValue());
                }
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

    private EmailTreeItem createFolderTreeItem(String folderPath, int numberOfContainedEmails){
        return createFolderTreeItem(folderPath, numberOfContainedEmails, false);
    }

    private EmailTreeItem createFolderTreeItem(String folderPath, int numberOfContainedEmails,
                                               boolean isRoot){
        EmailTreeItem treeItem = new EmailTreeItem();

        FolderValue folderValue = new FolderValue(folderPath, treeItem, emailFetcher_,
            numberOfContainedEmails, isRoot);

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
