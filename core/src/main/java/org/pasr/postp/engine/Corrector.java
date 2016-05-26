package org.pasr.postp.engine;


import org.pasr.corpus.Corpus;
import org.pasr.postp.dictionary.Dictionary;

import java.util.ArrayList;
import java.util.List;


public class Corrector {
    public Corrector(Corpus corpus, Dictionary dictionary){
        corpus_ = corpus;
        dictionary_ = dictionary;

        correctionAlgorithms_ = new ArrayList<>();
    }

    public String correct(String asrOutput) {
        for (CorrectionAlgorithm algorithm : correctionAlgorithms_){
            asrOutput = algorithm.apply(asrOutput, corpus_, dictionary_);
        }

        return asrOutput;
    }

    public interface CorrectionAlgorithm{
        String apply(String asrOutput, Corpus corpus, Dictionary dictionary);
    }

    public void addCorrectionAlgorithm(CorrectionAlgorithm correctionAlgorithm){
        correctionAlgorithms_.add(correctionAlgorithm);
    }

    private List<CorrectionAlgorithm> correctionAlgorithms_;

    private Corpus corpus_;

    private Dictionary dictionary_;

}
