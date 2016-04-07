package org.engine;


import org.corpus.TextLine;
import org.corpus.Corpus;
import org.utilities.Margin;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;
import static org.utilities.Utilities.arrayMargins;
import static org.utilities.Utilities.collectionToArray;


import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorCorrector {
    public ErrorCorrector(Corpus corpus, Dictionary dictionary) throws FileNotFoundException {
        corpus_ = corpus;
        dictionary_ = dictionary;
    }

    public String correct(TextLine aSROutput, ErrorWord[] errorWords) {
        if(errorWords == null){
            return aSROutput.getLine();
        }

        if(errorWords.length == 0){
            return aSROutput.getLine();
        }

        WordSequencePattern[] wordSequencePatterns = getWordSequencePatterns(aSROutput, errorWords);

        for(WordSequencePattern wordSequencePattern : wordSequencePatterns){
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

            wordSequencePattern.setReplacingPart(bestKey);
        }

        return getCorrectLine(wordSequencePatterns, unChangedParts_);
    }

    private WordSequencePattern[] getWordSequencePatterns(TextLine aSROutput, ErrorWord[] errorWords){
        int numberOfContextWords = aSROutput.tokenize().length;

        boolean[] wordIsChangeable = new boolean[numberOfContextWords];
        for(ErrorWord errorWord : errorWords){
            for(int i = errorWord.getWordOnTheLeftIndex(), n = errorWord.getWordOnTheRightIndex();i <= n;i++){
                wordIsChangeable[i] = true;
            }
        }

        unChangedParts_ = arrayMargins(wordIsChangeable, false);

        ArrayList<WordSequencePattern> wordSequencePatterns = new ArrayList<WordSequencePattern>();
        Margin[] margins = arrayMargins(wordIsChangeable, true);
        for(Margin margin : margins){
            wordSequencePatterns.add(new WordSequencePattern(aSROutput.subLine(margin.leftIndex_, margin.rightIndex_), aSROutput, margin.leftIndex_));
        }

        WordSequencePattern[] wordSequencePatternsArray = new WordSequencePattern[wordSequencePatterns.size()];
        collectionToArray(wordSequencePatterns, wordSequencePatternsArray);

        return wordSequencePatternsArray;
    }

    private String getCorrectLine(WordSequencePattern[] wordSequencePatterns, Margin[] unChangedParts){
        TextLine aSROutput = wordSequencePatterns[0].getLine();

        StringBuilder stringBuilder = new StringBuilder();
        int index = 0;
        int numberOfWordSequencePatterns = wordSequencePatterns.length;

        if(unChangedParts != null && unChangedParts.length > 0 && unChangedParts[0].leftIndex_ > 0){
            stringBuilder.append(wordSequencePatterns[0].getReplacingPart());
            index++;
        }

        for(Margin unChangedPart : unChangedParts){
            stringBuilder.append(wordSeparator_);
            stringBuilder.append(aSROutput.subLine(unChangedPart.leftIndex_, unChangedPart.rightIndex_));

            if(index < numberOfWordSequencePatterns) {
                stringBuilder.append(wordSeparator_);
                stringBuilder.append(wordSequencePatterns[index].getReplacingPart());
                index++;
            }
        }

        return stringBuilder.toString();
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

    public String wordSeparator_ = " ";

    private Margin[] unChangedParts_;

}
