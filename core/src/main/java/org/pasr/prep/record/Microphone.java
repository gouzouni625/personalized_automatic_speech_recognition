package org.pasr.prep.record;

import javax.sound.sampled.*;
import java.io.*;

public class Microphone {
    public Microphone() {
        audioFormat_ = new AudioFormat(16000, 16, 1, true, false);

        fileCount_ = 0;
    }

    public void record() throws LineUnavailableException {
        targetDataLine_ = AudioSystem.getTargetDataLine(audioFormat_);
        targetDataLine_.open(audioFormat_);
        targetDataLine_.start();

        audioInputStream_ = new AudioInputStream(targetDataLine_);

        fileCount_++;
        recorder_ = new Recorder(new File("acoustic_model_adaptation/" + getCurrentFileName()));
        recordingThread_ = new Thread(recorder_);
        recordingThread_.start();
    }

    public void stop() throws InterruptedException, IOException {
        targetDataLine_.stop();

        recorder_.stop();

        recordingThread_.join();

        targetDataLine_.close();
        audioInputStream_.close();
    }

    public String getCurrentFileName(){
        return ("record_" + fileCount_ + ".wav");
    }

    private TargetDataLine targetDataLine_;

    private AudioInputStream audioInputStream_;

    private AudioFormat audioFormat_;

    private Recorder recorder_;
    private Thread recordingThread_;

    private int fileCount_;

    private class Recorder implements Runnable{
        Recorder(File outputFile){
            outputFile_ = outputFile;
        }

        public void run() {
            while(running_) {
                try {
                    System.out.println("Before");
                    AudioSystem.write(audioInputStream_, AudioFileFormat.Type.WAVE, outputFile_);
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

        private File outputFile_;
    }

}
