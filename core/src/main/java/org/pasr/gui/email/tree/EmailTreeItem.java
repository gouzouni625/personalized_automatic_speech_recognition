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
        for(TreeItem<Value> child : getChildren()){
            child.getValue().setSelected(selected);
        }
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

}
