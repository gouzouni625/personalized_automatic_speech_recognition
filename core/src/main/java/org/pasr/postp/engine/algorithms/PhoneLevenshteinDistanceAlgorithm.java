package org.pasr.postp.engine.algorithms;


import org.pasr.corpus.Corpus;
import org.pasr.corpus.Word;
import org.pasr.corpus.WordSequence;
import org.pasr.postp.dictionary.Dictionary;
import org.pasr.postp.engine.Corrector.CorrectionAlgorithm;
import org.pasr.postp.engine.POSTagger.Tags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.collections4.ListUtils.longestCommonSubsequence;
import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;


public class PhoneLevenshteinDistanceAlgorithm implements CorrectionAlgorithm {
    public PhoneLevenshteinDistanceAlgorithm(){}

    @Override
    public String apply (String asrOutput, Corpus corpus, Dictionary dictionary) {
        corpus_ = corpus;

        WordSequence asrOutputWS = new WordSequence(asrOutput.toLowerCase(), " ");

        List<WordSequence> matchingWordSequences = matchWordSequence(corpus, asrOutputWS);
        matchPOSPatterns(matchingWordSequences);

        // If the whole asr output exists inside the corpus, consider it correct
        for (WordSequence matchingWordSequence : matchingWordSequences) {
            if (matchingWordSequence.equals(asrOutputWS.getText())) {
                return asrOutputWS.getText();
            }
        }

        WordSequence chosenSequenceOnTheLeft = null;
        WordSequence chosenMatchingSequence = null;
        WordSequence chosenSequenceOnTheRight = null;

        double minDifference = Double.POSITIVE_INFINITY;
        for(int i = 0, n = matchingWordSequences.size();i < n;i++){
            WordSequence matchingWordSequence = matchingWordSequences.get(i);

            double score = 0; // Lower score wins

            WordSequence bestMatchOnTheLeft = null;
            WordSequence bestMatchOnTheRight = null;

            WordSequence[] errorWordSequences = asrOutputWS.split(matchingWordSequence);
            WordSequence errorSequenceOnTheLeft = errorWordSequences[0];
            WordSequence errorSequenceOnTheRight = errorWordSequences[1];

            if(errorSequenceOnTheLeft.getWords().length > 0) {
                WordSequence candidateSequenceOnTheLeft = matchingWordSequence
                    .getWords()[0]
                    .getWordSequence().subSequence(0,
                        matchingWordSequence.getWords()[0].getIndex());

                double tempScore = scoreCandidate(dictionary, errorSequenceOnTheLeft,
                    candidateSequenceOnTheLeft);

                bestMatchOnTheLeft = bestMatch_;

                int counter = 0;
                for(WordSequence wordSequence : pOSPatternsOnTheLeft_.get(i)){
                    if(Tags.tagArrayToAbbreviatedString(wordSequence.getPOSPattern()).contains(
                        Tags.tagArrayToAbbreviatedString(bestMatchOnTheLeft.getPOSPattern()))){
                        counter++;
                    }
                }

                score += tempScore * ((double)(counter)) / (pOSPatternsOnTheLeft_.get(i).size());
            }

            if(errorSequenceOnTheRight.getWords().length > 0) {
                WordSequence candidateSequenceOnTheRight = matchingWordSequence
                    .getWords()[matchingWordSequence.getWords().length - 1]
                    .getWordSequence().subSequence(
                        matchingWordSequence.getWords()[
                            matchingWordSequence.getWords().length - 1].getIndex() + 1);

                double tempScore = scoreCandidate(dictionary, errorSequenceOnTheRight,
                    candidateSequenceOnTheRight);

                bestMatchOnTheRight = bestMatch_;

                int counter = 0;
                for(WordSequence wordSequence : pOSPatternsOnTheRight_.get(i)){
                    if(Tags.tagArrayToAbbreviatedString(wordSequence.getPOSPattern()).contains(
                        Tags.tagArrayToAbbreviatedString(bestMatchOnTheRight.getPOSPattern()))){
                        counter++;
                    }
                }

                score += tempScore * ((double)(counter)) / (pOSPatternsOnTheRight_.get(i).size());
            }

            if(score < minDifference){
                chosenSequenceOnTheLeft = bestMatchOnTheLeft;
                chosenMatchingSequence = matchingWordSequence;
                chosenSequenceOnTheRight = bestMatchOnTheRight;
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        if(chosenSequenceOnTheLeft != null){
            stringBuilder.append(chosenSequenceOnTheLeft).append(" ");
        }

        stringBuilder.append(chosenMatchingSequence);

        if(chosenSequenceOnTheRight != null){
            stringBuilder.append(" ").append(chosenSequenceOnTheRight);
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
            subSequences.add(new WordSequence(
                longestCommonSubsequence(
                    Arrays.asList(sentence.getWords()),
                    Arrays.asList(words),
                    Word.textEquator_
                ), " ").longestContinuousSubSequence()
            );
        }

        int maximumLength = Collections.max(
            subSequences,
            (wordSequence1, wordSequence2) -> wordSequence1.getWords().length -
                wordSequence2.getWords().length).
            getWords().length;

        ArrayList<WordSequence> longestSubSequences = new ArrayList<>();

        for(WordSequence subSequence : subSequences){
            if(subSequence.getWords().length == maximumLength){
                longestSubSequences.add(subSequence);
            }
        }

        return longestSubSequences;
    }

    private double scoreCandidate (Dictionary dictionary, WordSequence reference, WordSequence candidate) {
        int numberOfReferenceWords = reference.getWords().length;
        int numberOfCandidateWords = candidate.getWords().length;

        String[] phones1 = dictionary.getPhones(reference);
        String[] phones2 = dictionary.getPhones(candidate);

        if (numberOfReferenceWords >= numberOfCandidateWords) {
            bestMatch_ = candidate;

            return getLevenshteinDistance(String.join("", (CharSequence[]) phones1), String.join("", (CharSequence[]) phones2));
        }

        int minDistance = Integer.MAX_VALUE;
        int index = - 1;
        for (int i = 0, n = numberOfCandidateWords / numberOfReferenceWords; i <= n; i++) {
            // Notice that in copyOfRange(boolean[] original, int from, int to),
            // argument 'to' is exclusive and may lie outside the array.
            int currentDistance = getLevenshteinDistance(
                String.join("", (CharSequence[]) phones1),
                String.join("", (CharSequence[]) Arrays.copyOfRange(phones2, i, i + numberOfReferenceWords)));

            if (currentDistance < minDistance) {
                minDistance = currentDistance;

                index = i;
            }
        }

        bestMatch_ = candidate.subSequence(index, index + numberOfReferenceWords);

        return minDistance;
    }

    private void matchPOSPatterns(List<WordSequence> wordSequences){
        pOSPatternsOnTheLeft_ = new ArrayList<>();
        pOSPatternsOnTheRight_ = new ArrayList<>();

        for(WordSequence wordSequence : wordSequences){
            ArrayList<WordSequence> left = new ArrayList<>();
            ArrayList<WordSequence> right = new ArrayList<>();

            String wordSequencePOSPattern = Tags.tagArrayToAbbreviatedString(wordSequence.getPOSPattern());

            for(WordSequence sentence : corpus_){
                int index = Tags.tagArrayToAbbreviatedString(sentence.getPOSPattern()).indexOf(wordSequencePOSPattern);

                if(index != -1){
                    left.add(sentence.subSequence(0, index));
                    right.add(sentence.subSequence(index + wordSequencePOSPattern.length()));
                }
            }

            pOSPatternsOnTheLeft_.add(left);
            pOSPatternsOnTheRight_.add(right);
        }
    }

    private Corpus corpus_;

    private WordSequence bestMatch_;

    private List<List<WordSequence>> pOSPatternsOnTheLeft_;
    private List<List<WordSequence>> pOSPatternsOnTheRight_;

}
