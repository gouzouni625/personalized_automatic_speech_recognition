package org.pasr.utilities;


import java.io.File;
import java.io.InputStream;


public class Utilities {
    public static InputStream getResourceStream(String resource){
        if(resource.substring(0, 9).equals("resource:")){
            resource = resource.substring(9);
        }

        return Utilities.class.getResourceAsStream(resource);
    }

    public static File getResourceFile(String filename){
        return new File(Utilities.class.getResource(filename).getPath());
    }

}
