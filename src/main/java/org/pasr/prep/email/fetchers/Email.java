package org.pasr.prep.email.fetchers;


import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;


public class Email {
    Email(String[] senders, String[] tORecipients, String[] cCRecipients,
                 String[] bCCRecipients, long receivedDate, String subject, String body){
        senders_ = senders != null ? senders : new String[0];

        tORecipients_ = tORecipients != null ? tORecipients : new String[0];
        cCRecipients_ = cCRecipients != null ? cCRecipients : new String[0];
        bCCRecipients_ = bCCRecipients != null ? bCCRecipients : new String[0];

        receivedDate_ = receivedDate;
        subject_ = subject;
        body_ = body;

        id_ = receivedDate_;
    }

    public String[] getSenders(){
        return senders_;
    }

    public String[] getRecipients(RecipientType type){
        switch (type){
            case TO:
                return tORecipients_;
            case CC:
                return cCRecipients_;
            case BCC:
                return bCCRecipients_;
            default:
                return null;
        }
    }

    public long getReceivedDate(){
        return receivedDate_;
    }

    public String getSubject(){
        return subject_;
    }

    public String getBody(){
        return body_;
    }

    public long getId (){
        return id_;
    }

    @Override
    public int hashCode(){
        return new HashCodeBuilder(17, 37)
            .append(senders_)
            .append(tORecipients_)
            .append(cCRecipients_)
            .append(bCCRecipients_)
            .append(receivedDate_)
            .append(subject_)
            .append(body_).toHashCode();
    }

    @Override
    public boolean equals(Object o){
        if(! (o instanceof Email)){
            return false;
        }

        Email email = (Email) o;

        return Arrays.equals(email.getSenders(), senders_)
            && Arrays.equals(email.getRecipients(RecipientType.TO), tORecipients_)
            && Arrays.equals(email.getRecipients(RecipientType.CC), cCRecipients_)
            && Arrays.equals(email.getRecipients(RecipientType.BCC), bCCRecipients_)
            && email.getReceivedDate() == receivedDate_
            && email.getSubject().equals(subject_)
            && email.getBody().equals(body_);
    }

    private final String[] senders_;
    private final String[] tORecipients_;
    private final String[] cCRecipients_;
    private final String[] bCCRecipients_;
    private final long receivedDate_;
    private final String subject_;
    private final String body_;
    private final long id_;

    public enum RecipientType{
        TO,
        CC,
        BCC
    }

}
