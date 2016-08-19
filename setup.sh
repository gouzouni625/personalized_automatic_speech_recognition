#!/usr/bin/env bash

## ===== Init submodules ===== ##
git submodule init

## ===== Update submodules ===== ##
git submodule update

## ===== Install sphinxbase ===== ##
cd sphinxbase

# autogen script will run configure with the given arguments
./autogen.sh --prefix=${PWD}/installation && make && make check && make install

if [ $? -eq 1 ]
then
  echo "Could not build sphinxbase"
  exit 1
else
  echo "sphinxbase was successfully built and installed" >> setup.log
fi

export PKG_CONFIG_PATH=${PKG_CONFIG_PATH}:${PWD}

cd ..

## ===== Install sphinxtrain ===== ##
cd sphinxtrain

# autogen script will run configure with the given arguments
./autogen.sh --prefix=${PWD}/installation && make && make check && make install

if [ $? -eq 1 ]
then
  echo "Could not build sphinxtrain"
  exit 1
else
  echo "sphinxtrain was successfully built and installed" >> setup.log
fi

cd ..

## ===== Install pocketsphinx ===== ##
cd pocketsphinx

# autogen script will run configure with the given arguments
./autogen.sh  --prefix=${PWD}/installation && make && make check && make install

if [ $? -eq 1 ]
then
  echo "Could not build pocketsphinx"
  exit 1
else
  echo "pocketsphinx was successfully built and installed" >> setup.log
fi

# If no first argument is supplied
if [ -z "$1" ]
  then
    export JAVA_HOME=/usr/lib/jvm/default-java
else
    export JAVA_HOME=$1
fi

export PKG_CONFIG_PATH=${PKG_CONFIG_PATH}:${PWD}

cd swig/java

make && javac edu/cmu/pocketsphinx/*.java && jar -cf pocketsphinx.jar edu libpocketsphinx_jni.so

if [ $? -eq 1 ]
then
  echo "Could not build pocketsphinx for java"
  exit 1
else
  echo "pocketsphinx for java was successfully built" >> setup.log
fi

cp pocketsphinx.jar ../../../core/libs

cd ../../../

## ===== Download and Install cmuclmtk ===== ##
wget https://sourceforge.net/projects/cmusphinx/files/cmuclmtk/0.7/cmuclmtk-0.7.tar.gz/download \
-O cmuclmtk-0.7.tar.gz

tar -xf cmuclmtk-0.7.tar.gz
rm cmuclmtk-0.7.tar.gz
mv cmuclmtk-0.7 cmuclmtk

cd cmuclmtk

./configure --prefix=${PWD}/installation && make && make check && make install

if [ $? -eq 1 ]
then
  echo "Could not build cmuclmtk"
  exit 1
else
  echo "cmuclmtk was successfully built and installed" >> setup.log
fi

cd ..
