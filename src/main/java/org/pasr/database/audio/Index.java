package org.pasr.database.audio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.pasr.database.Configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Logger;


/**
 * @class Index
 * @brief Implements the DataBase index of the audio files
 */
public class Index extends ArrayList<Index.Entry> {
    private static final Logger logger_ = Logger.getLogger(Index.class.getName());

    static {
        Index instance;
        try {
            instance = new Gson().fromJson(new InputStreamReader(new FileInputStream(
                Configuration.getInstance().getAudioIndexPath()
            )), Index.class);
        } catch (FileNotFoundException | JsonIOException | JsonSyntaxException e) {
            logger_.warning("Could not load Index.");
            instance = new Index();
        }

        instance_ = instance == null ? new Index() : instance;
    }

    private Index () {
    }

    /**
     * @class Entry
     * @brief Implements and Index Entry
     */
    public static class Entry {
        public Entry (String filename, String sentence, int corpusID) {
            this.filename = filename;
            this.sentence = sentence;
            this.corpusID = corpusID;
        }

        public String getFilename () {
            return filename;
        }

        public String getSentence () {
            return sentence;
        }

        public int getCorpusID () {
            return corpusID;
        }

        private final String filename;
        private final String sentence;
        private final int corpusID;
    }

    /**
     * @brief Returns a JSON String of this Index
     *
     * @return A JSON String of this INdex
     */
    public String toJson () {
        return serializer_.toJson(this);
    }

    /**
     * @brief Saves this Index
     *
     * @throws FileNotFoundException If an I/O error occurs
     */
    public void save () throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
            Configuration.getInstance().getAudioIndexPath()
        )));

        serializer_.toJson(this, printWriter);

        printWriter.close();
    }

    /**
     * @brief Returns the instance of this singleton
     *
     * @return The instance of this singleton
     */
    public static Index getInstance () {
        return instance_;
    }

    private static Index instance_; //!< The instance of this singleton

    private static Gson serializer_ = new GsonBuilder().setPrettyPrinting().create();

}
