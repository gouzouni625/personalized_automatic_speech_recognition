package org.pasr.asr.recognizers;


import cz.adamh.utils.NativeUtils;
import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import org.pasr.asr.Configuration;
import org.pasr.prep.recorder.Recorder;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.util.Observable;


public class StreamSpeechRecognizer extends Observable{
    static {
        try {
            NativeUtils.loadLibraryFromJar("/libpocketsphinx_jni.so");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StreamSpeechRecognizer(Configuration configuration) throws IOException {
        try {
            recorder_ = new Recorder();
        }
        catch (LineUnavailableException e) {
            throw new IOException("Failed to initialize recorder. Microphone might be unavailable");
        }

        Config decoderConfig = Decoder.defaultConfig();
        decoderConfig.setString("-hmm", configuration.getAcousticModelPath());
        decoderConfig.setString("-dict", configuration.getDictionaryPath());
        decoderConfig.setString("-lm", configuration.getLanguageModelPath());

        decoder_ = new Decoder(decoderConfig);
    }

    public void startRecognition(){
        recognizingThread_ = new RecognizingThread();
        recognizingThread_.start();
    }

    public void stopRecognition(){
        if(recognizingThread_ == null){
            return;
        }

        try {
            recognizingThread_.terminate();
            recognizingThread_.join();
        } catch (InterruptedException e) {
            // Restore the interrupted status
            Thread.currentThread().interrupt();
        }
        recognizingThread_ = null;
    }

    public void close() throws IOException {
        stopRecognition();

        recorder_.close();
    }

    private class RecognizingThread extends Thread {
        RecognizingThread (){
            sampleRate_ = recorder_.getSampleRate();
            bufferSize_ = Math.round(sampleRate_ * BUFFER_SIZE_SECONDS);
        }

        @Override
        public void run(){
            recorder_.startRecording();

            decoder_.startUtt();
            short[] buffer = new short[bufferSize_];

            String previousHypothesis = "";
            while(run_){
                int read = recorder_.read(buffer);

                if(read == -1){
                    throw new RuntimeException("Error reading audio buffer");
                }
                else{
                    decoder_.processRaw(buffer, read, false, false);

                    if(decoder_.hyp() != null){
                        String currentHypothesis = decoder_.hyp().getHypstr();

                        if(!currentHypothesis.equals(previousHypothesis)){
                            setChanged();
                            notifyObservers(decoder_.hyp());

                            previousHypothesis = currentHypothesis;
                        }
                    }
                }
            }
            decoder_.endUtt();

            recorder_.stopRecording();
        }

        public void terminate(){
            run_ = false;
        }

        private int sampleRate_;
        private int bufferSize_;
        private static final float BUFFER_SIZE_SECONDS = 0.4f;

        private boolean run_ = true;
    }

    private Recorder recorder_;
    private Decoder decoder_;

    private RecognizingThread recognizingThread_;

}
