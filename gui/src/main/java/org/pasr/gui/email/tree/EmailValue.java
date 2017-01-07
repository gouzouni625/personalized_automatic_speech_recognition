package org.pasr.gui.email.tree;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.pasr.utilities.email.fetchers.Email;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import static org.pasr.utilities.Utilities.getResource;


/**
 * @class EmailValue
 * @brief Implements a Value wrapping an Email
 *        The user can select an EmailValue or un-select it.
 */
public class EmailValue extends AnchorPane implements Value {

    /**
     * @brief Constructor
     *
     * @param email
     *     The Email of this EmailValue
     */
    EmailValue (Email email) {
        email_ = email;

        try {
            URL location = getResource("/fxml/tree/email_value.fxml");

            if (location == null) {
                throw new IOException("getResource(\"/fxml/tree/email_value.fxml\") returned null");
            }

            FXMLLoader loader = new FXMLLoader(location);
            loader.setRoot(this);
            loader.setController(this);

            loader.load();
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).severe(
                "Could not load resource:/fxml/tree/email_value.fxml\n" +
                    "The file might be missing or be corrupted.\n" +
                    "Application will terminate.\n" +
                    "Exception Message: " + e.getMessage()
            );
            Platform.exit();
        }
    }

    @FXML
    public void initialize () {
        label.setText(email_.getSubject());
    }

    public Email getEmail () {
        return email_;
    }

    @Override
    public boolean isSelected () {
        return checkBox.isSelected();
    }

    @Override
    public void setSelected (boolean selected) {
        checkBox.setSelected(selected);
    }

    @Override
    public boolean isEmail () {
        return true;
    }

    @Override
    public boolean isFolder () {
        return false;
    }

    @Override
    public String toString () {
        return email_.getSubject();
    }

    @FXML
    private CheckBox checkBox;

    @FXML
    private Label label;

    private Email email_;

}
