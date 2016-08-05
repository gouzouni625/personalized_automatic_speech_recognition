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

        File freqFile = Files.createTempFile(inputFileName, "freq").toFile();
        File vocabFile = Files.createTempFile(inputFileName, "vocab").toFile();
        File idngramFile = Files.createTempFile(inputFileName, "idngram").toFile();

        Configuration configuration = Configuration.getInstance();

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
