package org.pasr.prep.recorder;


import org.apache.commons.io.output.ByteArrayOutputStream;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.pasr.utilities.Utilities.rootMeanSquare;


public class BufferedRecorder extends Recorder implements Runnable {
    public BufferedRecorder() throws LineUnavailableException {
        super();

        byteArrayOutputStream_ = new ByteArrayOutputStream();

        thread_ = new Thread(this);
        thread_.setDaemon(true);
    }

    @Override
    public synchronized void startRecording() {
        if(!thread_.isAlive()){
            // Start the thread
            thread_.start();

            // wait for the thread to go to sleep in a while loop to avoid a spurious wake up
            while(!ready_) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    logger_.info("Woke up while waiting for thread to be ready.");
                }
            }

            live_ = true;
        }

        run_ = true;

        // Notify the thread to wake up
        notify();

        super.startRecording();
    }

    @Override
    public synchronized void stopRecording(){
        super.stopRecording();

        run_ = false;
    }

    @Override
    public void run(){
        // Upon start, go to wait immediately after you signal that you are ready to
        synchronized (this){
            ready_ = true;
            notify();

            while(!live_){
                try {
                    wait();
                } catch (InterruptedException e) {
                    logger_.info("Thread woke up while waiting to become alive.");
                }
            }
        }

        // for buffer size see https://docs.oracle.com/javase/tutorial/sound/capturing.html
        byte[] buffer = new byte[targetDataLine_.getBufferSize() / 5];

        while(live_) {
            while (run_) {
                int readSize = read(buffer);

                if (readSize > 0) {
                    level_ = rootMeanSquare(buffer) / 10000;

                    byteArrayOutputStream_.write(buffer, 0, readSize);
                }
            }

            level_ = 0;

            // There is no need to go to wait if you should die
            if(!live_){
                break;
            }

            synchronized(this) {
                while(!run_){
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        logger_.info("Thread woke up while waiting to run.");
                    }
                }
            }
        }
    }

    public double getLevel(){
        return level_;
    }

    public byte[] getData(){
        return byteArrayOutputStream_.toByteArray();
    }

    public Clip getClip() throws LineUnavailableException {
        byte[] array = byteArrayOutputStream_.toByteArray();

        Clip clip = AudioSystem.getClip();
        clip.open(AUDIO_FORMAT, array, 0, array.length);

        return clip;
    }

    public void flush(){
        byteArrayOutputStream_.reset();
    }

    @Override
    public synchronized void terminate() {
        super.terminate();

        // Make sure that the thread that runs the run method can terminate
        run_ = false;
        live_ = false;

        notify();

        try {
            // Don't wait forever on this thread since it is a daemon and will not block the JVM
            // from shutting down
            thread_.join(3000);
        } catch (InterruptedException e) {
            logger_.warning("Interrupted while joining thread.");
        }

        try {
            byteArrayOutputStream_.close();
        } catch (IOException e) {
            logger_.log(Level.WARNING, "Could not close the ByteArrayOutputStream instance.", e);
        }
    }

    private Thread thread_;

    private volatile boolean ready_ = false;
    private volatile boolean live_ = false;
    private volatile boolean run_ = false;

    private volatile double level_ = 0;

    private volatile ByteArrayOutputStream byteArrayOutputStream_;

    private final Logger logger_ = Logger.getLogger(getClass().getName());

}
