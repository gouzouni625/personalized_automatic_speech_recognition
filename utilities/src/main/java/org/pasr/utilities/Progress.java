package org.pasr.utilities;

import java.util.Observable;


/**
 * @class Progress
 * @brief Implements the Observable progress of a Dictionary processing by a Corpus
 */
public class Progress extends Observable {

    /**
     * @brief Default Constructor
     */
    public Progress () {
    }

    /**
     * @brief Sets the value of the Progress
     *
     * @param value
     *     The new value
     */
    public void setValue (double value) {
        setChanged();
        notifyObservers(value);
    }

}
