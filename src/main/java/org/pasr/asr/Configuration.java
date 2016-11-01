package org.pasr.asr;

import com.google.gson.Gson;

import java.io.InputStreamReader;

import static org.pasr.utilities.Utilities.getResourceStream;


/**
 * @class Configuration
 * @brief Implements the configuration of the CMU Sphinx ASR engine
 *        Contains the path to the acoustic model, the language model and the dictionary.
 */
public class Configuration {

    static {
        DEFAULT_CONFIGURATION = new Gson().fromJson(
            new InputStreamReader(getResourceStream("/asr/default_paths.json")),
            Configuration.class
        );
    }

    /**
     * @brief Default Constructor
     */
    public Configuration () {
        acousticModelPath = "";
        dictionaryPath = "";
        languageModelPath = "";
    }

    /**
     * @brief Returns the acoustic model path
     *
     * @return The acoustic model path
     */
    public String getAcousticModelPath () {
        return acousticModelPath;
    }

    /**
     * @brief Returns the dictionary path
     *
     * @return The dictionary path
     */
    public String getDictionaryPath () {
        return dictionaryPath;
    }

    /**
     * @brief Returns the language model path
     *
     * @return The language model path
     */
    public String getLanguageModelPath () {
        return languageModelPath;
    }

    /**
     * @brief Sets the acoustic model path
     *
     * @param acousticModelPath
     *     The new acoustic model path
     */
    public void setAcousticModelPath (String acousticModelPath) {
        this.acousticModelPath = acousticModelPath;
    }

    /**
     * @brief Sets the dictionary path
     *
     * @param dictionaryPath
     *     The new dictionary path
     */
    public void setDictionaryPath (String dictionaryPath) {
        this.dictionaryPath = dictionaryPath;
    }

    /**
     * @brief Sets the language model path
     *
     * @param languageModelPath
     *     The new language model path
     */
    public void setLanguageModelPath (String languageModelPath) {
        this.languageModelPath = languageModelPath;
    }

    /**
     * @brief Returns the default ASR configuration
     *
     * @return The default ASR configuration
     */
    public static Configuration getDefaultConfiguration () {
        return DEFAULT_CONFIGURATION;
    }

    private String acousticModelPath; //!< The acoustic model path of this Configuration
    private String dictionaryPath; //!< The dictionary path of this Configuration
    private String languageModelPath; //!< The language model path of this Configuration

    private static final Configuration DEFAULT_CONFIGURATION; //!< The default configuration

}
