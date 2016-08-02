package org.pasr.gui.controllers.dialog;


import org.pasr.gui.dialog.Dialog;


// Each controller class in this package should extend this class
abstract class Controller<T> {
    Controller(Dialog<T> dialog){
        dialog_ = dialog;
    }

    Dialog<T> dialog_;

}
