package org.pasr.database.processes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


/**
 * @class LanguageModelProcess
 * @brief A process that will create a language model
 */
public class LanguageModelProcess extends Process {
    public LanguageModelProcess (File inputFile, File outputFile,
                                 int depth, LanguageModelProcessConfiguration configuration)
            throws IOException {

        String inputFileName = inputFile.getName();

        File freqFile;
        File vocabFile;
        File idngramFile;
        try {
            freqFile = Files.createTempFile(inputFileName, "freq").toFile();
            vocabFile = Files.createTempFile(inputFileName, "vocab").toFile();
            idngramFile = Files.createTempFile(inputFileName, "idngram").toFile();
        } catch (IOException e) {
            throw new IOException("Could not create temporary file.");
        }

        setOutputRedirectionFile(new File(outputFile.getParentFile(), "output.log"));
        setErrorRedirectionFile(new File(outputFile.getParentFile(), "error.log"));

        add(new ProcessBuilder(
            configuration.getText2wfreqPath().toString()
        ).redirectInput(inputFile).redirectOutput(freqFile));

        add(new ProcessBuilder(
            configuration.getWfreq2vocabPath().toString()
        ).redirectInput(freqFile).redirectOutput(vocabFile));

        add(new ProcessBuilder(
            configuration.getText2idngramPath().toString(),
            "-n", String.valueOf(depth),
            "-vocab", vocabFile.getPath(),
            "-idngram", idngramFile.getPath(),
            "-write_ascii"
        ).redirectInput(inputFile));

        add(new ProcessBuilder(
            configuration.getIdngram2lmPath().toString(),
            "-n", String.valueOf(depth),
            "-vocab", vocabFile.getPath(),
            "-idngram", idngramFile.getPath(),
            "-arpa", outputFile.toPath().toString(),
            "-ascii_input"
        ));
    }

}
