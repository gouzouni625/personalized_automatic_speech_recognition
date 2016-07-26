package org.pasr.gui.email.tree;


import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.pasr.prep.email.fetchers.Email;
import org.pasr.prep.email.fetchers.Folder;


public class EmailTreeItem extends TreeItem<String> {
    public EmailTreeItem(Folder folder){
        super(folder.getName());

        isFolder_ = true;
        folder_ = folder;

        email_ = null;

        ObservableList<TreeItem<String>> children = getChildren();
        for(Email email : folder.getEmails()){
            children.add(new EmailTreeItem(email));
        }
    }

    public EmailTreeItem(Email email){
        super(email.getSubject());

        isFolder_ = false;
        folder_ = null;

        email_ = email;
    }

    @Override
    public String toString(){
        if(isFolder()){
            return folder_.getName();
        }
        else{
            return email_.getSubject();
        }
    }

    public Folder getFolder(){
        return folder_;
    }

    public Email getEmail(){
        return email_;
    }

    public boolean isFolder(){
        return isFolder_;
    }

    private final Folder folder_;
    private final Email email_;

    private final boolean isFolder_;

}
