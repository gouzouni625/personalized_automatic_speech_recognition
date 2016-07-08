package org.pasr.postp.engine;


import org.pasr.asr.language.LanguageModel;
import org.pasr.prep.corpus.Corpus;
import org.pasr.asr.dictionary.Dictionary;

import java.util.ArrayList;
import java.util.List;


public class Corrector {
    public Corrector(Corpus corpus, Dictionary dictionary, LanguageModel languageModel){
        corpus_ = corpus;
        dictionary_ = dictionary;
        languageModel_ = languageModel;

        correctionAlgorithms_ = new ArrayList<>();
    }

    public String correct(String asrOutput) {
        for (CorrectionAlgorithm algorithm : correctionAlgorithms_){
            asrOutput = algorithm.apply(asrOutput, corpus_, dictionary_, languageModel_);
        }

        return asrOutput;
    }

    public interface CorrectionAlgorithm{
        String apply(String asrOutput, Corpus corpus, Dictionary dictionary,
                     LanguageModel languageModel);
    }

    public void addCorrectionAlgorithm(CorrectionAlgorithm correctionAlgorithm){
        correctionAlgorithms_.add(correctionAlgorithm);
    }

    private List<CorrectionAlgorithm> correctionAlgorithms_;

    private Corpus corpus_;

    private Dictionary dictionary_;

    private LanguageModel languageModel_;

}
