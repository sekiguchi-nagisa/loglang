#!/bin/bash

trap "echo; echo '++++ error happened ++++'; exit 1" ERR

check_tool() {
    echo -n "-- found $1 - "; which $1
}

apply_patch() {
    test -d $1
    for p in $(find $1 -name "*.patch"); do
        echo "apply patch: $p"
        git apply $p
    done
}


# check tools
check_tool git
check_tool ant
check_tool mvn

# init submodule
git submodule update --init

# build Nez
cd ./external/nez
apply_patch ../../patch
ant clean
ant
ant nez-core
git stash && git stash clear


mvn install:install-file -Dfile=nez-core.jar -DgroupId=nez-core -DartifactId=nez-core -Dversion=0.1 -Dpackaging=jar
