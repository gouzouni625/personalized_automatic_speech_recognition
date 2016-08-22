#!/usr/bin/env bash

export PATH=sphinxbase/installation/bin:${PATH}
export PATH=sphinxtrain/installation/bin:${PATH}
export PATH=pocketsphinx/installation/bin:${PATH}
export PATH=cmuclmtk/installation/bin:${PATH}

export LD_LIBRARY_PATH=sphinxbase/installation/lib:${LD_LIBRARY_PATH}
export LD_LIBRARY_PATH=sphinxtrain/installation/lib:${LD_LIBRARY_PATH}
export LD_LIBRARY_PATH=pocketsphinx/installation/lib:${LD_LIBRARY_PATH}
export LD_LIBRARY_PATH=cmuclmtk/installation/lib:${LD_LIBRARY_PATH}

java -jar core/build/libs/core.jar
