#!/bin/bash

if [ $# != 1 ]; then
    echo "usage: $0 [script file name]"
    exit 1
fi

if [ -e $1 ]; then
    :
else
    echo "usage: $0 [script file name]"
    exit 1
fi

java -jar ./external/nez/nez.jar ast -p ./src/main/resources/loglang.nez  -i $1
