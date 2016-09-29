package org.pasr.prep.recorder;

import org.apache.commons.io.output.ByteArrayOutputStream;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.pasr.utilities.Utilities.rootMeanSquare;


/**
 * @class BufferedRecorder
 * @brief Implements a Recorder that buffs its data in a Stream
 */
public class BufferedRecorder extends Recorder {

    /**
     * @brief Default Constructor
     *
     * @throws LineUnavailableException If the sound line is unavailable
     */
    public BufferedRecorder () throws LineUnavailableException {
        super();

        byteArrayOutputStream_ = new ByteArrayOutputStream();

        thread_ = new Thread(this :: run);
        thread_.setDaemon(true);
    }

    /**
     * @brief Starts the recording process
     */
    @Override
    public synchronized void startRecording () {
        lock_.lock();

        // Make sure that successive calls of this method doesn't cause any harm
        if (run_ && live_) {
            lock_.unlock();
            return;
        }

        // Make sure that terminate hasn't been called before
        if (byteArrayOutputStream_ == null || thread_ == null) {
            lock_.unlock();
            return;
        }

        if (! thread_.isAlive()) {
            // Start the thread
            thread_.start();

            // wait for the thread to go to sleep in a while loop to avoid a spurious wake up
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

        super.startRecording();

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
     * @brief Stops the recording process
     */
    @Override
    public synchronized void stopRecording () {
        lock_.lock();

        // Make sure that successive calls of this method doesn't cause any harm
        if (! (run_ && live_)) {
            lock_.unlock();
            return;
        }

        // Make sure that terminate hasn't been called before
        if (byteArrayOutputStream_ == null || thread_ == null) {
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

        super.stopRecording();

        lock_.unlock();
    }

    /**
     * @brief The recording method
     */
    private void run () {
        logger_.info("BufferedRecorder thread started!");

        // Upon start, go to wait immediately after you signal that you are ready to
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

        // For the size of the buffer see
        // https://docs.oracle.com/javase/tutorial/sound/capturing.html
        byte[] buffer = new byte[targetDataLine_.getBufferSize() / 5];

        while (live_) {
            while (run_) {
                int readSize = read(buffer);

                if (readSize > 0) {
                    setLevel(rootMeanSquare(buffer) / 10000);

                    byteArrayOutputStream_.write(buffer, 0, readSize);
                }
            }

            setLevel(0);

            // There is no need to go to wait if you should die
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

        logger_.info("BufferedRecorder thread shut down gracefully!");
    }

    /**
     * @brief Returns the buffed data
     *
     * @return The buffed data
     */
    public byte[] getData () {
        return byteArrayOutputStream_.toByteArray();
    }

    /**
     * @brief Returns a Clip created from the buffed data
     *
     * @return A Clip created from the buffed data
     *
     * @throws LineUnavailableException If a Clip cannot be created
     */
    public Clip getClip () throws LineUnavailableException {
        byte[] array = byteArrayOutputStream_.toByteArray();

        int length = array.length;
        if (length == 0) {
            return null;
        }
        else {
            Clip clip = AudioSystem.getClip();
            clip.open(AUDIO_FORMAT, array, 0, length);

            return clip;
        }
    }

    /**
     * @brief Sets the input sound level
     *
     * @param level
     *     The level valu
     */
    private void setLevel (double level) {
        setChanged();
        notifyObservers(level);
    }

    /**
     * @brief Deletes all the buffed data
     */
    public synchronized void flush () {
        lock_.lock();

        byteArrayOutputStream_.reset();

        lock_.unlock();
    }

    /**
     * @brief Terminates this Recorder releasing all of its resources
     */
    @Override
    public synchronized void terminate () {
        lock_.lock();

        // Make sure that successive calls of this method doesn't cause any harm
        if (byteArrayOutputStream_ == null || thread_ == null) {
            lock_.unlock();
            return;
        }

        if (live_) {
            if (run_) {
                stopRecording();
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

        try {
            byteArrayOutputStream_.close();
        } catch (IOException e) {
            logger_.log(Level.WARNING, "Could not close the ByteArrayOutputStream instance.", e);
        } finally {
            byteArrayOutputStream_ = null;
        }

        thread_ = null;

        super.terminate();

        lock_.unlock();
    }

    private Thread thread_; //!< The Thread reading the TargetDataLine

    private volatile boolean ready_ = false; //!< Thread synchronization flag
    private volatile boolean live_ = false; //!< Thread synchronization flag
    private volatile boolean run_ = false; //!< Thread synchronization flag

    private final ReentrantLock lock_ = new ReentrantLock(); //!< Thread synchronization lock

    private volatile ByteArrayOutputStream byteArrayOutputStream_; //!< The Stream buffer

    private final Logger logger_ = Logger.getLogger(getClass().getName()); //!< The Logger

}
