package org.configuration;


import java.util.List;


public class Configuration {
    public Configuration(){}

    public String getCorpusPath () {
        return corpusPath;
    }

    public String getAcousticModelPath () {
        return acousticModelPath;
    }

    public String getDictionaryPath () {
        return dictionaryPath;
    }

    public String getLanguageModelPath () {
        return languageModelPath;
    }

    public List<String> getSamples () {
        return samples;
    }

    public void setLanguageModelPath (String languageModelPath) {
        this.languageModelPath = languageModelPath;
    }

    public void setCorpusPath (String corpusPath) {
        this.corpusPath = corpusPath;
    }

    public void setDictionaryPath (String dictionaryPath) {
        this.dictionaryPath = dictionaryPath;
    }

    public void setAcousticModelPath (String acousticModelPath) {
        this.acousticModelPath = acousticModelPath;
    }

    public void setSamples (List<String> samples) {
        this.samples = samples;
    }

    private String corpusPath;
    private String dictionaryPath;

    private String acousticModelPath;

    private String languageModelPath;

    private List<String> samples;

}
