package org.pasr.asr.recognizers;


import cz.adamh.utils.NativeUtils;
import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import org.pasr.asr.Configuration;
import org.pasr.prep.recorder.Recorder;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.util.Observable;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;


public class StreamSpeechRecognizer extends Observable {
    public StreamSpeechRecognizer(Configuration configuration)
        throws IOException, LineUnavailableException {

        if(!isLibraryLoaded_) {
            NativeUtils.loadLibraryFromJar("/libpocketsphinx_jni.so");
            isLibraryLoaded_ = true;
        }

        recorder_ = new Recorder();
        sampleRate_ = recorder_.getSampleRate();

        decoderConfig_ = Decoder.defaultConfig();
        decoderConfig_.setString("-hmm", configuration.getAcousticModelPath());
        decoderConfig_.setString("-dict", configuration.getDictionaryPath());
        decoderConfig_.setString("-lm", configuration.getLanguageModelPath());
        decoder_ = new Decoder(decoderConfig_);

        thread_ = new Thread(this :: run);
        thread_.setDaemon(true);
    }

    public synchronized void startRecognition(){
        lock_.lock();

        // Make sure that successive calls of this method doesn't cause any harm
        if(run_ && live_){
            lock_.unlock();
            return;
        }

        // Make sure that terminate hasn't been called before
        if(decoder_ == null || recorder_ == null || thread_ == null){
            lock_.unlock();
            return;
        }

        if(!thread_.isAlive()){
            // Start the thread
            thread_.start();

            // Wait for the thread to get ready
            while(!ready_){
                try {
                    wait();
                } catch (InterruptedException e) {
                    logger_.info("Interrupted while waiting for thread to be ready.");
                }
            }

            live_ = true;
        }

        run_ = true;

        // Notify the thread to wake up
        ready_ = false;
        notifyAll();

        startRecorder();
        startDecoder();

        while(!ready_){
            try {
                wait();
            } catch (InterruptedException e) {
                logger_.info("Interrupted while waiting for thread to be ready.");
            }
        }

        lock_.unlock();
    }

    public synchronized void stopRecognition(){
        lock_.lock();

        // Make sure that successive calls of this method doesn't cause any harm
        if(!(run_ && live_)){
            lock_.unlock();
            return;
        }

        // Make sure that terminate hasn't been called before
        if(decoder_ == null || recorder_ == null || thread_ == null){
            lock_.unlock();
            return;
        }

        run_ = false;
        ready_ = false;

        while(!ready_){
            try {
                wait();
            } catch (InterruptedException e) {
                logger_.info("Interrupted while waiting for thread to be ready.");
            }
        }

        stopDecoder();
        stopRecorder();

        lock_.unlock();
    }

    // Will not implement Runnable because run method should be private to prevent others from
    // calling it
    private void run(){
        synchronized (this){
            ready_ = true;
            notifyAll();

            while(!live_){
                try {
                    wait();
                } catch (InterruptedException e) {
                    logger_.info("Thread was interrupted while waiting to become alive.");
                }
            }

            ready_ = true;
            notifyAll();
        }

        short[] buffer = new short[sampleRate_ / 10];


        while(live_){
            setChanged();
            notifyObservers(Stage.STARTED);

            String previousHypothesis = "";
            while(run_){
                int readSize = recorder_.read(buffer);

                if(readSize > 0){
                    decoder_.processRaw(buffer, readSize, false, false);

                    if(decoder_.hyp() != null){
                        String currentHypothesis = decoder_.hyp().getHypstr();

                        if(!currentHypothesis.equals(previousHypothesis)){
                            setChanged();
                            notifyObservers(currentHypothesis);

                            previousHypothesis = currentHypothesis;
                        }
                    }
                }
            }

            setChanged();
            notifyObservers(Stage.STOPPED);

            if(!live_){
                break;
            }

            synchronized (this) {
                ready_ = true;
                notifyAll();

                while(!run_ && live_){
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        logger_.info("Thread was interrupted while waiting to run.");
                    }
                }

                ready_ = true;
                notifyAll();
            }
        }

        logger_.info("StreamSpeechRecognizer thread shut down gracefully!");
    }

    private void startDecoder(){
        if(!isDecoderStarted_ && decoder_ != null){
            decoder_.startUtt();
            isDecoderStarted_ = true;
        }
    }

    private void stopDecoder(){
        if(isDecoderStarted_ && decoder_ != null){
            decoder_.endUtt();
            decoder_.reinit(decoderConfig_);
            isDecoderStarted_ = false;
        }
    }

    private void startRecorder(){
        if(!isRecorderStarted_ && recorder_ != null){
            recorder_.startRecording();
            isRecorderStarted_ = true;
        }
    }

    private void stopRecorder(){
        if(isRecorderStarted_ && recorder_ != null){
            recorder_.stopRecording();
            isRecorderStarted_ = false;
        }
    }

    public synchronized void terminate() {
        lock_.lock();

        if(live_) {

            if(run_){
                stopRecognition();
            }

            live_ = false;
            ready_ = false;
            notifyAll();

            while (!ready_){
                try {
                    wait();
                } catch (InterruptedException e) {
                    logger_.warning("Interrupted while waiting for thread to be ready.");
                }
            }

            try {
                // Don't wait forever on this thread since it is a daemon and will not block the JVM
                // from shutting down
                thread_.join(3000);
            } catch (InterruptedException e) {
                logger_.warning("Interrupted while joining thread.");
            }
        }

        stopDecoder();
        decoder_ = null;

        stopRecorder();
        recorder_ = null;

        thread_ = null;

        lock_.unlock();
    }

    private boolean isLibraryLoaded_ = false;

    private Config decoderConfig_;

    private Thread thread_;

    private volatile boolean ready_ = false;
    private volatile boolean live_ = false;
    private volatile boolean run_ = false;

    private final ReentrantLock lock_ = new ReentrantLock();

    private Recorder recorder_;
    private boolean isRecorderStarted_ = false;
    private final int sampleRate_;

    private Decoder decoder_;
    private boolean isDecoderStarted_ = false;

    public enum Stage{
        STARTED,
        STOPPED
    }

    private final Logger logger_ = Logger.getLogger(getClass().getName());

}
