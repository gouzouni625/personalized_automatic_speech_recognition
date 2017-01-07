package org.pasr.model.asr;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;

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
            new InputStreamReader(getResourceStream("/default_paths.json")),
            Configuration.class
        );
    }

    @Setter
    @Getter
    private String acousticModelPath = ""; //!< The acoustic model path of this Configuration

    @Setter
    @Getter
    private String dictionaryPath = ""; //!< The dictionary path of this Configuration

    @Setter
    @Getter
    private String languageModelPath = ""; //!< The language model path of this Configuration

    public static final Configuration DEFAULT_CONFIGURATION; //!< The default configuration

}
