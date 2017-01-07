package org.pasr.database.corpus;

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
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * @class Index
 * @brief Implements the DataBase index of the corpora
 */
public class Index extends ArrayList<Index.Entry> {
    private static final Logger logger_ = Logger.getLogger(Index.class.getName());

    static {
        Index instance;
        try {
            instance = new Gson().fromJson(new InputStreamReader(new FileInputStream(
                Configuration.getInstance().getCorpusIndexPath()
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
     * @brief Implements an Index entry
     */
    public static class Entry {
        public Entry (int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId () {
            return id;
        }

        public String getName () {
            return name;
        }

        @Override
        public String toString () {
            return id + ". " + name;
        }

        private final int id;
        private final String name;
    }

    /**
     * @brief Returns the id of the next Corpus
     *
     * @return The id of the next Corpus
     */
    public int nextId () {
        Optional<Integer> maxId = stream()
            .map(Entry:: getId)
            .reduce(BinaryOperator.maxBy(Integer:: compare));

        if (maxId.isPresent()) {
            return maxId.get() + 1;
        }
        else {
            return 1;
        }
    }

    /**
     * @brief Returns true if the given id exists
     *
     * @param id
     *     The id
     *
     * @return True if the given id exists
     */
    public boolean containsId (int id) {
        for (Entry entry : this) {
            if (entry.getId() == id) {
                return true;
            }
        }

        return false;
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
     * @brief Removes the Entry with the given id
     *
     * @param id
     *     The id of the Entry to be removed
     */
    public void removeById (int id) {
        removeAll(stream()
            .filter(entry -> entry.getId() == id)
            .collect(Collectors.toList()));
    }

    /**
     * @brief Saves this Index
     *
     * @throws FileNotFoundException If an I/O error occurs
     */
    public void save () throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
            Configuration.getInstance().getCorpusIndexPath()
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
