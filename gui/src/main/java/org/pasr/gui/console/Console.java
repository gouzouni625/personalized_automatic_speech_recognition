package org.pasr.gui.console;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import static org.pasr.utilities.Utilities.getResource;


/**
 * @class Console
 * @brief The console acts as an independent JavaFX Stage
 *        It is used to print messages to the user. In the future this will be replaced by dialogs.
 */
public class Console extends Stage implements Runnable {

    /**
     * @brief Constructor
     *        Made private to prevent instantiation
     *
     * @param owner
     *     The owner window of this Console
     */
    private Console (Window owner) {
        initOwner(owner);

        initStyle(StageStyle.UTILITY);

        setTitle("Message Console");

        try {
            URL location = getResource("/fxml/console/view.fxml");

            if (location == null) {
                throw new IOException("getResource(\"/fxml/console/view.fxml\") returned null");
            }

            FXMLLoader loader = new FXMLLoader(location);
            loader.setController(this);

            setScene(new Scene(loader.load()));
        } catch (IOException e) {
            logger_.severe("Could not load resource:/fxml/console/view.fxml\n" +
                "The file might be missing or be corrupted.\n" +
                "Application will terminate.\n" +
                "Exception Message: " + e.getMessage());
            Platform.exit();
        }

        setX(owner.getX() + owner.getWidth() / 8);
        setY(owner.getY() + owner.getHeight() / 2);

        messageQueue_ = new LinkedBlockingQueue<>();

        thread_ = new Thread(this);
        thread_.setDaemon(true);
        thread_.start();

        setOnHidden(event -> {
            thread_.interrupt();

            try {
                // Don't wait forever on this thread since it is a daemon and will not block the JVM
                // from shutting down
                thread_.join(3000);
            } catch (InterruptedException e) {
                logger_.warning("Interrupted while joining console thread.");
            }
        });
    }

    @FXML
    public void initialize () {
        textArea.setText("Console initiated...\nApplication messages will be printed here!");

        button.setOnAction(this :: buttonOnAction);
    }

    /**
     * @brief Clear button on action listener
     *
     * @param actionEvent
     *     The actionEvent
     */
    private void buttonOnAction (ActionEvent actionEvent) {
        textArea.clear();
    }

    @Override
    public void run () {
        logger_.info("Console thread started!");

        while (true) {
            try {
                String message = messageQueue_.take();

                textArea.appendText("\n\n" + message);
                textArea.setScrollTop(Double.MAX_VALUE);
            } catch (InterruptedException e) {
                // If interrupted while waiting on the queue, return
                break;
            }
        }

        logger_.info("Console thread shut down gracefully!");
    }

    /**
     * @brief Creates the Console singleton
     *
     * @param owner
     *     The owner window of the console
     *
     * @return The instance of the Console
     */
    public static Console create (Window owner) {
        if (instance_ == null) {
            instance_ = new Console(owner);
        }

        return instance_;
    }

    /**
     * @brief Adds a message to the message queue
     *
     * @param message
     *     The message to be added to the message queue
     */
    public void postMessage (String message) {
        // Since the queue has practically infinite size, no need to check if the message was
        // inserted
        messageQueue_.offer(message);
    }

    /**
     * @brief Returns the instance of this singleton
     *
     * @return The instance of this singleton
     */
    public static Console getInstance () {
        return instance_;
    }

    @FXML
    private TextArea textArea; //!< The TextArea where messages get printed

    @FXML
    private Button button; //!< The clear button of the console

    private static Console instance_; //!< The instance of this singleton

    private Thread thread_; //!< Thread used to read messages from a queue and post them on the
                            //!< text area

    private LinkedBlockingQueue<String> messageQueue_; //!< Message queue used as message entrance

    private static Logger logger_ = Logger.getLogger(Console.class.getName()); //!< The logger

}
