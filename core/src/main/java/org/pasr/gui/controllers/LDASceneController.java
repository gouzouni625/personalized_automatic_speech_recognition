package org.pasr.gui.controllers;


import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.pasr.prep.corpus.Word;


public class LDASceneController extends Controller {
    LDASceneController (org.pasr.gui.controllers.Controller.API api) {
        super(api);
    }

    @FXML
    public void initialize(){
        classesTextField.textProperty().addListener(
            (observable, oldValue, newValue) -> {

                try {
                    //noinspection ResultOfMethodCallIgnored
                    Integer.parseInt(newValue);
                } catch (NumberFormatException e){
                    ((StringProperty) observable).setValue(oldValue);
                }
        });
    }

    public interface API extends org.pasr.gui.controllers.Controller.API{

    }

    @FXML
    private Button pronounceButton;

    @FXML
    private ListView<Word> wordsListView;

    @FXML
    private Button chooseButton;

    @FXML
    private ListView<Word> candidatesListView;

    @FXML
    private AnchorPane lDAPane;

    @FXML
    private TextArea resultsTextArea;

    @FXML
    private Button runAgainButton;

    @FXML
    private TextField classesTextField;

    @FXML
    private Button declineButton;

    @FXML
    private Button acceptButton;

    @FXML
    private Button backButton;

    @FXML
    private Button doneButton;

}
