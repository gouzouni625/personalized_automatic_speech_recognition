package org.pasr.gui.email.tree;

import javafx.scene.control.TreeItem;
import org.pasr.utilities.email.fetchers.Email;

import java.util.HashSet;
import java.util.Set;


/**
 * @class EmailTreeItem
 * @brief Implements a TreeItem holding a Value
 *        This class is used to override comparison between TreeItem objects and also as a way to
 *        signal the children of a TreeItem as a results of an event.
 */
class EmailTreeItem extends TreeItem<Value> {

    /**
     * @brief Default Constructor
     */
    EmailTreeItem () {
        super();
    }

    /**
     * @brief Constructor
     *
     * @param value
     *     The Value of this EmailTreeItem
     */
    EmailTreeItem (Value value) {
        super(value);
    }

    void expandUpToRoot () {
        setExpanded(true);

        TreeItem<Value> parent = getParent();
        while (parent != null) {
            parent.setExpanded(true);

            parent = parent.getParent();
        }
    }

    /**
     * @brief Sets the selected value for all the children of this EmailTreeItem recursively
     *
     * @param selected
     *     The new selected value
     */
    void setSelectedDownToLeaves (boolean selected) {
        getChildren().stream()
            .forEach(child -> child.getValue().setSelected(selected));
    }

    /**
     * @brief Returns the selected Email objects that are contained in this EmailTreeItem
     *
     * @return A Set of the selected Email objects
     */
    Set<Email> getSelectedEmails () {
        Set<Email> emailSet = new HashSet<>();

        for (TreeItem<Value> child : getChildren()) {
            Value value = child.getValue();

            if (value.isEmail()) {
                if (value.isSelected()) {
                    emailSet.add(((EmailValue) value).getEmail());
                }
            }
            else {
                emailSet.addAll(((EmailTreeItem) child).getSelectedEmails());
            }
        }

        return emailSet;
    }

    /**
     * @brief Returns true is this EmailTreeItem is equal to the given Object
     *        This method is overridden to compare two TreeItem objects in Value level and in object
     *        level.
     *
     * @param o
     *     The given Object
     *
     * @return True if this EmailTreeItem is equal to the given object
     */
    @Override
    public boolean equals (Object o) {
        if (! (o instanceof TreeItem<?>)) {
            return false;
        }

        Object objectValue = ((TreeItem) o).getValue();

        if (! (objectValue instanceof Value)) {
            return false;
        }

        Value value = (Value) objectValue;

        if (value.isFolder()) {
            Value thisValue = getValue();

            return thisValue.isFolder() && thisValue.toString().equals(value.toString());
        }

        if (value.isEmail()) {
            Value thisValue = getValue();

            return thisValue.isEmail()
                && ((EmailValue) thisValue).getEmail().equals(((EmailValue) value).getEmail());
        }

        return false;
    }

    /**
     * @brief Returns the hash code of this EmailTreeItem
     *        This method is overridden to make sure that two TreeItem objects with the same Value
     *        have the same hash code.
     *
     * @return The hash code of this EmailTreeItem
     */
    @Override
    public int hashCode () {
        Value value = getValue();

        if (value.isFolder()) {
            return value.toString().hashCode();
        }
        else {
            return ((EmailValue) value).getEmail().hashCode();
        }
    }

}
