package org.pasr.prep.email.fetchers;

import javax.mail.MessagingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Observable;
import java.util.Properties;
import java.util.logging.Logger;

import static org.pasr.utilities.Utilities.getResourceStream;


/**
 * @class EmailFetcher
 * @brief Defines the API that every EmailFetcher should implement. This class is created to allow
 *        the integration of many e-mail providers with minimum changes. It is an Observable to
 *        allow notifying the Observer whenever a new Email has arrived.
 */
public abstract class EmailFetcher extends Observable {

    /**
     * @brief Constructor
     *
     * @param propertiesResourcePath
     *     The path for the configuration properties
     *
     * @throws IOException When the configuration file is not found
     */
    EmailFetcher (String propertiesResourcePath) throws IOException {
        properties_ = new Properties();

        InputStream inputStream = getResourceStream(propertiesResourcePath);

        if (inputStream == null) {
            throw new IOException(
                "getResourceStream(" + propertiesResourcePath + ") returned null"
            );
        }

        properties_.load(inputStream);
    }

    /**
     * @brief Opens the EmailFetcher
     *
     * @param address
     *     The e-mail address to connect to
     * @param password
     *     The password of the e-mail address
     *
     * @throws MessagingException If the address-password combinaton is incorrect
     */
    public abstract void open (String address, String password) throws MessagingException;

    /**
     * @brief Returns a Map with the folder names and number of e-mails in each folder
     *
     * @return A Map with the folder names and the number of e-mails in each folder
     */
    public abstract Map<String, Integer> getFolderInfo ();

    /**
     * @brief Fetches count number of e-mails
     *
     * @param count
     *     The number of e-mails to fetch
     */
    public abstract void fetch (int count);

    /**
     * @brief Fetches count number of e-mails from inside the given folder
     *
     * @param folderPath
     *     The path of the folder
     * @param count
     *     The number of e-mails to fetch
     */
    public abstract void fetch (String folderPath, int count);

    /**
     * @brief Stops the fetching process
     */
    public abstract void stop ();

    /**
     * @brief Terminates the EmailFetcher releasing all of its resources
     */
    public abstract void terminate ();

    /**
     * @brief Returns the Logger for this EmailFetcher
     *
     * @return The Logger for this EmailFetcher
     */
    Logger getLogger () {
        return logger_;
    }

    Properties properties_; //!< The Properties of this EmailFetcher

    private final Logger logger_ = Logger.getLogger(getClass().getName()); //!< The Logger of this
                                                                           //!< EmailFetcher

    /**
     * @class Stage
     * @brief Holds the different stages of the fetching process
     */
    public enum Stage {
        STARTED_FETCHING,
        STOPPED_FETCHING
    }

}
