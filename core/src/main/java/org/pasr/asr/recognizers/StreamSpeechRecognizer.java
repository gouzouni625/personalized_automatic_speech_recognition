package org.pasr.asr.recognizers;


import cz.adamh.utils.NativeUtils;
import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import org.pasr.asr.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class StreamSpeechRecognizer {
    public StreamSpeechRecognizer (Configuration configuration) throws IOException {

        if(!isLibraryLoaded_) {
            NativeUtils.loadLibraryFromJar("/libpocketsphinx_jni.so");
            isLibraryLoaded_ = true;
        }

        decoderConfig_ = Decoder.defaultConfig();
        decoderConfig_.setString("-hmm", configuration.getAcousticModelPath());
        decoderConfig_.setString("-dict", configuration.getDictionaryPath());
        decoderConfig_.setString("-lm", configuration.getLanguageModelPath());

        decoder_ = new Decoder(decoderConfig_);
    }

    public String recognize(InputStream inputStream) throws IOException {
        decoder_.startUtt();
        short[] buffer = new short[16384];
        int read;

        while((read = read(inputStream, buffer)) > 0){
            decoder_.processRaw(buffer, read, false, false);
        }
        decoder_.endUtt();

        inputStream.close();

        String hypothesis = "";
        if(decoder_.hyp() != null){
            hypothesis = decoder_.hyp().getHypstr();
        }

        return hypothesis;
    }

    private int read(InputStream inputStream, short[] buffer) throws IOException {
        byte[] byteArray = new byte[buffer.length * 2];

        int bytesRead = inputStream.read(byteArray);

        if(bytesRead != -1){
            ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(buffer);
        }

        return bytesRead / 2;
    }

    private Decoder decoder_;
    private Config decoderConfig_;

    private static boolean isLibraryLoaded_ = false;

}
