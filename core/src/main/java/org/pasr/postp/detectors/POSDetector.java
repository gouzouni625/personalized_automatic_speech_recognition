package org.pasr.postp.detectors;


import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import org.pasr.prep.corpus.Corpus;
import org.pasr.prep.corpus.Word;
import org.pasr.prep.corpus.WordSequence;
import org.pasr.utilities.LevenshteinMatrix;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.ListUtils.longestCommonSubsequence;
import static org.pasr.utilities.Utilities.getResourceStream;


public class POSDetector implements Detector {
    public POSDetector(Corpus corpus) throws IOException {
        InputStream inputStream = getResourceStream("/detectors/pos/en-pos-perceptron.bin");

        if(inputStream == null){
            throw new IOException("getResourceStream(\"/detectors/pos/en-pos-perceptron.bin\")" +
                " returned null.");
        }

        tagger_ = new POSTaggerME(new POSModel(inputStream));

        createPOSPatterns(corpus);
    }

    private void createPOSPatterns(Corpus corpus){
        pOSPatterns_ = new ArrayList<>();

        for(WordSequence wordSequence : corpus){
            pOSPatterns_.add(tag(wordSequence));
        }
    }

    private List<Tags> tag (WordSequence wordSequence) {
        return Tags.tag(
            tagger_.tag(
                wordSequence.getWords().stream()
                    .map(Word:: getText)
                    .toArray(String[] ::new)
            )
        );
    }

    @Override
    public List<Word> detect (WordSequence wordSequence) {
        List<Tags> wordSequencePattern = tag(wordSequence);

        List<Tags> bestPattern = getBestPattern(wordSequencePattern);

        return findErrorWords(bestPattern, wordSequencePattern).stream()
            .map(wordSequence:: getWord)
            .collect(Collectors.toList());
    }

    private List<Tags> getBestPattern (List<Tags> wordSequenceTagList) {
        int minDistance = new LevenshteinMatrix<>(wordSequenceTagList, pOSPatterns_.get(0))
            .getDistance();
        int minIndex = 0;

        int currentDistance;
        for(int i = 1, n = pOSPatterns_.size();i < n;i++){
            currentDistance = new LevenshteinMatrix<>(wordSequenceTagList, pOSPatterns_.get(i))
                .getDistance();

            if(currentDistance < minDistance){
                minDistance = currentDistance;
                minIndex = i;
            }
        }

        return pOSPatterns_.get(minIndex);
    }

    private List<Integer> findErrorWords (List<Tags> referencePattern,
                                         List<Tags> hypothesisPattern) {
        List<Tags> matchingTags = longestCommonSubsequence(referencePattern, hypothesisPattern);

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

    private List<List<Tags>> pOSPatterns_;

    private POSTaggerME tagger_;

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
            ArrayList<Tags> tags = new ArrayList<>();

            for(String tagString : tagStringArray){
                tags.add(tag(tagString));
            }

            return tags;
        }

        private final String partOfSpeech_;
    }

}
