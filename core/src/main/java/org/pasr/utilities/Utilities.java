package org.pasr.utilities;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class Utilities {
    public static InputStream getResourceStream(String resource) throws FileNotFoundException {
        if(resource.substring(0, 9).equals("resource:")){
            return Utilities.class.getResourceAsStream(resource.substring(9));
        }

        return new FileInputStream(resource);
    }

    public static File getResourceFile(String filename){
        return new File(Utilities.class.getResource(filename).getPath());
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
