package org.pasr.gui.email.tree;


import javafx.scene.control.TreeItem;
import org.pasr.prep.email.fetchers.Email;
import org.pasr.prep.email.fetchers.Folder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class EmailTreeItem extends TreeItem<String> {
    public EmailTreeItem(Folder folder){
        super(folder.getName());

        isFolder_ = true;
        folder_ = folder;

        email_ = null;

        getChildren().addAll(
            folder.getEmails().stream().map(EmailTreeItem::new).collect(Collectors.toList())
        );
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

    public List<Email> getEmails(){
        ArrayList<Email> emails = new ArrayList<>();

        if(isFolder()){
            emails.addAll(folder_.getEmails());
        }
        else{
            emails.add(email_);
        }

        return emails;
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
