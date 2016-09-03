package org.pasr.gui.email.tree;


public interface Value {
    boolean isSelected();
    void setSelected(boolean selected);

    boolean isEmail();
    boolean isFolder();

}
