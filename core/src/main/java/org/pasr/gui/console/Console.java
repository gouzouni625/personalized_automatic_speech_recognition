package org.pasr.gui.console;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;

import static org.pasr.utilities.Utilities.getResource;


public class Console extends Stage {
    private Console (Window owner){
        initOwner(owner);

        // Remove title bar
        initStyle(StageStyle.UTILITY);

        setTitle("Message Console");

        FXMLLoader loader = new FXMLLoader(getResource("/fxml/console/view.fxml"));
        loader.setController(this);

        try {
            setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        setX(owner.getX());
        setY(owner.getY() + owner.getHeight());
    }

    public static Console create(Window owner){
        instance_ = new Console(owner);

        return instance_;
    }

    public static Console getInstance () {
        return instance_;
    }

    private static Console instance_;

}
