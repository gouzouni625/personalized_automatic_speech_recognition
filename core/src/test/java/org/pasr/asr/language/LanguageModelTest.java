package org.pasr.asr.language;


import org.junit.Before;
import org.junit.Test;
import org.pasr.prep.corpus.WordSequence;

import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.pasr.utilities.Utilities.getResourceStream;


public class LanguageModelTest {
    private LanguageModel languageModel_;

    @Before
    public void setUp() throws FileNotFoundException {
        languageModel_ = LanguageModel.createFromInputStream(getResourceStream(
            "resource:/language_models/language_model.lm"
        ));
    }

    @Test
    public void testCreateFromInputStream(){
        assertEquals(0.1, languageModel_.getProbability(new WordSequence("word1", " ")), 1e-04);
        assertEquals(0.1, languageModel_.getProbability(new WordSequence("word2", " ")), 1e-04);
        assertEquals(0.1, languageModel_.getProbability(new WordSequence("word3", " ")), 1e-04);
        assertEquals(0.1, languageModel_.getProbability(new WordSequence("word4", " ")), 1e-04);
        assertEquals(0.1, languageModel_.getProbability(new WordSequence("word5", " ")), 1e-04);
        assertEquals(0.05, languageModel_.getProbability(new WordSequence("word6", " ")), 1e-04);
        assertEquals(0.05, languageModel_.getProbability(new WordSequence("word7", " ")), 1e-04);
        assertEquals(0.05, languageModel_.getProbability(new WordSequence("word8", " ")), 1e-04);
        assertEquals(0.05, languageModel_.getProbability(new WordSequence("word9", " ")), 1e-04);
        assertEquals(0.05, languageModel_.getProbability(new WordSequence("word10", " ")), 1e-04);
        assertEquals(0.06, languageModel_.getProbability(new WordSequence("word11", " ")), 1e-04);
        assertEquals(0.08, languageModel_.getProbability(new WordSequence("word12", " ")), 1e-04);
        assertEquals(0.01, languageModel_.getProbability(new WordSequence("word13", " ")), 1e-04);
        assertEquals(0.025, languageModel_.getProbability(new WordSequence("word14", " ")), 1e-04);
        assertEquals(0.075, languageModel_.getProbability(new WordSequence("word15", " ")), 1e-04);

        assertEquals(0.44, languageModel_.getProbability(new WordSequence("word1 word2", " ")),
            1e-04);
        assertEquals(0.09, languageModel_.getProbability(new WordSequence("word1 word5", " ")),
            1e-04);
        assertEquals(0.12, languageModel_.getProbability(new WordSequence("word3 word4", " ")),
            1e-04);
        assertEquals(0.24, languageModel_.getProbability(new WordSequence("word5 word6", " ")),
            1e-04);
        assertEquals(0.34, languageModel_.getProbability(new WordSequence("word7 word8", " ")),
            1e-04);
        assertEquals(0.55, languageModel_.getProbability(new WordSequence("word12 word14", " ")),
            1e-04);
        assertEquals(0.62, languageModel_.getProbability(new WordSequence("word9 word10", " ")),
            1e-04);
        assertEquals(0.43, languageModel_.getProbability(new WordSequence("word3 word13", " ")),
            1e-04);
        assertEquals(0.03, languageModel_.getProbability(new WordSequence("word11 word12", " ")),
            1e-04);
        assertEquals(0.11, languageModel_.getProbability(new WordSequence("word13 word15", " ")),
            1e-04);

        assertEquals(0.2000, languageModel_.getProbability(new WordSequence(
            "word1 word2 word3", " ")), 1e-04);
        assertEquals(0.4300, languageModel_.getProbability(new WordSequence(
            "word7 word12 word15", " ")), 1e-04);
        assertEquals(0.1200, languageModel_.getProbability(new WordSequence(
            "word8 word9 word4", " ")), 1e-04);
        assertEquals(0.5400, languageModel_.getProbability(new WordSequence(
            "word15 word14 word13", " ")), 1e-04);
        assertEquals(0.4000, languageModel_.getProbability(new WordSequence(
            "word3 word5 word6", " ")), 1e-04);

        // A 3-gram where the first 2-gram (word2 word3) doesn't exist in the language model.
        assertEquals(0.12, languageModel_.getProbability(new WordSequence(
            "word2 word3 word4", " ")), 1e-04);

        // A 3-gram where the first 2-gram (word1 word2) exists in the language model.
        assertEquals(0.3168, languageModel_.getProbability(new WordSequence(
            "word1 word2 word4", " ")), 1e-04);

        // A sequence of 4 words
        assertEquals(0.001056, languageModel_.getProbability(new WordSequence(
            "word1 word2 word3 word4", " ")), 1e-06);

        // A sequence of 5 words
        assertEquals(0, languageModel_.getProbability(new WordSequence(
            "word10 word11 word12 word13 word14 word15", " ")), 1e-06);
    }
}
