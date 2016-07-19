package org.pasr.asr;


import com.google.gson.Gson;
import java.io.InputStreamReader;

import static org.pasr.utilities.Utilities.getResourceStream;

public class Configuration {

    private Configuration () {
    }

    public static Configuration getInstance () {
        return instance;
    }

    public String getDefaultAcousticModelPath () {
        return defaultAcousticModelPath;
    }

    public String getDefaultDictionaryPath () {
        return defaultDictionaryPath;
    }

    private static Configuration instance = new Gson().fromJson(
        new InputStreamReader(getResourceStream("/asr/default_paths.json")), Configuration.class
    );

    private String defaultAcousticModelPath;
    private String defaultDictionaryPath;

}
