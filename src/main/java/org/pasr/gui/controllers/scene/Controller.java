package org.pasr.gui.controllers.scene;


import java.util.logging.Logger;


// Each controller class in this package should extend this class
public abstract class Controller {
    Controller(API api){
        api_ = api;
    }

    public interface API{}

    API api_;

    public void terminate() {}

    protected Logger getLogger(){
        return logger_;
    }

    private final Logger logger_ = Logger.getLogger(getClass().getName());

}
