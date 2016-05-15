package org.pasr.prep.email;


public class EmailFolder {
    public EmailFolder(String name, String[] emailsSubjects){
        name_ = name;
        emailsSubjects_ = emailsSubjects;
    }

    public String getName(){
        return name_;
    }

    public String[] getEmails(){
        return emailsSubjects_;
    }

    private final String name_;
    private final String[] emailsSubjects_;

}
