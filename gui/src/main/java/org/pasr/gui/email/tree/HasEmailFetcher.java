package org.pasr.gui.email.tree;

import java.util.Observer;


/**
 * @class HasEmailFetcher
 * @brief Defines the API that should be implemented by a class that has an EmailFetcher
 *        This interface is implemented by EmailTreePane in order to connect its FolderValue objects
 *        with the EmailFetcher it holds. In order to avoid providing the same EmailFetcher to every
 *        FolderValue, the HasEmailFetcher implementation is injected in every FolderValue
 *        constructor. That way, each FolderValue can call fetch to fetch themselves or stop to
 *        stop fetching themselves. They can also add them selves as observers and get notified when
 *        another FolderValue is downloading. That way, each FolderValue knows its state at any time
 *        and thus it can configure its visualisation accordingly.
 */
interface HasEmailFetcher {

    /**
     * @brief Fetches the contents of the folder with the given path
     *
     * @param path
     *     The path of the folder to fetch
     */
    void fetch (String path);

    /**
     * @brief Stops fetching
     */
    void stop ();

    /**
     * @brief Adds a new Observer
     *
     * @param observer
     *     The Observer to be added
     */
    void addObserver (Observer observer);

}
