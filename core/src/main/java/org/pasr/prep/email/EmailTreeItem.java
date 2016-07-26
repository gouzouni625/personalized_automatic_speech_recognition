package org.pasr.prep.email;


import javafx.scene.control.TreeItem;


public class EmailTreeItem extends TreeItem<String> {
    private EmailTreeItem(String name){
        super(name);

        isFolder_ = true;

        name_ = name;
        subject_ = null;
        body_ = null;
    }

    private EmailTreeItem(String subject, String body){
        super(subject);

        isFolder_ = false;

        name_ = null;

        subject_ = subject;
        body_ = body;
    }

    public static EmailTreeItem createFolder(String name){
        return new EmailTreeItem(name);
    }

    public static EmailTreeItem createEmail(String subject, String body){
        return new EmailTreeItem(subject, body);
    }

    @Override
    public String toString(){
        if(isFolder()){
            return getName();
        }
        else{
            return getSubject();
        }
    }

    public boolean isFolder(){
        return isFolder_;
    }

    public String getName(){
        return name_;
    }

    public String getSubject(){
        return subject_;
    }

    public String getBody(){
        return body_;
    }

    private final String name_;

    private final String subject_;
    private final String body_;

    private final boolean isFolder_;

}
