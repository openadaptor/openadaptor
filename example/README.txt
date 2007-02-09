INTRODUCTION
============
There are two types of examples

  * spring (http://www.springframework.org) xml config files
  * java source code examples

Openadaptor does not rely on the spring framework in any way, however it has been
written to be "spring friendly". Previous versions of openadaptor had a properties file
based mechanism for initialising and "wiring together" the adaptor components, in the
new version all the adaptor components have been written as "beans" allowing spring to be used 
to achieve the same result.

JARS & CLASSPATH
================
  
  lib/openadaptor.jar          // the openadaptor code
  lib/openadaptor-depends.jar  // subset of 3rd party jars upon which our core code depends
  lib/openadaptor-spring.jar   // spring specific helper classes
  lib/openadaptor-stub.jar     // stub implementations of APIs we don't want to or cannot ship
  lib/3rdparty/...             // dir containing all the individual jars we depend upon

Unless the individual example README states otherwise, the appropriate CLASSPATH may be set as follows...

windows
   (Note: This assumes %OPENADAPTOR_HOME% points to openadaptor install location)
   set CLASSPATH=%OPENADAPTOR_HOME%\lib;%OPENADAPTOR_HOME%\lib\openadaptor.jar;%OPENADAPTOR_HOME%\lib\openadaptor-spring.jar;%OPENADAPTOR_HOME%\lib\openadaptor-depends.jar
   
unix (sh)
   (Note: This assumes $OPENADAPTOR_HOME points to openadaptor install location))

   CLASSPATH=$OPENADAPTOR_HOME/lib:$OPENADAPTOR_HOME/lib/openadaptor.jar:$OPENADAPTOR_HOME/lib/openadaptor-spring.jar:$OPENADAPTOR_HOME/lib/openadaptor-depends.jar
   export CLASSPATH

The example/bin directory contains 2 scripts for setting your CLASSPATH with absolute paths.
These scripts may be run from the command line like this...

windows

  cd example\bin
  .\setclasspath.bat
  echo %CLASSPATH%

unix

  cd example/bin
  source setclasspath.sh
  echo $CLASSPATH
  
  
SPRING EXAMPLES
===============

openadaptor provides a spring helper class for running adaptors as a standalone process.

  org.openadaptor.spring.SpringApplication
  
this expects the following arguments

  -config <url>   : where url points to a spring config, defaults to file:
  -bean <id>      : where id is bean id of something that implements Runnable
  -props <url>    : (optional) url for properties file
  -jmx <port>     : (optional) if specified then runs default jmx mbean server and http adaptor
  
Here is how to run your first adaptor
  
  java org.openadaptor.spring.SpringApplication -config example/spring/adaptor/simple.xml -bean Adaptor

or (if you have a CLASSPATH with absolute paths)

  cd example/spring/adaptor
  java org.openadaptor.spring.SpringApplication -config simple.xml -bean Adaptor
  
For more details on this specific adaptor look and the README in that directory and the comments
in the config file.

CODE EXAMPLES
=============

The src dir contains code examples. The ant build file build.xml will compile these examples. 

In order to run the compiled examples you will need to set your classpath and include example/classes. 
  
  
   