package org.engine;


import org.corpus.Corpus;
import org.corpus.WordSequence;

public class Corrector {
    public Corrector(Corpus corpus){
        corpus_ = corpus;
    }

     public String correct(String asrOutput){
         WordSequence matchingSequence = corpus_.matchText(asrOutput);

         // If the whole asr output exists inside the corpus, consider it correct
         if(matchingSequence.equals(asrOutput)){
             return asrOutput;
         }

         return matchingSequence.getText();
     }

    private Corpus corpus_;

}
