package org.pasr.utilities;


import java.io.InputStream;


public class Utilities {
    public static InputStream getResourceStream(String resource){
        if(resource.substring(0, 9).equals("resource:")){
            resource = resource.substring(9);
        }

        return Utilities.class.getResourceAsStream(resource);
    }

}
