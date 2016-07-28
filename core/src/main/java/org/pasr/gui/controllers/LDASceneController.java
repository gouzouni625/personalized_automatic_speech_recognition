package org.pasr.gui.controllers;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import org.pasr.asr.dictionary.Dictionary;
import org.pasr.prep.corpus.Corpus;
import org.pasr.prep.email.fetchers.Email;

import java.io.FileNotFoundException;
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

        corpus_ = new Corpus(((API) api_).getEmails());
    }

    @FXML
    public void initialize(){
        startDictionaryThread();
        startLDAThread();

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
                Thread.sleep(10000);
            } catch (InterruptedException e) {
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


    private void startDictionaryThread(){
        if(dictionaryThread_ == null || !dictionaryThread_.isAlive()){
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

    @FXML
    private AnchorPane lDAProgressPane;

    @FXML
    private ProgressBar lDAProgressBar;

    private ObservableList<String> unknownWords_;
    private ObservableList<ObservableList<String>> candidateWords_;

    private Corpus corpus_;
    private Dictionary dictionary_ = null;

    private Thread dictionaryThread_;
    private Thread lDAThread_;

}
