package org.pasr.gui.controllers.scene;

// Each controller class in this package should extend this class
public abstract class Controller {
    Controller(API api){
        api_ = api;
    }

    public interface API{}

    API api_;

    public void terminate() throws Exception {}

}
