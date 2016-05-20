package org.pasr.utilities;


import java.io.InputStream;


public class Utilities {
    public static InputStream getResourceStream(String resource){
        return Utilities.class.getResourceAsStream(resource);
    }

}
