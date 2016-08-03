package org.pasr.gui;


import javafx.application.Application;
import javafx.stage.Stage;
import org.pasr.database.DataBase;
import org.pasr.gui.controllers.scene.EmailListController;
import org.pasr.gui.controllers.scene.LDAController;
import org.pasr.gui.controllers.scene.MainController;
import org.pasr.gui.controllers.scene.RecordController;
import org.pasr.prep.email.fetchers.Email;

import java.io.IOException;
import java.util.List;


public class MainView extends Application implements MainController.API,
    EmailListController.API, LDAController.API, RecordController.API {

    private Stage primaryStage_;

    private String emailAddress_;
    private String password_;

    private List<Email> emails_;

    private int corpusID_;

    private SceneFactory sceneFactory_ = SceneFactory.getInstance();

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage_ = primaryStage;

        primaryStage.setTitle("Personalized Automatic Speech Recognition");

        primaryStage.setScene(sceneFactory_.create(SceneFactory.Scenes.MAIN_SCENE, this));

        primaryStage.show();
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
