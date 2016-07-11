package org.pasr.postp.engine.algorithms;


import org.pasr.asr.dictionary.Dictionary;
import org.pasr.asr.language.LanguageModel;
import org.pasr.postp.engine.Corrector;
import org.pasr.prep.corpus.Corpus;
import org.pasr.prep.corpus.Word;
import org.pasr.prep.corpus.WordSequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.collections4.ListUtils.longestCommonSubsequence;
import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;


public class RegularExpressionAlgorithm implements Corrector.CorrectionAlgorithm {

    @Override
    public String apply (String asrOutput, Corpus corpus,
                         Dictionary dictionary, LanguageModel languageModel) {
        corpus_ = corpus;
        dictionary_ = dictionary;
        languageModel_ = languageModel;

        // If the whole asr output exists inside the corpus, consider it correct
        for(WordSequence sentence : corpus_){
            if(sentence.getText().contains(asrOutput)){
                return asrOutput;
            }
        }

        // Create a WordSequence from the asr output and get the words
        WordSequence asrOutputWS = new WordSequence(asrOutput.toLowerCase(), " ");
        Word[] asrOutputWords = asrOutputWS.getWords();

        // Find the longest common subsequence between each corpus line and the asr output.
        HashSet<MatchingWordSequence> longestCommonSubSequences = new HashSet<>();

        for(WordSequence sentence : corpus){
            List<Word> candidate = longestCommonSubsequence(
                Arrays.asList(asrOutputWords),
                Arrays.asList(sentence.getWords()),
                Word.textEquator_
            );

            if(candidate.size() > 0) {
                longestCommonSubSequences.add(new MatchingWordSequence(candidate, asrOutputWS));
            }
        }

        // Find the sub-sequences with the 2 largest lengths (except if the largest length is
        // greater that the second largest by 3 or more.
        HashSet<Integer> lengths = new HashSet<>();
        longestCommonSubSequences.stream().forEach(matchingWordSequence -> lengths.add(matchingWordSequence.numberOfWords()));

        List<Integer> sortedLengths = new ArrayList<>(lengths);
        Collections.sort(sortedLengths);

        ArrayList<MatchingWordSequence> subSequences = new ArrayList<>();
        int maxLength = sortedLengths.get(sortedLengths.size() - 1);
        // Avoid the case where there is only 1 sub-sequence.
        if(sortedLengths.size() > 1) {
            int secondMaxLength = sortedLengths.get(sortedLengths.size() - 2);
            if (maxLength - secondMaxLength >= 2) {
                longestCommonSubSequences.stream().filter(lcss -> lcss.numberOfWords() == maxLength).forEach(subSequences:: add);
            }
            else {
                longestCommonSubSequences.stream().filter(lcss -> lcss.numberOfWords() >= secondMaxLength).forEach(subSequences:: add);
            }
        }
        else{
            longestCommonSubSequences.stream().filter(lcss -> lcss.numberOfWords() == maxLength).forEach(subSequences:: add);
        }

        // Create a candidate result from each matching word sequence.
        // Keep them in a Set instead of keeping only the best to be able to show different
        // alternatives to the user.
        ArrayList<String> resultCandidates = new ArrayList<>();
        ArrayList<Double> candidatesScores = new ArrayList<>();

        for(MatchingWordSequence subSequence : subSequences){

            String currentResult = "";
            double currentSore = 0;

            WordSequence hypothesisOnTheLeft = subSequence.getHypothesisOnTheLeft();
            if(hypothesisOnTheLeft != null && hypothesisOnTheLeft.numberOfWords() > 0){
                Pattern pattern = Pattern.compile("((.*)\\s|^)" + subSequence.getFirstWord());
                Match matchOnTheLeft = matchOnCorpus(pattern, hypothesisOnTheLeft);

                currentResult = matchOnTheLeft.getMatch().getText();
                currentSore = matchOnTheLeft.getScore();
            }

            if(subSequence.getHypothesisIntermediates().size() == 0) {
                currentResult += " " + subSequence;
            }
            else{
                List<WordSequence> intermediates = subSequence.getHypothesisIntermediates();
                List<Integer> intermediatesIndices = subSequence.getIntermediatesIndices();

                int currentWord = intermediatesIndices.get(0);
                currentResult = subSequence.subSequence(0, currentWord + 1).getText();
                currentWord++;

                for(int i = 0, n = intermediates.size();i < n;i++){
                    Pattern pattern = Pattern.compile(
                        subSequence.getWord(intermediatesIndices.get(i)) + "(\\s(.*)\\s|\\s)" +
                            subSequence.getWord(intermediatesIndices.get(i) + 1));

                    Match match = matchOnCorpus(pattern, intermediates.get(i));

                    currentResult += " " + match.getMatch() + " " + subSequence.getWord(currentWord);
                    currentSore += match.getScore();

                    currentWord++;
                }

                if(currentWord < subSequence.numberOfWords()){
                    for(int i = currentWord;i < subSequence.numberOfWords();i++){
                        currentResult += " " + subSequence.getWord(i);
                    }
                }
            }

            WordSequence hypothesisOnTheRight = subSequence.getHypothesisOnTheRight();
            if(hypothesisOnTheRight != null && hypothesisOnTheRight.numberOfWords() > 0){
                Pattern pattern = Pattern.compile(subSequence.getLastWord() + "(\\s(.*)|$)");
                Match matchOnTheRight = matchOnCorpus(pattern, hypothesisOnTheRight);

                currentResult += " " + matchOnTheRight.getMatch();
                currentSore += matchOnTheRight.getScore();
            }

            resultCandidates.add(currentResult);
            candidatesScores.add(currentSore);
        }

        // Find and return the best candidate result.
        double minimumScore = candidatesScores.get(0);
        int indexOfMinimum = 0;

        for(int i = 1, n = candidatesScores.size();i < n;i++){
            if(candidatesScores.get(i) < minimumScore){
                minimumScore = candidatesScores.get(i);

                indexOfMinimum = i;
            }
        }

        return resultCandidates.get(indexOfMinimum);
    }

    private Match matchOnCorpus(Pattern pattern, WordSequence hypothesis){
        Match match = new Match(hypothesis, Double.POSITIVE_INFINITY);

        String wordOnTheLeft = "";
        String wordOnTheRight = "";

        // String split method documentation: "Trailing empty string are not included"
        // This means that:
        // "abc#".split("#") will return ["abc"]
        // "#abc".split("#") will return ["", "abc"]
        // that is why, in the third case, calling tokens[1] will throw an exception.
        if(pattern.toString().contains("((.*)\\s|^)")){
            String[] tokens = pattern.toString().split(Pattern.quote("((.*)\\s|^)"));

            wordOnTheLeft = tokens[0];
            wordOnTheRight = tokens[1];
        }
        else if(pattern.toString().contains("(\\s(.*)\\s|\\s)")){
            String[] tokens = pattern.toString().split(Pattern.quote("(\\s(.*)\\s|\\s)"));

            wordOnTheLeft = tokens[0];
            wordOnTheRight = tokens[1];
        }
        else if(pattern.toString().contains("(\\s(.*)|$)")){
            String[] tokens = pattern.toString().split(Pattern.quote("(\\s(.*)|$)"));

            wordOnTheLeft = "";
            wordOnTheRight = tokens[0];
        }

        for(WordSequence sentence : corpus_){
            Matcher matcher = pattern.matcher(sentence.getText());

            Match sentenceMatch = new Match(null, Double.POSITIVE_INFINITY);

            while(matcher.find()){
                Match currentMatch = score(
                    new WordSequence(matcher.group(1).trim(), " "), hypothesis, wordOnTheLeft,
                    wordOnTheRight);

                if(currentMatch.getScore() < sentenceMatch.getScore()){
                    sentenceMatch = currentMatch;
                }
            }



            if(sentenceMatch.getScore() < match.getScore()){
                match = sentenceMatch;
            }
        }

        return match;
    }

    private Match score (WordSequence candidate, WordSequence hypothesis,
                         String wordOnTheLeft, String wordOnTheRight) {
        Match match = new Match();

        int numberOfHypothesisWords = hypothesis.getWords().length;
        int numberOfCandidateWords = candidate.getWords().length;

        String[] hypothesisPhones = dictionary_.getPhones(hypothesis);
        String[] candidatePhones = dictionary_.getPhones(candidate);

        double wholeDistance = getLevenshteinDistance(
            String.join("", (CharSequence[]) hypothesisPhones),
            String.join("", (CharSequence[]) candidatePhones));

        if (numberOfHypothesisWords >= numberOfCandidateWords) {
            match.setMatch(candidate);
            match.setScore(wholeDistance);

            return match;
        }

        double minDistance = Integer.MAX_VALUE;
        int index = - 1;
        for (int i = 0, n = numberOfCandidateWords - numberOfHypothesisWords; i <= n; i++) {
            double currentDistance = getLevenshteinDistance(
                String.join("", (CharSequence[]) hypothesisPhones),
                String.join("", (CharSequence[]) Arrays.copyOfRange(
                    candidatePhones, i, i + numberOfHypothesisWords)
                ));

            WordSequence connectingWS = candidate.subSequence(i, i + numberOfHypothesisWords);
            if(!wordOnTheLeft.isEmpty()){
                connectingWS.prependWord(new Word(wordOnTheLeft, null, 0));
            }
            if(!wordOnTheRight.isEmpty()){
                connectingWS.appendWord(new Word(wordOnTheRight, null, 0));
            }

            double languageModelProbability = languageModel_.getProbability(connectingWS);

            currentDistance = 0.9 * currentDistance + 10 * (1 - languageModelProbability);

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
            match.setScore(wholeDistance);
            match.setMatch(candidate);
        }
        else {
            match.setScore(minDistance);
            match.setMatch(candidate.subSequence(index, index + numberOfHypothesisWords));
        }

        return match;
    }

    private class MatchingWordSequence extends WordSequence{
        MatchingWordSequence(List<Word> words, WordSequence hypothesis){
            this(words.toArray(new Word[words.size()]), hypothesis);
        }

        MatchingWordSequence(Word[] words, WordSequence hypothesis){
            super(words, " ");

            process(hypothesis);
        }

        private void process(WordSequence hypothesis){
            hypothesisIntermediates_ = new ArrayList<>();
            intermediatesIndices_ = new ArrayList<>();

            if(getFirstWord().getIndex() != 0){
                hypothesisOnTheLeft_ = hypothesis.subSequence(0, getFirstWord().getIndex());
            }

            for(int i = 0, n = numberOfWords() - 1;i < n;i++){
                int index = getWord(i).getIndex();
                int nextIndex = getWord(i + 1).getIndex();
                if(nextIndex != index + 1){
                    hypothesisIntermediates_.add(hypothesis.subSequence(index + 1, nextIndex));
                    intermediatesIndices_.add(i);
                }
            }

            if(getLastWord().getIndex() != hypothesis.numberOfWords() - 1){
                hypothesisOnTheRight_ = hypothesis.subSequence(getLastWord().getIndex() + 1);
            }
        }

        @Override
        public boolean equals(Object o){
            if(o instanceof MatchingWordSequence){
                String thisText = getText();
                String objectText = ((MatchingWordSequence) o).getText();

                return thisText.equals(objectText);
            }
            else{
                return false;
            }
        }

        @Override
        public int hashCode(){
            return getText().hashCode();
        }

        WordSequence getHypothesisOnTheLeft(){
            return hypothesisOnTheLeft_;
        }

        List<WordSequence> getHypothesisIntermediates(){
            return hypothesisIntermediates_;
        }

        List<Integer> getIntermediatesIndices(){
            return intermediatesIndices_;
        }

        WordSequence getHypothesisOnTheRight(){
            return hypothesisOnTheRight_;
        }

        private WordSequence hypothesisOnTheLeft_;
        private List<WordSequence> hypothesisIntermediates_;
        private List<Integer> intermediatesIndices_;
        private WordSequence hypothesisOnTheRight_;
    }

    private class Match{
        Match(){}

        Match(WordSequence match, double score){
            match_ = match;
            score_ = score;
        }

        void setMatch(WordSequence match){
            match_ = match;
        }

        void setScore(double score){
            score_ = score;
        }

        WordSequence getMatch(){
            return match_;
        }

        double getScore(){
            return score_;
        }

        private WordSequence match_;
        private double score_;

    }

    private Corpus corpus_;
    private Dictionary dictionary_;
    private LanguageModel languageModel_;

}
