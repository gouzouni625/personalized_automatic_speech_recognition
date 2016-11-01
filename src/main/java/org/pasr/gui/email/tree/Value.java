package org.pasr.gui.email.tree;


/**
 * @class Value
 * @brief A Value to be held by an EmailTreeItem
 *        It can be either an EmailValue or a FolderValue.
 */
public interface Value {

    /**
     * @brief Returns true if this Value has been selected
     *
     * @return True if this Value has been selected
     */
    boolean isSelected ();

    /**
     * @brief Sets the selected value for this Value
     *
     * @param selected
     *     The new selected value for this Value
     */
    void setSelected (boolean selected);

    /**
     * @brief Returns true if this Value is an EmailValue
     *
     * @return True if this Value is an EmailValue
     */
    boolean isEmail ();

    /**
     * @brief Returns true if this Value is a FolderValue
     *
     * @return True if this Value is a FolderValue
     */
    boolean isFolder ();

}
