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
        WordSequence asrOutputWS = new WordSequence(asrOutput.toLowerCase(), " ");

        List<WordSequence> matchingSequences = corpus_.matchWordSequence(asrOutputWS);

        // If the whole asr output exists inside the corpus, consider it correct
        for (WordSequence matchingSequence : matchingSequences) {
            if (matchingSequence.equals(asrOutputWS.getText())) {
                return asrOutputWS.getText();
            }
        }

        WordSequence[] errorWordSequences = asrOutputWS.split(matchingSequences.get(0));
        WordSequence errorSequenceOnTheLeft = errorWordSequences[0];
        WordSequence errorSequenceOnTheRight = errorWordSequences[1];

        boolean checkLeft = errorSequenceOnTheLeft.getWords().length > 0;
        boolean checkRight = errorSequenceOnTheRight.getWords().length > 0;

        double minDifferenceOnTheLeft = Double.POSITIVE_INFINITY;
        double minDifferenceOnTheRight = Double.POSITIVE_INFINITY;

        WordSequence chosenSequenceOnTheLeft = null;
        WordSequence chosenSequenceOnTheRight = null;

        for (WordSequence matchingSequence : matchingSequences) {
            WordSequence candidateSequenceOnTheLeft = matchingSequence
                    .getWords()[0]
                    .getWordSequence().subSequence(0,
                            matchingSequence.getWords()[0].getIndex());

            WordSequence candidateSequenceOnTheRight = matchingSequence
                    .getWords()[matchingSequence.getWords().length - 1]
                    .getWordSequence().subSequence(
                            matchingSequence.getWords()[
                                    matchingSequence.getWords().length - 1].getIndex() + 1);

            if(checkLeft) {
                double difference = wordSequencesDifference(errorSequenceOnTheLeft,
                        candidateSequenceOnTheLeft);

                if (difference < minDifferenceOnTheLeft) {
                    minDifferenceOnTheLeft = difference;

                    chosenSequenceOnTheLeft = bestMatch_;
                }
            }

            if(checkRight) {
                double difference = wordSequencesDifference(errorSequenceOnTheRight,
                        candidateSequenceOnTheRight);

                if (difference < minDifferenceOnTheRight) {
                    minDifferenceOnTheRight = difference;

                    chosenSequenceOnTheRight = bestMatch_;
                }
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        if(chosenSequenceOnTheLeft != null){
            stringBuilder.append(chosenSequenceOnTheLeft);
        }

        stringBuilder.append(" ").append(matchingSequences.get(0)).append(" ");

        if(chosenSequenceOnTheRight != null){
            stringBuilder.append(chosenSequenceOnTheRight);
        }

        return stringBuilder.toString();
     }

    private double wordSequencesDifference(WordSequence wordSequence1, WordSequence wordSequence2){
        String[] phones1 = dictionary_.getPhones(wordSequence1);
        String[] phones2 = dictionary_.getPhones(wordSequence2);

        bestMatch_ = wordSequence2;

        return getLevenshteinDistance(String.join(" ", phones1), String.join(" ", phones2));
    }

    private WordSequence bestMatch_;

    private Corpus corpus_;

    private Dictionary dictionary_;

}
