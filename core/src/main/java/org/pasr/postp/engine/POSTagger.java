package org.pasr.postp.engine;


import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import org.pasr.corpus.Word;
import org.pasr.corpus.WordSequence;

import java.io.IOException;

import static org.pasr.utilities.Utilities.getResourceStream;


/** Singleton **/
public class POSTagger {

    // Make constructor private so it cannot be instantiated
    private POSTagger() throws IOException {
        pOSTagger_ = new POSTaggerME(new POSModel(getResourceStream("/models/en-pos-maxent.bin")));
    }

    public static POSTagger getInstance(){
        return instance_;
    }

    public Tags[] tag(WordSequence wordSequence){
        return Tags.createFromTagsStringArray(pOSTagger_.tag(wordSequence.getWordsText()));
    }

    public Tags tag(Word word){
        return Tags.createFromTagString(pOSTagger_.tag(word.getText()));
    }

    public enum Tags {
        CC("", "0"),
        CD("", "1"),
        DT("", "2"),
        EX("", "3"),
        FW("", "4"),
        IN("", "5"),
        JJ("", "6"),
        JJR("", "7"),
        JJS("", "8"),
        LS("", "9"),
        MD("", "A"),
        NN("", "B"),
        NNS("", "C"),
        NNP("", "D"),
        NNPS("", "E"),
        PDT("", "F"),
        POS("", "G"),
        PRP("", "H"),
        PRP_DS("", "I"),
        RB("", "J"),
        RBR("", "K"),
        RBS("", "L"),
        RP("", "M"),
        SYM("", "N"),
        TO("", "O"),
        UH("", "P"),
        VB("", "Q"),
        VBD("", "R"),
        VBG("", "S"),
        VBN("", "T"),
        VBP("", "U"),
        VBZ("", "V"),
        WDT("", "W"),
        WP("", "X"),
        WP_DS("", "Y"),
        WRB("", "Z"),
        UNKNOWN("", "");

        Tags(String partOfSpeech, String abbreviation) {
            partOfSpeech_ = partOfSpeech;
            abbreviation_ = abbreviation;
        }

        public static Tags createFromTagString(String tagString) {
            for (Tags tag : Tags.values()) {
                if (tagString.equals(tag.toString())) {
                    return tag;
                }
            }

            // PRP_DS is a replacement for PRP$ to avoid using the $ (Dollar Symbol).
            // WP_DS is a replacement for WP$ to avoid using the $ (Dollar Symbol).
            if (tagString.equals("PRP$")) {
                return PRP_DS;
            } else if (tagString.equals("WP$")) {
                return WP_DS;
            }

            return UNKNOWN;
        }

        public static Tags[] createFromTagsStringArray(String[] tagStrings) {
            Tags[] tagArray = new Tags[tagStrings.length];

            for (int i = 0, n = tagArray.length; i < n; i++) {
                tagArray[i] = createFromTagString(tagStrings[i]);
            }

            return tagArray;
        }

        public static String tagArrayToAbbreviatedString(Tags[] tagArray) {
            StringBuilder stringBuilder = new StringBuilder();

            for (Tags tag : tagArray) {
                stringBuilder.append(tag.getAbbreviation());
            }

            return stringBuilder.toString();
        }

        public String getPartOfSpeech() {
            return partOfSpeech_;
        }

        public String getAbbreviation() {
            return abbreviation_;
        }

        private String partOfSpeech_;
        private String abbreviation_;
    }

    private static POSTagger instance_;
    static{
        try {
            instance_ = new POSTagger();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private POSTaggerME pOSTagger_;

}
