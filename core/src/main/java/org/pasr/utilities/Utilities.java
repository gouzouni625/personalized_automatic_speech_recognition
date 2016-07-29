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

    public static String[] reduceDimensions(String[][] array){
        int numberOfStrings = 0;
        for(String[] subArray : array){
            numberOfStrings += subArray.length;
        }

        String[] newArray = new String[numberOfStrings];

        int index = 0;
        for(String[] subArray : array){
            for(String string : subArray){
                newArray[index] = string;
                index++;
            }
        }

        return newArray;
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

}
