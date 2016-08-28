package org.pasr.asr;


import com.google.gson.Gson;
import java.io.InputStreamReader;

import static org.pasr.utilities.Utilities.getResourceStream;

public class Configuration {
    static{
        DEFAULT_CONFIGURATION = new Gson().fromJson(
            new InputStreamReader(getResourceStream("/asr/default_paths.json")),
            Configuration.class
        );
    }

    public Configuration () {
        acousticModelPath = "";
        dictionaryPath = "";
        languageModelPath = "";
    }

    public String getAcousticModelPath(){
        return acousticModelPath;
    }

    public String getDictionaryPath(){
        return dictionaryPath;
    }

    public String getLanguageModelPath(){
        return languageModelPath;
    }

    public void setAcousticModelPath(String acousticModelPath){
        this.acousticModelPath = acousticModelPath;
    }

    public void setDictionaryPath(String dictionaryPath){
        this.dictionaryPath = dictionaryPath;
    }

    public void setLanguageModelPath(String languageModelPath){
        this.languageModelPath = languageModelPath;
    }

    public static Configuration getDefaultConfiguration(){
        return DEFAULT_CONFIGURATION;
    }

    private String acousticModelPath;
    private String dictionaryPath;
    private String languageModelPath;

    private static final Configuration DEFAULT_CONFIGURATION;

}
