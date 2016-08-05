package org.pasr.prep.recorder;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class Recorder {
    public Recorder() throws LineUnavailableException {
        targetDataLine_ = AudioSystem.getTargetDataLine(AUDIO_FORMAT);
        targetDataLine_.open(AUDIO_FORMAT);

        // TODO Seems like the audio capturing begins when the line opens and not when the line
        // TODO starts. This might be an Ubuntu thing. For now, do this dirty fix to discard data
        // TODO recorded between open and start calls.
        targetDataLine_.start();
        targetDataLine_.stop();
        targetDataLine_.flush();
    }

    public void startRecording(){
        targetDataLine_.start();
    }

    public void stopRecording(){
        targetDataLine_.stop();
        targetDataLine_.flush();
    }

    int read(byte[] buffer){
        return targetDataLine_.read(buffer, 0, buffer.length);
    }

    public int read(short[] buffer){
        byte[] byteArray = new byte[buffer.length * 2];

        int bytesRead = read(byteArray);

        if(bytesRead != -1){
            ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(buffer);
        }

        return bytesRead / 2;
    }

    public void terminate () throws IOException {
        stopRecording();

        targetDataLine_.close();
    }

    public int getSampleRate(){
        return SAMPLE_RATE;
    }

    volatile TargetDataLine targetDataLine_;

    private static final int SAMPLE_RATE = 16000; // Samples per second

    public static final AudioFormat AUDIO_FORMAT = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);

}
