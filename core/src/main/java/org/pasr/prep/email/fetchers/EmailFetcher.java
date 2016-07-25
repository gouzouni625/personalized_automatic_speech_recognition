package org.pasr.prep.email.fetchers;

import java.util.Observable;


public abstract class EmailFetcher extends Observable {

    public abstract void fetch();
    public abstract void close();

    public static class RecentFolder {
        RecentFolder (String path, Email[] emails){
            path_ = path;
            emails_ = emails;
        }

        public String getPath(){
            return path_;
        }

        public Email[] getEmails(){
            return emails_;
        }

        public static class Email {
            Email(String subject, String body){
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

        private String path_;
        private Email[] emails_;
    }

}
