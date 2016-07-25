package org.pasr.gui;


import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.io.IOException;

import static org.pasr.utilities.Utilities.getResource;


class SceneFactory {
    private SceneFactory () {}

    public static SceneFactory getInstance () {
        return instance_;
    }

    public Scene create(Scenes scene) throws IOException {
        return new Scene(new FXMLLoader(getResource(scene.getfXMLResource())).load());
    }

    public enum Scenes{
        MAIN_SCENE("/fxml/main_scene.fxml");

        Scenes(String fXMLResource){
            fXMLResource_ = fXMLResource;
        }

        public String getfXMLResource(){
            return fXMLResource_;
        }

        private String fXMLResource_;
    }

    private static SceneFactory instance_ = new SceneFactory();

}
