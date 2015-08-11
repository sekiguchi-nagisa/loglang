#!/bin/bash

trap "echo; echo '++++ error happened ++++'; exit 1" ERR

check_tool() {
    echo -n "-- found $1 - "; which $1
}


# check tools
check_tool git
check_tool ant
check_tool mvn

# init submodule
git submodule update --init

# build Nez
cd ./external/nez
ant nez-core

mvn install:install-file -Dfile=nez-core.jar -DgroupId=nez-core -DartifactId=nez-core -Dversion=0.1 -Dpackaging=jar
