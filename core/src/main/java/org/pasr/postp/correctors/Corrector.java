package org.pasr.postp.correctors;


import org.pasr.asr.dictionary.Dictionary;
import org.pasr.postp.detectors.Detector;
import org.pasr.prep.corpus.Corpus;
import org.pasr.prep.corpus.Word;
import org.pasr.prep.corpus.WordSequence;
import org.pasr.utilities.LevenshteinMatrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.Integer.min;


public class Corrector{
    public Corrector(Corpus corpus, Dictionary dictionary){
        corpus_ = corpus;
        dictionary_ = dictionary;

        detectorList_ = new ArrayList<>();
    }

    public void addDetector(Detector detector){
        if(detector != null){
            detectorList_.add(detector);
        }
    }

    public String correct(String onTheLeft, String input){
        if(corpus_.contains(input)){
            return input;
        }

        WordSequence onTheLeftWS = new WordSequence(onTheLeft);
        WordSequence inputWS = new WordSequence(input);

        List<Range> changeablePartIndexList = getChangeablePartList(inputWS);
        int size = changeablePartIndexList.size();

        if(size == 0){
            return input;
        }

        // Score and replace first changeable part
        Range part = changeablePartIndexList.get(0);
        onTheLeftWS.addAll(inputWS.subSequence(0, part.getLeft()));

        WordSequence onTheRightWS;
        if(changeablePartIndexList.size() > 1){
            onTheRightWS = inputWS.subSequence(part.getRight(),
                changeablePartIndexList.get(1).getLeft());
        }
        else{
            onTheRightWS = inputWS.subSequence(part.getRight());
        }

        WordSequence substitute = scoreAndReplace(
            onTheLeftWS,
            inputWS.subSequence(part.getLeft(), part.getRight()),
            onTheRightWS
        );

        onTheLeftWS.addAll(substitute);
        onTheLeftWS.addAll(onTheRightWS);

        if(size == 1){
            String result = onTheLeftWS.toString();
            return checkResult(result) ? result : input;
        }

        int index = 0;
        while(index < size){
            part = changeablePartIndexList.get(index);

            if(index < size - 1){
                onTheRightWS = inputWS.subSequence(part.getRight(),
                    changeablePartIndexList.get(index + 1).getLeft());
            }
            else{
                onTheRightWS = inputWS.subSequence(part.getRight());
            }

            substitute = scoreAndReplace(
                onTheLeftWS,
                inputWS.subSequence(part.getLeft(), part.getRight()),
                onTheRightWS
            );

            onTheLeftWS.addAll(substitute);
            onTheLeftWS.addAll(onTheRightWS);

            index++;
        }

        String result = onTheLeftWS.toString();
        return checkResult(result) ? result : input;
    }

    private List<Range> getChangeablePartList (WordSequence wordSequence){
        int size = wordSequence.size();

        Set<Integer> errorWordIndexSet = getErrorWordSet(wordSequence).stream()
            .map(wordSequence :: indexOf)
            .collect(Collectors.toSet());

        // For each error word, consider the one on its left and the one on its right also as error
        // words
        List<Integer> neighbourErrorWordIndexList = new ArrayList<>();
        for(int index : errorWordIndexSet){
            if(index > 0){
                neighbourErrorWordIndexList.add(index - 1);
            }

            if(index < size - 1){
                neighbourErrorWordIndexList.add(index + 1);
            }
        }

        errorWordIndexSet.addAll(neighbourErrorWordIndexList);

        List<Range> rangeList = new ArrayList<>();
        int currentLeft_ = 0;
        int currentRight_ = 0;
        for(int i = 0;i < size;i++){
            if(errorWordIndexSet.contains(i)){
                currentRight_++;
            }
            else{
                if(currentLeft_ < currentRight_) {
                    rangeList.add(new Range(currentLeft_, currentRight_));
                }
                currentLeft_ = i + 1;
                currentRight_ = currentLeft_;
            }
        }
        if(currentLeft_ < currentRight_){
            rangeList.add(new Range(currentLeft_, currentRight_));
        }

        return rangeList;
    }

    private Set<Word> getErrorWordSet (WordSequence wordSequence){
        Set<Word> errorWordSet = new HashSet<>();

        for(Detector detector : detectorList_){
            errorWordSet.addAll(detector.detect(wordSequence));
        }

        return errorWordSet;
    }

    private WordSequence scoreAndReplace(WordSequence onTheLeft,
                                   WordSequence changeablePart,
                                   WordSequence onTheRight){
        if(onTheLeft.size() == 0 && onTheRight.size() == 0){
            return replaceWithoutContext(changeablePart);
        }

        String[] changeablePartPhones = dictionary_.getPhonesInLine(changeablePart).stream()
            .toArray(String[] :: new);

        // String: replacing part
        // String: regular expression
        // Double: sum of scores of the replacing part on the matched word patterns of the
        //         regular expression
        Map<String, Map<String, Double>> scoreMap = new HashMap<>();
        Map<String, Integer> contextMap = buildContextMap(onTheLeft, onTheRight);
        contextMap.entrySet().stream().
            forEach(entry -> entry.setValue(entry.getValue() + changeablePartPhones.length));
        Map<String, Double> candidateScoreMap = new HashMap<>();

        for(String regExp : contextMap.keySet()) {
            for (WordSequence wordSequence : corpus_) {
                Matcher matcher = Pattern.compile(regExp)
                    .matcher(wordSequence.toString());
                while (matcher.find()) {
                    String candidate = matcher.group();

                    if (scoreMap.containsKey(candidate)) {
                        Map<String, Double> candidateMap = scoreMap.get(candidate);

                        if(candidateMap.containsKey(regExp)) {
                            candidateMap.put(
                                regExp,
                                candidateMap.get(regExp) + candidateScoreMap.get(candidate)
                            );
                        }
                        else{
                            candidateMap.put(regExp, candidateScoreMap.get(candidate));
                        }
                    }
                    else {
                        Map<String, Double> candidateMap = new HashMap<>();
                        double candidateScore = score(
                            new WordSequence(candidate),
                            changeablePartPhones
                        );
                        candidateMap.put(regExp, candidateScore);
                        candidateScoreMap.put(candidate, candidateScore);

                        scoreMap.put(candidate, candidateMap);
                    }
                }
            }
        }

        double bestScore = Double.MIN_VALUE;
        String chosenCandidate = "";
        for(Map.Entry<String, Map<String, Double>> candidateEntry : scoreMap.entrySet()){
            Map<String, Double> candidateMap = candidateEntry.getValue();

            double candidateScore = 0;
            for(Map.Entry<String, Double> candidateMapEntry : candidateMap.entrySet()){
                candidateScore += 1 -
                    (candidateMapEntry.getValue() / contextMap.get(candidateMapEntry.getKey()));
            }

            if(candidateScore > bestScore){
                bestScore = candidateScore;
                chosenCandidate = candidateEntry.getKey();
            }
        }

        return new WordSequence(chosenCandidate);
    }

    private Map<String, Integer> buildContextMap(WordSequence onTheLeft, WordSequence onTheRight){
        Map<String, Integer> contextMap = new HashMap<>();

        String[] onTheLeftWords = onTheLeft.stream()
            .map(Word :: toString)
            .toArray(String[] ::new);

        String[] onTheRightWords = onTheRight.stream()
            .map(Word :: toString)
            .toArray(String[] :: new);

        int sizeOnTheLeft = onTheLeft.size();
        int n = min(sizeOnTheLeft, REGULAR_EXPRESSION_SPAN);
        int m = min(onTheRight.size(), REGULAR_EXPRESSION_SPAN);

        if(n == 0 && m == 0){
            return contextMap;
        }
        else if(n == 0){
            for(int j = 1;j <= m;j++){
                String arg2 = String.join(" ",
                    (CharSequence[]) Arrays.copyOfRange(onTheRightWords, 0, j)
                );

                String contextKey = REGULAR_EXPRESSION_TEMPLATE +
                    REGULAR_EXPRESSION_TEMPLATE_RIGHT.replace("ARG2", arg2);

                int contextValue = 0;
                contextValue += dictionary_.getPhonesInLine(new WordSequence(arg2)).stream()
                    .toArray(String[] ::new).length;

                contextMap.put(contextKey, contextValue);
            }

            return contextMap;
        }
        else if(m == 0){
            for(int i = 1;i <= n;i++){
                String arg1 = String.join(
                    " ",
                    (CharSequence[]) Arrays.copyOfRange(
                        onTheLeftWords, sizeOnTheLeft - i, sizeOnTheLeft
                    )
                );

                String contextKey = REGULAR_EXPRESSION_TEMPLATE_LEFT.replace("ARG1", arg1) +
                    REGULAR_EXPRESSION_TEMPLATE;

                int contextValue = 0;
                contextValue += dictionary_.getPhonesInLine(new WordSequence(arg1)).stream()
                    .toArray(String[] :: new).length;

                contextMap.put(contextKey, contextValue);
            }

            return contextMap;
        }
        else {
            for (int i = 0; i <= n; i++) {
                String arg1 = String.join(
                    " ",
                    (CharSequence[]) Arrays.copyOfRange(
                        onTheLeftWords, sizeOnTheLeft - i, sizeOnTheLeft
                    )
                );

                for (int j = 0; j <= m; j++) {
                    if (i == 0 && j == 0) {
                        continue;
                    }

                    String arg2 = String.join(" ",
                        (CharSequence[]) Arrays.copyOfRange(onTheRightWords, 0, j)
                    );

                    String contextKey = REGULAR_EXPRESSION_TEMPLATE_LEFT.replace("ARG1", arg1) +
                        REGULAR_EXPRESSION_TEMPLATE +
                        REGULAR_EXPRESSION_TEMPLATE_RIGHT.replace("ARG2", arg2);

                    int contextValue = 0;
                    contextValue += dictionary_.getPhonesInLine(new WordSequence(arg1)).stream()
                        .toArray(String[] ::new).length;
                    contextValue += dictionary_.getPhonesInLine(new WordSequence(arg2)).stream()
                        .toArray(String[] ::new).length;

                    contextMap.put(contextKey, contextValue);
                }
            }

            return contextMap;
        }
    }

    private double score(WordSequence candidate, String[] changeablePartPhoneArray){
        String[] candidatePhoneArray = dictionary_.getPhonesInLine(candidate).stream()
            .toArray(String[] :: new);

        return new LevenshteinMatrix<>(
            Arrays.asList(candidatePhoneArray),
            Arrays.asList(changeablePartPhoneArray)
        ).getDistance();
    }

    private boolean checkResult(String result){
        return ! result.trim().isEmpty();
    }

    private WordSequence replaceWithoutContext(WordSequence changeablePart){
        List<String> changeablePartPhones = dictionary_.getPhonesInLine(changeablePart);

        int minDistance = Integer.MAX_VALUE;
        WordSequence bestMatch = null;

        for(WordSequence wordSequence : corpus_){
            List<List<String>> wordSequenceWordPhoneList = dictionary_.getPhones(wordSequence);

            for(int i = 0, n = wordSequenceWordPhoneList.size();i < n;i++){
                for(int j = i + 1;j <= n;j++){
                    List<String> candidate = new ArrayList<>();
                    wordSequenceWordPhoneList.subList(i, j).stream()
                        .forEach(candidate :: addAll);

                    int currentDistance = LevenshteinMatrix.getDistance(
                        changeablePartPhones,candidate
                    );

                    if(currentDistance < minDistance){
                        minDistance = currentDistance;

                        bestMatch = wordSequence.subSequence(i, j);
                    }
                }
            }
        }

        return bestMatch == null ? new WordSequence("") : bestMatch;
    }

    public static class Range {
        Range(int left, int right){
            left_ = left;
            right_ = right;
        }

        int getLeft(){
            return left_;
        }

        int getRight(){
            return right_;
        }

        private final int left_;
        private final int right_;
    }

    private Corpus corpus_;
    private Dictionary dictionary_;

    private List<Detector> detectorList_;

    private static final String REGULAR_EXPRESSION_TEMPLATE_LEFT = "(?<=ARG1 )";
    private static final String REGULAR_EXPRESSION_TEMPLATE = "(.*)";
    private static final String REGULAR_EXPRESSION_TEMPLATE_RIGHT = "(?= ARG2)";
    private static final int REGULAR_EXPRESSION_SPAN = 5;

}