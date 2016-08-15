package org.pasr.postp.engine.detectors;


import org.pasr.prep.corpus.Word;
import org.pasr.prep.corpus.WordSequence;

import java.util.List;


public interface Detector {
    List<Word> detect (WordSequence wordSequence);
}
