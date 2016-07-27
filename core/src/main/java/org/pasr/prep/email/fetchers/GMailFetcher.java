package org.pasr.prep.email.fetchers;


import org.pasr.utilities.Utilities;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import java.io.IOException;
import java.util.ArrayList;
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

            if(notUsableFolder(folder)){
                continue;
            }

            String folderFullName = folder.getFullName();
            Message[] messages;

            try {
                folder.open(Folder.READ_ONLY);
                messages = folder.getMessages();
            } catch (MessagingException e) {
                // TODO debug information: could not get folder messages
                continue;
            }

            int numberOfMessages = messages.length;
            ArrayList<Email> emails = new ArrayList<>();

            for(int i = 0;i < numberOfMessages;i++){
                String messageSubject;
                try {
                    messageSubject = messages[i].getSubject();
                } catch (MessagingException e) {
                    // TODO debug information: could not get message subject
                    continue;
                }

                String[] senders;
                try {
                    Address[] addresses = messages[i].getFrom();
                    if(addresses == null){
                        senders = new String[0];
                    }
                    else {
                        int numberOfSenders = addresses.length;

                        senders = new String[numberOfSenders];
                        for (int j = 0; j < numberOfSenders; j++) {
                            senders[j] = addresses[j].toString();
                        }
                    }
                } catch (MessagingException e) {
                    // TODO debug information: could not get message senders
                    continue;
                }

                String[] tORecipients;
                try {
                    Address[] addresses = messages[i].getRecipients(Message.RecipientType.TO);
                    if(addresses == null){
                        tORecipients = new String[0];
                    }
                    else {
                        int numberOfRecipients = addresses.length;

                        tORecipients = new String[numberOfRecipients];
                        for (int j = 0; j < numberOfRecipients; j++) {
                            tORecipients[j] = addresses[j].toString();
                        }
                    }
                } catch (MessagingException e) {
                    // TODO debug information: could not get message "TO" recipients
                    continue;
                }

                String[] cCRecipients;
                try {
                    Address[] addresses = messages[i].getRecipients(Message.RecipientType.CC);
                    if(addresses == null){
                        cCRecipients = new String[0];
                    }
                    else {
                        int numberOfRecipients = addresses.length;

                        cCRecipients = new String[numberOfRecipients];
                        for (int j = 0; j < numberOfRecipients; j++) {
                            cCRecipients[j] = addresses[j].toString();
                        }
                    }
                } catch (MessagingException e) {
                    // TODO debug information: could not get message "CC" recipients
                    continue;
                }

                String[] bCCRecipients;
                try {
                    Address[] addresses = messages[i].getRecipients(Message.RecipientType.BCC);
                    if(addresses == null){
                        bCCRecipients = new String[0];
                    }
                    else {
                        int numberOfRecipients = addresses.length;

                        bCCRecipients = new String[numberOfRecipients];
                        for (int j = 0; j < numberOfRecipients; j++) {
                            bCCRecipients[j] = addresses[j].toString();
                        }
                    }
                } catch (MessagingException e) {
                    // TODO debug information: could not get message "BCC" recipients
                    continue;
                }

                String messageReceivedDate;
                try {
                    messageReceivedDate = messages[i].getSentDate().toString();
                } catch (MessagingException e) {
                    // TODO debug information: could not get message received date
                    continue;
                }

                Object messageContent;
                String messageBody;
                try {
                    messageContent = messages[i].getContent();

                    if(messageContent instanceof String){
                        messageBody = (String) messageContent;
                    }
                    else if(messageContent instanceof Multipart){
                        messageBody = getBodyFromMultiPart((Multipart) messageContent);
                    }
                    else{
                        messageBody = "";
                    }
                } catch (IOException | MessagingException e) {
                    // TODO debug information: could not get message content
                    continue;
                }

                emails.add(new Email(senders, tORecipients, cCRecipients, bCCRecipients,
                    messageReceivedDate, messageSubject, messageBody));
            }

            try {
                folder.close(false);
            } catch (MessagingException e) {
                // TODO debug information: could not close the folder normally
            }

            setChanged();
            notifyObservers(new org.pasr.prep.email.fetchers.Folder(folderFullName, emails));
        }
    }

    private String getBodyFromMultiPart(Multipart multipart) throws MessagingException, IOException {
        StringBuilder stringBuilder = new StringBuilder();

        Object content = multipart.getBodyPart(0).getContent();
        if(content instanceof String){
            stringBuilder.append(content);
        }
        else if(content instanceof Multipart){
            stringBuilder.append(getBodyFromMultiPart((Multipart) content));
        }

        return stringBuilder.toString();
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
