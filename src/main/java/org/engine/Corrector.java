package org.engine;


import org.corpus.Corpus;
import org.corpus.WordSequence;
import org.dictionary.Dictionary;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;

public class Corrector {
    public Corrector(Corpus corpus, Dictionary dictionary){
        corpus_ = corpus;

        dictionary_ = dictionary;
    }

    public String correct(String asrOutput) {
        List<WordSequence> matchingSequences = corpus_.matchText(asrOutput);

        // If the whole asr output exists inside the corpus, consider it correct
        for (WordSequence matchingSequence : matchingSequences) {
            if (matchingSequence.equals(asrOutput)) {
                return asrOutput;
            }
        }

        String[] errorWords = asrOutput.toLowerCase().split(matchingSequences.get(0).getText());
        String precedingErrorWords = errorWords[0].trim();
        String succeedingErrorWords = errorWords[1].trim();

        boolean checkPreceding = precedingErrorWords.length() > 0;
        boolean checkSucceeding = succeedingErrorWords.length() > 0;

        String[] precedingErrorWordPhones = dictionary_.getPhones(precedingErrorWords.split(" "));
        String[] succeedingErrorWordPhones = dictionary_.getPhones(succeedingErrorWords.split(" "));

        int minPrecedingDistance = Integer.MAX_VALUE;
        int minSucceedingDistance = Integer.MAX_VALUE;

        int currentPrecedingDistance;
        int currentSucceedingDistance;

        WordSequence chosenPrecedingSequence = null;
        WordSequence chosenSucceedingSequence = null;

        for (WordSequence matchingSequence : matchingSequences) {
            WordSequence precedingCandidateWords = matchingSequence.
                    getWords()[0].
                    getWordSequence().subSequence(0,
                    matchingSequence.getWords()[0].getIndex());

            WordSequence succeedingCandidateWords = matchingSequence.
                    getWords()[matchingSequence.getWords().length - 1].
                    getWordSequence().subSequence(
                    matchingSequence.getWords()[
                            matchingSequence.getWords().length - 1].getIndex() + 1);

            if(checkPreceding){
                String[] precedingCandidateWordPhones = dictionary_.getPhones(precedingCandidateWords);

                currentPrecedingDistance = getLevenshteinDistance(
                        concatenateStringArray(precedingErrorWordPhones),
                        concatenateStringArray(precedingCandidateWordPhones));

                if(currentPrecedingDistance < minPrecedingDistance){
                    minPrecedingDistance = currentPrecedingDistance;

                    chosenPrecedingSequence = precedingCandidateWords;
                }
            }

            if(checkSucceeding) {
                String[] succeedingCandidateWordPhones = dictionary_.getPhones(succeedingCandidateWords);


                currentSucceedingDistance = getLevenshteinDistance(
                        concatenateStringArray(succeedingErrorWordPhones),
                        concatenateStringArray(succeedingCandidateWordPhones));

                if (currentSucceedingDistance < minSucceedingDistance) {
                    minSucceedingDistance = currentSucceedingDistance;

                    chosenSucceedingSequence = succeedingCandidateWords;
                }
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        if(chosenPrecedingSequence != null){
            stringBuilder.append(chosenPrecedingSequence);
        }

        stringBuilder.append(" ").append(matchingSequences.get(0)).append(" ");

        if(chosenSucceedingSequence != null){
            stringBuilder.append(chosenSucceedingSequence);
        }

        return stringBuilder.toString();
     }

    private String concatenateStringArray(String[] array){
        StringBuilder stringBuilder = new StringBuilder();

        for(String item : array){
            stringBuilder.append(item);
        }

        return stringBuilder.toString();
    }

    private Corpus corpus_;

    private Dictionary dictionary_;

}
