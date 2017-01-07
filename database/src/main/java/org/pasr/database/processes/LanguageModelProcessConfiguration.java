package org.pasr.database.processes;

import java.nio.file.Path;


public interface LanguageModelProcessConfiguration {

    Path getText2wfreqPath();

    Path getWfreq2vocabPath();

    Path getText2idngramPath();

    Path getIdngram2lmPath();

}
