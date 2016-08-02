package org.pasr.database.processes;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class LanguageModelProcess {
    public LanguageModelProcess(Path inputPath, Path outputPath, int depth) throws IOException {
        File inputFile = inputPath.toFile();

        String inputFileName = inputFile.getName();

        File freqFile = Files.createTempFile(inputFileName, "freq").toFile();
        File vocabFile = Files.createTempFile(inputFileName, "vocab").toFile();
        File idngramFile = Files.createTempFile(inputFileName, "idngram").toFile();

        processBuilderList_ = new ArrayList<>(4);

        processBuilderList_.add(new ProcessBuilder(
            "text2wfreq"
        ).redirectInput(inputFile).redirectOutput(freqFile));

        processBuilderList_.add(new ProcessBuilder(
            "wfreq2vocab"
        ).redirectInput(freqFile).redirectOutput(vocabFile));

        processBuilderList_.add(new ProcessBuilder(
            "text2idngram",
            "-n", String.valueOf(depth),
            "-vocab", vocabFile.getPath(),
            "-idngram", idngramFile.getPath(),
            "-write_ascii"
        ).redirectInput(inputFile));

        processBuilderList_.add(new ProcessBuilder(
            "idngram2lm",
            "-n", String.valueOf(depth),
            "-vocab", vocabFile.getPath(),
            "-idngram", idngramFile.getPath(),
            "-arpa", outputPath.toString(),
            "-ascii_input"
        ));
    }

    public void startAndWaitFor() throws IOException, InterruptedException {
        for(ProcessBuilder processBuilder : processBuilderList_){
            processBuilder.start().waitFor();
        }
    }

    private List<ProcessBuilder> processBuilderList_;

}
