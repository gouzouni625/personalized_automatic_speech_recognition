package org.pasr.gui.controllers.scene;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.pasr.asr.dictionary.Dictionary;
import org.pasr.database.DataBase;
import org.pasr.gui.console.Console;
import org.pasr.gui.dialog.CorpusNameDialog;
import org.pasr.gui.dialog.YesNoDialog;
import org.pasr.prep.corpus.Corpus;
import org.pasr.prep.lda.LDA;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
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
        backButton.setOnAction(this :: backButtonOnAction);
        doneButton.setOnAction(this :: doneButtonOnAction);

        iterationsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
            100, 10000, 1000, 100 // min, max, default, step
        ));

        topicsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
            2, 5, 3, 1 // min, max, default, step
        ));

        threadsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
            1, 64, 1, 1 // min, max, default, step
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

    private void stopDictionaryThread(){
        if(dictionaryThread_ != null && dictionaryThread_.isAlive()){
            dictionaryThread_.terminate();
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

            // Clear the selection so that the user is forced to select a new word
            wordsListView.getSelectionModel().clearSelection();
        }
    }

    private void runButtonOnAction (ActionEvent actionEvent){
        startLDAThread();
    }

    private void startLDAThread(){
        if(dictionaryThread_ != null && dictionaryThread_.isAlive()){
            Console.getInstance().postMessage("Cannot start LDA while your e-mails are being" +
                " processed.\nPlease try again when the processing has finished.");
            return;
        }

        if(lDAThread_ == null || !lDAThread_.isAlive()){
            lDAThread_ = new LDAThread();
            lDAThread_.setDaemon(true);
            lDAThread_.start();
        }
    }

    private void backButtonOnAction(ActionEvent actionEvent){
        terminate();

        ((API) api_).initialScene();
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
                terminate();

                ((API) api_).record(corpus_.getID());
            }
            else{
                terminate();

                ((API) api_).dictate(corpus_.getID());
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
            setLDAApplicability();

            getLogger().info("LDAController DictionaryThread shut down gracefully!");
        }

        private void setLDAApplicability (){
            if(corpus_.numberOfDocuments() > 1){
                lDAPane.setDisable(false);
            }
            else{
                disabledLDALabel.setVisible(true);
            }
        }

        public void terminate(){
            corpus_.cancelProcess();
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

            LDA lda;
            try {
                lda = new LDA(corpus_.getDocumentsText(), topicsSpinner.getValue(),
                    iterationsSpinner.getValue(), threadsSpinner.getValue());
            } catch(IllegalArgumentException e){
                getLogger().log(Level.SEVERE, "An illegal argument was provided to the LDA.\n" +
                    "LDA should not be used.", e);

                Console.getInstance().postMessage("There appears to be a problem with LDA.\n" +
                    "Please, refrain from using it.");

                beforeExit();

                return;
            }

            progressIndicator_.observe(lda);

            try {
                lda.start();

                // Show results to the user
                showResults(lda);
            } catch (IOException e) {
                getLogger().log(Level.WARNING, "lda threw an IOException", e);

                Console.getInstance().postMessage("An error has occurred while running LDA.\n" +
                    "Please try again in a few moments");
            }
            finally {
                beforeExit();
            }
        }

        private void beforeExit (){
            progressIndicator_.hideProgress();

            getLogger().info("LDAController LDAThread shut down gracefully!");
            Console.getInstance().postMessage("Hello World");
        }

        private void showResults(LDA lda){
            StringBuilder stringBuilder = new StringBuilder();

            int numberOfTopics = lda.getNumberOfTopics();

            stringBuilder.append("LDA algorithm has given the following results:\n\n")
                .append("Number of topics: ").append(numberOfTopics).append("\n")
                .append("Number of iterations: ").append(lda.getNumberOfIterations()).append("\n\n")
                .append("Ten most common words for each topic:\n\n");

            List<List<String>> topWords = lda.getTopWords(10);

            for(int i = 0;i < numberOfTopics;i++){
                stringBuilder.append("topic ").append(String.valueOf(i)).append(": ");

                List<String> currentTopicTopWords = topWords.get(i);
                for(int j = 0, m = currentTopicTopWords.size() - 1;j < m;j++){
                    stringBuilder.append(currentTopicTopWords.get(j)).append(" ");
                }
                // Do not append a space for the last word
                stringBuilder.append(currentTopicTopWords.get(currentTopicTopWords.size() - 1));

                stringBuilder.append("\n");
            }

            stringBuilder.append("\nTo see how your e-mails are distributed across the topics")
                .append(", press the \"interact\" button");

            Platform.runLater(() -> {
                interactButton.setDisable(false);
                resultTextArea.setText(stringBuilder.toString());
            });
        }

        private final ProgressIndicator progressIndicator_;
    }

    private class ProgressIndicator implements Observer{
        ProgressIndicator(Node waitingNode, ProgressBar progressBar, Node progressNode){
            waitingNode_ = waitingNode;
            progressBar_ = progressBar;
            progressNode_ = progressNode;
        }

        ProgressIndicator(Node waitingNode, ProgressBar progressBar, Node progressNode,
                          Observable observable){
            this(waitingNode, progressBar, progressNode);

            observe(observable);
        }

        void observe(Observable observable){
            observable.addObserver(this);
        }

        void showProgress (){
            waitingNode_.setDisable(true);
            progressBar_.setProgress(0.0);
            progressNode_.setVisible(true);

            setButtonsDisable(true);
        }

        void hideProgress (){
            waitingNode_.setDisable(false);
            progressNode_.setVisible(false);

            setButtonsDisable(false);
        }

        @Override
        public void update (Observable o, Object arg) {
            progressBar_.setProgress((Double) arg);
        }

        private final Node waitingNode_;
        private final ProgressBar progressBar_;
        private final Node progressNode_;
    }

    private void setButtonsDisable(boolean disable){
        removeButton.setDisable(disable);
        pronounceButton.setDisable(disable);
        chooseButton.setDisable(disable);
        chooseButton.setDisable(disable);
        runButton.setDisable(disable);
        backButton.setDisable(disable);
        doneButton.setDisable(disable);
    }

    @Override
    public void terminate(){
        stopDictionaryThread();
    }

    public interface API extends Controller.API{
        Corpus getCorpus();
        void initialScene();
        void record(int corpusID);
        void dictate(int corpusID);
    }

    @FXML
    private SplitPane wordsPane;

    @FXML
    private ListView<String> wordsListView;
    private ObservableList<String> unknownWords_;

    @FXML
    private Button removeButton;

    @FXML
    private Button pronounceButton;

    @FXML
    private ListView<String> candidatesListView;
    private ObservableList<ObservableList<String>> candidateWords_;

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
    private TextArea resultTextArea;

    @FXML
    private Label disabledLDALabel;

    @FXML
    private Button interactButton;

    @FXML
    private CheckBox useLDACheckBox;
    private static final String USE_LDA_CHECK_BOX_TOOLTIP = "Create more than one corpora" +
        " according to the LDA e-mail grouping";

    @FXML
    private AnchorPane lDAProgressPane;

    @FXML
    private ProgressBar lDAProgressBar;

    @FXML
    private Button backButton;

    @FXML
    private Button doneButton;

    private Corpus corpus_;
    private Dictionary dictionary_;

    private DictionaryThread dictionaryThread_;
    private LDAThread lDAThread_;

    private static final String YES_NO_DIALOG_PROMPT_TEXT = "Record voice samples for acoustic" +
        " model adaptation?";

}
