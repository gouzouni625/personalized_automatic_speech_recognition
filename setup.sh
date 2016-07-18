#!/bin/bash

## ===== Init submodules ===== ##
git submodule init

## ===== Update submodules ===== ##
git submodule update

## ===== Install sphinxbase ===== ##
cd sphinxbase

# autogen script will run configure with the given arguments
./autogen.sh --prefix=$PWD

make
make check
make install

cd ..

## ===== Install sphinxtrain ===== ##
cd sphinxtrain

# autogen script will run configure with the given arguments
./autogen.sh --prefix=$PWD

make
make check
make install

cd ..

## ===== Install pocketsphinx ===== ##
cd pocketsphinx

# autogen script will run configure with the given arguments
./autogen.sh  --prefix=$PWD

make
make check
make install

cd ..

## ===== Download and Install cmuclmtk ===== ##
wget https://sourceforge.net/projects/cmusphinx/files/cmuclmtk/0.7/cmuclmtk-0.7.tar.gz/download -O cmuclmtk-0.7.tar.gz

tar -xf cmuclmtk-0.7.tar.gz
rm cmuclmtk-0.7.tar.gz

cd cmuclmtk-0.7
./configure --prefix=$PWD

make
make check
make install

cd ..

## ===== Create acoustic model adaptation directory ===== ##
mkdir acoustic_model_adaptation

cp -a sphinx4/sphinx4-data/src/main/resources/edu/cmu/sphinx/models/en-us/en-us acoustic_model_adaptation/
cp sphinx4/sphinx4-data/src/main/resources/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict acoustic_model_adaptation/

cp sphinxbase/bin/sphinx_fe acoustic_model_adaptation/
cp sphinxtrain/libexec/sphinxtrain/bw acoustic_model_adaptation/
cp sphinxtrain/libexec/sphinxtrain/mllr_solve acoustic_model_adaptation/

cd ..
