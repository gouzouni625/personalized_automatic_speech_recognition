package org.pasr.gui.email.tree;


import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;
import org.pasr.prep.email.fetchers.EmailFetcher;

import java.io.IOException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import static org.pasr.utilities.Utilities.getResource;


class FolderValue extends AnchorPane implements Value, Observer {
    FolderValue(String path, EmailTreeItem emailTreeItem, EmailFetcher emailFetcher,
                boolean canDownload){

        path_ = path;
        String[] tokens = path.split("/");
        name_ = tokens[tokens.length - 1];

        emailTreeItem_ = emailTreeItem;
        emailTreeItem_.getChildren().addListener((ListChangeListener<TreeItem<Value>>) c -> {
            if (checkBox.isDisabled()) {
                if (c.getList().stream()
                    .map(TreeItem:: getValue)
                    .filter(Value:: isEmail)
                    .count() > 0) {
                    enableSelection();
                }
            }

            emailTreeItem_.expandUpToRoot();
        });

        emailFetcher_ = emailFetcher;
        emailFetcher_.addObserver(this);

        canDownload_ = canDownload;

        try{
            URL location = getResource("/fxml/tree/folder_value.fxml");

            if(location == null){
                throw new IOException(
                    "getResource(\"/fxml/tree/folder_value.fxml\") returned null"
                );
            }

            FXMLLoader loader = new FXMLLoader(location);
            loader.setRoot(this);
            loader.setController(this);

            loader.load();
        } catch (IOException e){
            Logger.getLogger(getClass().getName()).severe(
                "Could not load resource:/fxml/tree/folder_value.fxml\n" +
                "The file might be missing or be corrupted.\n" +
                "Application will terminate.\n" +
                "Exception Message: " + e.getMessage());
            Platform.exit();
        }
    }

    @FXML
    public void initialize(){
        label.setText(name_);

        if(! canDownload_){
            button.setVisible(false);
        }
        else{
            button.setOnAction(this :: buttonOnAction);
        }

        checkBox.setOnAction(this :: checkBoxOnAction);
    }

    private void buttonOnAction(ActionEvent actionEvent){
        button.setVisible(false);
        progressIndicator.setVisible(true);

        emailFetcher_.fetch(path_);
    }

    private void checkBoxOnAction(ActionEvent actionEvent){
        emailTreeItem_.setSelectedDownToLeaves(checkBox.isSelected());
    }

    private void enableDownload(){
        if(progressIndicator.isVisible()){
            progressIndicator.setVisible(false);
        }

        if(!button.isVisible()){
            button.setVisible(true);
        }

        if(button.isDisabled()){
            button.setDisable(false);
        }
    }

    private void disableDownload(){
        if(button.isVisible() && !button.isDisabled()){
             button.setDisable(true);
        }
    }

    private void enableSelection(){
        checkBox.setDisable(false);
    }

    @Override
    public boolean isSelected () {
        return checkBox.isSelected();
    }

    @Override
    public void setSelected (boolean selected) {
        if(!checkBox.isDisabled()) {
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
    public String toString(){
        return name_;
    }

    @Override
    public void update (Observable o, Object arg) {
        if(arg instanceof EmailFetcher.Stage){
            switch ((EmailFetcher.Stage) arg){
                case STARTED_FETCHING:
                    disableDownload();
                    break;
                case STOPPED_FETCHING:
                    enableDownload();
                    break;
            }
        }
    }

    @FXML
    private Label label;

    @FXML
    private CheckBox checkBox;

    @FXML
    private Button button;
    private boolean canDownload_;

    @FXML
    private ProgressIndicator progressIndicator;

    private String path_;
    private String name_;

    private EmailFetcher emailFetcher_;

    private EmailTreeItem emailTreeItem_;

}
