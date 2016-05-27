package org.utilities;


import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class LevenshteinMatrixTest {

    @Test
    public void testCalculateMatrix(){
        LevenshteinMatrix levenshteinMatrix = new LevenshteinMatrix<>(
            new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
            new Integer[] {0, 1, 2, 3, 3, 4, 5, 6, 7, 8}
        );

        assertEquals(2, levenshteinMatrix.getDistance());

        levenshteinMatrix = new LevenshteinMatrix<>(
            new Integer[] {100, 101, 102, 103, 104, 105, 106, 107, 108, 109},
            new Integer[] {100, 101, 102, 103, 103, 104, 105, 106, 107, 108}
        );

        assertEquals(2, levenshteinMatrix.getDistance());

        levenshteinMatrix = new LevenshteinMatrix<>(
            ArrayUtils.toObject("Hello World".toCharArray()),
            ArrayUtils.toObject("Hello W1rld".toCharArray())
        );

        assertEquals(1, levenshteinMatrix.getDistance());
    }

}
