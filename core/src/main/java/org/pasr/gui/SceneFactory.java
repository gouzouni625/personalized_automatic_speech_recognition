package org.pasr.gui;


import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import org.pasr.gui.controllers.scene.Controller;
import org.pasr.gui.controllers.scene.Controller.API;
import org.pasr.gui.controllers.scene.EmailListSceneController;
import org.pasr.gui.controllers.scene.LDASceneController;
import org.pasr.gui.controllers.scene.MainSceneController;

import java.io.IOException;

import static org.pasr.utilities.Utilities.getResource;


class SceneFactory {
    private SceneFactory () {}

    static SceneFactory getInstance () {
        return instance_;
    }

    Scene create(Scenes scene, API api) throws IOException {
        FXMLLoader loader = new FXMLLoader(getResource(scene.getFXMLResource()));
        switch(scene){
            case MAIN_SCENE:
                currentController_ = new MainSceneController(api);
                loader.setController(currentController_);
                break;
            case EMAIL_LIST_SCENE:
                currentController_ = new EmailListSceneController(api);
                loader.setController(currentController_);
                break;
            case LDA_SCENE:
                currentController_ = new LDASceneController(api);
                loader.setController(currentController_);
                break;
        }

        return new Scene(loader.load());
    }

    enum Scenes{
        MAIN_SCENE("/fxml/main_scene.fxml"),
        EMAIL_LIST_SCENE("/fxml/email_list_scene.fxml"),
        LDA_SCENE("/fxml/lda_scene.fxml");

        Scenes(String fXMLResource){
            fXMLResource_ = fXMLResource;
        }

        public String getFXMLResource(){
            return fXMLResource_;
        }

        private String fXMLResource_;
    }

    public Controller getCurrentController(){
        return currentController_;
    }

    private static SceneFactory instance_ = new SceneFactory();

    private Controller currentController_;

}
