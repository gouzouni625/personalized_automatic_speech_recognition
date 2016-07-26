package org.pasr.prep.email;


public class Email {
    public Email(String subject, String body){
        subject_ = subject;
        body_ = body;
    }

    public String getSubject(){
        return subject_;
    }

    public String getBody(){
        return body_;
    }

    private final String subject_;
    private final String body_;

}
