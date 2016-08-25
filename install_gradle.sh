#!/usr/bin/env bash

## ===== Download and Install gradle ===== ##
wget https://services.gradle.org/distributions/gradle-3.0-bin.zip -O gradle-3.0-bin.zip

unzip gradle-3.0-bin.zip
rm gradle-3.0-bin.zip
mv gradle-3.0 gradle
