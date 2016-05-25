package org.pasr.prep.email;


import org.pasr.utilities.Utilities;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import java.io.IOException;
import java.util.Properties;


public class GMailFetcher extends EmailFetcher{
    public GMailFetcher (String address, String password) throws IOException, MessagingException {
        Properties properties = new Properties();
        properties.load(Utilities.getResourceStream("/email/gmail-smtp.properties"));

        Session session = Session.getDefaultInstance(properties, null);

        store_ = session.getStore("imaps");
        store_.connect(properties.getProperty("mail.smtp.host"), address, password);

        folders_ = store_.getDefaultFolder().list("*");
    }

    public void fetch(){
        fetcherRunning_ = true;
        new Thread(this :: fetchMessages).start();
    }

    private void fetchMessages() {
        for(Folder folder : folders_){
            if (!fetcherRunning_){
                return;
            }

            try {
                if(checkFolder(folder)){
                    continue;
                }

                String folderName = folder.getFullName();

                folder.open(Folder.READ_ONLY);

                Message[] messages = folder.getMessages();

                int numberOfMessages = messages.length;
                RecentFolder.Email[] emails = new RecentFolder.Email[numberOfMessages];

                for(int i = 0;i < numberOfMessages;i++){
                    emails[i] = new RecentFolder.Email(messages[i].getSubject(),
                        ((Multipart)(messages[i].getContent())).getBodyPart(0).getContent().toString());
                }

                setChanged();
                notifyObservers(new RecentFolder(folderName, emails));

                folder.close(false);
            } catch (MessagingException | IOException e) {
                e.printStackTrace();
            }
        }

        try {
            store_.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private boolean checkFolder(Folder folder){
        return folder.getFullName().equals("[Gmail]");
    }

    @Override
    public void close() {
        fetcherRunning_ = false;
    }

    private volatile Store store_;

    private Folder[] folders_;

    private volatile boolean fetcherRunning_ = false;

}
