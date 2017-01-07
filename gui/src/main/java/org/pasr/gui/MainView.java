package org.pasr.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.pasr.database.DataBase;
import org.pasr.gui.console.Console;
import org.pasr.gui.controllers.scene.DictateController;
import org.pasr.gui.controllers.scene.EmailListController;
import org.pasr.gui.controllers.scene.IntermediateController;
import org.pasr.gui.controllers.scene.LDAController;
import org.pasr.gui.controllers.scene.MainController;
import org.pasr.gui.controllers.scene.RecordController;
import org.pasr.model.text.Corpus;
import org.pasr.model.text.Document;
import org.pasr.utilities.email.fetchers.Email;
import org.pasr.utilities.email.fetchers.EmailFetcher;
import org.pasr.utilities.email.fetchers.GMailFetcher;
import org.pasr.utilities.logging.PrettyFormatter;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.pasr.utilities.Utilities.getResourceStream;


/**
 * @class MainView
 * @brief Main entry point for the JavaFX application
 *        The structure is the following:
 *
 *        Each controller extends a base controller and defines an API interface which wraps all the
 *        interactions of the controller with the outer world. An implementation of this interface
 *        is injected in the constructor of the controller making it independent of the rest of the
 *        application. The MainView implements all the API interfaces of the controllers and is
 *        passed in the constructor of each controller during instantiation. The instantiation of
 *        each controller is done inside a factory class which acts as the intermediate between the
 *        MainView and the controllers. In the future, this can be done more efficiently using the
 *        Spring framework.
 */
public class MainView extends Application implements MainController.API,
    EmailListController.API, LDAController.API, RecordController.API, DictateController.API,
    IntermediateController.API {

    private static Logger logger_ = Logger.getLogger(MainView.class.getName());

    private static SceneFactory sceneFactory_ = SceneFactory.getInstance();

    private static DataBase dataBase_;

    private static Console console_;

    private Stage primaryStage_;

    private EmailFetcher emailFetcher_;
    private String emailAddress_;
    private String password_;

    private Corpus corpus_;

    private String intermediateMessage_ = "Please wait...";

    private enum IntermediateMessages {
        NEW_ACOUSTIC_MODEL("Please wait while the acoustic model is being adapted...");

        IntermediateMessages (String message) {
            message_ = message;
        }

        @Override
        public String toString () {
            return message_;
        }

        private String message_;
    }

    private NewAcousticModelThread newAcousticModelThread_;

    public static void main (String[] args) {
        logger_.info("Initializing logger...");

        try {
            InputStream inputStream = getResourceStream("/logging/logging.properties");

            if (inputStream == null) {
                throw new IOException(
                    "getResourceStream(\"/logging/logging.properties\") returned null"
                );
            }

            LogManager.getLogManager().readConfiguration(inputStream);
        } catch (IOException e) {
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new PrettyFormatter());

            logger_.setUseParentHandlers(false);

            logger_.addHandler(consoleHandler);

            logger_.warning("Could not load resource:/logging/logging.properties\n" +
                "Logging not configured (console output only).\n" +
                "Exception Message: " + e.getMessage());
        }

        try {
            dataBase_ = DataBase.create();
        } catch (IOException e) {
            logger_.log(Level.SEVERE,
                "Could not load resource:/database/default_configuration.json.\n" +
                    "The file might be missing or be corrupted.\n" +
                    "Application will terminate.\n" +
                    "Exception Message: " + e.getMessage());

            Platform.exit();
            return;
        }

        launch(args);
    }

    @Override
    public void start (Stage primaryStage) {
        primaryStage_ = primaryStage;

        primaryStage.setTitle("Personalized Automatic Speech Recognition");

        initialScene();

        primaryStage.show();

        console_ = Console.create(primaryStage);
        console_.show();

        primaryStage.requestFocus();
    }

    @Override
    public void initialScene () {
        try {
            primaryStage_.setScene(sceneFactory_.create(SceneFactory.Scenes.MAIN_SCENE, this));
        } catch (IOException e) {
            logger_.severe("Could not load resource:" +
                SceneFactory.Scenes.MAIN_SCENE.getFXMLResource() + "\n" +
                "The file might be missing or be corrupted.\n" +
                "Application will terminate.\n" +
                "Exception Message: " + e.getMessage());
            Platform.exit();
        }
    }

    @Override
    public String getEmailAddress () {
        return emailAddress_;
    }

    @Override
    public String getPassword () {
        return password_;
    }

    @Override
    public void newCorpus (String emailAddress, String password) {
        if (emailAddress == null) {
            logger_.warning("emailAddress in newCorpus was null");
            return;
        }

        if (password == null) {
            logger_.warning("password in newCorpus was null");
            return;
        }

        if (! emailAddress.endsWith("@gmail.com")) {
            console_.postMessage("At the moment, only gmail addresses are supported.");
            console_.postMessage(
                "Please, make sure that the provided address ends with @gmail.com" +
                    " and is a valid gmail address."
            );
            return;
        }

        emailAddress_ = emailAddress;
        password_ = password;

        if (! createNewEmailFetcher()) {
            logger_.severe("Could not create a new EmailFetcher.");
            return;
        }

        if (! openEmailFetcher(emailAddress_, password_)) {
            logger_.warning("Could not open EmailFetcher.");
            return;
        }

        try {
            primaryStage_.setScene(
                sceneFactory_.create(SceneFactory.Scenes.EMAIL_LIST_SCENE, this)
            );
        } catch (IOException e) {
            logger_.severe("Could not load resource:" +
                SceneFactory.Scenes.EMAIL_LIST_SCENE.getFXMLResource() + "\n" +
                "The file might be missing or be corrupted.\n" +
                "Application will terminate.\n" +
                "Exception Message: " + e.getMessage());
            Platform.exit();
        }
    }

    private boolean createNewEmailFetcher () {
        logger_.info("Creating new EmailFetcher.");

        // Before create a new fetcher, make sure any old one is dead
        if (emailFetcher_ != null) {
            emailFetcher_.terminate();
        }

        try {
            emailFetcher_ = new GMailFetcher();
        } catch (IOException e) {
            logger_.severe("Could not load email fetcher properties resource file.\n" +
                "The file might be missing or be corrupted.\n" +
                "Application will terminate.\n" +
                "Message: " + e.getMessage());
            Platform.exit();

            return false;
        }

        return true;
    }

    private boolean openEmailFetcher (String emailAddress, String password) {
        logger_.info("Opening EmailFetcher.");

        try {
            emailFetcher_.open(emailAddress, password);
        } catch (NoSuchProviderException e) {
            logger_.log(Level.SEVERE, "A provided for the given email protocol was not found.\n" +
                "Application will terminate.\n" +
                "Exception Message: " + e.getMessage());

            Platform.exit();

            return false;
        } catch (AuthenticationFailedException e) {
            console_.postMessage(
                "The provided email address and password were incorrect"
            );

            return false;
        } catch (IllegalStateException e) {
            console_.postMessage("Something went wrong with the email service.\n" +
                "Please try logging in again!");

            logger_.log(Level.WARNING, "The email service is already connected.\n" +
                "Exception Message: " + e.getMessage());

            return false;
        } catch (MessagingException e) {
            console_.postMessage("Something went wrong with the email service.\n" +
                "Please try logging in again!");

            logger_.log(Level.WARNING, "Something went wrong with the email service.\n" +
                "Exception Message: " + e.getMessage());

            return false;
        }

        return true;
    }

    @Override
    public EmailFetcher getEmailFetcher () {
        return emailFetcher_;
    }

    @Override
    public void processEmail (Set<Email> emails) {
        emailFetcher_.terminate();

        corpus_ = new Corpus();
        corpus_.setDocuments(emails.stream()
            .map(email -> new Document(email.getId(), email.getSubject(), email.getBody()))
            .collect(Collectors.toList()));

        try {
            primaryStage_.setScene(sceneFactory_.create(SceneFactory.Scenes.LDA_SCENE, this));
        } catch (IOException e) {
            logger_.severe("Could not load resource:" +
                SceneFactory.Scenes.LDA_SCENE.getFXMLResource() + "\n" +
                "The file might be missing or be corrupted.\n" +
                "Application will terminate.\n" +
                "Exception Message: " + e.getMessage());
            Platform.exit();
        }
    }

    @Override
    public Corpus getCorpus () {
        return corpus_;
    }

    @Override
    public void record (int corpusId) {
        setCorpus(corpusId);

        try {
            primaryStage_.setScene(sceneFactory_.create(SceneFactory.Scenes.RECORD_SCENE, this));
        } catch (IOException e) {
            logger_.severe("Could not load resource:" +
                SceneFactory.Scenes.RECORD_SCENE.getFXMLResource() + "\n" +
                "The file might be missing or be corrupted.\n" +
                "Application will terminate.\n" +
                "Exception Message: " + e.getMessage());
            Platform.exit();
        }
    }

    @Override
    public void dictate (int corpusId) {
        setCorpus(corpusId);

        setIntermediateMessage(IntermediateMessages.NEW_ACOUSTIC_MODEL);
        showIntermediateScene();

        createNewAcousticModel();
    }

    private void setIntermediateMessage (IntermediateMessages intermediateMessage) {
        intermediateMessage_ = intermediateMessage.toString();
    }

    private void showIntermediateScene () {
        try {
            primaryStage_.setScene(sceneFactory_.create(
                SceneFactory.Scenes.INTERMEDIATE_SCENE, this)
            );
        } catch (IOException e) {
            logger_.severe("Could not load resource:" +
                SceneFactory.Scenes.INTERMEDIATE_SCENE.getFXMLResource() + "\n" +
                "The file might be missing or be corrupted.\n" +
                "Application will terminate.\n" +
                "Exception Message: " + e.getMessage());
            Platform.exit();
        }
    }

    @Override
    public String getMessage () {
        return intermediateMessage_;
    }

    private class NewAcousticModelThread extends Thread {
        NewAcousticModelThread () {
            setDaemon(true);
        }

        @Override
        public void run () {
            logger_.info("NewAcousticModelThread started!");

            boolean created = false;
            try {
                created = DataBase.getInstance().newAcousticModel(10);
            } catch (IOException e) {
                console_.postMessage("Could not create an adapted acoustic model.\n" +
                    "You should check your user permissions inside the directory " +
                    dataBase_.getConfiguration().getDataBaseDirectoryPath() + ".\n" +
                    "Dictation will be possible only with the default acoustic model or with" +
                    " any (if any) previous adapted acoustic model.");
            } catch (InterruptedException e) {
                logger_.log(Level.WARNING, "Interrupted while creating new acoustic model.", e);
            }

            if (! created) {
                console_.postMessage("Could not create an adapted acoustic model.\n" +
                    "You should check your user permissions inside the directory " +
                    dataBase_.getConfiguration().getDataBaseDirectoryPath() + ".\n" +
                    "Dictation will be possible only with the default acoustic model or with" +
                    " any (if any) previous adapted acoustic model.");
            }

            Platform.runLater(MainView.this :: showDictateScene);

            logger_.info("NewAcousticModelThread shut down gracefully!");
        }

        private Logger logger_ = Logger.getLogger(getClass().getName());
    }

    private void createNewAcousticModel () {
        if (newAcousticModelThread_ == null || ! newAcousticModelThread_.isAlive()) {
            newAcousticModelThread_ = new NewAcousticModelThread();
            newAcousticModelThread_.start();
        }
    }

    private void showDictateScene () {
        try {
            primaryStage_.setScene(sceneFactory_.create(SceneFactory.Scenes.DICTATE_SCENE, this));
        } catch (IOException e) {
            logger_.severe("Could not load resource:" +
                SceneFactory.Scenes.DICTATE_SCENE.getFXMLResource() + "\n" +
                "The file might be missing or be corrupted.\n" +
                "Application will terminate.\n" +
                "Exception Message: " + e.getMessage());
            Platform.exit();
        }
    }

    private void setCorpus (int corpusId) {
        if (corpus_ != null && corpus_.getId() == corpusId) {
            return;
        }

        try {
            corpus_ = dataBase_.getCorpusById(corpusId);
        } catch (IOException e) {
            console_.postMessage("Could not load corpus with id: " + corpusId + ".\n" +
                "Exception Message: " + e.getMessage());

            logger_.log(Level.WARNING, "Could not load corpus with id: " + corpusId, e);

            corpus_ = null;
        }
    }

    @Override
    public void stop () {
        if (emailFetcher_ != null) {
            emailFetcher_.terminate();
        }

        if (newAcousticModelThread_ != null && newAcousticModelThread_.isAlive()) {
            newAcousticModelThread_.interrupt();
            try {
                // Don't wait forever on this thread since it is a daemon and will not block the JVM
                // from shutting down
                newAcousticModelThread_.join(3000);
            } catch (InterruptedException e) {
                logger_.warning("Interrupted while joining newAcousticModelThread.");
            }
        }

        sceneFactory_.getCurrentController().terminate();

        dataBase_.close();
    }

}
