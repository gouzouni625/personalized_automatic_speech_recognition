package org.pasr.gui.controllers;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import org.pasr.asr.dictionary.Dictionary;
import org.pasr.prep.corpus.Corpus;
import org.pasr.prep.corpus.Document;
import org.pasr.prep.email.fetchers.Email;
import org.pasr.prep.lda.LDA;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;


public class LDASceneController extends Controller {
    public LDASceneController (org.pasr.gui.controllers.Controller.API api) {
        super(api);

        unknownWords_ = FXCollections.observableArrayList();
        candidateWords_ = FXCollections.observableArrayList();

        corpus_ = new Corpus(((API) api_).getEmails().stream()
            .map(email -> new Document(email.getID(), email.getBody()))
            .collect(Collectors.toList())
        );
    }

    @FXML
    public void initialize(){
        startDictionaryThread();
        startLDAThread();

        removeButton.setOnAction(this :: removeButtonOnAction);
        chooseButton.setOnAction(this :: chooseButtonOnAction);
        runAgainButton.setOnAction(this :: runAgainButtonOnAction);

        iterationsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
            100, 10000, 1000, 100 // min, max, default, step
        ));

        topicsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
            2, 5, 3, 1 // min, max, default, step
        ));

        threadsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
            1, 64, 2, 1 // min, max, default, step
        ));

        wordsListView.setItems(unknownWords_);
        wordsListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                int selectedIndex = wordsListView.getSelectionModel().getSelectedIndex();
                if(selectedIndex != -1) {
                    candidatesListView.setItems(candidateWords_.get(selectedIndex));
                }
                else{
                    candidatesListView.setItems(null);
                }
        });

        useLDACheckBox.setTooltip(new Tooltip(USE_LDA_CHECK_BOX_TOOLTIP));
    }

    private void startDictionaryThread(){
        // The dictionary thread will run only once during the life cycle of this controller
        if(dictionaryThread_ == null){
            dictionaryThread_ = new DictionaryThread();
            dictionaryThread_.start();
        }
    }

    private void startLDAThread(){
        if(lDAThread_ == null || !lDAThread_.isAlive()){
            lDAThread_ = new LDAThread();
            lDAThread_.start();
        }
    }

    private void removeButtonOnAction(ActionEvent actionEvent){
        int selectedIndex = wordsListView.getSelectionModel().getSelectedIndex();

        if(selectedIndex != -1){
            String selectedWord = wordsListView.getSelectionModel().getSelectedItem();

            dictionary_.removeUnknownWord(selectedWord);
            corpus_.removeWordByText(selectedWord);

            unknownWords_.remove(selectedIndex);
            candidateWords_.remove(selectedIndex);
        }
    }

    private void chooseButtonOnAction(ActionEvent actionEvent){
        int selectedIndex = candidatesListView.getSelectionModel().getSelectedIndex();

        if(selectedIndex != -1){
            // Note that wrongWordIndex is guaranteed not equal to -1 because if the wordsListView
            // selection had been clear cleared, then the candidateListView items would have been
            // set to null and thus, selectedIndex would be equal to -1.
            int wrongWordIndex = wordsListView.getSelectionModel().getSelectedIndex();

            String wrongWord = wordsListView.getSelectionModel().getSelectedItem();
            String selectedWord = candidatesListView.getSelectionModel().getSelectedItem();

            dictionary_.removeUnknownWord(wrongWord);
            corpus_.replaceWordText(wrongWord, selectedWord);

            unknownWords_.remove(wrongWordIndex);
            candidateWords_.remove(wrongWordIndex);
        }
    }

    private void runAgainButtonOnAction(ActionEvent actionEvent){
        startLDAThread();
    }

    private class DictionaryThread extends Thread{
        DictionaryThread (){
            progressIndicator_ = new ProgressIndicator(
                wordsPane, wordsProgressBar, wordsProgressPane
            );
        }

        @Override
        public void run(){
            progressIndicator_.showProgress();

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

            progressIndicator_.hideProgress();
        }

        private final ProgressIndicator progressIndicator_;
    }

    private class LDAThread extends Thread{
        LDAThread (){
            progressIndicator_ = new ProgressIndicator(lDAPane, lDAProgressBar, lDAProgressPane);
        }

        @Override
        public void run(){
            progressIndicator_.showProgress();

            try {
                // Wait for the dictionary thread to finish before starting the lda thread so
                // that the data needed from the second are available.
                // Note that the dictionary thread will run only once during the life cycle of this
                // controller
                dictionaryThread_.join();
            } catch (InterruptedException e) {
                // TODO Act appropriately
                e.printStackTrace();
            }

            LDA lda = new LDA(corpus_.getDocumentsText(), topicsSpinner.getValue(),
                iterationsSpinner.getValue(), threadsSpinner.getValue());
            try {
                lda.start();
            } catch (IOException e) {
                // TODO Act appropriately: Set a flag that will indicate that something went wrong
                e.printStackTrace();
            }

            progressIndicator_.hideProgress();
        }

        private final ProgressIndicator progressIndicator_;
    }

    private class ProgressIndicator{
        ProgressIndicator(Node waitingNode, ProgressBar progressBar, Node progressNode){
            waitingNode_ = waitingNode;
            progressBar_ = progressBar;
            progressNode_ = progressNode;
        }

        void showProgress (){
            waitingNode_.setDisable(true);

            timer_ = new Timer();
            timer_.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run () {
                    double currentProgress = progressBar_.getProgress();

                    if(currentProgress >= 1){
                        progressBar_.setProgress(0.0);
                    }
                    else{
                        progressBar_.setProgress(currentProgress + 0.25);
                    }
                }
            }, new Date(System.currentTimeMillis()), 1000);

            progressNode_.setVisible(true);
        }

        void hideProgress (){
            timer_.cancel();

            waitingNode_.setDisable(false);

            progressNode_.setVisible(false);
        }

        private final Node waitingNode_;
        private final ProgressBar progressBar_;
        private final Node progressNode_;

        private Timer timer_;
    }

    public interface API extends org.pasr.gui.controllers.Controller.API{
        List<Email> getEmails();
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
    private AnchorPane wordsProgressPane;

    @FXML
    private ProgressBar wordsProgressBar;

    @FXML
    private AnchorPane lDAPane;

    @FXML
    private Spinner<Integer> iterationsSpinner;

    @FXML
    private Spinner<Integer> threadsSpinner;

    @FXML
    private Spinner<Integer> topicsSpinner;

    @FXML
    private Button runAgainButton;

    @FXML
    private TextArea resultsTextArea;

    @FXML
    private CheckBox useLDACheckBox;

    @FXML
    private Button backButton;

    @FXML
    private Button doneButton;

    @FXML
    private AnchorPane lDAProgressPane;

    @FXML
    private ProgressBar lDAProgressBar;

    private ObservableList<String> unknownWords_;
    private ObservableList<ObservableList<String>> candidateWords_;

    private Corpus corpus_;
    private Dictionary dictionary_ = null;

    private DictionaryThread dictionaryThread_ = null;
    private LDAThread lDAThread_ = null;

    private static final String USE_LDA_CHECK_BOX_TOOLTIP = "Create more than one corpora" +
        " according to the LDA e-mail grouping";

}
