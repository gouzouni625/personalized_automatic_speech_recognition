package org.metrics;


import edu.cmu.sphinx.api.SpeechResult;
import org.configuration.Configuration;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import org.pasr.prep.corpus.Corpus;
import org.pasr.prep.corpus.WordSequence;
import org.pasr.asr.dictionary.Dictionary;
import org.pasr.postp.engine.Corrector;
import org.pasr.postp.engine.algorithms.PhoneDistanceAlgorithm;
import org.pasr.utilities.LevenshteinMatrix;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static org.pasr.utilities.Utilities.getResourceStream;


public class WordErrorRate{
    public WordErrorRate(Configuration configuration) throws IOException {
        corpus_ = Corpus.createFromStream(getResourceStream(configuration.getCorpusPath()));
        corpus_.process(null);
        dictionary_ = Dictionary.createFromStream(getResourceStream(configuration.getDictionaryPath()));
        samples_ = configuration.getSamples();

        corrector_ = new Corrector(corpus_, dictionary_, null);
        corrector_.addCorrectionAlgorithm(new PhoneDistanceAlgorithm());

        edu.cmu.sphinx.api.Configuration sphinxConfiguration = new edu.cmu.sphinx.api.Configuration();
        sphinxConfiguration.setAcousticModelPath(configuration.getAcousticModelPath());
        sphinxConfiguration.setLanguageModelPath(configuration.getLanguageModelPath());
        sphinxConfiguration.setDictionaryPath(configuration.getDictionaryPath());
        recognizer_ = new StreamSpeechRecognizer(sphinxConfiguration);
    }

    private Corpus corpus_;
    private Dictionary dictionary_;

    private Corrector corrector_;

    private List<String> samples_;

    private StreamSpeechRecognizer recognizer_;

    private boolean isReady_ = false;

    private int numberOfWordEdits = 0;
    private int numberOfReferenceWords = 0;

    public void start() throws FileNotFoundException {
        isReady_ = false;

        Iterator<WordSequence> corpusIterator = corpus_.iterator();

        for(String sample : samples_){
            System.out.println("Processing sample: " + sample);

            recognizer_.startRecognition(getResourceStream(sample));
            SpeechResult recognitionResult;
            StringBuilder hypothesisBuilder = new StringBuilder();

            while ((recognitionResult = recognizer_.getResult()) != null) {
                hypothesisBuilder.append(recognitionResult.getHypothesis()).append(" ");
            }

            recognizer_.stopRecognition();

            // updateErrorWordRate(corpusIterator.next().toString(), correctionBuilder.toString());
            // updateErrorWordRate(corpusIterator.next().toString(), hypothesisBuilder.toString());
            updateErrorWordRate(corpusIterator.next().toString(), corrector_.correct(hypothesisBuilder.toString()));
        }


        isReady_ = true;
    }

    public boolean isReady(){
        return isReady_;
    }

    public double getResult(){
        return ((double)(numberOfWordEdits)) / ((double)(numberOfReferenceWords)) * 100;
    }

    private void updateErrorWordRate(String reference, String hypothesis){
        Integer[] referenceHashes = generateWordHashes(reference);
        Integer[] hypothesisHashes = generateWordHashes(hypothesis);

        numberOfReferenceWords += referenceHashes.length;

        int currentNumberOfWordEdits = new LevenshteinMatrix<>(hypothesisHashes, referenceHashes).getDistance();
        numberOfWordEdits += currentNumberOfWordEdits;

        System.out.println("Number of words: " + referenceHashes.length);
        System.out.println("Number of edits: " + currentNumberOfWordEdits);

        if(referenceHashes.length != 0) {
            System.out.println("Accuracy: " + ((1 - ((double)(currentNumberOfWordEdits) / (double)(referenceHashes.length))) * 100) + "%");
        }
        else{
            System.out.println("Accuracy: 100%");
        }
    }

    private Integer[] generateWordHashes(String string){
        String[] words = string.split(" ");

        int numberOfWords = words.length;

        Integer[] hashes = new Integer[numberOfWords];

        for(int i = 0;i < numberOfWords;i++){
            hashes[i] = words[i].hashCode();
        }

        return hashes;
    }

}
