package org.pasr.gui.controllers;

// Each controller class should extend this one
public abstract class Controller {
    Controller(API api){
        api_ = api;
    }

    public interface API{}

    API api_;

}
