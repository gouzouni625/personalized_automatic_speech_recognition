package org.pasr.gui.controllers.dialog;

import org.pasr.gui.dialog.Dialog;


/**
 * @class Controller
 * @brief Abstract Controller for a custom Dialog
 *        Each controller class in this package should extend this class.
 *
 * @param <T>
 *     The value type of the Dialog of this controller
 */
abstract class Controller<T> {

    /**
     * @brief Constructor
     *
     * @param dialog
     *     The Dialog of this Controller
     */
    Controller (Dialog<T> dialog) {
        dialog_ = dialog;
    }

    Dialog<T> dialog_; //!< The Dialog of this Controller

}
