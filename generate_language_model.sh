#!/bin/bash

export PATH=$PWD/cmuclmtk-0.7/installation/bin:$PATH
export LD_LIBRARY_PATH=$PWD/cmuclmtk-0.7/installation/lib:$LD_LIBRARY_PATH

TEXTFILE=$1
OUTPUT_FILE=$2
N=$3

cd cmuclmtk-0.7

cat $TEXTFILE | text2wfreq > $TEXTFILE.freq

cat $TEXTFILE.freq | wfreq2vocab > $TEXTFILE.vocab

cat $TEXTFILE | text2idngram -n $N -vocab $TEXTFILE.vocab -idngram $TEXTFILE.idngram -write_ascii

idngram2lm -n $N -idngram $TEXTFILE.idngram -vocab $TEXTFILE.vocab -arpa $OUTPUT_FILE -ascii_input

cd ..
