package org.pasr.prep.email.fetchers;


import org.pasr.gui.console.Console;
import org.pasr.utilities.SortedMapEntryList;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class GMailFetcher extends EmailFetcher{
    public GMailFetcher () throws IOException {
        super("/email/gmail-imaps.properties");
    }

    @Override
    public synchronized void open(String address, String password) throws MessagingException {
        if(store_ != null){
            throw new IllegalStateException("Fetcher has already been opened");
        }

        store_ = Session.getInstance(properties_).getStore("imaps");
        store_.connect(address, password);

        folderMap_ = new Hashtable<>();
        for(Folder folder : store_.getDefaultFolder().list("*")){
            if(notUsableFolder(folder)){
                continue;
            }

            folderMap_.put(folder.getFullName(), folder);
        }
    }

    @Override
    public Map<String, Integer> getFolderInfo(){
        if(folderMap_ == null){
            throw new IllegalStateException("Fetcher is not open or has been terminated.");
        }

        return folderMap_.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry :: getKey,
                entry -> {
                    try {
                        return entry.getValue().getMessageCount();
                    } catch (MessagingException e) {
                        return 0;
                    }
                }
            ));
    }

    @Override
    public synchronized void fetch(int count) {
        if(store_ == null){
            throw new IllegalStateException("Fetcher is not open or has been terminated.");
        }

        fetch(SENT_MAIL_FOLDER_PATH, count);
    }

    @Override
    public synchronized void fetch(String folderPath, int count) {
        if(store_ == null){
            throw new IllegalStateException("Fetcher is not open or has been terminated.");
        }

        if(folderPath == null){
            getLogger().warning("folderPath must not be null!");
            return;
        }

        if(!folderMap_.containsKey(folderPath)){
            Console.getInstance().postMessage("Could not fetch folder: " + folderPath);
            return;
        }

        startNewFetcherThread(folderPath, count);
    }

    @Override
    public synchronized void stop(){
        if(fetcherThread_ == null || !fetcherThread_.isAlive()){
            return;
        }

        // There is no need to join fetcherThread_. Any observer of this EmailFetcher will be
        // notified when fetching is stopped
        fetcherThread_.terminate();
    }

    private void startNewFetcherThread(String folderPath, int count) {
        if(fetcherThread_ == null || !fetcherThread_.isAlive()){
            fetcherThread_ = new FetcherThread(folderMap_.get(folderPath), count);
            fetcherThread_.start();
        }
        else{
            throw new IllegalStateException("Already fetching!");
        }
    }

    private class FetcherThread extends Thread {
        FetcherThread (Folder folder, int count) {
            folder_ = folder;
            count_ = count;

            setDaemon(true);
        }

        @Override
        public void run () {
            logger_.info("FetcherThread started!");

            setChanged();
            notifyObservers(Stage.STARTED_FETCHING);

            Console console = Console.getInstance();

            String folderFullName = folder_.getFullName();
            Message[] messages;

            try {
                folder_.open(Folder.READ_ONLY);

                messages = folder_.getMessages();

                FetchProfile fetchProfile = new FetchProfile();
                fetchProfile.add(FetchProfile.Item.ENVELOPE);
                folder_.fetch(messages, fetchProfile);
            } catch (FolderNotFoundException e) {
                logger_.warning("Tried to interact with a folder that doesn't exist.\n" +
                    "Folder name: " + folderFullName);
                console.postMessage("Could not fetch emails from folder: " + folderFullName);

                beforeExit();
                return;
            } catch (IllegalStateException e) {
                logger_.warning("Got an illegal state on folder: " + folderFullName);
                console.postMessage("Could not fetch emails from folder: " + folderFullName);

                beforeExit();
                return;
            } catch (MessagingException e) {
                logger_.warning("Got an error while processing folder: " + folderFullName);
                console.postMessage("Could not fetch emails from folder: " + folderFullName);

                beforeExit();
                return;
            }

            List<Message> mostRecentMessageList = getMostRecent(messages);

            for (Message message : mostRecentMessageList) {
                if (! run_) {
                    beforeExit();
                    return;
                }

                String messageSubject;
                try {
                    messageSubject = message.getSubject();
                } catch (MessagingException e) {
                    logger_.log(Level.WARNING, "Could not get the subject of a message", e);
                    console.postMessage(
                        "There was an error processing an email in folder: " + folderFullName +
                            ". Email will not be used."
                    );
                    continue;
                }

                if (! run_) {
                    beforeExit();
                    return;
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
                    logger_.log(Level.WARNING, "Could not get the senders of a message", e);
                    console.postMessage(
                        "There was an error processing the senders of email: " + messageSubject +
                            "in folder: " + folderFullName + ". Email will not be used."
                    );
                    continue;
                }

                if (! run_) {
                    beforeExit();
                    return;
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
                    logger_.log(Level.WARNING, "Could not get \"TO\" recipients of a message", e);
                    console.postMessage(
                        "There was an error processing \"TO\" recipients of email: " +
                            messageSubject + "in folder: " + folderFullName +
                            ". Email will not be used."
                    );
                    continue;
                }

                if (! run_) {
                    beforeExit();
                    return;
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
                    logger_.log(Level.WARNING, "Could not get \"CC\" recipients of a message", e);
                    console.postMessage(
                        "There was an error processing \"CC\" recipients of email: " +
                            messageSubject + "in folder: " + folderFullName +
                            ". Email will not be used."
                    );
                    continue;
                }

                if (! run_) {
                    beforeExit();
                    return;
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
                    logger_.log(Level.WARNING, "Could not get \"BCC\" recipients of a message", e);
                    console.postMessage(
                        "There was an error processing \"BCC\" recipients of email: " +
                            messageSubject + "in folder: " + folderFullName +
                            ". Email will not be used."
                    );
                    continue;
                }

                if (! run_) {
                    beforeExit();
                    return;
                }

                long messageReceivedDate;
                try {
                    messageReceivedDate = message.getSentDate().getTime();
                } catch (MessagingException e) {
                    logger_.log(Level.WARNING, "Could not get sent date of a message", e);
                    console.postMessage(
                        "There was an error processing sent date of email: " +
                            messageSubject + "in folder: " + folderFullName +
                            ". Email will not be used."
                    );
                    continue;
                }

                if (! run_) {
                    beforeExit();
                    return;
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
                    logger_.log(Level.WARNING, "Could not get the body of a message", e);
                    console.postMessage(
                        "There was an error processing the body of email: " +
                            messageSubject + " in folder: " + folderFullName +
                            ". Email will not be used."
                    );
                    continue;
                }

                setChanged();
                notifyObservers(new Email(senders, tORecipients, cCRecipients, bCCRecipients,
                    messageReceivedDate, messageSubject, messageBody, folderFullName));
            }

            beforeExit();
        }

        private String getBodyFromMultiPart (Multipart multipart)
            throws MessagingException, IOException {

            StringBuilder stringBuilder = new StringBuilder();

            Object content = multipart.getBodyPart(0).getContent();
            if (content instanceof String) {
                stringBuilder.append(content);
            }
            else if (content instanceof Multipart) {
                stringBuilder.append(getBodyFromMultiPart((Multipart) content));
            }

            return stringBuilder.toString();
        }

        private void beforeExit () {
            if(folder_.isOpen()){
                String folderFullName = folder_.getFullName();

                try {
                    folder_.close(false);
                } catch (IllegalStateException e) {
                    logger_.warning("Got an illegal state on folder: " + folderFullName +
                        " while closing it");
                } catch (MessagingException e) {
                    logger_.warning("Got an error while closing folder: " + folderFullName);
                }
            }

            setChanged();
            notifyObservers(Stage.STOPPED_FETCHING);

            logger_.info("FetcherThread shut down gracefully!");
        }

        private List<Message> getMostRecent(Message[] messages){
            SortedMapEntryList<Message, Long> sortedMapEntryList = new SortedMapEntryList<>(
                count_, false
            );

            sortedMapEntryList.addAll(Arrays.stream(messages)
                .collect(Collectors.toMap(Function.identity(), message -> {
                    try {
                        return message.getSentDate().getTime();
                    } catch (MessagingException e) {
                        logger_.warning("Error while fetching message sent date");
                        return (long) 0;
                    }
                })).entrySet());

            return sortedMapEntryList.keyList();
        }

        public synchronized void terminate(){
            run_ = false;
        }

        private Folder folder_;

        private int count_;

        private volatile boolean run_ = true;

        private Logger logger_ = Logger.getLogger(getClass().getName());
    }

    private boolean notUsableFolder (Folder javaMailFolder){
        return javaMailFolder.getFullName().equals("[Gmail]");
    }

    @Override
    public synchronized void terminate () {
        if(fetcherThread_ != null && fetcherThread_.isAlive()) {
            fetcherThread_.terminate();

            try {
                // Don't wait forever on this thread since it is a daemon and will not block the JVM
                // from shutting down
                fetcherThread_.join(3000);
            } catch (InterruptedException e) {
                getLogger().warning("Interrupted while joining GmailFetcher thread.");
            }
        }

        fetcherThread_ = null;

        if(store_ != null && store_.isConnected()) {
            try {
                store_.close();
            } catch (MessagingException e) {
                getLogger().log(
                    Level.WARNING, "There were errors when trying to close the email store", e
                );
            }
        }

        store_ = null;

        if(folderMap_ != null) {
            folderMap_.values().stream()
                .filter(Folder:: isOpen).forEach(folder -> {

                String folderFullName = folder.getFullName();
                try {
                    folder.close(false);
                } catch (IllegalStateException e) {
                    getLogger().warning("Got an illegal state on folder: " + folderFullName +
                        " while closing it");
                } catch (MessagingException e) {
                    getLogger().warning("Got an error while closing folder: " + folderFullName);
                }
            });

            folderMap_ = null;
        }
    }

    private FetcherThread fetcherThread_;

    private Store store_;

    private volatile Map<String, Folder> folderMap_;

    private static final String SENT_MAIL_FOLDER_PATH = "[Gmail]/Sent Mail";

}
