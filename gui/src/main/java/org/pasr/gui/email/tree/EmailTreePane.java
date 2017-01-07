package org.pasr.gui.email.tree;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import org.apache.commons.io.FilenameUtils;
import org.pasr.gui.nodes.IntegerField;
import org.pasr.utilities.email.fetchers.Email;
import org.pasr.utilities.email.fetchers.EmailFetcher;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.logging.Logger;

import static org.pasr.utilities.Utilities.getResource;


/**
 * @class EmailTreePane
 * @brief Implements an e-mail tree
 *        It wraps a JavaFX TreeView which hosts TreeItem objects as children. The TreeItem is
 *        implemented as an EmailTreeItem with a value of type Value. The value can be either a
 *        FolderValue or an EmailValue each with different visualization and functionality.
 */
public class EmailTreePane extends AnchorPane implements Observer, HasEmailFetcher {

    /**
     * @brief Default Constructor
     */
    public EmailTreePane () {
        try {
            URL location = getResource("/fxml/tree/pane.fxml");

            if (location == null) {
                throw new IOException("getResource(\"/fxml/tree/pane.fxml\") returned null");
            }

            FXMLLoader loader = new FXMLLoader(location);
            loader.setRoot(this);
            loader.setController(this);

            loader.load();

        } catch (IOException e) {
            Logger.getLogger(getClass().getName())
                .severe("Could not load resource:/fxml/corpus/pane.fxml\n" +
                    "The file might be missing or be corrupted.\n" +
                    "Application will terminate.\n" +
                    "Exception Message: " + e.getMessage());
            Platform.exit();
        }
    }

    @FXML
    public void initialize () {
        integerField.setMinValue(1);
        integerField.updateFilter();
    }

    public void addSelectionListener (ChangeListener<TreeItem<Value>> changeListener) {
        if (changeListener != null) {
            treeView.getSelectionModel().selectedItemProperty().addListener(changeListener);
        }
    }

    /**
     * @brief Returns the value of the field with the number of e-mails to fetch
     *
     * @return The value of the field with the number of e-mails to fetch
     */
    public int getFieldValue () {
        return integerField.getValue();
    }

    public Set<Email> getSelectedEmails () {
        return ((EmailTreeItem) treeView.getRoot()).getSelectedEmails();
    }

    public void init (EmailFetcher emailFetcher) {
        emailFetcher_ = emailFetcher;
        emailFetcher_.addObserver(this);

        treeView.setRoot(createFolderTreeItem("E-mails", 0, true));

        emailFetcher.getFolderInfo().entrySet().forEach(
            entry -> addFolder(entry.getKey(), entry.getValue())
        );
    }

    private void addFolder (String path, int numberOfContainedEmails) {
        add(
            FilenameUtils.getFullPathNoEndSeparator(path),
            createFolderTreeItem(path, numberOfContainedEmails)
        );
    }

    private void addEmail (Email email) {
        add(email.getPath(), new EmailTreeItem(new EmailValue(email)));
    }

    private void add (String path, TreeItem<Value> treeItem) {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null!");
        }

        if (treeItem == null) {
            throw new IllegalArgumentException("treeItem must not be null!");
        }

        if (path.startsWith("/")) {
            throw new IllegalArgumentException("path must not start with /!");
        }

        if (path.endsWith("/")) {
            throw new IllegalArgumentException("path must not end with /!");
        }

        if (path.isEmpty()) {
            if (treeItem.getValue().isEmail()) {
                throw new IllegalArgumentException("path must not be empty for an email!");
            }
            else {
                // Top folders will be added here
                ObservableList<TreeItem<Value>> rootChildren = treeView.getRoot().getChildren();
                if (! rootChildren.contains(treeItem)) {
                    rootChildren.add(treeItem);
                }
                else {
                    TreeItem<Value> existingChild = rootChildren.get(
                        rootChildren.indexOf(treeItem)
                    );

                    // No need to copy the children of treeItem to existingChild since we are
                    // at folder state and e-mails will start arriving later.
                    existingChild.setValue(treeItem.getValue());
                }

                return;
            }
        }

        String[] folders = path.split("/");
        int numberOfFolders = folders.length;

        int depth = 0;
        TreeItem<Value> currentFolder = treeView.getRoot();
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
        if (depth < numberOfFolders) {
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
        else if (depth == numberOfFolders) {
            ObservableList<TreeItem<Value>> currentFolderChildren = currentFolder.getChildren();

            if (! currentFolderChildren.contains(treeItem)) {
                currentFolderChildren.add(treeItem);
            }
            else {
                // Replace old folders with new ones since the new might have information regarding
                // the number of Emails that they contain. Note that, there is no need to replace
                // the TreeItem which would force us to move the children to the new TreeItem. We
                // only need to move the values.
                // No need to check if the existing child is a Folder since to match treeItem it
                // has to be a folder.
                TreeItem<Value> oldFolder = currentFolderChildren.get(
                    currentFolderChildren.indexOf(treeItem)
                );

                oldFolder.setValue(treeItem.getValue());
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

    private EmailTreeItem createFolderTreeItem (String folderPath, int numberOfContainedEmails) {
        return createFolderTreeItem(folderPath, numberOfContainedEmails, false);
    }

    private EmailTreeItem createFolderTreeItem (String folderPath, int numberOfContainedEmails,
                                                boolean isRoot) {
        EmailTreeItem treeItem = new EmailTreeItem();

        FolderValue folderValue = new FolderValue(folderPath, treeItem, this,
            numberOfContainedEmails, isRoot);

        treeItem.setValue(folderValue);

        return treeItem;
    }

    @Override
    public void fetch (String path) {
        emailFetcher_.fetch(path, integerField.getValue());
    }

    @Override
    public void stop () {
        emailFetcher_.stop();
    }

    @Override
    public void addObserver (Observer observer) {
        emailFetcher_.addObserver(observer);
    }

    @Override
    public void update (Observable o, Object arg) {
        if (arg instanceof Email) {
            addEmail((Email) arg);
        }
        else if (arg instanceof EmailFetcher.Stage) {
            switch ((EmailFetcher.Stage) arg) {
                case STARTED_FETCHING:
                    progressIndicator.setVisible(true);
                    break;
                case STOPPED_FETCHING:
                    progressIndicator.setVisible(false);
                    break;
            }
        }
    }

    @FXML
    private TreeView<Value> treeView;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private IntegerField integerField;

    private EmailFetcher emailFetcher_;

}
