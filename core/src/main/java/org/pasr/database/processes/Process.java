package org.pasr.database.processes;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public abstract class Process {
    Process(){
        processBuilderList_ = new ArrayList<>();
    }

    public void startAndWaitFor() throws IOException, InterruptedException {
        for(ProcessBuilder processBuilder : processBuilderList_){
            processBuilder.start().waitFor();
        }
    }

    List<ProcessBuilder> processBuilderList_;

}
