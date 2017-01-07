package org.pasr.database;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.pasr.utilities.Utilities.getResourceStream;


/**
 * @class Configuration
 * @brief Implements the configuration of the Database
 */
public class Configuration {

    /**
     * @brief Default Constructor
     *        Made private to prevent direct instantiation
     */
    private Configuration () {
    }

    /**
     * @brief Creates the singleton instance
     *
     * @return The instance
     *
     * @throws IOException If the default database configuration file is not found
     */
    public static Configuration create () throws IOException {
        if (instance_ == null) {
            InputStream inputStream = getResourceStream("/database/default_configuration.json");

            if (inputStream == null) {
                throw new IOException("Could not find configuration file");
            }

            instance_ = new Gson().fromJson(
                new InputStreamReader(inputStream), Configuration.class
            );
        }

        return instance_;
    }

    /**
     * @brief Returns the database directory path
     *
     * @return The database directory path
     */
    public String getDataBaseDirectoryPath () {
        return dataBaseDirectoryPath;
    }

    /**
     * @brief Returns the corpus directory path
     *
     * @return The corpus directory path
     */
    public String getCorpusDirectoryPath () {
        return corpusDirectoryPath;
    }

    /**
     * @brief Returns the corpus index directory path
     *
     * @return The corpus index directory path
     */
    public String getCorpusIndexPath () {
        return corpusIndexPath;
    }

    /**
     * @brief Returns the arctic index directory path
     *
     * @return The arctic index directory path
     */
    public String getArcticIndexPath () {
        return arcticIndexPath;
    }

    /**
     * @brief Returns the audio directory path
     *
     * @return The audio directory path
     */
    public String getAudioDirectoryPath () {
        return audioDirectoryPath;
    }

    /**
     * @brief Returns the audio index path
     *
     * @return The audio index path
     */
    public String getAudioIndexPath () {
        return audioIndexPath;
    }

    /**
     * @brief Returns the acoustic model directory path
     *
     * @return The acoustic model directory path
     */
    public String getAcousticModelDirectoryPath () {
        return acousticModelDirectoryPath;
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
     * @brief Returns the text2wfreq executable path
     *
     * @return The text2wfreq executable path
     */
    public String getText2wfreqPath () {
        return text2wfreqPath;
    }

    /**
     * @brief Returns the wfreq2vocab executable path
     *
     * @return The wfreq2vocab executable path
     */
    public String getWfreq2vocabPath () {
        return wfreq2vocabPath;
    }

    /**
     * @brief Returns the text2idngram executable path
     *
     * @return The text2idngram executable path
     */
    public String getText2idngramPath () {
        return text2idngramPath;
    }

    /**
     * @brief Returns the idngram2lm executable path
     *
     * @return The idngram2lm executable path
     */
    public String getIdngram2lmPath () {
        return idngram2lmPath;
    }

    /**
     * @brief Returns the sphinxFe executable path
     *
     * @return The sphinxFe executable path
     */
    public String getSphinxFePath () {
        return sphinxFePath;
    }

    /**
     * @brief Returns the bw executable path
     *
     * @return The bw executable path
     */
    public String getBwPath () {
        return bwPath;
    }

    /**
     * @brief Returns the mllrSolve executable path
     *
     * @return The mllrSolve executable path
     */
    public String getMllrSolvePath () {
        return mllrSolvePath;
    }

    /**
     * @brief Returns the mapAdapt executable path
     *
     * @return The mapAdapt executable path
     */
    public String getMapAdaptPath () {
        return mapAdaptPath;
    }

    /**
     * @brief Returns the instance of this singleton
     *
     * @return The instance of this singleton
     */
    public static Configuration getInstance () {
        return instance_;
    }

    private static Configuration instance_; //!< The instance of this singleton

    private String dataBaseDirectoryPath; //!< The path where the database is stored

    private String corpusDirectoryPath; //!< The path where all the corpora are stored
    private String corpusIndexPath; //!< The path where the corpora index is stored

    private String arcticIndexPath; //!< The path where the arctic index is stored

    private String audioDirectoryPath; //!< The path where all the audio data are stored
    private String audioIndexPath; //!< The path where the audio index is stored

    private String acousticModelDirectoryPath; //!< The path where the acoustic model is stored
    private String acousticModelPath; //!< The path were the acoustic model files are stored

    private String text2wfreqPath; //!< The path to the text2wfreq executable
    private String wfreq2vocabPath; //!< The path to the wfreq2vocab executable
    private String text2idngramPath; //!< The path to the text2idngram executable
    private String idngram2lmPath; //!< The path to the idngram2lm executable

    private String sphinxFePath; //!< The path to the sphinxFe executable
    private String bwPath; //!< The path to the bw executable
    private String mllrSolvePath; //!< The path to the mllrSolve executable
    private String mapAdaptPath; //!< The path to the mapAdapt executable

}
