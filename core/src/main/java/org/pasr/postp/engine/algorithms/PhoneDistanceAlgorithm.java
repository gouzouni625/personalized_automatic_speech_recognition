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
import java.util.stream.Collectors;

import static org.apache.commons.collections4.ListUtils.longestCommonSubsequence;
import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;


public class PhoneDistanceAlgorithm implements CorrectionAlgorithm {
    public PhoneDistanceAlgorithm (){}

    @Override
    public String apply (String asrOutput, Corpus corpus, Dictionary dictionary) {
        dictionary_ = dictionary;

        WordSequence asrOutputWS = new WordSequence(asrOutput.toLowerCase(), " ");

        List<MatchingWordSequence> matchingWordSequences = matchWordSequence(corpus, asrOutputWS);
        WordSequence matchingSequence = matchingWordSequences.get(0);

        // If the whole asr output exists inside the corpus, consider it correct
        for (MatchingWordSequence matchingWordSequence : matchingWordSequences) {
            if (matchingWordSequence.equals(asrOutputWS.getText())) {
                return asrOutputWS.getText();
            }
        }

        WordSequence chosenSequenceOnTheLeft = null;
        WordSequence chosenSequenceOnTheRight = null;

        double minDifferenceOnTheLeft = Double.POSITIVE_INFINITY;
        double minDifferenceOnTheRight = Double.POSITIVE_INFINITY;
        for (MatchingWordSequence matchingWordSequence : matchingWordSequences){
            WordSequence[] errorWordSequences = asrOutputWS.split(matchingWordSequence);
            WordSequence errorSequenceOnTheLeft = errorWordSequences[0];
            WordSequence errorSequenceOnTheRight = errorWordSequences[1];

            if (errorSequenceOnTheLeft.getWords().length > 0) {
                WordSequence candidateSequenceOnTheLeft = matchingWordSequence.
                    getCandidateOnTheLeft();

                double currentDifferenceOnTheLeft = scoreCandidate(errorSequenceOnTheLeft,
                    candidateSequenceOnTheLeft);

                if(currentDifferenceOnTheLeft < minDifferenceOnTheLeft){
                    minDifferenceOnTheLeft = currentDifferenceOnTheLeft;

                    chosenSequenceOnTheLeft = bestMatch_;
                }
            }

            if (errorSequenceOnTheRight.getWords().length > 0) {
                WordSequence candidateSequenceOnTheRight = matchingWordSequence.
                    getCandidateOnTheRight();

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

    private List<MatchingWordSequence> matchWordSequence(Corpus corpus, WordSequence wordSequence){
        // Get the words of the given String
        Word[] words = wordSequence.getWords();

        ArrayList<WordSequence> subSequences = new ArrayList<>();

        // Apache Commons longestCommonSubsequence returns the objects of its first argument. That
        // means that the Words added in subSequences are the actual Words that exists inside this
        // Corpus.
        for(WordSequence sentence : corpus){
            List<WordSequence> candidateSubSequences = new WordSequence(
                longestCommonSubsequence(
                    Arrays.asList(sentence.getWords()),
                    Arrays.asList(words),
                    Word.textEquator_
                ), " ").continuousSubSequences();

            subSequences.addAll(candidateSubSequences.stream().filter(candidateSubSequence -> candidateSubSequence.numberOfWords() > 0).collect(Collectors.toList()));
        }

        int maximumLength = Collections.max(
            subSequences,
            (wordSequence1, wordSequence2) -> wordSequence1.getWords().length -
                wordSequence2.getWords().length).
            getWords().length;

        ArrayList<MatchingWordSequence> longestSubSequences = new ArrayList<>();

        subSequences.stream().filter(subSequence -> subSequence.getWords().length == maximumLength).
            forEach(subSequence -> longestSubSequences.add(new MatchingWordSequence(subSequence)));

        for(MatchingWordSequence longestSubSequence : longestSubSequences){
            for(WordSequence subSequence : subSequences){
                if(subSequence.getFirstWord().getWordSequence() != longestSubSequence.getFirstWord().getWordSequence()){
                    continue;
                }

                if(subSequence.getFirstWord().getIndex() < longestSubSequence.getFirstWord().getIndex()){
                    longestSubSequence.reduceLeftLimit(subSequence.getFirstWord().getIndex());
                }

                if(subSequence.getLastWord().getIndex() > longestSubSequence.getLastWord().getIndex()){
                    longestSubSequence.increaseRightLimit(subSequence.getLastWord().getIndex());
                }
            }

            longestSubSequence.finalizeLimits();
        }

        List<MatchingWordSequence> matchingWordSequences = new ArrayList<>();

        int maxExtraWordsCount = longestSubSequences.stream().max((o1, o2) -> o1.getExtraWordsCounter() - o2.getExtraWordsCounter()).get().getExtraWordsCounter();
        longestSubSequences.stream().filter(subSequence -> subSequence.getExtraWordsCounter() == maxExtraWordsCount).forEach(matchingWordSequences:: add);

        return matchingWordSequences;
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

        double minDistance = Integer.MAX_VALUE;
        int index = - 1;
        for (int i = 0, n = numberOfCandidateWords - numberOfHypothesisWords; i <= n; i++) {
            double currentDistance = getLevenshteinDistance(
                String.join("", (CharSequence[]) hypothesisPhones),
                String.join("", (CharSequence[]) Arrays.copyOfRange(
                    candidatePhones, i, i + numberOfHypothesisWords)
                ));

            if (currentDistance < minDistance) {
                minDistance = currentDistance;
            }

            index = i;
        }

        // About minDistance == 0:
        //   If minDistance == 0, we known that the hypothesis exists inside the candidate and it
        //   is also incorrect since it doesn't exist inside the corpus. This results in the case
        //   that the candidate has some extra words that should also be included (e.g.
        //   (and most members of)(correct part) (our (society)(hypothesis))(candidate)
        //   )
        //   In the above example, hypothesis (society) exists inside the candidate (our society)
        //   but not inside the corpus. This means that "our" should be also included to the result.
        if(wholeDistance < minDistance || minDistance == 0){
            bestMatch_ = candidate;
        }
        else {
            bestMatch_ = candidate.subSequence(index, index + numberOfHypothesisWords);
        }

        return minDistance;
    }

    private class MatchingWordSequence extends WordSequence{
        MatchingWordSequence(WordSequence wordSequence){
            this(wordSequence.getWords(), wordSequence.getWordSeparator());
        }

        MatchingWordSequence(Word[] words, String wordSeparator){
            super(words, wordSeparator);
        }

        WordSequence getCandidateOnTheLeft(){
            return getFirstWord()
                .getWordSequence()
                .subSequence(leftLimit_, getFirstWord().getIndex());
        }

        WordSequence getCandidateOnTheRight (){
            return getLastWord()
                .getWordSequence()
                .subSequence(getLastWord().getIndex() + 1, rightLimit_ + 1);
        }

        int getLeftLimit(){
            return leftLimit_;
        }

        int getRightLimit(){
            return rightLimit_;
        }

        int getExtraWordsCounter () {
            return extraWordsCounter_;
        }

        void reduceLeftLimit(int leftLimit){
            extraWordsCounter_++;

            if(leftLimit < leftLimit_) {
                leftLimit_ = leftLimit;
            }
        }

        void increaseRightLimit (int rightLimit){
            extraWordsCounter_++;

            if(rightLimit > rightLimit_) {
                rightLimit_ = rightLimit;
            }
        }

        void finalizeLimits (){
            if(leftLimit_ == Integer.MAX_VALUE){
                leftLimit_ = 0;
            }

            if(rightLimit_ == Integer.MIN_VALUE){
                rightLimit_ = getLastWord().getWordSequence().getLastWord().getIndex();
            }
        }

        private int leftLimit_ = Integer.MAX_VALUE;
        private int rightLimit_ = Integer.MIN_VALUE;

        int extraWordsCounter_ = 0;

    }

    private Dictionary dictionary_;

    private WordSequence bestMatch_;

}
