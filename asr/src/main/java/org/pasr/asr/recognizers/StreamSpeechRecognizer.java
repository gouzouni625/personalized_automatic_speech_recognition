package org.pasr.asr.recognizers;

import cz.adamh.utils.NativeUtils;
import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import org.pasr.asr.ASRConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * @class StreamSpeechRecognizer
 * @brief Implements a recognizer getting its input from an InputStream
 */
public class StreamSpeechRecognizer {

    /**
     * @brief Constructor
     *
     * @param asrConfiguration
     *     The ASRConfiguration for this recognizer
     *
     * @throws IOException If the pocket sphinx native library cannot be loaded
     */
    public StreamSpeechRecognizer (ASRConfiguration asrConfiguration) throws IOException {

        if (! isLibraryLoaded_) {
            NativeUtils.loadLibraryFromJar("/libpocketsphinx_jni.so");
            isLibraryLoaded_ = true;
        }

        decoderConfig_ = Decoder.defaultConfig();
        decoderConfig_.setString("-hmm", asrConfiguration.getAcousticModelPath().toString());
        decoderConfig_.setString("-dict", asrConfiguration.getDictionaryPath().toString());
        decoderConfig_.setString("-lm", asrConfiguration.getLanguageModelPath().toString());

        decoder_ = new Decoder(decoderConfig_);
    }

    /**
     * @brief Recognizes the data read from the given InputStream
     *
     * @param inputStream
     *     The InputStream to read from
     *
     * @return The recognized word sequence
     *
     * @throws IOException If an I/O error occurs
     */
    public String recognize (InputStream inputStream) throws IOException {
        decoder_.startUtt();
        short[] buffer = new short[16384];
        int read;

        while ((read = read(inputStream, buffer)) > 0) {
            decoder_.processRaw(buffer, read, false, false);
        }
        decoder_.endUtt();

        inputStream.close();

        String hypothesis = "";
        if (decoder_.hyp() != null) {
            hypothesis = decoder_.hyp().getHypstr();
        }

        return hypothesis;
    }

    /**
     * @brief Reads data from an InputStream and places them inside a short buffer
     *
     * @param inputStream
     *     The InputStream to read data from
     * @param buffer
     *     The buffer to place the data
     *
     * @return The number of shorts read
     *
     * @throws IOException If an I/O error occurs
     */
    private int read (InputStream inputStream, short[] buffer) throws IOException {
        byte[] byteArray = new byte[buffer.length * 2];

        int bytesRead = inputStream.read(byteArray);

        if (bytesRead != - 1) {
            ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(buffer);
        }

        return bytesRead / 2;
    }

    private Decoder decoder_; //!< The pocket sphinx decoder of this recognizer
    private Config decoderConfig_; //!< The ASR Configuration of this recognizer

    private static boolean isLibraryLoaded_ = false; //!< Flag denoting whether the pocket sphinx
                                                     //!< native library has been loaded

}
