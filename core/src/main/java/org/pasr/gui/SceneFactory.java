package org.pasr.gui;


import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import org.pasr.gui.controllers.scene.Controller;
import org.pasr.gui.controllers.scene.Controller.API;
import org.pasr.gui.controllers.scene.DictateController;
import org.pasr.gui.controllers.scene.EmailListController;
import org.pasr.gui.controllers.scene.IntermediateController;
import org.pasr.gui.controllers.scene.LDAController;
import org.pasr.gui.controllers.scene.MainController;
import org.pasr.gui.controllers.scene.RecordController;

import java.io.IOException;
import java.net.URL;

import static org.pasr.utilities.Utilities.getResource;


class SceneFactory {
    private SceneFactory () {}

    static SceneFactory getInstance () {
        return instance_;
    }

    Scene create(Scenes scene, API api) throws IOException {
        URL location = getResource(scene.getFXMLResource());

        if(location == null){
            throw new IOException("getResource(" + scene.getFXMLResource() +") returned null");
        }

        FXMLLoader loader = new FXMLLoader(location);
        switch(scene){
            case MAIN_SCENE:
                currentController_ = new MainController(api);
                loader.setController(currentController_);
                break;
            case EMAIL_LIST_SCENE:
                currentController_ = new EmailListController(api);
                loader.setController(currentController_);
                break;
            case LDA_SCENE:
                currentController_ = new LDAController(api);
                loader.setController(currentController_);
                break;
            case RECORD_SCENE:
                currentController_ = new RecordController(api);
                loader.setController(currentController_);
                break;
            case DICTATE_SCENE:
                currentController_ = new DictateController(api);
                loader.setController(currentController_);
                break;
            case INTERMEDIATE_SCENE:
                currentController_ = new IntermediateController(api);
                loader.setController(currentController_);
                break;
        }

        return new Scene(loader.load());
    }

    enum Scenes{
        MAIN_SCENE("/fxml/scene/main.fxml"),
        EMAIL_LIST_SCENE("/fxml/scene/email_list.fxml"),
        LDA_SCENE("/fxml/scene/lda.fxml"),
        RECORD_SCENE("/fxml/scene/record.fxml"),
        DICTATE_SCENE("/fxml/scene/dictate.fxml"),
        INTERMEDIATE_SCENE("/fxml/scene/intermediate.fxml");

        Scenes(String fXMLResource){
            fXMLResource_ = fXMLResource;
        }

        public String getFXMLResource(){
            return fXMLResource_;
        }

        private String fXMLResource_;
    }

    Controller getCurrentController(){
        return currentController_;
    }

    private static SceneFactory instance_ = new SceneFactory();

    private Controller currentController_;

}
