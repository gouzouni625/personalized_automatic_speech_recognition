package org.pasr.gui;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.pasr.database.DataBase;
import org.pasr.gui.console.Console;
import org.pasr.gui.controllers.scene.DictateController;
import org.pasr.gui.controllers.scene.EmailListController;
import org.pasr.gui.controllers.scene.LDAController;
import org.pasr.gui.controllers.scene.MainController;
import org.pasr.gui.controllers.scene.RecordController;
import org.pasr.prep.email.fetchers.Email;
import org.pasr.prep.email.fetchers.EmailFetcher;
import org.pasr.prep.email.fetchers.GMailFetcher;
import org.pasr.utilities.logging.PrettyFormatter;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.pasr.utilities.Utilities.getResourceStream;


public class MainView extends Application implements MainController.API,
    EmailListController.API, LDAController.API, RecordController.API, DictateController.API {

    private static Logger logger_ = Logger.getLogger(MainView.class.getName());

    private SceneFactory sceneFactory_ = SceneFactory.getInstance();

    private Stage primaryStage_;

    // TODO When initial scene is called from the email list controller, put emailAddress and
    // TODO password to their respective fields. If newCorpus is clicked again, with the same
    // TODO username and password, make sure that you don't create a new emailFetcher but use
    // TODO the one you already have.
    private EmailFetcher emailFetcher_;
    private String emailAddress_;
    private String password_;

    private List<Email> emails_;

    private int corpusID_;

    public static void main(String[] args){
        logger_.info("Initializing logger...");

        try {
            InputStream inputStream = getResourceStream("/logging/logging.properties");

            if(inputStream == null){
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

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage_ = primaryStage;

        primaryStage.setTitle("Personalized Automatic Speech Recognition");

        initialScene();

        primaryStage.show();

        Console.create(primaryStage).show();

        primaryStage.requestFocus();
    }

    @Override
    public void initialScene(){
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
    public void newCorpus (String emailAddress, String password) {
        emailAddress_ = emailAddress;
        password_ = password;

        if(!emailAddress.endsWith("@gmail.com")){
            Console console = Console.getInstance();
            console.postMessage("At the moment, only gmail addresses are supported.");
            console.postMessage(
                "Please, make sure that the provided address ends with @gmail.com" +
                    " and is a valid gmail address."
            );
            return;
        }

        try {
            emailFetcher_ = new GMailFetcher();
        } catch (IOException e) {
            logger_.severe("Could not load email fetcher properties resource file.\n" +
                "The file might be missing or be corrupted.\n" +
                "Application will terminate.\n" +
                "Message: " + e.getMessage());
            Platform.exit();

            // Make sure that no other command is executed
            return;
        }

        try {
            emailFetcher_.open(emailAddress, password);
        } catch (NoSuchProviderException e) {
            logger_.log(Level.SEVERE, "A provided for the given email protocol was not found.\n" +
                "Application will terminate.", e);

            Platform.exit();
        } catch (AuthenticationFailedException e) {
            Console.getInstance().postMessage(
                "The provided email address and password were incorrect"
            );

            return;
        } catch (IllegalStateException e) {
            logger_.log(Level.SEVERE, "The email service is already connected.\n" +
                "Application will terminate.", e);

            Platform.exit();
        } catch (MessagingException e) {
            logger_.log(Level.SEVERE, "Something went wrong with the email service.\n" +
                "Application will terminate.", e);

            Platform.exit();
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

    @Override
    public EmailFetcher getEmailFetcher(){
        return emailFetcher_;
    }

    @Override
    public void processEmail(List<Email> emails){
        emails_ = emails;

        try {
            primaryStage_.setScene(sceneFactory_.create(SceneFactory.Scenes.LDA_SCENE, this));
        } catch (IOException e) {
            // TODO Act appropriately
            e.printStackTrace();
        }
    }

    @Override
    public List<Email> getEmails(){
        return emails_;
    }

    @Override
    public void record(int corpusID){
        corpusID_ = corpusID;

        try {
            primaryStage_.setScene(sceneFactory_.create(SceneFactory.Scenes.RECORD_SCENE, this));
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
    }

    @Override
    public void dictate(int corpusID){
        DataBase.getInstance().newAcousticModel();

        corpusID_ = corpusID;

        try {
            primaryStage_.setScene(sceneFactory_.create(SceneFactory.Scenes.DICTATE_SCENE, this));
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
    }

    @Override
    public int getCorpusID(){
        return corpusID_;
    }

    @Override
    public void stop() throws Exception {
        if(emailFetcher_ != null){
            emailFetcher_.terminate();
        }

        sceneFactory_.getCurrentController().terminate();

        DataBase.getInstance().close();
    }

}
