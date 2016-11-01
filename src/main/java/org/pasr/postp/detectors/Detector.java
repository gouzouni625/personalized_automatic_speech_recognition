package org.pasr.postp.detectors;

import org.pasr.prep.corpus.Word;
import org.pasr.prep.corpus.WordSequence;

import java.util.List;


/**
 * @class Detector
 * @brief Defines the API that every error word detector should implement
 */
public interface Detector {

    /**
     * @brief Returns a List with the error words from the given WordSequence
     *
     * @param wordSequence
     *     The WordSequence
     *
     * @return A List with the error words from the given WordSequence
     */
    List<Word> detect (WordSequence wordSequence);

}
