package org.pasr.postp.engine.algorithms;


import org.pasr.corpus.Corpus;
import org.pasr.corpus.Word;
import org.pasr.corpus.WordSequence;
import org.pasr.postp.dictionary.Dictionary;
import org.pasr.postp.engine.Corrector.CorrectionAlgorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.collections4.ListUtils.longestCommonSubsequence;
import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;


public class PhoneDistanceAlgorithm implements CorrectionAlgorithm {
    public PhoneDistanceAlgorithm (){}

    @Override
    public String apply (String asrOutput, Corpus corpus, Dictionary dictionary) {
        dictionary_ = dictionary;

        WordSequence asrOutputWS = new WordSequence(asrOutput.toLowerCase(), " ");

        List<WordSequence> matchingWordSequences = matchWordSequence(corpus, asrOutputWS);
        WordSequence matchingSequence = matchingWordSequences.get(0);

        // If the whole asr output exists inside the corpus, consider it correct
        for (WordSequence matchingWordSequence : matchingWordSequences) {
            if (matchingWordSequence.equals(asrOutputWS.getText())) {
                return asrOutputWS.getText();
            }
        }

        WordSequence chosenSequenceOnTheLeft = null;
        WordSequence chosenSequenceOnTheRight = null;

        double minDifferenceOnTheLeft = Double.POSITIVE_INFINITY;
        double minDifferenceOnTheRight = Double.POSITIVE_INFINITY;
        for (WordSequence matchingWordSequence : matchingWordSequences){
            WordSequence[] errorWordSequences = asrOutputWS.split(matchingWordSequence);
            WordSequence errorSequenceOnTheLeft = errorWordSequences[0];
            WordSequence errorSequenceOnTheRight = errorWordSequences[1];

            if (errorSequenceOnTheLeft.getWords().length > 0) {
                WordSequence candidateSequenceOnTheLeft = matchingWordSequence
                    .getWords()[0]
                    .getWordSequence().subSequence(0,
                        matchingWordSequence.getFirstWord().getIndex());

                double currentDifferenceOnTheLeft = scoreCandidate(errorSequenceOnTheLeft,
                    candidateSequenceOnTheLeft);

                if(currentDifferenceOnTheLeft < minDifferenceOnTheLeft){
                    minDifferenceOnTheLeft = currentDifferenceOnTheLeft;

                    chosenSequenceOnTheLeft = bestMatch_;
                }
            }

            if (errorSequenceOnTheRight.getWords().length > 0) {
                WordSequence candidateSequenceOnTheRight = matchingWordSequence
                    .getLastWord().getWordSequence().subSequence(
                        matchingWordSequence.getLastWord().getIndex() + 1);

                double currentDifferenceOnTheRight = scoreCandidate(errorSequenceOnTheRight,
                    candidateSequenceOnTheRight);

                if(currentDifferenceOnTheRight < minDifferenceOnTheRight){
                    minDifferenceOnTheRight = currentDifferenceOnTheRight;

                    chosenSequenceOnTheRight = bestMatch_;
                }
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        if (chosenSequenceOnTheLeft != null && chosenSequenceOnTheLeft.numberOfWords() > 0) {
            stringBuilder.append(chosenSequenceOnTheLeft).append(" ");
        }

        // Remove duplicate words in final string. This can occur because each chosen sequence
        // might contain a word from the matching sequence.
        if (chosenSequenceOnTheLeft != null && chosenSequenceOnTheLeft.numberOfWords() > 0 &&
            chosenSequenceOnTheLeft.getLastWord().getText().equals(matchingSequence.getFirstWord().
                getText())) {
            stringBuilder.append(matchingSequence.subSequence(1));
        }
        else {
            stringBuilder.append(matchingSequence);
        }

        if (chosenSequenceOnTheRight != null && chosenSequenceOnTheRight.numberOfWords() > 0) {
            if (matchingSequence.getLastWord().getText().equals(chosenSequenceOnTheRight.
                getFirstWord().getText())) {
                stringBuilder.append(" ").append(chosenSequenceOnTheRight.subSequence(1));
            }
            else {
                stringBuilder.append(" ").append(chosenSequenceOnTheRight);
            }
        }

        return stringBuilder.toString();
    }

    private List<WordSequence> matchWordSequence(Corpus corpus, WordSequence wordSequence){
        // Get the words of the given String
        Word[] words = wordSequence.getWords();

        ArrayList<WordSequence> subSequences = new ArrayList<WordSequence>();

        // Apache Commons longestCommonSubsequence returns the objects of its first argument. That
        // means that the Words added in subSequences are the actual Words that exists inside this
        // Corpus.
        for(WordSequence sentence : corpus){
            WordSequence subSequence = new WordSequence(
                longestCommonSubsequence(
                    Arrays.asList(sentence.getWords()),
                    Arrays.asList(words),
                    Word.textEquator_
                ), " ").longestContinuousSubSequence();

            if(subSequence.numberOfWords() > 0) {
                subSequences.add(subSequence);
            }
        }

        int maximumLength = Collections.max(
            subSequences,
            (wordSequence1, wordSequence2) -> wordSequence1.getWords().length -
                wordSequence2.getWords().length).
            getWords().length;

        ArrayList<WordSequence> longestSubSequences = new ArrayList<>();

        subSequences.stream().filter(subSequence -> subSequence.getWords().length == maximumLength).
            forEach(longestSubSequences:: add);

        return longestSubSequences;
    }

    private double scoreCandidate (WordSequence hypothesis, WordSequence candidate) {
        int numberOfHypothesisWords = hypothesis.getWords().length;
        int numberOfCandidateWords = candidate.getWords().length;

        String[] hypothesisPhones = dictionary_.getPhones(hypothesis);
        String[] candidatePhones = dictionary_.getPhones(candidate);

        double wholeDistance = getLevenshteinDistance(
            String.join("", (CharSequence[]) hypothesisPhones),
            String.join("", (CharSequence[]) candidatePhones));

        if (numberOfHypothesisWords >= numberOfCandidateWords) {
            bestMatch_ = candidate;

            return wholeDistance;
        }

        int minDistance = Integer.MAX_VALUE;
        int index = - 1;
        for (int i = 0, n = numberOfCandidateWords - numberOfHypothesisWords; i <= n; i++) {
            int currentDistance = getLevenshteinDistance(
                String.join("", (CharSequence[]) hypothesisPhones),
                String.join("", (CharSequence[]) Arrays.copyOfRange(
                    candidatePhones, i, i + numberOfHypothesisWords)
                ));

            if (currentDistance < minDistance) {
                minDistance = currentDistance;
            }

            index = i;
        }

        if(wholeDistance < minDistance){
            bestMatch_ = candidate;
        }
        else {
            bestMatch_ = candidate.subSequence(index, index + numberOfHypothesisWords);
        }

        return minDistance;
    }

    private Dictionary dictionary_;

    private WordSequence bestMatch_;

}
