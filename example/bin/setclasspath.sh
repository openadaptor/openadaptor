#!/bin/sh
# Example script to setup openadaptor classpath.
# Works as follows:
# 1. If OPENDADAPTOR_HOME is set it is used to determine
#    the path for the openadaptor libraries
#    If OPENADAPTOR_HOME is not set, it attempts to guess it.
#    It first tries '.' and then '../../' (assuming it has run
#    from the example/bin directory) and  guesses the lib dir 
#    accordingly.
#
# 2. In then checks that the lib dir exists. If not, it exits.
#
# 3. Next it checks to see if the CLASSPATH is already set
#    and contains the libraries. If so, it exits.
#
# 4. It creates or appends the CLASSPATH with the openadaptor
#    libraries.
# Use existing OPENADAPTOR_HOME if it is already set
#
if [ ${OPENADAPTOR_HOME} ] ; then
  _OA_ROOT=`(cd ${OPENADAPTOR_HOME}; pwd )`
  echo "OPENADAPTOR HOME is $OPENADAPTOR_HOME (absolute - $_OA_ROOT)"
# Otherwise guess it, assuming current dir is bin/examples
else
  echo "Checking if current directory is openadaptor install directory"
  _OA_ROOT=`pwd`
  if [ ! -d $_OA_ROOT/lib ] ; then
     echo "Checking if ../../ is openadaptor install directory"
    _OA_ROOT=`(cd ../../; pwd )`
  fi
  echo "Detected openadaptor install at $_OA_ROOT"
fi

# Set the location of the oa libraries.
OA_LIB=$_OA_ROOT/lib
unset _OA_ROOT
if [ -d $OA_LIB ] ; then
  OA_CP=${OA_LIB}:${OA_LIB}/openadaptor.jar:${OA_LIB}/openadaptor-spring.jar:${OA_LIB}/openadaptor-depends.jar
    if [ ${CLASSPATH} ] ; then
      if [ "`echo $CLASSPATH | grep /openadaptor.jar`" != "" ] ; then
        echo "FATAL: CLASSPATH already contains openadaptor.jar. Not adding a second instance."
      else
        CLASSPATH=${CLASSPATH}:${OA_CP}
        export CLASSPATH
      fi
    else
      CLASSPATH=${OA_CP}
      export CLASSPATH
    fi
  unset OA_CP
else 
  echo "FATAL: Openadaptor lib dir ${OA_LIB} not found. Unable to set classpath"
  echo "       Please set OPENADAPTOR_HOME appropriately and retry."
fi
unset OA_LIB
