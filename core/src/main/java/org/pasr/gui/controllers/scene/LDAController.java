package org.pasr.gui.controllers.scene;


import javafx.application.Platform;
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
import org.pasr.database.DataBase;
import org.pasr.gui.dialog.CorpusNameDialog;
import org.pasr.gui.dialog.YesNoDialog;
import org.pasr.prep.corpus.Corpus;
import org.pasr.prep.lda.LDA;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.stream.Collectors;


public class LDAController extends Controller {
    public LDAController (Controller.API api) {
        super(api);

        corpus_ = ((API) api_).getCorpus();
    }

    @FXML
    public void initialize(){
        unknownWords_ = FXCollections.observableArrayList();
        candidateWords_ = FXCollections.observableArrayList();

        startDictionaryThread();

        removeButton.setOnAction(this :: removeButtonOnAction);
        chooseButton.setOnAction(this :: chooseButtonOnAction);
        runButton.setOnAction(this :: runButtonOnAction);
        doneButton.setOnAction(this :: doneButtonOnAction);

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
            dictionaryThread_.setDaemon(true);
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

    private void runButtonOnAction (ActionEvent actionEvent){
        startLDAThread();
    }

    private void doneButtonOnAction(ActionEvent actionEvent){
        DataBase database = DataBase.getInstance();

        if(useLDACheckBox.isSelected()){

        }
        else {
            try {
                CorpusNameDialog corpusNameDialog = new CorpusNameDialog("corpus_" +
                    String.valueOf(database.getNumberOfCorpora() + 1)
                );
                corpusNameDialog.showAndWait();

                corpus_.setName(corpusNameDialog.getValue());

                DataBase.getInstance().newCorpusEntry(corpus_, dictionary_);

            } catch (IOException e) {
                // TODO
                e.printStackTrace();
            }
        }

        try {
            YesNoDialog yesNoDialog = new YesNoDialog(true, YES_NO_DIALOG_PROMPT_TEXT);
            yesNoDialog.showAndWait();

            if(yesNoDialog.getValue()){
                ((API)api_).record(corpus_.getID());
            }
            else{
                ((API)api_).dictate(corpus_.getID());
            }
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
    }

    private class DictionaryThread extends Thread{
        DictionaryThread (){
            progressIndicator_ = new ProgressIndicator(
                wordsPane, wordsProgressBar, wordsProgressPane, corpus_
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
                getLogger().log(Level.SEVERE, "Default dictionary was not found.\n" +
                    "Application will exit.", e);
                Platform.exit();
            }

            progressIndicator_.hideProgress();
            lDAPane.setDisable(false);
        }

        private final ProgressIndicator progressIndicator_;
    }

    private class LDAThread extends Thread{
        LDAThread (){
            progressIndicator_ = new ProgressIndicator(lDAPane, lDAProgressBar, lDAProgressPane, null);
        }

        @Override
        public void run(){
            progressIndicator_.showProgress();
            doneButton.setDisable(true);

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

            doneButton.setDisable(false);
            progressIndicator_.hideProgress();
        }

        private final ProgressIndicator progressIndicator_;
    }

    private class ProgressIndicator implements Observer{
        ProgressIndicator(Node waitingNode, ProgressBar progressBar, Node progressNode,
                          Observable observable){
            waitingNode_ = waitingNode;
            progressBar_ = progressBar;
            progressNode_ = progressNode;

            observable.addObserver(this);
        }

        void showProgress (){
            waitingNode_.setDisable(true);

            progressBar_.setProgress(0.0);

            progressNode_.setVisible(true);
        }

        void hideProgress (){
            waitingNode_.setDisable(false);

            progressNode_.setVisible(false);
        }

        @Override
        public void update (Observable o, Object arg) {
            progressBar_.setProgress((Double) arg);
        }

        private final Node waitingNode_;
        private final ProgressBar progressBar_;
        private final Node progressNode_;
    }

    public interface API extends Controller.API{
        Corpus getCorpus();
        void record(int corpusID);
        void dictate(int corpusID);
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
    private Button runButton;

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
    private Dictionary dictionary_;

    private DictionaryThread dictionaryThread_;
    private LDAThread lDAThread_;

    private static final String USE_LDA_CHECK_BOX_TOOLTIP = "Create more than one corpora" +
        " according to the LDA e-mail grouping";

    private static final String YES_NO_DIALOG_PROMPT_TEXT = "Record voice samples for acoustic" +
        " model adaptation?";

}
