package org.pasr.prep.email;


import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Observable;


public abstract class EmailFetcher extends Observable {

    public abstract String[] getFoldersNames() throws MessagingException;
    public abstract String[] getEmailsSubjects(String folder) throws MessagingException, IOException;
    public abstract void fetch();
    public abstract void close() throws MessagingException;

}
