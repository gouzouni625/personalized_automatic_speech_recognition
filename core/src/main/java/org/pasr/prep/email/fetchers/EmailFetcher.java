package org.pasr.prep.email.fetchers;

import java.util.Observable;


public abstract class EmailFetcher extends Observable {

    public abstract void fetch() throws Exception;
    public abstract void close() throws Exception;

}
