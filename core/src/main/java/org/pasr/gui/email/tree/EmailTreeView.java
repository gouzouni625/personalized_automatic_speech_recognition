package org.pasr.gui.email.tree;


import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.pasr.prep.email.fetchers.Email;
import org.pasr.prep.email.fetchers.EmailFetcher;
import org.pasr.prep.email.fetchers.Folder;

import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.stream.Collectors;


public class EmailTreeView extends TreeView<Value> implements Observer {
    public void init(EmailFetcher emailFetcher){
        emailFetcher_ = emailFetcher;
        emailFetcher_.addObserver(this);

        setRoot(createFolderTreeItem("E-mails", false));
    }

    public Set<Email> getSelectedEmails (){
        return ((EmailTreeItem) getRoot()).getSelectedEmails();
    }

    private void add (String path, TreeItem<Value> treeItem) {
        if(path == null){
            throw new IllegalArgumentException("path must not be null!");
        }

        if(path.isEmpty()){
            throw new IllegalArgumentException("path must not be empty!");
        }

        if(path.startsWith("/")){
            throw new IllegalArgumentException("path must not start with /!");
        }

        if(path.endsWith("/")){
            throw new IllegalArgumentException("path must not end with /!");
        }

        if(treeItem == null){
            throw new IllegalArgumentException("treeItem must not be null!");
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
        if(depth < numberOfFolders - 1){
            for (int i = depth, n = numberOfFolders - 1; i < n; i++) {
                TreeItem<Value> parentFolder = createFolderTreeItem(
                    String.join("/", (CharSequence[]) Arrays.copyOfRange(folders, 0, i + 1))
                );

                currentFolder.getChildren().add(parentFolder);

                currentFolder = parentFolder;
            }

            currentFolder.getChildren().add(treeItem);
        }
        if(depth == numberOfFolders - 1){
            // The path is there but there is no other folder with the same name with treeItem
            currentFolder.getChildren().add(treeItem);
        }
        else if(depth == numberOfFolders){
            // There is a folder with the same path and name with treeItem
            moveChildren(treeItem, currentFolder);
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

    private void moveChildren (TreeItem<Value> from, TreeItem<Value> to) {
        ObservableList<TreeItem<Value>> destinationChildren = to.getChildren();

        destinationChildren.addAll(from.getChildren().stream()
            .filter(child -> ! destinationChildren.contains(child))
            .collect(Collectors.toList()));
    }

    public void add (String path) {
        add(path, createFolderTreeItem(path));
    }

    public void addAll(Set<String> stringSet){
        stringSet.forEach(this :: add);
    }

    public void add (Folder folder) {
        TreeItem<Value> folderTreeItem = createFolderTreeItem(folder.getPath());
        folderTreeItem.getChildren().addAll(
            folder.stream()
                .map(email -> new EmailTreeItem(new EmailValue(email)))
                .collect(Collectors.toList())
        );

        add(folder.getPath(), folderTreeItem);
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
        if(arg instanceof Folder){
            add((Folder) arg);
        }
    }

    private EmailFetcher emailFetcher_;

}
