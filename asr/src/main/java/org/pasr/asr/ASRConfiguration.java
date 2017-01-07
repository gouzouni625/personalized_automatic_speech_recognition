package org.pasr.asr;

import java.nio.file.Path;


public interface ASRConfiguration {

    Path getAcousticModelPath();
    void setAcousticModelPath(Path path);

    Path getLanguageModelPath();
    void setLanguageModelPath(Path path);

    Path getDictionaryPath();
    void setDictionaryPath(Path path);

}