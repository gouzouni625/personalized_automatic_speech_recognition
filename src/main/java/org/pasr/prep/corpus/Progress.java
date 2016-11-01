package org.pasr.prep.corpus;

import java.util.Observable;


/**
 * @class Progress
 * @brief Implements the Observable progress of a Dictionary processing by a Corpus
 */
class Progress extends Observable {

    /**
     * @brief Default Constructor
     */
    Progress () {
    }

    /**
     * @brief Sets the value of the Progress
     *
     * @param value
     *     The new value
     */
    void setValue (double value) {
        setChanged();
        notifyObservers(value);
    }

}
