package org.pasr.database;


import com.google.gson.Gson;

import java.io.InputStreamReader;

import static org.pasr.utilities.Utilities.getResourceStream;


public class Configuration {
    static{
        Gson gson = new Gson();

        instance_ = gson.fromJson(new InputStreamReader(getResourceStream(
            "/database/default_configuration.json"
        )), Configuration.class);
    }

    private Configuration () {}

    public static Configuration getInstance () {
        return instance_;
    }

    public String getIdngram2lmPath () {
        return idngram2lmPath;
    }

    public String getText2idngramPath () {
        return text2idngramPath;
    }

    public String getWfreq2vocabPath () {
        return wfreq2vocabPath;
    }

    public String getText2wfreqPath () {
        return text2wfreqPath;
    }

    public String getDatabaseDirectoryPath () {
        return databaseDirectoryPath;
    }

    private static Configuration instance_;

    private String databaseDirectoryPath;
    private String text2wfreqPath;
    private String wfreq2vocabPath;
    private String text2idngramPath;
    private String idngram2lmPath;

}
