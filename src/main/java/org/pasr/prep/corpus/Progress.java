package org.pasr.prep.corpus;


import java.util.Observable;


class Progress extends Observable {
    Progress(){}

    void setValue(double value){
        setChanged();
        notifyObservers(value);
    }

}
