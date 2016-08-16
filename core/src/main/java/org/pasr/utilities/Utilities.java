package org.pasr.utilities;

import org.apache.commons.lang3.ArrayUtils;

import java.io.InputStream;
import java.net.URL;
import java.util.OptionalDouble;
import java.util.stream.DoubleStream;


public class Utilities {
    public static URL getResource(String resource){
        return Utilities.class.getResource(resource);
    }

    public static InputStream getResourceStream(String resource){
        return Utilities.class.getResourceAsStream(resource);
    }

    public static int indexOfMax(double[] array){
        OptionalDouble result = DoubleStream.of(array).max();

        if(result.isPresent()){
            return ArrayUtils.indexOf(array, result.getAsDouble());
        }
        else{
            return -1;
        }
    }

    public static double rootMeanSquare(byte[] array){
        int length = array.length;

        long sum = 0;
        for(byte b : array){
            sum += b;
        }

        double average = sum / length;

        sum = 0;
        for(byte b : array){
            sum += Math.pow(b - average, 2);
        }

        return (sum / length);
    }

}
