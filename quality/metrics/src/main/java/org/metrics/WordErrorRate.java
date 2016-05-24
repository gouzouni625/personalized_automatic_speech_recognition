package org.metrics;


import edu.cmu.sphinx.api.SpeechResult;
import org.configuration.Configuration;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import org.pasr.corpus.Corpus;
import org.pasr.corpus.WordSequence;
import org.pasr.postp.dictionary.Dictionary;
import org.utilities.LevenshteinMatrix;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static org.pasr.utilities.Utilities.getResourceStream;


public class WordErrorRate{
    public WordErrorRate(Configuration configuration) throws IOException {
        corpus_ = Corpus.createFromStream(getResourceStream(configuration.getCorpusPath()));
        dictionary_ = Dictionary.createFromStream(getResourceStream(configuration.getDictionaryPath()));
        samples_ = configuration.getSamples();

        edu.cmu.sphinx.api.Configuration sphinxConfiguration = new edu.cmu.sphinx.api.Configuration();
        sphinxConfiguration.setAcousticModelPath(configuration.getAcousticModelPath());
        sphinxConfiguration.setLanguageModelPath(configuration.getLanguageModelPath());
        sphinxConfiguration.setDictionaryPath(configuration.getDictionaryPath());
        recognizer_ = new StreamSpeechRecognizer(sphinxConfiguration);
    }

    private Corpus corpus_;
    private Dictionary dictionary_;

    private List<String> samples_;

    private StreamSpeechRecognizer recognizer_;

    private boolean isReady_ = false;

    private double errorWordRate_ = 0;

    public void start(){
        isReady_ = false;

        Iterator<WordSequence> corpusIterator = corpus_.iterator();

        for(String sample : samples_){
            recognizer_.startRecognition(getResourceStream(sample));
            SpeechResult recognitionResult;
            StringBuilder hypothesisBuilder = new StringBuilder();

            while ((recognitionResult = recognizer_.getResult()) != null) {
                hypothesisBuilder.append(recognitionResult.getHypothesis());

                System.out.println(recognitionResult.getHypothesis());
            }

            recognizer_.stopRecognition();

            updateErrorWordRate(corpusIterator.next().toString(), hypothesisBuilder.toString());
        }


        isReady_ = true;
    }

    public boolean isReady(){
        return isReady_;
    }

    public double getResult(){
        return errorWordRate_;
    }

    private void updateErrorWordRate(String reference, String hypothesis){
        Integer[] referenceHashes = generateWordHashes(reference);
        Integer[] hypothesisHashes = generateWordHashes(hypothesis);

        errorWordRate_ += ((double) (new LevenshteinMatrix(hypothesisHashes, referenceHashes).getDistance())) / referenceHashes.length;
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
