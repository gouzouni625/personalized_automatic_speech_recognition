package org.pasr.gui;


import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import org.pasr.gui.controllers.Controller;
import org.pasr.gui.controllers.Controller.API;
import org.pasr.gui.controllers.EmailListSceneController;
import org.pasr.gui.controllers.MainSceneController;

import java.io.IOException;

import static org.pasr.utilities.Utilities.getResource;


class SceneFactory {
    private SceneFactory () {}

    static SceneFactory getInstance () {
        return instance_;
    }

    Scene create(Scenes scene, API api) throws IOException {
        FXMLLoader loader = new FXMLLoader(getResource(scene.getFXMLResource()));
        Controller controller;

        switch(scene){
            case MAIN_SCENE:
                controller = new MainSceneController(api);
                loader.setController(controller);
                break;
            case EMAIL_LIST_SCENE:
                controller = new EmailListSceneController(api);
                loader.setController(controller);
                break;
        }

        return new Scene(loader.load());
    }

    enum Scenes{
        MAIN_SCENE("/fxml/main_scene.fxml"),
        EMAIL_LIST_SCENE("/fxml/email_list_scene.fxml");

        Scenes(String fXMLResource){
            fXMLResource_ = fXMLResource;
        }

        public String getFXMLResource(){
            return fXMLResource_;
        }

        private String fXMLResource_;
    }

    private static SceneFactory instance_ = new SceneFactory();

}
