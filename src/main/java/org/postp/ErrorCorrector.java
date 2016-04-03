package org.postp;


import org.prep.Corpus;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;


import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorCorrector {
    public ErrorCorrector(Corpus corpus, Dictionary dictionary) throws FileNotFoundException {
        corpus_ = corpus;
        dictionary_ = dictionary;
    }

    public String correct(ErrorWord[] errorWords) {
        for(ErrorWord errorWord : errorWords){
            WordSequencePattern wordSequencePattern = errorWord.getWordSequencePattern();
            String[] regularExpressions = wordSequencePattern.getRegularExpressionPatterns(5);

            // Each considerable word sequence pattern has an array of matched word patterns.
            String[][] matchedWordPatterns = getMatchedWordPatterns(regularExpressions);

            // Each matched word pattern has a replacing part.
            String[][] replacingParts = getReplacingParts(regularExpressions, matchedWordPatterns);

            // get the pronunciation sequences of the considerable word sequence patterns.
            String[] pronunciationSequencesCWSP = wordSequencePattern.getPronunciationSequences(dictionary_);

            // get the pronunciation sequences of the matched word patterns.
            String[][] pronunciationSequencesMWP = new String[matchedWordPatterns.length][];
            for (int i = 0, n = matchedWordPatterns.length; i < n; i++) {
                pronunciationSequencesMWP[i] = new String[matchedWordPatterns[i].length];

                for (int j = 0, m = matchedWordPatterns[i].length; j < m; j++) {
                    pronunciationSequencesMWP[i][j] = getPronunciationSequence(matchedWordPatterns[i][j].split(wordSeparator_));
                }
            }

            // Score replacing parts.
            Hashtable<String, Double> scores = new Hashtable<String, Double>();
            for (int i = 0, n = replacingParts.length; i < n; i++){
                for(int j = 0, m = replacingParts[i].length;j < m;j++){
                    if(scores.get(replacingParts[i][j]) == null) {
                        scores.put(replacingParts[i][j], (double) (pronunciationSequencesCWSP[i].length() - getLevenshteinDistance(pronunciationSequencesCWSP[i], pronunciationSequencesMWP[i][j])) /
                                pronunciationSequencesCWSP[i].length());
                    }
                    else{
                        scores.put(replacingParts[i][j], (double) (pronunciationSequencesCWSP[i].length() - getLevenshteinDistance(pronunciationSequencesCWSP[i], pronunciationSequencesMWP[i][j])) /
                                pronunciationSequencesCWSP[i].length() +
                                scores.get(replacingParts[i][j]));
                    }
                }
            }

            Enumeration<String> scoreKeys = scores.keys();
            double max = Double.NEGATIVE_INFINITY;
            String bestKey = "";
            while(scoreKeys.hasMoreElements()){
                String key = scoreKeys.nextElement();

                double value = scores.get(key);

                if(value > max){
                    max = value;
                    bestKey = key;
                }
            }

            errorWord.setReplacingPart(bestKey);
        }

        return getCorrectLine(errorWords);
    }

    private String getCorrectLine(ErrorWord[] errorWords){
        String aSROutput = errorWords[0].getContextLine().getLine();

        aSROutput = aSROutput.replace(errorWords[0].getChangeablePart().getLine(), errorWords[0].getReplacingPart());

        return aSROutput;
    }

    private String[][] getMatchedWordPatterns(String[] regularExpressionPatterns){
        ArrayList<ArrayList<String>> allMatchedWordPatterns = new ArrayList<ArrayList<String>>();
        ArrayList<String> matchedWordPatterns;
        for(String regularExpressionPattern : regularExpressionPatterns){
            matchedWordPatterns = new ArrayList<String>();
            Pattern pattern = Pattern.compile(regularExpressionPattern);

            for(TextLine sentence : corpus_.getSentences()) {
                Matcher matcher = pattern.matcher(sentence.getLine());

                while (matcher.find()) {
                    matchedWordPatterns.add(matcher.group());
                }
            }

            allMatchedWordPatterns.add(matchedWordPatterns);
        }

        String[][] matchedWordPatternsArray = new String[allMatchedWordPatterns.size()][];
        for (int i = 0;i < allMatchedWordPatterns.size();i++){
            matchedWordPatternsArray[i] = new String[allMatchedWordPatterns.get(i).size()];

            for(int j = 0;j < allMatchedWordPatterns.get(i).size();j++){
                matchedWordPatternsArray[i][j] = allMatchedWordPatterns.get(i).get(j);
            }
        }

        return matchedWordPatternsArray;
    }

    // Replacing parts are substring of matchedWordPatterns.
    private String[][] getReplacingParts(String[] regularExpressions, String[][] matchedWordPatterns){
        ArrayList<ArrayList<String>> replacingParts = new ArrayList<ArrayList<String>>();
        ArrayList<String> regularExpressionReplacingParts;

        for(int i = 0, n = regularExpressions.length;i < n;i++){
            regularExpressionReplacingParts = new ArrayList<String>();

            for(int j = 0, m = matchedWordPatterns[i].length;j < m;j++){
                Pattern pattern = Pattern.compile(regularExpressions[i]);
                Matcher matcher = pattern.matcher(matchedWordPatterns[i][j]);

                if(matcher.find()) {
                    regularExpressionReplacingParts.add(matcher.group(1));
                }
            }

            replacingParts.add(regularExpressionReplacingParts);
        }

        String[][] replacingPartsArray = new String[replacingParts.size()][];
        for(int i = 0, n = replacingParts.size();i < n;i++){
            replacingPartsArray[i] = new String[replacingParts.get(i).size()];

            for(int j = 0, m = replacingParts.get(i).size();j < m;j++){
                replacingPartsArray[i][j] = replacingParts.get(i).get(j);
            }
        }

        return replacingPartsArray;
    }

    private String getPronunciationSequence(String[] words){
        StringBuilder stringBuilder = new StringBuilder();

        for(String word : words){
            stringBuilder.append(dictionary_.getPhones(word));
        }

        return stringBuilder.toString();
    }

    private Corpus corpus_;

    private Dictionary dictionary_;

    private int minimumSurroundingWordLength_ = 5;
    private int maximumSurroundingWordLength_ = 5;

    public String wordSeparator_ = " ";

}
