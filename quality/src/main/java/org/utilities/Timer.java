package org.utilities;


public class Timer {
    public Timer(){}

    public void start(){
        startTime_ = System.currentTimeMillis();
    }

    public void stop(){
        stopTime_ = System.currentTimeMillis();
    }

    public long getElapsedTime(){
        return stopTime_ - startTime_;
    }


    private long startTime_;
    private long stopTime_;

}
