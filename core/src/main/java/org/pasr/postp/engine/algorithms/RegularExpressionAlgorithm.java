package org.pasr.postp.engine.algorithms;


import org.pasr.asr.dictionary.Dictionary;
import org.pasr.asr.language.LanguageModel;
import org.pasr.postp.engine.Corrector;
import org.pasr.prep.corpus.Corpus;
import org.pasr.prep.corpus.Word;
import org.pasr.prep.corpus.WordSequence;
import org.pasr.utilities.LevenshteinMatrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class RegularExpressionAlgorithm implements Corrector.CorrectionAlgorithm {

    @Override
    public String apply (String asrOutput, Corpus corpus,
                         Dictionary dictionary, LanguageModel languageModel) {
        corpus_ = corpus;
        dictionary_ = dictionary;
        languageModel_ = languageModel;

        // Create a WordSequence from the asr output and get the words
        WordSequence asrOutputWS = new WordSequence(asrOutput.toLowerCase(), " ");

        // If the whole asr output exists inside the corpus, consider it correct
        if(corpus_.contains(asrOutputWS)){
            return asrOutput;
        }

        // Find the longest common subsequence between each corpus line and the asr output.
        List<MatchingWordSequence> subSequences = matchOnCorpus(asrOutputWS);

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
                Match matchOnTheLeft = scoreOnCorpus(pattern, hypothesisOnTheLeft);

                currentResult = matchOnTheLeft.getMatch().getText();
                currentSore = matchOnTheLeft.getScore();
            }

            if(subSequence.getIntermediates().size() == 0) {
                currentResult += " " + subSequence;
            }
            else{
                List<MatchingWordSequence.Intermediate> intermediates = subSequence.getIntermediates();

                currentResult = subSequence.subSequence(0,
                    intermediates.get(0).getIndex() + 1).getText();

                for(int i = 0, n = intermediates.size();i < n;i++){
                    Word wordOnTheLeft = subSequence.getWord(intermediates.get(i).getIndex());
                    int wordOnTheRightIndex = intermediates.get(i).getIndex() + 1;
                    Word wordOnTheRight = subSequence.getWord(wordOnTheRightIndex);

                    Pattern pattern = Pattern.compile(wordOnTheLeft + "(\\s(.*)\\s|\\s)" +
                        wordOnTheRight);

                    Match match = scoreOnCorpus(pattern, intermediates.get(i).getWordSequence());

                    currentResult += " " + match.getMatch();

                    if(i < n - 1) {
                        for (int j = wordOnTheRightIndex, m = intermediates.get(i + 1).getIndex(); j <= m; j++) {
                            currentResult += " " + subSequence.getWord(j);
                        }
                    }

                    currentSore += match.getScore();
                }

                for(int i = intermediates.get(intermediates.size() - 1).getIndex() + 1;i < subSequence.numberOfWords();i++){
                        currentResult += " " + subSequence.getWord(i);
                }
            }

            WordSequence hypothesisOnTheRight = subSequence.getHypothesisOnTheRight();
            if(hypothesisOnTheRight != null && hypothesisOnTheRight.numberOfWords() > 0){
                Pattern pattern = Pattern.compile(subSequence.getLastWord() + "(\\s(.*)|$)");
                Match matchOnTheRight = scoreOnCorpus(pattern, hypothesisOnTheRight);

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

    private Match scoreOnCorpus (Pattern pattern, WordSequence hypothesis){
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

        WordSequence candidateCopy = candidate.subSequence(0);
        if(!wordOnTheLeft.isEmpty()){
            candidateCopy.prependWord(new Word(wordOnTheLeft, null, 0));
        }
        if(!wordOnTheRight.isEmpty()){
            candidateCopy.appendWord(new Word(wordOnTheRight, null, 0));
        }

        double wholeDistance = 0.9 * calculatePhoneDistance(hypothesisPhones, candidatePhones) +
            10 * (1 - languageModel_.getProbability(candidateCopy));

        double minDistance = Integer.MAX_VALUE;
        int index = - 1;
        for (int i = 0, n = numberOfCandidateWords - numberOfHypothesisWords; i <= n; i++) {
            double currentDistance = calculatePhoneDistance(
                hypothesisPhones,
                Arrays.copyOfRange(candidatePhones, i, i + numberOfHypothesisWords)
            );

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

    private double calculatePhoneDistance(String[] sequence1, String[] sequence2){
        if(sequence1.length == 0 && sequence2.length == 0){
            return 0;
        }
        else if(sequence1.length == 0){
            return sequence2.length;
        }
        else if(sequence2.length == 0){
            return sequence1.length;
        }
        else {
            return new LevenshteinMatrix<>(sequence1, sequence2).getDistance();
        }
    }

    private List<MatchingWordSequence> matchOnCorpus (WordSequence wordSequence){

        HashSet<MatchingWordSequence> longestCommonSubSequences = longestCommonSubSequenceOnCorpus(
            wordSequence).stream().
            map(lCS -> new MatchingWordSequence(lCS, wordSequence)).
            collect(Collectors.toCollection(HashSet:: new));

        // Find the sub-sequences with the 2 largest lengths (except if the largest length is
        // greater that the second largest by 3 or more.
        HashSet<Integer> lengths = new HashSet<>();
        longestCommonSubSequences.stream().
            forEach(matchingWordSequence -> lengths.add(matchingWordSequence.numberOfWords()));

        List<Integer> sortedLengths = new ArrayList<>(lengths);
        Collections.sort(sortedLengths);

        ArrayList<MatchingWordSequence> subSequences = new ArrayList<>();
        int maxLength = sortedLengths.get(sortedLengths.size() - 1);
        // Avoid the case where there is only 1 sub-sequence.
        if(sortedLengths.size() > 1) {
            int secondMaxLength = sortedLengths.get(sortedLengths.size() - 2);
            if (maxLength - secondMaxLength >= 2) {
                longestCommonSubSequences.
                    stream().
                    filter(lCSS -> lCSS.numberOfWords() == maxLength).
                    forEach(subSequences:: add);
            }
            else {
                longestCommonSubSequences.
                    stream().
                    filter(lCSS -> lCSS.numberOfWords() >= secondMaxLength).
                    forEach(subSequences:: add);
            }
        }
        else{
            longestCommonSubSequences.stream().
                filter(lCSS -> lCSS.numberOfWords() == maxLength).
                forEach(subSequences:: add);
        }

        return subSequences;
    }

    private Set<WordSequence> longestCommonSubSequenceOnCorpus(WordSequence wordSequence){
        return corpus_.longestCommonSubSequence(wordSequence).
            stream().
            collect(Collectors.toCollection(HashSet::new));
    }

    private class MatchingWordSequence extends WordSequence{
        MatchingWordSequence(WordSequence wordSequence, WordSequence hypothesis){
            this(wordSequence.getWords(), hypothesis);
        }

        MatchingWordSequence(List<Word> words, WordSequence hypothesis){
            this(words.toArray(new Word[words.size()]), hypothesis);
        }

        MatchingWordSequence(Word[] words, WordSequence hypothesis){
            super(words, " ");

            process(hypothesis);
        }

        private void process(WordSequence hypothesis){
            intermediates_ = new ArrayList<>();

            if(getFirstWord().getIndex() != 0){
                hypothesisOnTheLeft_ = hypothesis.subSequence(0, getFirstWord().getIndex());
            }

            // Find intermediates
            for(int i = 0, n = numberOfWords() - 1;i < n;i++){
                int index = getWord(i).getIndex();
                int nextIndex = getWord(i + 1).getIndex();
                if(nextIndex != index + 1){
                    addIntermediate(new Intermediate(
                        hypothesis.subSequence(index + 1, nextIndex), i
                    ));
                }
            }

            // Search for this matching sequence inside the corpus. If it doesn't exist,
            // add new custom intermediates where necessary.
            if(!corpus_.contains(this)){
                List<WordSequence> matches = corpus_.matchAsCommonSubSequence(this);

                for(WordSequence match : matches){
                    for(int i = 0, n = match.numberOfWords() - 1;i < n;i++){
                        int index = match.getWord(i).getIndex();
                        int nextIndex = match.getWord(i + 1).getIndex();
                        if(nextIndex != index + 1){
                            addIntermediate(new Intermediate(new WordSequence("", " "), i));
                        }
                    }
                }
            }

            if(getLastWord().getIndex() != hypothesis.numberOfWords() - 1){
                hypothesisOnTheRight_ = hypothesis.subSequence(getLastWord().getIndex() + 1);
            }
        }

        WordSequence getHypothesisOnTheLeft(){
            return hypothesisOnTheLeft_;
        }

        List<Intermediate> getIntermediates(){
            return intermediates_;
        }

        WordSequence getHypothesisOnTheRight(){
            return hypothesisOnTheRight_;
        }

        private void addIntermediate(Intermediate intermediate){
            for(Intermediate intermediate_ : intermediates_){
                if(intermediate_.getIndex() ==
                    intermediate.getIndex()){
                    return;
                }
            }

            intermediates_.add(intermediate);
        }

        private WordSequence hypothesisOnTheLeft_;
        private List<Intermediate> intermediates_;
        private WordSequence hypothesisOnTheRight_;

        private class Intermediate{
            public Intermediate(WordSequence candidate, int index){
                candidate_ = candidate;
                index_ = index;
            }

            WordSequence getWordSequence (){
                return candidate_;
            }

            int getIndex (){
                return index_;
            }

            private WordSequence candidate_;
            private int index_; // index on matching sequence
        }
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
