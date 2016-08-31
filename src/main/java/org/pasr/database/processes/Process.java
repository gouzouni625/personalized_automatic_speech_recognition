package org.pasr.database.processes;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public abstract class Process {
    Process() throws IOException {
        processBuilderList_ = new ArrayList<>();

        errorRedirectionFile_ = Files.createTempFile("error", "log").toFile();
        outputRedirectionFile_ = Files.createTempFile("output", "log").toFile();
    }

    public void startAndWaitFor() throws IOException, InterruptedException {
        for(ProcessBuilder processBuilder : processBuilderList_){
            startProcessBuilder(processBuilder).waitFor();
        }
    }

    public boolean startAndWaitFor(long totalTimeout) throws IOException, InterruptedException {
        long timeout = totalTimeout / processBuilderList_.size() + 1;

        for(ProcessBuilder processBuilder : processBuilderList_){
            java.lang.Process process = startProcessBuilder(processBuilder);
            boolean result = process.waitFor(timeout, TimeUnit.SECONDS);

            if(!result){
                process.destroy();
                return false;
            }
        }

        return true;
    }

    private java.lang.Process startProcessBuilder(ProcessBuilder processBuilder)
        throws IOException {

        if(processBuilder.redirectOutput().file() == null){
            processBuilder.redirectOutput(outputRedirectionFile_);
        }

        if(processBuilder.redirectError().file() == null){
            processBuilder.redirectError(errorRedirectionFile_);
        }

        return processBuilder.start();
    }

    void setOutputRedirectionFile(File outputRedirectionFile){
        outputRedirectionFile_ = outputRedirectionFile;
    }

    void setErrorRedirectionFile(File errorRedirectionFile){
        errorRedirectionFile_ = errorRedirectionFile;
    }

    List<ProcessBuilder> processBuilderList_;

    private File outputRedirectionFile_;
    private File errorRedirectionFile_;

}
