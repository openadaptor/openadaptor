#!/bin/sh
#
# sets classpath for basic examples, uses absolute paths, but relies on this
# script being sourced from the bin dir
#
ROOT=`pwd`/../../lib
CLASSPATH=${ROOT}:${ROOT}/openadaptor.jar:${ROOT}/openadaptor-spring.jar:${ROOT}/openadaptor-depends.jar
export CLASSPATH
