package org.pasr.prep.record;

import javax.sound.sampled.*;
import java.io.*;

public class Microphone {
    public Microphone() {
        audioFormat_ = new AudioFormat(16000, 16, 1, true, false);

        numberOfFiles_ = 1;
        currentFile_ = new File("record_" + numberOfFiles_ + ".wav");
    }

    public void record() throws LineUnavailableException {
        targetDataLine_ = AudioSystem.getTargetDataLine(audioFormat_);
        targetDataLine_.open(audioFormat_);
        targetDataLine_.start();

        audioInputStream_ = new AudioInputStream(targetDataLine_);

        recorder_ = new Recorder();
        recordingThread_ = new Thread(recorder_);
        recordingThread_.start();
    }

    public void stop() throws InterruptedException, IOException {
        targetDataLine_.stop();

        recorder_.stop();

        recordingThread_.join();

        targetDataLine_.close();
        audioInputStream_.close();

        numberOfFiles_++;
        currentFile_ = new File("record_" + numberOfFiles_ + ".wav");
    }

    private TargetDataLine targetDataLine_;

    private AudioInputStream audioInputStream_;

    private AudioFormat audioFormat_;

    private Recorder recorder_;
    private Thread recordingThread_;

    private int numberOfFiles_;
    private File currentFile_;

    private class Recorder implements Runnable{
        public void run() {
            while(running_) {
                try {
                    System.out.println("Before");
                    AudioSystem.write(audioInputStream_, AudioFileFormat.Type.WAVE, currentFile_);
                    System.out.println("After");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        void stop(){
            running_ = false;
        }

        private boolean running_ = true;
    }

}
