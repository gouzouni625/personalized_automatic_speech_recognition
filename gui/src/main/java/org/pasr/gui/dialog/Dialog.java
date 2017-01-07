package org.pasr.gui.dialog;

import javafx.stage.Stage;


/**
 * @class Dialog
 * @brief Custom implementation of a Dialog as a JavaFX Stage
 *        Custom dialogs are implemented for flexibility against custom JavaFX dialogs.
 *
 * @param <T>
 *     The value type of this Dialog
 */
public abstract class Dialog<T> extends Stage {

    /**
     * @brief Constructor
     *
     * @param defaultValue
     *     The default value of this Dialog
     */
    Dialog (T defaultValue) {
        value_ = defaultValue;
    }

    /**
     * @brief Sets the value of this Dialog
     *        After the value of a Dialog is set, the Dialog is closed.
     *
     * @param value
     *     The new value of this Dialog
     */
    public void setValue (T value) {
        value_ = value;

        close();
    }

    /**
     * @brief Returns the value of this Dialog
     *
     * @return The value of this Dialog
     */
    public T getValue () {
        return value_;
    }

    private T value_; //!< The value of this Dialog

}
