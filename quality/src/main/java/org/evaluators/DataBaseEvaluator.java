package org.evaluators;

import org.database.DataBase;
import org.pasr.asr.Configuration;
import org.pasr.asr.recognizers.FileSpeechRecognizer;
import org.utilities.Timer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;


public class DataBaseEvaluator {
    private final Logger logger_ = Logger.getLogger(this.getClass().getName());

    public DataBaseEvaluator(Configuration aSRConfiguration, DataBase dataBase){
        recognizer_ = new FileSpeechRecognizer(aSRConfiguration);
        database_ = dataBase;
    }

    public long evaluate(String hypothesisFilePath, String resultFilePath)
        throws IOException, InterruptedException {
        PrintWriter hypothesisWriter = new PrintWriter(new File(hypothesisFilePath));

        Timer timer = new Timer();
        timer.start();
        for(DataBase.Entry entry : database_){
            logger_.info("Processing: " + entry.getId());

            hypothesisWriter.println(
                recognizer_.recognize(entry.getSoundFile()) + " (" + entry.getId() + ")"
            );
        }
        timer.stop();

        hypothesisWriter.close();

        // Produce metrics
        Process process = new ProcessBuilder(
            "./" + WORD_ALIGN_SCRIPT,
            database_.getConfiguration().getTranscriptionsPath(),
            hypothesisFilePath
        ).redirectOutput(new File(resultFilePath)).start();

        process.waitFor();

        return timer.getElapsedTime();
    }

    private FileSpeechRecognizer recognizer_;
    private DataBase database_;

    private static final String WORD_ALIGN_SCRIPT = "sphinxtrain/installation/lib/sphinxtrain/" +
        "scripts/decode/word_align.pl";

}
