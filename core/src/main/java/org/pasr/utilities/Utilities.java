package org.pasr.utilities;

import java.io.InputStream;
import java.net.URL;


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

}
