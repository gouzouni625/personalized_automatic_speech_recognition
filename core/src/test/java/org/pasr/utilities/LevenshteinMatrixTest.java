package org.pasr.utilities;


import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.pasr.postp.engine.detectors.POSDetector.Tags;

import java.util.Arrays;
import java.util.Random;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;
import static org.junit.Assert.assertEquals;


public class LevenshteinMatrixTest {

    @Test
    public void testCalculateMatrix(){
        LevenshteinMatrix levenshteinMatrix = new LevenshteinMatrix<>(
            Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
            Arrays.asList(0, 1, 2, 3, 3, 4, 5, 6, 7, 8)
        );

        assertEquals(2, levenshteinMatrix.getDistance());

        levenshteinMatrix = new LevenshteinMatrix<>(
            Arrays.asList(100, 101, 102, 103, 104, 105, 106, 107, 108, 109),
            Arrays.asList(100, 101, 102, 103, 103, 104, 105, 106, 107, 108)
        );

        assertEquals(2, levenshteinMatrix.getDistance());

        levenshteinMatrix = new LevenshteinMatrix<>(
            Arrays.asList(ArrayUtils.toObject("Hello World".toCharArray())),
            Arrays.asList(ArrayUtils.toObject("Hello W1rld".toCharArray()))
        );

        assertEquals(1, levenshteinMatrix.getDistance());

        levenshteinMatrix = new LevenshteinMatrix<>(
            Arrays.asList("Hello", "W0r1d"),
            Arrays.asList("Hello", "World")
        );

        assertEquals(1, levenshteinMatrix.getDistance());

        levenshteinMatrix = new LevenshteinMatrix<>(
            Arrays.asList(Tags.JJ, Tags.CD, Tags.POS, Tags.EX, Tags.JJ),
            Arrays.asList(Tags.CC, Tags.JJ, Tags.CD, Tags.EX, Tags.JJ)
        );

        assertEquals(2, levenshteinMatrix.getDistance());

        byte[] buffer = new byte[32];
        Random random = new Random(System.currentTimeMillis());
        for(int i = 0;i < 10000;i++){
            random.nextBytes(buffer);
            String string1 = new String(buffer);

            random.nextBytes(buffer);
            String string2 = new String(buffer);

            assertEquals(
                getLevenshteinDistance(string1, string2),
                new LevenshteinMatrix<>(
                    Arrays.asList(ArrayUtils.toObject(string1.toCharArray())),
                    Arrays.asList(ArrayUtils.toObject(string2.toCharArray()))
                ).getDistance()
            );
        }
    }

}
