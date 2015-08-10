#!/bin/bash

git submodule update --init

# build Nez
cd ./external/nez
ant nez-core

mvn install:install-file -Dfile=nez-core.jar -DgroupId=nez-core -DartifactId=nez-core -Dversion=0.1 -Dpackaging=jar
