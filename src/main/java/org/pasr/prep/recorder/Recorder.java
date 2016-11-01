package org.pasr.prep.recorder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Observable;


/**
 * @class Recorder
 * @brief Implements a sound recorder wrapping a TargetDataLine
 */
public class Recorder extends Observable {

    /**
     * @brief Default Constructor
     *
     * @throws LineUnavailableException If the sound line is unavailable
     */
    public Recorder () throws LineUnavailableException {
        targetDataLine_ = AudioSystem.getTargetDataLine(AUDIO_FORMAT);
        targetDataLine_.open(AUDIO_FORMAT);

        // TODO Seems like the audio capturing begins when the line opens and not when the line
        // TODO starts. This might be an Ubuntu thing. For now, do this dirty fix to discard data
        // TODO recorded between open and start calls.
        targetDataLine_.start();
        targetDataLine_.stop();
        targetDataLine_.flush();
    }

    /**
     * @brief Starts the recording process
     */
    public synchronized void startRecording () {
        targetDataLine_.start();
    }

    /**
     * @brief Stops the recording process
     */
    public synchronized void stopRecording () {
        targetDataLine_.stop();
        targetDataLine_.flush();
    }

    /**
     * @brief Reads data from the TargetDataLine into the given buffer
     *        The number of bytes read is equal to the length of the buffer
     *
     * @param buffer
     *     The buffer to save the data in
     *
     * @return The number of bytes actually read
     */
    synchronized int read (byte[] buffer) {
        return targetDataLine_.read(buffer, 0, buffer.length);
    }

    /**
     * @brief Reads data from the TargetDataLine into the given buffer
     *        The number of bytes read is equals to the double of the length of the buffer
     *
     * @param buffer
     *     The buffer to save the data in
     *
     * @return The number of shorts actually read
     */
    public synchronized int read (short[] buffer) {
        byte[] byteArray = new byte[buffer.length * 2];

        int bytesRead = read(byteArray);

        if (bytesRead != - 1) {
            ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(buffer);
        }

        return bytesRead / 2;
    }

    /**
     * @brief Returns the sample rate of this Recorder
     *
     * @return The sample rate of this Recorder
     */
    public int getSampleRate () {
        return SAMPLE_RATE;
    }

    /**
     * @brief Terminates this Recorder releasing all of its resources
     */
    public synchronized void terminate () {
        stopRecording();

        targetDataLine_.close();
    }

    volatile TargetDataLine targetDataLine_; //!< The TargetDataLine of this Recorder

    private static final int SAMPLE_RATE = 16000; //!< The sample rate in samples per second

    public static final AudioFormat AUDIO_FORMAT = new AudioFormat(
        SAMPLE_RATE, 16, 1, true, false); //!< The AudioFormat of this Recorder

}
