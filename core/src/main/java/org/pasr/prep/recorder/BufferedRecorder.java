package org.pasr.prep.recorder;


import org.apache.commons.io.output.ByteArrayOutputStream;
import org.pasr.utilities.Utilities;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;

public class BufferedRecorder extends Recorder implements Runnable {
    public BufferedRecorder() throws LineUnavailableException {
        super();

        byteArrayOutputStream_ = new ByteArrayOutputStream();
    }

    @Override
    public synchronized void startRecording() {
        notify();

        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        super.startRecording();
    }

    @Override
    public void stopRecording(){
        super.stopRecording();

        run_ = false;
    }

    @Override
    public void run(){
        synchronized (this){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            notify();
        }

        // for buffer size see https://docs.oracle.com/javase/tutorial/sound/capturing.html
        byte[] buffer = new byte[targetDataLine_.getBufferSize() / 5];

        while(live_) {
            while (run_) {
                int readSize = read(buffer);

                if (readSize > 0) {
                    level_ = Utilities.rootMeanSquare(buffer) / 10000;

                    byteArrayOutputStream_.write(buffer, 0, readSize);
                }
            }

            level_ = 0;

            synchronized(this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                notify();
            }

            run_ = true;
        }
    }

    public double getLevel(){
        return level_;
    }

    public void flush(){
        byteArrayOutputStream_.reset();
    }

    public Clip getClip() throws LineUnavailableException {
        byte[] array = byteArrayOutputStream_.toByteArray();

        Clip clip = AudioSystem.getClip();
        clip.open(AUDIO_FORMAT, array, 0, array.length);

        return clip;
    }

    @Override
    public void terminate() throws IOException {
        super.terminate();

        live_ = false;

        byteArrayOutputStream_.close();
    }

    private ByteArrayOutputStream byteArrayOutputStream_;

    private double level_ = 0;

    private boolean run_ = true;
    private boolean live_ = true;

}
