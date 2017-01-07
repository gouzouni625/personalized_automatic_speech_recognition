package org.pasr.gui.email.tree;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.apache.commons.io.FilenameUtils;
import org.pasr.gui.console.Console;
import org.pasr.utilities.email.fetchers.EmailFetcher;

import java.io.IOException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import static org.pasr.utilities.Utilities.getResource;
import static org.pasr.utilities.Utilities.getResourceStream;


/**
 * @class FolderValue
 * @brief Implements a Value as a folder
 *        The user can select a folder (selecting all its containing folders and e-mails) or
 *        download it if it hasn't been downloaded yet.
 */
class FolderValue extends AnchorPane implements Value, Observer {

    /**
     * @brief Constructor
     *
     * @param path
     *     The folder path inside the e-mail tree
     * @param emailTreeItem
     *     The EmailTreeItem holding this FolderValue
     * @param hasEmailFetcher
     *     An implementation of the HasEmailFetcher interface to trigger downloading of this folder
     * @param numberOfContainedEmails
     *     The number of e-mails contained by this folder
     * @param isRoot
     *     Whether this is the root folder of the e-mail tree
     */
    FolderValue (String path, EmailTreeItem emailTreeItem, HasEmailFetcher hasEmailFetcher,
                 int numberOfContainedEmails, boolean isRoot) {

        path_ = path;
        emailTreeItem_ = emailTreeItem;
        hasEmailFetcher_ = hasEmailFetcher;
        numberOfContainedEmails_ = numberOfContainedEmails;

        name_ = FilenameUtils.getName(path_);

        if (isRoot || numberOfContainedEmails == 0) {
            currentState_ = State.DISABLED;
        }
        else {
            currentState_ = State.IDLE;

            emailTreeItem_.getChildren().addListener(
                (ListChangeListener<TreeItem<Value>>) c -> emailTreeItem_.expandUpToRoot()
            );

            hasEmailFetcher_.addObserver(this);
        }

        try {
            URL location = getResource("/fxml/tree/folder_value.fxml");

            if (location == null) {
                throw new IOException(
                    "getResource(\"/fxml/tree/folder_value.fxml\") returned null"
                );
            }

            FXMLLoader loader = new FXMLLoader(location);
            loader.setRoot(this);
            loader.setController(this);

            loader.load();
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).severe(
                "Could not load resource:/fxml/tree/folder_value.fxml\n" +
                    "The file might be missing or be corrupted.\n" +
                    "Application will terminate.\n" +
                    "Exception Message: " + e.getMessage());
            Platform.exit();
        }
    }

    @FXML
    public void initialize () {
        nameLabel.setText(name_);

        // currentState_ value has been defined in the constructor, but now it is the time to update
        // the FXML objects
        setState(currentState_);

        if (currentState_ != State.DISABLED) {
            informationLabel.setText("0/" + numberOfContainedEmails_);

            emailTreeItem_.getChildren().addListener(new ListChangeListener<TreeItem<Value>>() {
                @Override
                public void onChanged (Change<? extends TreeItem<Value>> c) {
                    long numberOfDownloadedEmails = c.getList().stream()
                        .map(TreeItem:: getValue)
                        .filter(Value:: isEmail)
                        .count();

                    Platform.runLater(() -> {
                        informationLabel.setText(
                            numberOfDownloadedEmails + "/" + numberOfContainedEmails_
                        );

                        if (numberOfDownloadedEmails == numberOfContainedEmails_) {
                            setState(State.DOWNLOADED);
                        }
                    });
                }
            });

            toggleButton.setOnAction(this :: toggleButtonOnAction);
            toggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
                toggleButton.setGraphic(
                    newValue ? toggleButtonPressedGraphic : toggleButtonDefaultGraphic
                );
            });
        }

        checkBox.setOnAction(this :: checkBoxOnAction);
    }

    private void toggleButtonOnAction (ActionEvent actionEvent) {
        if (toggleButton.isSelected()) {
            try {
                hasEmailFetcher_.fetch(path_);

                setState(State.DOWNLOADING);
            } catch (IllegalStateException e) {
                Console.getInstance().postMessage("Could not start fetching emails. Please try" +
                    " again in a few moments.");
            }
        }
        else {
            hasEmailFetcher_.stop();
            if (currentState_ == State.DOWNLOADING) {
                setState(State.IDLE);
            }
        }
    }

    private void checkBoxOnAction (ActionEvent actionEvent) {
        emailTreeItem_.setSelectedDownToLeaves(checkBox.isSelected());
    }

    @Override
    public boolean isSelected () {
        return checkBox.isSelected();
    }

    @Override
    public void setSelected (boolean selected) {
        if (! checkBox.isDisabled()) {
            checkBox.setSelected(selected);

            emailTreeItem_.setSelectedDownToLeaves(selected);
        }
    }

    @Override
    public boolean isEmail () {
        return false;
    }

    @Override
    public boolean isFolder () {
        return true;
    }

    @Override
    public String toString () {
        return name_;
    }

    private void setState (State state) {
        switch (state) {
            case IDLE:
                disableSelection();
                showInformation();
                enableButton();
                showButton();
                hideProgressIndicator();
                break;
            case DOWNLOADING:
                disableSelection();
                showInformation();
                enableButton();
                showButton();
                showProgressIndicator();
                break;
            case BLOCKED:
                if (currentState_ == State.IDLE) {
                    disableSelection();
                }
                else {
                    enableSelection();
                }
                showInformation();
                disableButton();
                showButton();
                hideProgressIndicator();
                break;
            case DOWNLOADED:
                enableSelection();
                showInformation();
                disableButton();
                hideButton();
                hideProgressIndicator();
                break;
            case DISABLED:
                enableSelection();
                hideInformation();
                disableButton();
                hideButton();
                hideProgressIndicator();
                break;
        }

        currentState_ = state;
    }

    private void enableSelection () {
        if (checkBox.isDisabled()) {
            checkBox.setDisable(false);
        }
    }

    private void disableSelection () {
        if (! checkBox.isDisabled()) {
            checkBox.setDisable(true);
        }
    }

    private void showInformation () {
        if (! informationLabel.isVisible()) {
            informationLabel.setVisible(true);
        }
    }

    private void hideInformation () {
        if (informationLabel.isVisible()) {
            informationLabel.setVisible(false);
        }
    }

    private void enableButton () {
        if (toggleButton.isDisabled()) {
            toggleButton.setDisable(false);
        }
    }

    private void disableButton () {
        if (! toggleButton.isDisabled()) {
            toggleButton.setDisable(true);
        }
    }

    private void showButton () {
        if (! toggleButton.isVisible()) {
            toggleButton.setVisible(true);
        }
    }

    private void hideButton () {
        if (toggleButton.isVisible()) {
            toggleButton.setVisible(false);
        }
    }

    private void showProgressIndicator () {
        if (! progressIndicator.isVisible()) {
            progressIndicator.setVisible(true);
        }
    }

    private void hideProgressIndicator () {
        if (progressIndicator.isVisible()) {
            progressIndicator.setVisible(false);
        }
    }

    @Override
    public void update (Observable o, Object arg) {
        if (arg instanceof EmailFetcher.Stage) {
            switch ((EmailFetcher.Stage) arg) {
                case STARTED_FETCHING:

                    switch (currentState_) {
                        case IDLE:
                            setState(State.BLOCKED);
                            break;
                        case DOWNLOADING:
                        case BLOCKED:
                        case DOWNLOADED:
                        case DISABLED:
                            break;
                    }

                    break;
                case STOPPED_FETCHING:

                    switch (currentState_) {
                        case IDLE:
                            break;
                        case DOWNLOADING:
                        case BLOCKED:
                            setState(State.IDLE);
                            break;
                        case DOWNLOADED:
                        case DISABLED:
                            break;
                    }

                    if (toggleButton.isSelected()) {
                        Platform.runLater(() -> toggleButton.fire());
                    }

                    break;
            }
        }
    }

    /**
     * @class State
     * @brief Holds the different states of a folder
     */
    public enum State {
        IDLE, //!< Waiting for an action
        DOWNLOADING, //!< Downloading the contents of this folder
        BLOCKED, //!< Blocked because another folder is downloading
        DOWNLOADED, //!< The contents of this folder have been downloaded
        DISABLED //!< If this is the root folder of the e-mail tree
                 //!< or contains no e-mails or folders
    }

    @FXML
    private CheckBox checkBox;

    @FXML
    private Label nameLabel;

    @FXML
    private Label informationLabel;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private ToggleButton toggleButton;
    private final Node toggleButtonDefaultGraphic = new ImageView(
        new Image(getResourceStream("/icons/download_black.png")));
    private final Node toggleButtonPressedGraphic = new ImageView(
        new Image(getResourceStream("/icons/download_green.png")));

    private String path_;
    private String name_;

    private int numberOfContainedEmails_;

    private HasEmailFetcher hasEmailFetcher_;

    private EmailTreeItem emailTreeItem_;

    private State currentState_;

}
