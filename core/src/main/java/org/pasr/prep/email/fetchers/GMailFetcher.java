package org.pasr.prep.email.fetchers;


import org.pasr.prep.email.Email;
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
        fetchingThreadRunning_ = true;
        fetchingThread_ = new Thread(this :: fetchMessages);
        fetchingThread_.start();
    }

    private void fetchMessages() {
        for(Folder folder : folders_){
            if (! fetchingThreadRunning_){
                return;
            }

            String folderName;
            Email[] emails;

            if(notUsableFolder(folder)){
                continue;
            }

            folderName = folder.getFullName();

            try {
                folder.open(Folder.READ_ONLY);

                Message[] messages = folder.getMessages();

                int numberOfMessages = messages.length;
                emails = new Email[numberOfMessages];

                for(int i = 0;i < numberOfMessages;i++){
                    String messageSubject = messages[i].getSubject();

                    Object messageContent = messages[i].getContent();
                    String messageBody;
                    if(messageContent instanceof String){
                        messageBody = (String) messageContent;
                    }
                    else{
                        messageBody = ((Multipart)(messages[i].getContent())).
                            getBodyPart(0).getContent().toString();
                    }

                    emails[i] = new Email(messageSubject, messageBody);
                }

                folder.close(false);
            } catch (MessagingException | IOException e) {
                emails = new Email[0];
            }

            setChanged();
            notifyObservers(new org.pasr.prep.email.Folder(folderName, emails));
        }
    }

    private boolean notUsableFolder (Folder folder){
        return folder.getFullName().equals("[Gmail]");
    }

    @Override
    public void close() throws InterruptedException, MessagingException {
        fetchingThreadRunning_ = false;

        fetchingThread_.join();

        store_.close();
    }

    private Thread fetchingThread_;
    private volatile boolean fetchingThreadRunning_ = false;

    private volatile Store store_;
    private Folder[] folders_;

}
