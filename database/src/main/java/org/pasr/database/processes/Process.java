package org.pasr.database.processes;

import lombok.Getter;
import lombok.Setter;

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
public abstract class Process extends ArrayList<ProcessBuilder> {

    /**
     * @brief Default Constructor
     *
     * @throws IOException If the default output and error files cannot be created
     */
    public Process () throws IOException {
        // If an output or an error file is not defined for the Java Process, it will buff the lines
        // sent to the corresponding channels. If any of these channels' buffers get filled up, the
        // Process will block until a Thread reads from the buffer. That is why default error and
        // output files are created.
        errorRedirectionFile = Files.createTempFile("error", "log").toFile();
        outputRedirectionFile = Files.createTempFile("output", "log").toFile();
    }

    /**
     * @brief Starts and waits this process
     *
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the process is interrupted
     */
    public void startAndWaitFor () throws IOException, InterruptedException {
        for (ProcessBuilder processBuilder : this) {
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
        long timeout = totalTimeout / this.size() + 1;

        for (ProcessBuilder processBuilder : this) {
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
            processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(outputRedirectionFile));
        }

        if (processBuilder.redirectError().file() == null) {
            processBuilder.redirectError(ProcessBuilder.Redirect.appendTo(errorRedirectionFile));
        }

        return processBuilder.start();
    }

    @Getter
    @Setter
    private File outputRedirectionFile; //!< The output file of this Process

    @Getter
    @Setter
    private File errorRedirectionFile; //!< The error file of this Process

}
