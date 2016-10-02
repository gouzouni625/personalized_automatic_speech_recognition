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


/**
 * @class SceneFactory
 * @brief Singleton that is used to create the JavaFX scenes and properly instantiate the
 *        corresponding controllers
 */
class SceneFactory {

    /**
     * @brief Default Constructor
     *        Made private to prevent instantiation
     */
    private SceneFactory () {
    }

    /**
     * @brief Returns the instance of this singleton
     *
     * @return The instance of this singleton
     */
    static SceneFactory getInstance () {
        return instance_;
    }

    /**
     * @brief Creates a new Scene
     *
     * @param scene
     *     The scene to create
     * @param api
     *     The API implementation to inject to the scene controller
     *
     * @return The created Scene
     *
     * @throws IOException If the fxml file of the scene cannot be loaded
     */
    Scene create (Scenes scene, API api) throws IOException {
        URL location = getResource(scene.getFXMLResource());

        if (location == null) {
            throw new IOException("getResource(" + scene.getFXMLResource() + ") returned null");
        }

        FXMLLoader loader = new FXMLLoader(location);
        switch (scene) {
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

    /**
     * @class Scenes
     * @brief Holds the different scenes of the application
     */
    enum Scenes {
        MAIN_SCENE("/fxml/scene/main.fxml"),
        EMAIL_LIST_SCENE("/fxml/scene/email_list.fxml"),
        LDA_SCENE("/fxml/scene/lda.fxml"),
        RECORD_SCENE("/fxml/scene/record.fxml"),
        DICTATE_SCENE("/fxml/scene/dictate.fxml"),
        INTERMEDIATE_SCENE("/fxml/scene/intermediate.fxml");

        /**
         * @brief Constructor
         *
         * @param fXMLResource
         *     The path of the fxml file for this scene
         */
        Scenes (String fXMLResource) {
            fXMLResource_ = fXMLResource;
        }

        /**
         * @brief Returns the path of the fxml file for this scene
         *
         * @return The path of the fxml file for this scene
         */
        public String getFXMLResource () {
            return fXMLResource_;
        }

        private String fXMLResource_; //!< The path of the fxml file for this scene
    }

    /**
     * @brief Returns the last created Controller
     *
     * @return The last created Controller
     */
    Controller getCurrentController () {
        return currentController_;
    }

    private static SceneFactory instance_ = new SceneFactory(); //!< The instance of this singleton

    private Controller currentController_; //!< The last controller created

}
