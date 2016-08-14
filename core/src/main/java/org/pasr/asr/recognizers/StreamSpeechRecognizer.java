package org.pasr.asr.recognizers;


import cz.adamh.utils.NativeUtils;
import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import org.pasr.asr.Configuration;
import org.pasr.prep.recorder.Recorder;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.util.Observable;
import java.util.logging.Logger;


public class StreamSpeechRecognizer extends Observable implements Runnable {
    public StreamSpeechRecognizer(Configuration configuration)
        throws IOException, LineUnavailableException {

        NativeUtils.loadLibraryFromJar("/libpocketsphinx_jni.so");

        recorder_ = new Recorder();
        sampleRate_ = recorder_.getSampleRate();

        Config decoderConfig = Decoder.defaultConfig();
        decoderConfig.setString("-hmm", configuration.getAcousticModelPath());
        decoderConfig.setString("-dict", configuration.getDictionaryPath());
        decoderConfig.setString("-lm", configuration.getLanguageModelPath());
        decoder_ = new Decoder(decoderConfig);

        thread_ = new Thread(this);
        thread_.setDaemon(true);
    }

    public synchronized void startRecognition(){
        if(!thread_.isAlive()){
            // Start the thread
            thread_.start();

            // Wait for the thread to get ready
            while(!ready_){
                try {
                    wait();
                } catch (InterruptedException e) {
                    logger_.info("Woke up while waiting for thread to be ready.");
                }
            }

            live_ = true;
            startDecoder();
        }

        run_ = true;

        // Notify the thread to wake up
        notify();

        recorder_.startRecording();
    }

    public synchronized void stopRecognition(){
        run_ = false;

        recorder_.stopRecording();
    }

    @Override
    public void run(){
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

        short[] buffer = new short[sampleRate_ / 10];

        while(live_){

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

            if(!live_){
                break;
            }

            synchronized (this) {
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

    private void startDecoder(){
        if(!isDecoderStarted_){
            decoder_.startUtt();
            isDecoderStarted_ = true;
        }
    }

    private void stopDecoder(){
        if(isDecoderStarted_){
            decoder_.endUtt();
            isDecoderStarted_ = false;
        }
    }

    public synchronized void terminate() {
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

        stopDecoder();
        recorder_.stopRecording();
    }

    private Thread thread_;

    private volatile boolean ready_ = false;
    private volatile boolean live_ = false;
    private volatile boolean run_ = false;

    private Recorder recorder_;
    private final int sampleRate_;

    private Decoder decoder_;
    private boolean isDecoderStarted_ = false;

    private final Logger logger_ = Logger.getLogger(getClass().getName());

}
