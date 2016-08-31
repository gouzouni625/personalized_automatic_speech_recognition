package org.pasr.database.processes;


import org.pasr.database.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class LanguageModelProcess extends Process{
    public LanguageModelProcess(Path inputPath, Path outputPath, int depth) throws IOException {
        File inputFile = inputPath.toFile();

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

        Configuration configuration = Configuration.getInstance();

        setOutputRedirectionFile(new File(outputPath.getParent().toFile(), "output.log"));
        setErrorRedirectionFile(new File(outputPath.getParent().toFile(), "error.log"));

        processBuilderList_.add(new ProcessBuilder(
            configuration.getText2wfreqPath()
        ).redirectInput(inputFile).redirectOutput(freqFile));

        processBuilderList_.add(new ProcessBuilder(
            configuration.getWfreq2vocabPath()
        ).redirectInput(freqFile).redirectOutput(vocabFile));

        processBuilderList_.add(new ProcessBuilder(
            configuration.getText2idngramPath(),
            "-n", String.valueOf(depth),
            "-vocab", vocabFile.getPath(),
            "-idngram", idngramFile.getPath(),
            "-write_ascii"
        ).redirectInput(inputFile));

        processBuilderList_.add(new ProcessBuilder(
            configuration.getIdngram2lmPath(),
            "-n", String.valueOf(depth),
            "-vocab", vocabFile.getPath(),
            "-idngram", idngramFile.getPath(),
            "-arpa", outputPath.toString(),
            "-ascii_input"
        ));
    }

}
