package org.pasr.database.processes;

import org.pasr.asr.ASRConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * @class AcousticModelProcess
 *
 * @bfief A process that will adapt the acoustic model
 */
public class AcousticModelProcess extends Process {
    public AcousticModelProcess(
            File inputDirectory,
            File outputDirectory,
            Path transcriptions,
            Path ids,
            AcousticModelProcessConfiguration acousticModelProcessConfiguration,
            ASRConfiguration asrConfiguration) throws IOException {


        String inputDirectoryPathString = inputDirectory.toPath().toString() + "/";
        String adaptedAcousticModelPathString = Paths.get(
                outputDirectory.toPath().toString(),
                asrConfiguration.getAcousticModelPath().getFileName().toString()
        ).toString() + "/";
        String baseAcousticModelPathString = asrConfiguration.getAcousticModelPath().toString() + "/";

        setOutputRedirectionFile(new File(outputDirectory, "output.log"));
        setErrorRedirectionFile(new File(outputDirectory, "error.log"));

        add(new ProcessBuilder(
                acousticModelProcessConfiguration.getSphinxFePath().toString(),
                "-argfile", baseAcousticModelPathString + "feat.params",
                "-samprate", "16000",
                "-c", ids.toString(),
                "-di", inputDirectoryPathString,
                "-do", inputDirectoryPathString,
                "-ei", "wav",
                "-eo", "mfc",
                "-mswav", "yes"
        ));

        add(new ProcessBuilder(
                acousticModelProcessConfiguration.getBwPath().toString(),
                "-hmmdir", baseAcousticModelPathString,
                "-cepdir", inputDirectoryPathString,
                "-moddeffn", baseAcousticModelPathString + "mdef.txt",
                "-ts2cbfn", ".ptm.",
                "-feat", "1s_c_d_dd",
                "-svspec", "0-12/13-25/26-38",
                "-cmn", "current",
                "-agc", "none",
                "-dictfn", asrConfiguration.getDictionaryPath().toString(),
                "-ctlfn", ids.toString(),
                "-lsnfn", transcriptions.toString(),
                "-accumdir", inputDirectoryPathString
        ));

        add(new ProcessBuilder(
                acousticModelProcessConfiguration.getMllrSolvePath().toString(),
                "-meanfn", baseAcousticModelPathString + "means",
                "-varfn", baseAcousticModelPathString + "variances",
                "-outmllrfn", inputDirectoryPathString + "mllr_matrix",
                "-accumdir", inputDirectoryPathString
        ));

        add(new ProcessBuilder(
                "cp", "-r",
                baseAcousticModelPathString,
                outputDirectory.toPath().toString()
        ));

        add(new ProcessBuilder(
                acousticModelProcessConfiguration.getMapAdaptPath().toString(),
                "-moddeffn", baseAcousticModelPathString + "mdef.txt",
                "-ts2cbfn", ".ptm.",
                "-meanfn", baseAcousticModelPathString + "means",
                "-varfn", baseAcousticModelPathString + "variances",
                "-mixwfn", baseAcousticModelPathString + "mixture_weights",
                "-tmatfn", baseAcousticModelPathString + "transition_matrices",
                "-accumdir", inputDirectoryPathString,
                "-mapmeanfn", adaptedAcousticModelPathString + "means",
                "-mapvarfn", adaptedAcousticModelPathString + "variances",
                "-mapmixwfn", adaptedAcousticModelPathString + "mixture_weights",
                "-maptmatfn", adaptedAcousticModelPathString + "transition_matrices"
        ));
    }

}
