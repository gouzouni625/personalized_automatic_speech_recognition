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
import org.pasr.utilities.logging.PrettyFormatter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.pasr.utilities.Utilities.getResourceStream;


public class MainView extends Application implements MainController.API,
    EmailListController.API, LDAController.API, RecordController.API, DictateController.API {

    private static Logger logger_ = Logger.getLogger(MainView.class.getName());

    private Stage primaryStage_;

    private String emailAddress_;
    private String password_;

    private List<Email> emails_;

    private int corpusID_;

    private SceneFactory sceneFactory_ = SceneFactory.getInstance();

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

        try {
            primaryStage_.setScene(
                sceneFactory_.create(SceneFactory.Scenes.EMAIL_LIST_SCENE, this)
            );
        } catch (IOException e) {
            // TODO Act appropriately
            e.printStackTrace();
        }
    }

    @Override
    public void dictate () {
        // TODO Implement
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
    public void back(){
        // TODO Implement
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
        sceneFactory_.getCurrentController().terminate();

        DataBase.getInstance().close();
    }

}
