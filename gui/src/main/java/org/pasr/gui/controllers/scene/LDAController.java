package org.pasr.gui.controllers.scene;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.apache.commons.collections4.MultiValuedMap;
import org.pasr.model.asr.dictionary.Dictionary;
import org.pasr.database.DataBase;
import org.pasr.gui.console.Console;
import org.pasr.gui.dialog.CorpusNameDialog;
import org.pasr.gui.dialog.LDAInteractDialog;
import org.pasr.gui.dialog.ListDialog;
import org.pasr.gui.dialog.YesNoDialog;
import org.pasr.model.text.Corpus;
import org.pasr.model.text.Document;
import org.pasr.external.lda.LDA;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * @class LDAController
 * @brief Controller for the LDA scene of the application
 */
public class LDAController extends Controller {

    /**
     * @brief Constructor
     *
     * @param api
     *     The implementation of the API of this Controller
     */
    public LDAController (Controller.API api) {
        super(api);

        corpus_ = ((API) api_).getCorpus();
    }

    @FXML
    public void initialize () {
        unknownWords_ = FXCollections.observableArrayList();
        candidateWords_ = FXCollections.observableArrayList();

        startDictionaryThread();

        removeButton.setOnAction(this :: removeButtonOnAction);
        autoPronounceButton.setOnAction(this :: autoPronounceButtonOnAction);
        chooseButton.setOnAction(this :: chooseButtonOnAction);
        runButton.setOnAction(this :: runButtonOnAction);
        interactButton.setOnAction(this :: interactButtonOnAction);
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
                if (selectedIndex != - 1) {
                    candidatesListView.setItems(candidateWords_.get(selectedIndex));
                }
                else {
                    candidatesListView.setItems(null);
                }
            });

        useLDACheckBox.setTooltip(new Tooltip(USE_LDA_CHECK_BOX_TOOLTIP));
    }

    private void startDictionaryThread () {
        // The dictionary thread will run only once during the life cycle of this controller
        if (dictionaryThread_ == null) {
            dictionaryThread_ = new DictionaryThread();
            dictionaryThread_.start();
        }
    }

    private void stopDictionaryThread () {
        if (dictionaryThread_ != null && dictionaryThread_.isAlive()) {
            dictionaryThread_.terminate();
        }
    }

    private void removeButtonOnAction (ActionEvent actionEvent) {
        int selectedIndex = wordsListView.getSelectionModel().getSelectedIndex();

        if (selectedIndex != - 1) {
            String selectedWord = wordsListView.getSelectionModel().getSelectedItem();

            dictionary_.removeUnknownWord(selectedWord);
            corpus_.removeWordByText(selectedWord);

            unknownWords_.remove(selectedIndex);
            candidateWords_.remove(selectedIndex);
        }
    }

    private void autoPronounceButtonOnAction (ActionEvent actionEvent) {
        int selectedIndex = wordsListView.getSelectionModel().getSelectedIndex();

        if (selectedIndex != - 1) {
            String selectedWord = wordsListView.getSelectionModel().getSelectedItem();

            dictionary_.put(
                selectedWord,
                String.join(" ", Dictionary.autoPronounce(selectedWord))
            );

            unknownWords_.remove(selectedIndex);
            candidateWords_.remove(selectedIndex);
        }
    }

    private void chooseButtonOnAction (ActionEvent actionEvent) {
        int selectedIndex = candidatesListView.getSelectionModel().getSelectedIndex();

        if (selectedIndex != - 1) {
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

    private void runButtonOnAction (ActionEvent actionEvent) {
        startLDAThread();
    }

    private void startLDAThread () {
        if (dictionaryThread_ != null && dictionaryThread_.isAlive()) {
            Console.getInstance().postMessage("Cannot start LDA while your e-mails are being" +
                " processed.\nPlease try again when the processing has finished.");
            return;
        }

        if (lDAThread_ == null || ! lDAThread_.isAlive()) {
            lDAThread_ = new LDAThread();
            lDAThread_.start();
        }
    }

    private void interactButtonOnAction (ActionEvent actionEvent) {
        LDAInteractDialog lDAInteractDialog;
        try {
            lDAInteractDialog = new LDAInteractDialog(lda_);
            lDAInteractDialog.showAndWait();
        } catch (IllegalArgumentException e) {
            Console.getInstance().postMessage("You should first run the LDA algorithm before" +
                "interacting with the results (press \"run\" button before \"interact\" button)");
            return;
        } catch (IOException e) {
            getLogger().severe("Could not load resource:/fxml/dialog/lda_interact.fxml\n" +
                "The file might be missing or be corrupted.\n" +
                "Application will terminate.\n" +
                "Exception Message: " + e.getMessage());

            Platform.exit();
            return;
        }

        ldaResults_ = lDAInteractDialog.getValue();
    }

    private void backButtonOnAction (ActionEvent actionEvent) {
        terminate();

        ((API) api_).initialScene();
    }

    private void doneButtonOnAction (ActionEvent actionEvent) {
        DataBase database = DataBase.getInstance();

        Map<Integer, String> corpusInformation = new LinkedHashMap<>();

        if (useLDACheckBox.isSelected()) {
            List<Document> documents = corpus_.getDocuments();

            for (Map.Entry<String, List<Long>> entry : ldaResults_.entries()) {
                List<Long> documentIDs = entry.getValue();

                Corpus corpus = new Corpus();
                corpus.setDocuments(documents.stream()
                    .filter(document -> documentIDs.contains(document.getId()))
                    .collect(Collectors.toList()));

                corpus.setName(entry.getKey());

                Dictionary dictionary = corpus.process(dictionary_);

                try {
                    corpusInformation.put(
                        DataBase.getInstance().newCorpusEntry(corpus, dictionary), corpus.getName()
                    );
                } catch (IOException e) {
                    Console.getInstance().postMessage("There was an error trying to save the a" +
                        " corpus.\n" +
                        "Check the permissions inside the directory: " +
                        database.getConfiguration().getDataBaseDirectoryPath() + "\n" +
                        "Exception Message: " + e.getMessage());

                    return;
                }
            }
        }
        else {
            try {
                CorpusNameDialog corpusNameDialog = new CorpusNameDialog("corpus_" +
                    String.valueOf(database.getNumberOfCorpora() + 1)
                );
                corpusNameDialog.showAndWait();

                corpus_.setName(corpusNameDialog.getValue());
            } catch (IOException e) {
                getLogger().severe("Could not load resource:/fxml/dialog/corpus_name.fxml\n" +
                    "The file might be missing or be corrupted.\n" +
                    "Application will terminate.\n" +
                    "Exception Message: " + e.getMessage());

                Platform.exit();
                return;
            }

            try {
                corpusInformation.put(
                    DataBase.getInstance().newCorpusEntry(corpus_, dictionary_), corpus_.getName()
                );
            } catch (IOException e) {
                Console.getInstance().postMessage("There was an error trying to save the a" +
                    " corpus.\n" +
                    "Check the permissions inside the: " +
                    database.getConfiguration().getDataBaseDirectoryPath() + "\n" +
                    "Exception Message: " + e.getMessage());

                return;
            }
        }

        YesNoDialog yesNoDialog;
        try {
            yesNoDialog = new YesNoDialog(true, YES_NO_DIALOG_PROMPT_TEXT);
            yesNoDialog.showAndWait();
        } catch (IOException e) {
            getLogger().severe("Could not load resource:/fxml/dialog/yes_no.fxml\n" +
                "The file might be missing or be corrupted.\n" +
                "Application will terminate.\n" +
                "Exception Message: " + e.getMessage());

            Platform.exit();
            return;
        }

        if (corpusInformation.size() == 0) {
            getLogger().severe("Corpus information is empty");

            Console.getInstance().postMessage(
                "There has been an error while creating your corpora.\n" +
                    "Try choosing more e-mail and running LDA algorithm again."
            );

            return;
        }

        // Get the first corpus id
        int selectedCorpusID = corpusInformation.entrySet().iterator().next().getKey();

        if (corpusInformation.size() > 1) {
            ListDialog<String> listDialog;
            try {
                listDialog = new ListDialog<>(
                    String.valueOf(selectedCorpusID), CORPUS_CHOOSE_DIALOG_PROMPT_TEXT,
                    corpusInformation.entrySet().stream()
                        .map(entry -> entry.getKey() + " " + entry.getValue())
                        .collect(Collectors.toList())
                );
                listDialog.showAndWait();
            } catch (IOException e) {
                getLogger().severe("Could not load resource:/fxml/dialog/list.fxml\n" +
                    "The file might be missing or be corrupted.\n" +
                    "Application will terminate.\n" +
                    "Exception Message: " + e.getMessage());

                Platform.exit();
                return;
            }


            // Do not split(" ") the result since the name of the corpus might begin with space
            Matcher matcher = Pattern.compile("([0-9]+) .+").matcher(listDialog.getValue());
            if (matcher.matches()) {
                selectedCorpusID = Integer.parseInt(matcher.group(1));
            }
        }

        if (yesNoDialog.getValue()) {
            terminate();

            ((API) api_).record(selectedCorpusID);
        }
        else {
            terminate();

            ((API) api_).dictate(selectedCorpusID);
        }
    }

    private class DictionaryThread extends Thread {

        DictionaryThread () {
            progressIndicator_ = new ProgressIndicator(
                wordsPane, wordsProgressBar, wordsProgressPane, corpus_.getProgress()
            );

            setDaemon(true);
        }

        @Override
        public void run () {
            logger_.info("DictionaryThread started!");

            progressIndicator_.showProgress();

            try {
                Dictionary defaultDictionary = Dictionary.getDefaultDictionary();

                dictionary_ = corpus_.process(defaultDictionary);

                unknownWords_.addAll(dictionary_.getUnknownWords());
                candidateWords_.addAll(unknownWords_.stream()
                    .map(defaultDictionary:: fuzzyMatch)
                    .map(FXCollections:: observableArrayList)
                    .collect(Collectors.toList()));
            } catch (FileNotFoundException e) {
                logger_.log(Level.SEVERE, "Default dictionary was not found.\n" +
                    "Application will exit.", e);

                Platform.exit();
                return;
            }

            progressIndicator_.hideProgress();
            setLDAApplicability();

            logger_.info("DictionaryThread shut down gracefully!");
        }

        private void setLDAApplicability () {
            if (corpus_.numberOfDocuments() > 1) {
                lDAPane.setDisable(false);
            }
            else {
                disabledLDALabel.setVisible(true);
            }
        }

        public void terminate () {
            corpus_.cancelProcess();
        }

        private final ProgressIndicator progressIndicator_;

        private Logger logger_ = Logger.getLogger(getClass().getName());
    }

    private class LDAThread extends Thread {

        LDAThread () {
            progressIndicator_ = new ProgressIndicator(lDAPane, lDAProgressBar, lDAProgressPane);

            setDaemon(true);
        }

        @Override
        public void run () {
            logger_.info("LDAThread started!");

            progressIndicator_.showProgress();

            try {
                if (lda_ == null) {
                    lda_ = new LDA(corpus_.getDocuments(), topicsSpinner.getValue(),
                        iterationsSpinner.getValue(), threadsSpinner.getValue());
                }
                else {
                    lda_.setDocuments(corpus_.getDocuments())
                        .setNumberOfTopics(topicsSpinner.getValue())
                        .setNumberOfIterations(iterationsSpinner.getValue())
                        .setNumberOfThreads(threadsSpinner.getValue());
                }
            } catch (IllegalArgumentException e) {
                logger_.log(Level.SEVERE, "An illegal argument was provided to the LDA.\n" +
                    "LDA should not be used.", e);

                Console.getInstance().postMessage("There appears to be a problem with LDA.\n" +
                    "Please, refrain from using it.");

                beforeExit();

                return;
            }

            progressIndicator_.observe(lda_);

            try {
                lda_.start();

                // Show results to the user
                showResults(lda_);
            } catch (IOException e) {
                logger_.log(Level.WARNING, "lda threw an IOException", e);

                Console.getInstance().postMessage("An error has occurred while running LDA.\n" +
                    "Please try again in a few moments");
            } finally {
                beforeExit();
            }
        }

        private void beforeExit () {
            progressIndicator_.hideProgress();

            logger_.info("LDAThread shut down gracefully!");
        }

        private void showResults (LDA lda) {
            StringBuilder stringBuilder = new StringBuilder();

            int numberOfTopics = lda.getNumberOfTopics();

            stringBuilder.append("LDA algorithm has given the following results:\n\n")
                .append("Number of topics: ").append(numberOfTopics).append("\n")
                .append("Number of iterations: ").append(lda.getNumberOfIterations()).append("\n\n")
                .append("Ten most common words for each topic:\n\n");

            List<List<String>> topWords = lda.getTopWords(10);

            for (int i = 0; i < numberOfTopics; i++) {
                stringBuilder.append("topic ").append(String.valueOf(i)).append(": ");

                List<String> currentTopicTopWords = topWords.get(i);
                for (int j = 0, m = currentTopicTopWords.size() - 1; j < m; j++) {
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

        private Logger logger_ = Logger.getLogger(getClass().getName());
    }

    private class ProgressIndicator implements Observer {

        ProgressIndicator (Node waitingNode, ProgressBar progressBar, Node progressNode) {
            waitingNode_ = waitingNode;
            progressBar_ = progressBar;
            progressNode_ = progressNode;
        }

        ProgressIndicator (Node waitingNode, ProgressBar progressBar, Node progressNode,
                           Observable observable) {
            this(waitingNode, progressBar, progressNode);

            observe(observable);
        }

        void observe (Observable observable) {
            observable.addObserver(this);
        }

        void showProgress () {
            waitingNode_.setDisable(true);
            progressBar_.setProgress(0.0);
            progressNode_.setVisible(true);

            setButtonsDisable(true);
        }

        void hideProgress () {
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

    private void setButtonsDisable (boolean disable) {
        removeButton.setDisable(disable);
        autoPronounceButton.setDisable(disable);
        chooseButton.setDisable(disable);
        chooseButton.setDisable(disable);
        runButton.setDisable(disable);
        backButton.setDisable(disable);
        doneButton.setDisable(disable);
    }

    @Override
    public void terminate () {
        stopDictionaryThread();
    }

    public interface API extends Controller.API {
        Corpus getCorpus ();

        void initialScene ();

        void record (int corpusID);

        void dictate (int corpusID);
    }

    @FXML
    private SplitPane wordsPane;

    @FXML
    private ListView<String> wordsListView;
    private ObservableList<String> unknownWords_;

    @FXML
    private Button removeButton;

    @FXML
    private Button autoPronounceButton;

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

    private LDA lda_;

    private MultiValuedMap<String, List<Long>> ldaResults_;

    private static final String YES_NO_DIALOG_PROMPT_TEXT = "Record voice samples for acoustic" +
        " model adaptation?";

    private static final String CORPUS_CHOOSE_DIALOG_PROMPT_TEXT = "Choose the corpus to use";

}
