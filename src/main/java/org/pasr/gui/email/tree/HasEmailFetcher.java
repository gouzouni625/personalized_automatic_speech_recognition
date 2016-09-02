package org.pasr.gui.email.tree;


import java.util.Observer;


interface HasEmailFetcher {
    void fetch(String path);
    void stop();

    void addObserver(Observer observer);

}
