package org.pasr.prep.email.fetchers;


public class Email {
    public Email(String[] senders, String[] tORecipients, String[] cCRecipients,
                 String[] bCCRecipients, String receivedDate, String subject, String body){
        senders_ = senders != null ? senders : new String[0];

        tORecipients_ = tORecipients != null ? tORecipients : new String[0];
        cCRecipients_ = cCRecipients != null ? cCRecipients : new String[0];
        bCCRecipients_ = bCCRecipients != null ? bCCRecipients : new String[0];

        receivedDate_ = receivedDate;
        subject_ = subject;
        body_ = body;
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

    public String getReceivedDate(){
        return receivedDate_;
    }

    public String getSubject(){
        return subject_;
    }

    public String getBody(){
        return body_;
    }

    private final String[] senders_;
    private final String[] tORecipients_;
    private final String[] cCRecipients_;
    private final String[] bCCRecipients_;
    private final String receivedDate_;
    private final String subject_;
    private final String body_;

    public enum RecipientType{
        TO,
        CC,
        BCC
    }

}
