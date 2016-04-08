package org.corpus;


import org.junit.Before;
import org.junit.Test;
import org.utilities.Utilities;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class CorpusTest {
    private Corpus corpus_;

    @Before
    public void setUp() throws FileNotFoundException {
        corpus_ = Corpus.createFromFile(new File(Utilities.getResourcePath(
                "/corpora/custom_text.txt")
        ));
    }

    @Test
    public void testContains(){
        // Test full sentence search
        assertTrue(corpus_.containsText("this is a frase that exists inside this corpus"));

        assertFalse(corpus_.containsText("this frase doesn't exist inside this corpus"));

        // Test sub-sentence search
        assertTrue(corpus_.containsText("this is a"));
        assertTrue(corpus_.containsText("frase that exists"));
        assertTrue(corpus_.containsText("inside this corpus"));

        assertFalse(corpus_.containsText(" this is a")); // Notice the space before 'this'
        assertFalse(corpus_.containsText("no this is a"));
        assertFalse(corpus_.containsText("inside this corpus ")); // Notice the space after 'corpus'
        assertFalse(corpus_.containsText("inside this corpus and"));
        assertFalse(corpus_.containsText("this frase exists"));
    }

}
