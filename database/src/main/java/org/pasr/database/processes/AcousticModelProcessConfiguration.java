package org.pasr.database.processes;


import java.nio.file.Path;

public interface AcousticModelProcessConfiguration {

    Path getSphinxFePath();

    Path getBwPath();

    Path getMllrSolvePath();

    Path getMapAdaptPath();

}
