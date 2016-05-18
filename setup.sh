#!/bin/bash

## ===== Init submodules ===== ##
git submodule init

## ===== Update submodules ===== ##
git submodule update

## ===== Install sphinxbase ===== ##
cd sphinxbase
./autogen.sh --prefix=$PWD

make
make check
make install

cd ..

## ===== Install sphinxtrain ===== ##
cd sphinxtrain
./autogen.sh --prefix=$PWD

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
