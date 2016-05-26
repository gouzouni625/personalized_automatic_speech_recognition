package org.pasr.postp.engine.algorithms;


import org.pasr.corpus.Corpus;
import org.pasr.corpus.WordSequence;
import org.pasr.postp.dictionary.Dictionary;
import org.pasr.postp.engine.Corrector.CorrectionAlgorithm;

import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;


public class PhoneLevenshteinDistanceAlgorithm implements CorrectionAlgorithm {

    @Override
    public String apply (String asrOutput, Corpus corpus, Dictionary dictionary) {
        dictionary_ = dictionary;

        WordSequence asrOutputWS = new WordSequence(asrOutput.toLowerCase(), " ");

        List<WordSequence> matchingSequences = corpus.matchWordSequence(asrOutputWS);

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
            stringBuilder.append(chosenSequenceOnTheLeft).append(" ");
        }

        stringBuilder.append(matchingSequences.get(0));

        if(chosenSequenceOnTheRight != null){
            stringBuilder.append(" ").append(chosenSequenceOnTheRight);
        }

        return stringBuilder.toString();
    }

    private double wordSequencesDifference(WordSequence wordSequence1, WordSequence wordSequence2){
        int numberOfWords1 = wordSequence1.getWords().length;
        int numberOfWords2 = wordSequence2.getWords().length;

        String[] phones1 = dictionary_.getPhones(wordSequence1);
        String[] phones2 = dictionary_.getPhones(wordSequence2);

        if(numberOfWords1 >= numberOfWords2){
            bestMatch_ = wordSequence2;

            return getLevenshteinDistance(String.join(" ", phones1), String.join(" ", phones2));
        }

        int minDistance = Integer.MAX_VALUE;
        int index = -1;
        for(int i = 0, n = numberOfWords2 / numberOfWords1;i <= n;i += numberOfWords1){
            // Notice that in copyOfRange(boolean[] original, int from, int to),
            // argument 'to' is exclusive and may lie outside the array.
            int currentDistance = getLevenshteinDistance(
                String.join(" ", phones1),
                String.join(" ", Arrays.copyOfRange(phones2, i, i + numberOfWords1)));

            if(currentDistance < minDistance){
                minDistance = currentDistance;

                index = i;
            }
        }

        bestMatch_ = wordSequence2.subSequence(index, index + numberOfWords1);

        return minDistance;
    }

    private Dictionary dictionary_;

    private WordSequence bestMatch_;

}
