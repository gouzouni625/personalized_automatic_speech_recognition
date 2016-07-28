package org.pasr.gui.controllers;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import org.pasr.asr.dictionary.Dictionary;
import org.pasr.prep.corpus.Corpus;
import org.pasr.prep.email.fetchers.Email;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Collectors;


public class LDASceneController extends Controller {
    public LDASceneController (org.pasr.gui.controllers.Controller.API api) {
        super(api);

        unknownWords_ = FXCollections.observableArrayList();
        candidateWords_ = FXCollections.observableArrayList();

        corpus_ = new Corpus(((API) api_).getEmails());

        dictionaryThread_ = new DictionaryThread();
        dictionaryThread_.start();
    }

    @FXML
    public void initialize(){
        iterationsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
            100, 10000, 1000, 100
        ));

        classesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
            2, 5, 3, 1
        ));

        wordsListView.setItems(unknownWords_);
        wordsListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                candidatesListView.setItems(
                    candidateWords_.get(wordsListView.getSelectionModel().getSelectedIndex())
                );
        });
    }

    public interface API extends org.pasr.gui.controllers.Controller.API{
        List<Email> getEmails();
    }

    private class DictionaryThread extends Thread{
        @Override
        public void run(){
            try {
                dictionary_ = corpus_.process(Dictionary.getDefaultDictionary());

                unknownWords_.addAll(dictionary_.getUnknownWords());
                candidateWords_.addAll(unknownWords_.stream()
                    .map(dictionary_ :: fuzzyMatch)
                    .map(FXCollections :: observableArrayList)
                    .collect(Collectors.toList()));
            } catch (FileNotFoundException e) {
                // TODO Act appropriately: Set a flag that will indicate that something went wrong
                e.printStackTrace();
            }
        }
    }

    @FXML
    private SplitPane wordsPane;

    @FXML
    private ListView<String> wordsListView;

    @FXML
    private Button removeButton;

    @FXML
    private Button pronounceButton;

    @FXML
    private ListView<String> candidatesListView;

    @FXML
    private Button chooseButton;

    @FXML
    private AnchorPane lDAPane;

    @FXML
    private Spinner<Integer> iterationsSpinner;

    @FXML
    private Button runAgainButton;

    @FXML
    private Spinner<Integer> classesSpinner;

    @FXML
    private TextArea resultsTextArea;

    @FXML
    private Button declineButton;

    @FXML
    private Button acceptButton;

    @FXML
    private Button backButton;

    @FXML
    private Button doneButton;

    private ObservableList<String> unknownWords_;
    private ObservableList<ObservableList<String>> candidateWords_;

    private Corpus corpus_;
    private Dictionary dictionary_ = null;

    private Thread dictionaryThread_;

}
