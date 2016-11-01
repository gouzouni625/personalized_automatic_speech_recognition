package org.pasr.gui.controllers.scene;

import java.util.logging.Logger;


/**
 * @class Controller
 * @brief Abstract controller for the JavaFX scenes of this application
 *        Each controller class in this package should extend this class
 */
public abstract class Controller {

    /**
     * @brief Constructor
     *
     * @param api
     *     The implementation of the API of this Controller
     */
    Controller (API api) {
        api_ = api;
    }

    /**
     * @class API
     * @brief The API that this Controller needs to communicate with the outside world
     */
    public interface API {

    }

    API api_; //!< The API implementation of this Controller

    /**
     * @brief Terminates this Controller releasing all of its resources
     */
    public void terminate () {
    }

    /**
     * @brief Returns the Logger of this Controller
     *
     * @return The Logger of this Controller
     */
    protected Logger getLogger () {
        return logger_;
    }

    private final Logger logger_ = Logger.getLogger(getClass().getName()); //!< The Logger of this
                                                                           //!< Controller

}
