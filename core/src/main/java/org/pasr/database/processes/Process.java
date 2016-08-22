package org.pasr.database.processes;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public abstract class Process {
    Process(){
        processBuilderList_ = new ArrayList<>();
    }

    public void startAndWaitFor() throws IOException, InterruptedException {
        for(ProcessBuilder processBuilder : processBuilderList_){
            processBuilder.start().waitFor();
        }
    }

    public boolean startAndWaitFor(long totalTimeout) throws IOException, InterruptedException {
        long timeout = totalTimeout / processBuilderList_.size() + 1;

        for(ProcessBuilder processBuilder : processBuilderList_){
            java.lang.Process process = processBuilder.start();
            boolean result = process.waitFor(timeout, TimeUnit.SECONDS);

            if(!result){
                process.destroy();
                return false;
            }
        }

        return true;
    }

    List<ProcessBuilder> processBuilderList_;

}
