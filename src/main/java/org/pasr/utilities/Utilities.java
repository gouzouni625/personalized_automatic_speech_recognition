package org.pasr.utilities;

import org.apache.commons.lang3.ArrayUtils;

import java.io.InputStream;
import java.net.URL;
import java.util.OptionalDouble;
import java.util.stream.DoubleStream;


/**
 * @class Utilities
 * @brief A class holding utility methods
 */
public class Utilities {

    /**
     * @brief Returns a URL given a resource path
     *
     * @param resource
     *     The resource path
     *
     * @return The resource URL
     */
    public static URL getResource (String resource) {
        return Utilities.class.getResource(resource);
    }

    /**
     * @brief Returns an InputStream given a resource path
     *
     * @param resource
     *     The resource path
     *
     * @return The resource InputStream
     */
    public static InputStream getResourceStream (String resource) {
        return Utilities.class.getResourceAsStream(resource);
    }

    /**
     * @brief Returns the index of the maximum value of the given array
     *
     * @param array
     *     The array to find the index of the maximum value
     *
     * @return The index of the maximum value of the given array
     */
    public static int indexOfMax (double[] array) {
        OptionalDouble result = DoubleStream.of(array).max();

        if (result.isPresent()) {
            return ArrayUtils.indexOf(array, result.getAsDouble());
        }
        else {
            return - 1;
        }
    }

    /**
     * @brief Returns the root mean square of signal
     *
     * @param array
     *     The byte array of the signal
     *
     * @return The root mean square value
     */
    public static double rootMeanSquare (byte[] array) {
        int length = array.length;

        long sum = 0;
        for (byte b : array) {
            sum += b;
        }

        double average = sum / length;

        sum = 0;
        for (byte b : array) {
            sum += Math.pow(b - average, 2);
        }

        return (sum / length);
    }

}
