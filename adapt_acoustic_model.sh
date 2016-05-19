#!/bin/bash

cd acoustic_model_adaptation

./sphinx_fe -argfile en-us/feat.params \
            -samprate 16000 -c records.fileids \
            -di . -do . -ei wav -eo mfc -mswav yes

./bw -hmmdir en-us \
     -moddeffn en-us/mdef \
     -ts2cbfn .ptm. \
     -feat 1s_c_d_dd \
     -svspec 0-12/13-25/26-38 \
     -cmn current \
     -agc none \
     -dictfn cmudict-en-us.dict \
     -ctlfn records.fileids \
     -lsnfn records.transcription \
     -accumdir .

./mllr_solve -meanfn en-us/means \
             -varfn en-us/variances \
             -outmllrfn mllr_matrix -accumdir .

cd ..
