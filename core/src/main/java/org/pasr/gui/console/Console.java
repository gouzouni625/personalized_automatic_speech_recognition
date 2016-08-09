package org.pasr.gui.console;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import static org.pasr.utilities.Utilities.getResource;


public class Console extends Stage implements Runnable {
    private Console (Window owner){
        initOwner(owner);

        initStyle(StageStyle.UTILITY);

        setTitle("Message Console");

        try {
            URL location = getResource("/fxml/console/view.fxml");

            if(location == null){
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


        setX(owner.getX() + owner.getWidth() / 4);
        setY(owner.getY() + owner.getHeight() / 4);

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
    public void initialize(){
        textArea.setText("Console initiated...\nApplication messages will be printed here!");
    }

    @Override
    public void run(){
        while(true){
            try {
                String message = messageQueue_.take();

                textArea.appendText("\n" + message);
                textArea.setScrollTop(Double.MAX_VALUE);
            } catch (InterruptedException e) {
                // If interrupted while waiting on the queue, return
                break;
            }
        }

        logger_.fine("Console thread shut down gracefully!");
    }

    public static Console create(Window owner){
        instance_ = new Console(owner);

        return instance_;
    }

    public void postMessage(String message){
        // Since the queue has practically infinite size, no need to check if the message was
        // inserted
        messageQueue_.offer(message);
    }

    public static Console getInstance () {
        return instance_;
    }

    @FXML
    private TextArea textArea;

    private static Console instance_;

    private Thread thread_;

    private LinkedBlockingQueue<String> messageQueue_;

    private static Logger logger_ = Logger.getLogger(Console.class.getName());

}
