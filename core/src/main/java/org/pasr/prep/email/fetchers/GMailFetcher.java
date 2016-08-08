package org.pasr.prep.email.fetchers;


import org.pasr.gui.console.Console;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class GMailFetcher extends EmailFetcher{
    public GMailFetcher () throws IOException {
        super("/email/gmail-smtp.properties");
    }

    @Override
    public void open(String address, String password) throws MessagingException {
        store_ = Session.getDefaultInstance(properties_, null).getStore("imaps");
        store_.connect(properties_.getProperty("mail.smtp.host"), address, password);

        folders_ = store_.getDefaultFolder().list("*");
    }

    @Override
    public void fetch() {
        if(thread_ == null || !thread_.isAlive()) {
            thread_ = new Thread(this);
            thread_.setDaemon(true);
            thread_.start();
        }
    }

    @SuppressWarnings ("ContinueOrBreakFromFinallyBlock")
    @Override
    public void run () {
        Logger logger = getLogger();
        Console console = Console.getInstance();

        for(Folder folder : folders_){
            if (! run_){
                break;
            }

            if(notUsableFolder(folder)){
                continue;
            }

            String folderFullName = folder.getFullName();
            Message[] messages = null;

            try {
                folder.open(Folder.READ_ONLY);
                messages = folder.getMessages();
            } catch (FolderNotFoundException e) {
                String message = "Tried to interact with a folder that doesn't exist.\n";

                // Should to something like that but the user might not like it if all his folders
                // are written in the log file
                // StringBuilder stringBuilder = new StringBuilder();
                // for(Folder tempFolder : folders_){
                //     stringBuilder.append(tempFolder.getFullName()).append("\n");
                // }
                // message += stringBuilder.toString();
                message += "Folder name: " + folderFullName;

                logger.log(Level.WARNING, message, e);
            } catch (IllegalStateException e) {
                logger.log(Level.WARNING, "Got an illegal state on folder: " + folderFullName, e);
            } catch (MessagingException e) {
                logger.log(
                    Level.WARNING, "Got an error while processing folder: " + folderFullName, e
                );
            }
            finally {
                if(messages == null){
                    console.postMessage("Folder: " + folderFullName + " could not be processed!");

                    // Obviously continue is called only if an exception has occurred
                    continue;
                }
            }

            ArrayList<Email> emails = new ArrayList<>();

            for (Message message : messages) {
                String messageSubject;
                try {
                    messageSubject = message.getSubject();
                } catch (MessagingException e) {
                    logger.log(Level.WARNING, "Could not get the subject of a message", e);
                    console.postMessage(
                        "There was an error processing an email in folder: " + folderFullName +
                            ". Email will not be used."
                    );
                    continue;
                }

                String[] senders;
                try {
                    Address[] addresses = message.getFrom();
                    if (addresses == null) {
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
                    logger.log(Level.WARNING, "Could not get the senders of a message", e);
                    console.postMessage(
                        "There was an error processing the senders of email: " + messageSubject +
                            "in folder: " + folderFullName + ". Email will not be used."
                    );
                    continue;
                }

                String[] tORecipients;
                try {
                    Address[] addresses = message.getRecipients(Message.RecipientType.TO);
                    if (addresses == null) {
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
                    logger.log(Level.WARNING, "Could not get \"TO\" recipients of a message", e);
                    console.postMessage(
                        "There was an error processing \"TO\" recipients of email: " +
                            messageSubject + "in folder: " + folderFullName +
                            ". Email will not be used."
                    );
                    continue;
                }

                String[] cCRecipients;
                try {
                    Address[] addresses = message.getRecipients(Message.RecipientType.CC);
                    if (addresses == null) {
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
                    logger.log(Level.WARNING, "Could not get \"CC\" recipients of a message", e);
                    console.postMessage(
                        "There was an error processing \"CC\" recipients of email: " +
                            messageSubject + "in folder: " + folderFullName +
                            ". Email will not be used."
                    );
                    continue;
                }

                String[] bCCRecipients;
                try {
                    Address[] addresses = message.getRecipients(Message.RecipientType.BCC);
                    if (addresses == null) {
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
                    logger.log(Level.WARNING, "Could not get \"BCC\" recipients of a message", e);
                    console.postMessage(
                        "There was an error processing \"BCC\" recipients of email: " +
                            messageSubject + "in folder: " + folderFullName +
                            ". Email will not be used."
                    );
                    continue;
                }

                String messageReceivedDate;
                try {
                    messageReceivedDate = message.getSentDate().toString();
                } catch (MessagingException e) {
                    logger.log(Level.WARNING, "Could not get sent date of a message", e);
                    console.postMessage(
                        "There was an error processing sent date of email: " +
                            messageSubject + "in folder: " + folderFullName +
                            ". Email will not be used."
                    );
                    continue;
                }

                Object messageContent;
                String messageBody;
                try {
                    messageContent = message.getContent();

                    if (messageContent instanceof String) {
                        messageBody = (String) messageContent;
                    }
                    else if (messageContent instanceof Multipart) {
                        messageBody = getBodyFromMultiPart((Multipart) messageContent);
                    }
                    else {
                        messageBody = "";
                    }
                } catch (IOException | MessagingException e) {
                    logger.log(Level.WARNING, "Could not get the body of a message", e);
                    console.postMessage(
                        "There was an error processing the body of email: " +
                            messageSubject + "in folder: " + folderFullName +
                            ". Email will not be used."
                    );
                    continue;
                }

                emails.add(new Email(senders, tORecipients, cCRecipients, bCCRecipients,
                    messageReceivedDate, messageSubject, messageBody));
            }

            try {
                folder.close(false);
            } catch (IllegalStateException e) {
                logger.log(
                    Level.WARNING, "Got an illegal state on folder: " + folderFullName +
                    "while closing it", e
                );
            } catch (MessagingException e) {
                logger.log(
                    Level.WARNING, "Got an error while closing folder: " + folderFullName, e
                );
            }

            setChanged();
            notifyObservers(new org.pasr.prep.email.fetchers.Folder(folderFullName, emails));
        }

        // There is no use for the GmailFetcher at this time so it should release its resources
        close();

        // Notify the observers that the fetching is finished
        setChanged();
        notifyObservers();

        getLogger().info("GmailFetcher thread shut down gracefully!");
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
    public void terminate () {
        killThread();

        close();
    }

    private void killThread(){
        run_ = false;

        try {
            thread_.join(3000);
        } catch (InterruptedException e) {
            getLogger().warning("Interrupted while joining GmailFetcher thread.");
        }
    }

    private void close(){
        try {
            store_.close();
        } catch (MessagingException e) {
            getLogger().log(
                Level.WARNING, "There were errors when trying to close the email store", e
            );
        }
    }

    private Thread thread_ = null;
    private volatile boolean run_ = true;

    private Store store_;
    private volatile Folder[] folders_;

}
