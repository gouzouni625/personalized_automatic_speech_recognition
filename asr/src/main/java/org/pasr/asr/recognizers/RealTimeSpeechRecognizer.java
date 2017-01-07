package org.pasr.asr.recognizers;

import cz.adamh.utils.NativeUtils;
import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import org.pasr.asr.ASRConfiguration;
import org.pasr.utilities.recorder.Recorder;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.util.Observable;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;


/**
 * @class RealTimeSpeechRecognizer
 * @brief Implements a recognizer that can recognize voice in real time wrapping a Recorder
 */
public class RealTimeSpeechRecognizer extends Observable {

    /**
     * @brief Constructor
     *
     * @param asrConfiguration
     *     The ASRConfiguration for this recognizer
     *
     * @throws IOException If the pocket sphinx native library cannot be loaded
     * @throws LineUnavailableException If the Recorder cannot be instantiated
     */
    public RealTimeSpeechRecognizer (ASRConfiguration asrConfiguration)
        throws IOException, LineUnavailableException {

        if (! isLibraryLoaded_) {
            NativeUtils.loadLibraryFromJar("/libpocketsphinx_jni.so");
            isLibraryLoaded_ = true;
        }

        recorder_ = new Recorder();
        sampleRate_ = recorder_.getSampleRate();

        Config decoderConfig = Decoder.defaultConfig();
        decoderConfig.setString("-hmm", asrConfiguration.getAcousticModelPath().toString());
        decoderConfig.setString("-dict", asrConfiguration.getDictionaryPath().toString());
        decoderConfig.setString("-lm", asrConfiguration.getLanguageModelPath().toString()
        );
        decoder_ = new Decoder(decoderConfig);

        thread_ = new Thread(this :: run);
        thread_.setDaemon(true);
    }

    /**
     * @brief Starts the recognition process reading data from the Recorder
     */
    public synchronized void startRecognition () {
        lock_.lock();

        // Make sure that successive calls of this method doesn't cause any harm
        if (run_ && live_) {
            lock_.unlock();
            return;
        }

        // Make sure that terminate hasn't been called before
        if (decoder_ == null || recorder_ == null || thread_ == null) {
            lock_.unlock();
            return;
        }

        if (! thread_.isAlive()) {
            // Start the thread
            thread_.start();

            // Wait for the thread to get ready
            while (! ready_) {
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

        while (! ready_) {
            try {
                wait();
            } catch (InterruptedException e) {
                logger_.info("Interrupted while waiting for thread to be ready.");
            }
        }

        lock_.unlock();
    }

    /**
     * @brief Stop the recognition process
     */
    public synchronized void stopRecognition () {
        lock_.lock();

        // Make sure that successive calls of this method doesn't cause any harm
        if (! (run_ && live_)) {
            lock_.unlock();
            return;
        }

        // Make sure that terminate hasn't been called before
        if (decoder_ == null || recorder_ == null || thread_ == null) {
            lock_.unlock();
            return;
        }

        run_ = false;
        ready_ = false;

        while (! ready_) {
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
    private void run () {
        logger_.info("RealTimeSpeechRecognizer thread started!");

        synchronized (this) {
            ready_ = true;
            notifyAll();

            while (! live_) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    logger_.info("Thread was interrupted while waiting to become alive.");
                }
            }

            ready_ = true;
            notifyAll();
        }

        short[] buffer = new short[sampleRate_];

        while (live_) {
            setChanged();
            notifyObservers(Stage.STARTED);

            String previousHypothesis = "";
            while (run_) {
                int readSize = recorder_.read(buffer);

                if (readSize > 0) {
                    decoder_.processRaw(buffer, readSize, false, false);

                    if (decoder_.hyp() != null) {
                        String currentHypothesis = decoder_.hyp().getHypstr();

                        if (! currentHypothesis.equals(previousHypothesis)) {
                            setChanged();
                            notifyObservers(currentHypothesis);

                            previousHypothesis = currentHypothesis;
                        }
                    }
                }
            }

            setChanged();
            notifyObservers(Stage.STOPPED);

            if (! live_) {
                break;
            }

            synchronized (this) {
                ready_ = true;
                notifyAll();

                while (! run_ && live_) {
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

        logger_.info("RealTimeSpeechRecognizer thread shut down gracefully!");
    }

    private void startDecoder () {
        if (! isDecoderStarted_ && decoder_ != null) {
            decoder_.startUtt();
            isDecoderStarted_ = true;
        }
    }

    private void stopDecoder () {
        if (isDecoderStarted_ && decoder_ != null) {
            decoder_.endUtt();
            isDecoderStarted_ = false;
        }
    }

    private void startRecorder () {
        if (! isRecorderStarted_ && recorder_ != null) {
            recorder_.startRecording();
            isRecorderStarted_ = true;
        }
    }

    private void stopRecorder () {
        if (isRecorderStarted_ && recorder_ != null) {
            recorder_.stopRecording();
            isRecorderStarted_ = false;
        }
    }

    /**
     * @brief Terminates this recognizer releasing all of its resources
     */
    public synchronized void terminate () {
        lock_.lock();

        if (live_) {

            if (run_) {
                stopRecognition();
            }

            live_ = false;
            ready_ = false;
            notifyAll();

            while (! ready_) {
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

    private static boolean isLibraryLoaded_ = false; //!< Flag denoting whether the pocket sphinx
                                                     //!< native library has been loaded

    private Thread thread_; //!< Thread used to make the recognition process asynchronous

    private volatile boolean ready_ = false; //!< Thread synchronization flag
    private volatile boolean live_ = false; //!< Thread synchronization flag
    private volatile boolean run_ = false; //!< Thread synchronization flag

    private final ReentrantLock lock_ = new ReentrantLock(); //!< Thread synchronization lock

    private Recorder recorder_; //!< The recorder of this recognizer
    private boolean isRecorderStarted_ = false; //!< Flag denoting whether the recorder has been
                                                //!< started
    private final int sampleRate_; //!< The sample rate of the recorder

    private Decoder decoder_; //!< The pocket sphinx decoder of this recognizer
    private boolean isDecoderStarted_ = false; //!< Flag denoting whether the decoder has been
                                               //!< started

    /**
     * @class Stage
     * @brief Holds the different stages of the recognition process
     */
    public enum Stage {
        STARTED,
        STOPPED
    }

    private final Logger logger_ = Logger.getLogger(getClass().getName()); //!< The logger of this
                                                                           //!< recognizer

}
