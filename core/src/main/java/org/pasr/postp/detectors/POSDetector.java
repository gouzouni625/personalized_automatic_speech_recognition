package org.pasr.postp.detectors;


import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import org.pasr.prep.corpus.Corpus;
import org.pasr.prep.corpus.Word;
import org.pasr.prep.corpus.WordSequence;
import org.pasr.utilities.LevenshteinMatrix;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.collections4.ListUtils.longestCommonSubsequence;
import static org.pasr.utilities.Utilities.getResourceStream;


public class POSDetector implements Detector {
    public POSDetector(Corpus corpus) throws IOException {
        if(corpus == null){
            throw new IllegalArgumentException("corpus must not be null!");
        }

        InputStream inputStream = getResourceStream("/detectors/pos/en-pos-perceptron.bin");

        if(inputStream == null){
            throw new FileNotFoundException(
                "getResourceStream(\"/detectors/pos/en-pos-perceptron.bin\")" +
                " returned null.");
        }

        tagger_ = new POSTaggerME(new POSModel(inputStream));

        createPOSPatterns(corpus);
    }

    private void createPOSPatterns(Corpus corpus){
        corpusMap_ = new HashMap<>();

        for (WordSequence wordSequence : corpus) {
            corpusMap_.put(wordSequence.getWordTextList(), tag(wordSequence));
        }
    }

    private List<Tags> tag (WordSequence wordSequence) {
        return Tags.tag(
            tagger_.tag(
                wordSequence.stream()
                    .map(Word :: toString)
                    .toArray(String[] :: new)
            )
        );
    }

    @Override
    public List<Word> detect (WordSequence wordSequence) {
        if(wordSequence == null){
            throw new IllegalArgumentException("wordSequence must not be null!");
        }

        if(wordSequence.isEmpty()){
            new ArrayList<>();
        }

        List<Tags> bestPattern = getBestPattern(wordSequence);

        if(bestPattern == null){
            return new ArrayList<>();
        }

        return findErrorWords(bestPattern, tag(wordSequence)).stream()
            .map(wordSequence :: get)
            .collect(Collectors.toList());
    }

    private List<Tags> getBestPattern (WordSequence wordSequence) {
        List<Tags> wordSequenceTagList = tag(wordSequence);
        List<String> wordSequenceWordTextList = wordSequence.getWordTextList();

        double minDistance = Double.POSITIVE_INFINITY;
        List<Tags> bestPattern = null;

        double currentDistance;
        for(Map.Entry<List<String>, List<Tags>> corpusMapEntry : corpusMap_.entrySet()){
            currentDistance = LevenshteinMatrix.getDistance(
                wordSequenceTagList,
                corpusMapEntry.getValue()
            );

            currentDistance = 0.5 * currentDistance + 0.5 * LevenshteinMatrix.getDistance(
                wordSequenceWordTextList,
                corpusMapEntry.getKey()
            );

            if(currentDistance < minDistance){
                minDistance = currentDistance;
                bestPattern = corpusMapEntry.getValue();
            }
        }

        return bestPattern;
    }

    private List<Integer> findErrorWords (List<Tags> referencePattern,
                                          List<Tags> hypothesisPattern) {
        List<Tags> matchingTags = longestCommonSubsequence(referencePattern, hypothesisPattern);

        if(matchingTags == null || matchingTags.size() == 0){
            return IntStream.range(0, hypothesisPattern.size())
                .boxed()
                .collect(Collectors.toList());
        }

        List<Integer> errorWordIndexList = new ArrayList<>();

        int matchingTagsSize = matchingTags.size();
        int hypothesisPatternSize = hypothesisPattern.size();

        int matchingTagsIndex = 0;
        int hypothesisIndex = 0;

        for(;hypothesisIndex < hypothesisPatternSize;hypothesisIndex++){
            if(hypothesisPattern.get(hypothesisIndex) == matchingTags.get(matchingTagsIndex)){
                matchingTagsIndex++;

                if(matchingTagsIndex == matchingTagsSize){
                    hypothesisIndex++;
                    break;
                }
            }
            else{
                errorWordIndexList.add(hypothesisIndex);
            }
        }
        for(int i = hypothesisIndex;i < hypothesisPatternSize;i++){
            errorWordIndexList.add(i);
        }

        return errorWordIndexList;
    }

    public enum Tags {
        CC(""),
        CD(""),
        DT(""),
        EX(""),
        FW(""),
        IN(""),
        JJ(""),
        JJR(""),
        JJS(""),
        LS(""),
        MD(""),
        NN(""),
        NNS(""),
        NNP(""),
        NNPS(""),
        PDT(""),
        POS(""),
        PRP(""),
        PRP_DS(""),
        RB(""),
        RBR(""),
        RBS(""),
        RP(""),
        SYM(""),
        TO(""),
        UH(""),
        VB(""),
        VBD(""),
        VBG(""),
        VBN(""),
        VBP(""),
        VBZ(""),
        WDT(""),
        WP(""),
        WP_DS(""),
        WRB(""),
        UNK("Unknown");

        Tags(String partOfSpeech){
            partOfSpeech_ = partOfSpeech;
        }

        public String getPartOfSpeech(){
            return partOfSpeech_;
        }

        public static Tags tag(String tagString){
            for (Tags tag : Tags.values()){
                if(tag.toString().equals(tagString)){
                    return tag;
                }
            }

            // PRP_DS is a replacement for PRP$ to avoid using the $ (Dollar Symbol).
            // WP_DS is a replacement for WP$ to avoid using the $ (Dollar Symbol).
            if (tagString.equals("PRP$")) {
                return PRP_DS;
            }
            else if (tagString.equals("WP$")) {
                return WP_DS;
            }

            return UNK;
        }

        public static List<Tags> tag(String[] tagStringArray){
            return Arrays.stream(tagStringArray)
                .map(Tags :: tag)
                .collect(Collectors.toList());
        }

        private final String partOfSpeech_;
    }

    private Map<List<String>, List<Tags>> corpusMap_;

    private POSTaggerME tagger_;

}
