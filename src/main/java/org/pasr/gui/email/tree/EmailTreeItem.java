package org.pasr.gui.email.tree;


import javafx.scene.control.TreeItem;
import org.pasr.prep.email.fetchers.Email;

import java.util.HashSet;
import java.util.Set;


class EmailTreeItem extends TreeItem<Value> {
    EmailTreeItem(){
        super();
    }

    EmailTreeItem(Value value){
        super(value);
    }

    void expandUpToRoot(){
        setExpanded(true);

        TreeItem<Value> parent = getParent();
        while(parent != null){
            parent.setExpanded(true);

            parent = parent.getParent();
        }
    }

    void setSelectedDownToLeaves(boolean selected){
        getChildren().stream()
            .forEach(child -> child.getValue().setSelected(selected));
    }

    Set<Email> getSelectedEmails(){
        Set<Email> emailSet = new HashSet<>();

        for(TreeItem<Value> child : getChildren()){
            Value value = child.getValue();

            if(value.isEmail()){
                if(value.isSelected()){
                    emailSet.add(((EmailValue) value).getEmail());
                }
            }
            else{
                emailSet.addAll(((EmailTreeItem) child).getSelectedEmails());
            }
        }

        return emailSet;
    }

    @Override
    public boolean equals(Object o){
        if(! (o instanceof TreeItem<?>)){
            return false;
        }

        Object objectValue = ((TreeItem) o).getValue();

        if(! (objectValue instanceof Value)){
            return false;
        }

        Value value = (Value) objectValue;

        if(value.isFolder()){
            Value thisValue = getValue();

            return thisValue.isFolder() && thisValue.toString().equals(value.toString());
        }

        if(value.isEmail()){
            Value thisValue = getValue();

            return thisValue.isEmail()
                && ((EmailValue) thisValue).getEmail().equals(((EmailValue) value).getEmail());
        }

        return false;
    }

    @Override
    public int hashCode(){
        Value value = getValue();

        if(value.isFolder()){
            return value.toString().hashCode();
        }
        else{
            return ((EmailValue) value).getEmail().hashCode();
        }
    }

}
