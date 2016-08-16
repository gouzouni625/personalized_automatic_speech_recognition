package org.pasr.postp.detectors;


import org.pasr.prep.corpus.Corpus;
import org.pasr.prep.corpus.Word;
import org.pasr.prep.corpus.WordSequence;
import org.pasr.utilities.SortedMapEntryList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Integer.max;


public class OccurrenceDetector implements Detector{
    public OccurrenceDetector(Corpus corpus){
        createOccurrenceMap(corpus);
    }

    // TODO Should be parallel. Maybe use Apache Lucene
    private void createOccurrenceMap(Corpus corpus){
        Map<String, Map<String, Double>> occurrenceMap = new HashMap<>();

        Map<String, Integer> wordOccurrenceCount = new HashMap<>();

        for(WordSequence wordSequence : corpus){
            List<Word> wordList = wordSequence.getWords();

            for(int i = 0, n = wordList.size();i < n;i++){
                String wordText = wordList.get(i).getText();

                if(wordOccurrenceCount.containsKey(wordText)){
                    wordOccurrenceCount.put(wordText, wordOccurrenceCount.get(wordText) + 1);
                }
                else{
                    wordOccurrenceCount.put(wordText, 1);
                }

                if(!occurrenceMap.containsKey(wordText)){
                    occurrenceMap.put(wordText, new HashMap<>());
                }

                for(int j = i + 1;j < n;j++){
                    String nextWordText = wordList.get(j).getText();
                    if(!occurrenceMap.containsKey(nextWordText)){
                        occurrenceMap.put(nextWordText, new HashMap<>());
                    }

                    Map<String, Double> wordMap = occurrenceMap.get(wordText);
                    if(wordMap.containsKey(nextWordText)){
                        wordMap.put(nextWordText, wordMap.get(nextWordText) + 1);
                    }
                    else{
                        wordMap.put(nextWordText, 1.0d);
                    }

                    Map<String, Double> nextWordMap = occurrenceMap.get(nextWordText);
                    if(nextWordMap.containsKey(wordText)){
                        nextWordMap.put(wordText, nextWordMap.get(wordText) + 1);
                    }
                    else{
                        nextWordMap.put(wordText, 1.0d);
                    }
                }
            }
        }

        for(String key : occurrenceMap.keySet()){
            Map<String, Double> map = occurrenceMap.get(key);

            int occurrenceCount = wordOccurrenceCount.get(key);

            for(String innerKey : map.keySet()){
                map.put(innerKey, map.get(innerKey) / occurrenceCount);
            }
        }

        occurrenceMap_ = new HashMap<>();
        for(Map.Entry<String, Map<String, Double>> entry : occurrenceMap.entrySet()){
            occurrenceMap_.put(entry.getKey(), getTopValues(entry.getValue()));

            occurrenceMap_.get(entry.getKey()).add(entry.getKey());
        }
    }

    private List<String> getTopValues(Map<String, Double> map){
        SortedMapEntryList<String, Double> sortedList = new SortedMapEntryList<>(
            WORD_LIST_SIZE - 1, false
        );

        sortedList.addAll(map.entrySet());

        return sortedList.valueList();
    }

    @Override
    public List<Word> detect (WordSequence wordSequence) {
        List<String> wordList = wordSequence.getWordsText();
        int numberOfWords = wordList.size();

        double[][] scoreBoard = new double[numberOfWords][numberOfWords];
        for (int i = 0; i < numberOfWords; i++) {
            for (int j = i; j < numberOfWords; j++) {
                if (i != j) {
                    List<String> list1 = occurrenceMap_.get(wordList.get(i));
                    List<String> list2 = occurrenceMap_.get(wordList.get(j));

                    scoreBoard[i][j] = (
                        ((double) intersectionSize(list1, list2))
                        / max(list1.size(), list2.size())
                    );
                    scoreBoard[j][i] = scoreBoard[i][j];
                }
                else{
                    scoreBoard[i][j] = 1;
                }
            }
        }

        List<String> errorCandidateList = new ArrayList<>();
        for(int i = 0;i < numberOfWords;i++){
            double score = 0;
            for(int j = 0;j < numberOfWords;j++){
                score += scoreBoard[i][j];
            }
            score /= numberOfWords;

            if(score < lowScoreThreshold_) {
                errorCandidateList.add(wordList.get(i));
            }
        }

        return wordSequence.stream()
            .filter(word -> errorCandidateList.contains(word.getText()))
            .collect(Collectors.toList());
    }

    private int intersectionSize (List<String> wordList1, List<String> wordList2){
        int size = 0;
        for(String word : wordList1){
            if(wordList2.contains(word)){
                size++;
            }
        }

        return size;
    }

    public double getLowScoreThreshold(){
        return lowScoreThreshold_;
    }

    public void setLowScoreThreshold(double lowScoreThreshold){
        lowScoreThreshold_ = lowScoreThreshold;
    }

    private Map<String, List<String>> occurrenceMap_;

    private static final int WORD_LIST_SIZE = 10;
    private double lowScoreThreshold_ = 0.50;

}
