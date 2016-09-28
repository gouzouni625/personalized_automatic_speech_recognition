package org.pasr.prep.corpus;

import javafx.scene.input.DataFormat;

import java.io.Serializable;


/**
 * @class Document
 * @brief Implements a Document which is a representation of an E-mail in the Corpus world
 */
public class Document implements Serializable {

    /**
     * @brief Constructor
     *
     * @param id
     *     The id of this Document
     * @param title
     *     The title of this Document
     * @param content
     *     The content of this Document
     */
    public Document (long id, String title, String content) {
        id_ = id;
        title_ = title;
        content_ = content;
    }

    /**
     * @brief Returns the id of this Document
     *
     * @return The id of this Document
     */
    public long getId () {
        return id_;
    }

    /**
     * @brief Returns the title of this Document
     *
     * @return The title of this Document
     */
    public String getTitle () {
        return title_;
    }

    /**
     * @brief Returns the content of this Document
     *
     * @return The content of this Document
     */
    public String getContent () {
        return content_;
    }

    /**
     * @brief Returns the String of this Document
     *        The String of a Document is its content
     *
     * @return The String of this Document
     */
    @Override
    public String toString () {
        return getContent();
    }

    private final long id_; //!< The id of this Document
    private final String title_; //!< The title of this Document
    private final String content_; //!< The content of this Document

    // Used for drag and drop
    public static final DataFormat DATA_FORMAT = new DataFormat(
        "org.pasr.prep.corpus.Document"); //!< DataFormat used for drag and drop operations with
                                          //!< JavaFX

}
