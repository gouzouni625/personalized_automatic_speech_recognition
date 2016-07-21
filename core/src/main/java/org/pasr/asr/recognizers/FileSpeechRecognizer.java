package org.pasr.asr.recognizers;


import cz.adamh.utils.NativeUtils;
import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import org.pasr.asr.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class FileSpeechRecognizer {
    static {
        try {
            NativeUtils.loadLibraryFromJar("/libpocketsphinx_jni.so");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileSpeechRecognizer(Configuration configuration){
        Config decoderConfig = Decoder.defaultConfig();
        decoderConfig.setString("-hmm", configuration.getAcousticModelPath());
        decoderConfig.setString("-dict", configuration.getDictionaryPath());
        decoderConfig.setString("-lm", configuration.getLanguageModelPath());

        decoder_ = new Decoder(decoderConfig);
    }

    public String recognize(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);

        decoder_.startUtt();
        short[] buffer = new short[2048];
        int read;

        while((read = read(fileInputStream, buffer)) > 0){
            decoder_.processRaw(buffer, read, false, false);
        }
        decoder_.endUtt();

        fileInputStream.close();

        if(decoder_.hyp() != null){
            return decoder_.hyp().getHypstr();
        }
        else{
            return "";
        }
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

}
