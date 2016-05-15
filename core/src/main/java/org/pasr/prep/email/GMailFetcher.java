package org.pasr.prep.email;


import org.pasr.utilities.Utilities;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;


public class GMailFetcher extends EmailFetcher{
    public GMailFetcher (String address, String password) throws IOException, MessagingException {
        Properties properties = new Properties();
        properties.load(Utilities.getResourceStream("/email/gmail-smtp.properties"));

        Session session = Session.getDefaultInstance(properties, null);

        store_ = session.getStore("imaps");
        store_.connect(properties.getProperty("mail.smtp.host"), address, password);

        folders_ = store_.getDefaultFolder().list("*");
        messages_ = new HashMap<>();
    }

    public void fetch(){
        new Thread(this :: fetchMessages).start();
    }

    private void fetchMessages(){
        for(Folder folder : folders_){
            try {
                if(checkFolder(folder)){
                    continue;
                }

                String folderName = folder.getFullName();

                folder.open(Folder.READ_ONLY);

                messages_.put(folderName, folder.getMessages());

                setChanged();
                notifyObservers(new EmailFolder(folderName, getEmailsSubjects(folderName)));
            } catch (MessagingException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkFolder(Folder folder){
        return folder.getFullName().equals("[Gmail]");
    }

    @Override
    public String[] getFoldersNames () throws MessagingException {
        int numberOfFolders = folders_.length;
        String[] folderNames = new String[numberOfFolders - 1];

        int index = 0;
        for (Folder folder : folders_) {
            if (checkFolder(folder)) {
                continue;
            }

            folderNames[index] = folder.getFullName();
            index++;
        }

        return folderNames;
    }

    @Override
    public String[] getEmailsSubjects (String folderFullName) throws MessagingException, IOException {
        Message[] messages = messages_.get(folderFullName);

        if(messages == null){
            return new String[0];
        }

        int numberOfMessages = messages.length;
        String[] subjects = new String[numberOfMessages];

        for(int i = 0;i < numberOfMessages;i++){
            subjects[i] = messages[i].getSubject();
        }

        return subjects;
        //Folder folder = store_.getFolder(folderFullName);
        //folder.open(Folder.READ_ONLY);
        //
        //Message[] messages = folder.getMessages();
        //
        //int numberOfMessages = messages.length;
        //Email[] emails = new Email[numberOfMessages];
        //
        //for(int i = 0;i < numberOfMessages;i++){
        //    String subject = messages[i].getSubject();
        //    String body = ((Multipart)(messages[i].getContent())).getBodyPart(1).getContent().toString();
        //
        //    emails[i] = new Email(subject, body);
        //}
        //
        //return emails;
    }

    @Override
    public void close() throws MessagingException {
        for(Folder folder : folders_){
            // There should be no deleted messages but use expunge = false just in case.
            folder.close(false);
        }

        store_.close();
    }

    private Store store_;

    private Folder[] folders_;

    private HashMap<String, Message[]> messages_;

}
