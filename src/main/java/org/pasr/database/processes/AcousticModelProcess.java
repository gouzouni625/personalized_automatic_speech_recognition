package org.pasr.database.processes;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.pasr.database.DataBase;
import org.pasr.database.audio.Index;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;


/**
 * @class AcousticModelProcess
 * @bfief A process that will adapt the acoustic model
 */
public class AcousticModelProcess extends Process {
    public AcousticModelProcess () throws IOException {
        org.pasr.database.Configuration dataBaseConfiguration = org.pasr.database.Configuration
            .getInstance();

        org.pasr.asr.Configuration aSRConfiguration = org.pasr.asr.Configuration
            .getDefaultConfiguration();

        String acousticModelDirectoryPath = dataBaseConfiguration.getAcousticModelDirectoryPath();
        String acousticModelPath = dataBaseConfiguration.getAcousticModelPath();

        String defaultAcousticModelPath = aSRConfiguration.getAcousticModelPath();

        setOutputRedirectionFile(new File(acousticModelDirectoryPath, "output.log"));
        setErrorRedirectionFile(new File(acousticModelDirectoryPath, "error.log"));

        processBuilderList_.add(new ProcessBuilder(
            dataBaseConfiguration.getSphinxFePath(),
            "-argfile", defaultAcousticModelPath + "feat.params",
            "-samprate", "16000",
            "-c", acousticModelDirectoryPath + "sentences.fileids",
            "-di", acousticModelDirectoryPath,
            "-do", acousticModelDirectoryPath,
            "-ei", "wav",
            "-eo", "mfc",
            "-mswav", "yes"
        ));

        processBuilderList_.add(new ProcessBuilder(
            dataBaseConfiguration.getBwPath(),
            "-hmmdir", defaultAcousticModelPath,
            "-cepdir", acousticModelDirectoryPath,
            "-moddeffn", defaultAcousticModelPath + "mdef.txt",
            "-ts2cbfn", ".ptm.",
            "-feat", "1s_c_d_dd",
            "-svspec", "0-12/13-25/26-38",
            "-cmn", "current",
            "-agc", "none",
            "-dictfn", aSRConfiguration.getDictionaryPath(),
            "-ctlfn", acousticModelDirectoryPath + "sentences.fileids",
            "-lsnfn", acousticModelDirectoryPath + "sentences.transcription",
            "-accumdir", acousticModelDirectoryPath
        ));

        processBuilderList_.add(new ProcessBuilder(
            dataBaseConfiguration.getMllrSolvePath(),
            "-meanfn", defaultAcousticModelPath + "means",
            "-varfn", defaultAcousticModelPath + "variances",
            "-outmllrfn", acousticModelDirectoryPath + "mllr_matrix",
            "-accumdir", acousticModelDirectoryPath
        ));

        processBuilderList_.add(new ProcessBuilder(
            "cp", "-a",
            defaultAcousticModelPath,
            acousticModelPath
        ));

        processBuilderList_.add(new ProcessBuilder(
            dataBaseConfiguration.getMapAdaptPath(),
            "-moddeffn", defaultAcousticModelPath + "mdef.txt",
            "-ts2cbfn", ".ptm.",
            "-meanfn", defaultAcousticModelPath + "means",
            "-varfn", defaultAcousticModelPath + "variances",
            "-mixwfn", defaultAcousticModelPath + "mixture_weights",
            "-tmatfn", defaultAcousticModelPath + "transition_matrices",
            "-accumdir", acousticModelDirectoryPath,
            "-mapmeanfn", acousticModelPath + "means",
            "-mapvarfn", acousticModelPath + "variances",
            "-mapmixwfn", acousticModelPath + "mixture_weights",
            "-maptmatfn", acousticModelPath + "transition_matrices"
        ));
    }

    @Override
    public boolean startAndWaitFor (long timeout) throws IOException, InterruptedException {
        processAudioEntries();

        return super.startAndWaitFor(timeout);
    }

    private void processAudioEntries () throws IOException {
        List<Index.Entry> audioEntryList = DataBase.getInstance().getAudioEntryList();

        org.pasr.database.Configuration dataBaseConfiguration = org.pasr.database.Configuration
            .getInstance();

        PrintWriter fileIdsPrintWriter = new PrintWriter(new OutputStreamWriter(
            new FileOutputStream(
                dataBaseConfiguration.getAcousticModelDirectoryPath() + "sentences.fileids"
            )
        ));

        PrintWriter transcriptionPrintWriter = new PrintWriter(new OutputStreamWriter(
            new FileOutputStream(
                dataBaseConfiguration.getAcousticModelDirectoryPath() + "sentences.transcription"
            )
        ));

        for (Index.Entry entry : audioEntryList) {
            String filename = entry.getFilename();

            FileUtils.copyFile(
                new File(dataBaseConfiguration.getAudioDirectoryPath() + filename),
                new File(dataBaseConfiguration.getAcousticModelDirectoryPath() + filename)
            );

            String fileId = FilenameUtils.getBaseName(filename);

            fileIdsPrintWriter.println(fileId);
            transcriptionPrintWriter.println(
                "<s> " + entry.getSentence() + " </s> (" + fileId + ")"
            );
        }

        fileIdsPrintWriter.close();
        transcriptionPrintWriter.close();
    }

}
