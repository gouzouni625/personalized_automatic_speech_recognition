package org.pasr.database.processes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * @class Process
 * @brief Wraps a Java Process to add functionality
 */
public abstract class Process {

    /**
     * @brief Default Constructor
     *
     * @throws IOException If the default output and error files cannot be created
     */
    Process () throws IOException {
        processBuilderList_ = new ArrayList<>();

        // If an output or an error file is not defined for the Java Process, it will buff the lines
        // sent to the corresponding channels. If any of these channels' buffers get filled up, the
        // Process will block until a Thread reads from the buffer. That is why default error and
        // output files are created.
        errorRedirectionFile_ = Files.createTempFile("error", "log").toFile();
        outputRedirectionFile_ = Files.createTempFile("output", "log").toFile();
    }

    /**
     * @brief Starts and waits this process
     *
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the process is interrupted
     */
    public void startAndWaitFor () throws IOException, InterruptedException {
        for (ProcessBuilder processBuilder : processBuilderList_) {
            startProcessBuilder(processBuilder).waitFor();
        }
    }

    /**
     * @brief Starts and waits this process for a period of time
     *
     * @param totalTimeout
     *     The amount of time to wait (in milliseconds)
     *
     * @return True if the process was finished
     *
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the process is interrupted
     */
    public boolean startAndWaitFor (long totalTimeout) throws IOException, InterruptedException {
        long timeout = totalTimeout / processBuilderList_.size() + 1;

        for (ProcessBuilder processBuilder : processBuilderList_) {
            java.lang.Process process = startProcessBuilder(processBuilder);
            boolean result = process.waitFor(timeout, TimeUnit.SECONDS);

            if (! result) {
                process.destroy();
                return false;
            }
        }

        return true;
    }

    private java.lang.Process startProcessBuilder (ProcessBuilder processBuilder)
        throws IOException {

        if (processBuilder.redirectOutput().file() == null) {
            processBuilder.redirectOutput(outputRedirectionFile_);
        }

        if (processBuilder.redirectError().file() == null) {
            processBuilder.redirectError(errorRedirectionFile_);
        }

        return processBuilder.start();
    }

    /**
     * @brief Sets the output file
     *
     * @param outputRedirectionFile
     *     The new output file
     */
    void setOutputRedirectionFile (File outputRedirectionFile) {
        outputRedirectionFile_ = outputRedirectionFile;
    }

    /**
     * @brief Sets the error file
     *
     * @param errorRedirectionFile
     *     the new error file
     */
    void setErrorRedirectionFile (File errorRedirectionFile) {
        errorRedirectionFile_ = errorRedirectionFile;
    }

    List<ProcessBuilder> processBuilderList_;

    private File outputRedirectionFile_; //!< The output file of this Process
    private File errorRedirectionFile_; //!< The error file of this Process

}
