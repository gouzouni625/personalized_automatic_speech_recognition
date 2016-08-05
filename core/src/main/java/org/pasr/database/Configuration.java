package org.pasr.database;


import com.google.gson.Gson;

import java.io.InputStreamReader;

import static org.pasr.utilities.Utilities.getResourceStream;


public class Configuration {
    static{
        instance_ = new Gson().fromJson(new InputStreamReader(getResourceStream(
            "/database/default_configuration.json"
        )), Configuration.class);
    }

    private Configuration () {}

    public String getCorpusDirectoryPath(){
        return corpusDirectoryPath;
    }

    public String getCorpusIndexPath(){
        return corpusIndexPath;
    }

    public String getArcticIndexPath(){
        return arcticIndexPath;
    }

    public String getAudioDirectoryPath (){
        return audioDirectoryPath;
    }

    public String getAudioIndexPath() {
        return audioIndexPath;
    }

    public String getAcousticModelDirectoryPath(){
        return acousticModelDirectoryPath;
    }

    public String getAcousticModelPath(){
        return acousticModelPath;
    }

    public String getText2wfreqPath () {
        return text2wfreqPath;
    }

    public String getWfreq2vocabPath () {
        return wfreq2vocabPath;
    }

    public String getText2idngramPath () {
        return text2idngramPath;
    }

    public String getIdngram2lmPath () {
        return idngram2lmPath;
    }

    public String getSphinxFePath () {
        return sphinxFePath;
    }

    public String getBwPath () {
        return bwPath;
    }

    public String getMllrSolvePath () {
        return mllrSolvePath;
    }

    public String getMapAdaptPath(){
        return mapAdaptPath;
    }

    public static Configuration getInstance () {
        return instance_;
    }

    private static Configuration instance_;

    private String corpusDirectoryPath;
    private String corpusIndexPath;

    private String arcticIndexPath;

    private String audioDirectoryPath;
    private String audioIndexPath;

    private String acousticModelDirectoryPath;
    private String acousticModelPath;

    private String text2wfreqPath;
    private String wfreq2vocabPath;
    private String text2idngramPath;
    private String idngram2lmPath;

    private String sphinxFePath;
    private String bwPath;
    private String mllrSolvePath;
    private String mapAdaptPath;

}
