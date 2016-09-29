package org.pasr.prep.email.fetchers;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;


/**
 * @class Email
 * @brief Implements an E-mail
 */
public class Email {

    /**
     * @brief Constructor
     *
     * @param senders
     *     The senders of the Email (plural based on the definition of JavaMail)
     * @param tORecipients
     *     The "to" recipients of the Email
     * @param cCRecipients
     *     The "cc" recipients of the Email
     * @param bCCRecipients
     *     The "bcc" recipients of the Email
     * @param receivedDate
     *     The date the Email was received
     * @param subject
     *     The subject of the Email
     * @param body
     *     The body of the Email
     * @param path
     *     The folder path of the Email inside the folder tree of the Email provider
     */
    Email (String[] senders, String[] tORecipients, String[] cCRecipients, String[] bCCRecipients,
           long receivedDate, String subject, String body, String path) {
        senders_ = senders != null ? senders : new String[0];

        tORecipients_ = tORecipients != null ? tORecipients : new String[0];
        cCRecipients_ = cCRecipients != null ? cCRecipients : new String[0];
        bCCRecipients_ = bCCRecipients != null ? bCCRecipients : new String[0];

        receivedDate_ = receivedDate;
        subject_ = subject;
        body_ = body;

        path_ = path;

        id_ = receivedDate_;
    }

    /**
     * @brief Returns the senders of this Email
     *
     * @return The senders of this Email
     */
    public String[] getSenders () {
        return senders_;
    }

    /**
     * @brief Returns the recipients of this Email
     *
     * @param type
     *     The type of the recipients to return
     *
     * @return The recipients of this Email
     */
    public String[] getRecipients (RecipientType type) {
        switch (type) {
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

    /**
     * @brief Returns the received date of this Email
     *
     * @return The received date of this Email
     */
    public long getReceivedDate () {
        return receivedDate_;
    }

    /**
     * @brief Returns the subject of this Email
     *
     * @return The subject of this Email
     */
    public String getSubject () {
        return subject_;
    }

    /**
     * @brief Returns the body of this Email
     *
     * @return The body of this Email
     */
    public String getBody () {
        return body_;
    }

    /**
     * @brief Returns the id of this Email
     *
     * @return The id of this Email
     */
    public long getId () {
        return id_;
    }

    /**
     * @brief Returns the path of this Email
     *
     * @return The path of this Email
     */
    public String getPath () {
        return path_;
    }

    /**
     * @brief Returns the hash code of this Email
     *        This method is overridden to build the hash code out of the values of this Email. This
     *        will make sure that Email objects are equal if and only if their values are equal.
     *
     * @return The hash code of this Email
     */
    @Override
    public int hashCode () {
        return new HashCodeBuilder(17, 37)
            .append(senders_)
            .append(tORecipients_)
            .append(cCRecipients_)
            .append(bCCRecipients_)
            .append(receivedDate_)
            .append(subject_)
            .append(body_)
            .append(path_).toHashCode();
    }

    /**
     * @brief Returns true if and only if this Email is equal with the given Object
     *        This method is overridden to make sure that Email objects are equal if and only if
     *        their values are equal.
     *
     * @param o
     *     The Object to check for equality with this Email
     *
     * @return True if and only this this Email is equal with the given Object
     */
    @Override
    public boolean equals (Object o) {
        if (! (o instanceof Email)) {
            return false;
        }

        Email email = (Email) o;

        return Arrays.equals(email.getSenders(), senders_)
            && Arrays.equals(email.getRecipients(RecipientType.TO), tORecipients_)
            && Arrays.equals(email.getRecipients(RecipientType.CC), cCRecipients_)
            && Arrays.equals(email.getRecipients(RecipientType.BCC), bCCRecipients_)
            && email.getReceivedDate() == receivedDate_
            && email.getSubject().equals(subject_)
            && email.getBody().equals(body_)
            && email.getPath().equals(path_);
    }

    private final String[] senders_; //!< The senders of this Email (plural based on the definition
                                     //!< of JavaMail)
    private final String[] tORecipients_; //!< The "to" recipients of this Email
    private final String[] cCRecipients_; //!< The "cc" recipients of this Email
    private final String[] bCCRecipients_; //!< The "bcc" recipients of this Email
    private final long receivedDate_; //!< The Unix timestamp of the received date of this Email
    private final String subject_; //!< The subject of this Email
    private final String body_; //!< The body of this Email
    private final long id_; //!< the id of this Email

    private final String path_; //!< The folder path of the Email inside the folder tree of the
                                //!< Email provider

    /**
     * @class RecipientType
     * @brief Holds the different types of an Email recipient
     */
    public enum RecipientType {
        TO, //!< Recipient of type "to"
        CC, //!< Recipient of type "cc"
        BCC //!< Recipient of type "bcc"
    }

}
