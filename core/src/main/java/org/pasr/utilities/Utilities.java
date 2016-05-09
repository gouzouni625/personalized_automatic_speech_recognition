package org.pasr.utilities;


public class Utilities {
    public static String getResourcePath(String resource){
        return Utilities.class.getResource(resource).getPath();
    }

}
