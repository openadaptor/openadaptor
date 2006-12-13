INTRODUCTION
============
There are two types of examples

  * spring (http://www.springframework.org) xml config files
  * java source code examples

openadaptor does not rely on the spring framework in anyway, however it has been
written to be "spring friendly". Previous versions of openadaptor had a properties file
based mechanism for initialising and "wiring together" the adaptor components, in the
new version all the adaptor components have been written as "beans" so spring can be used 
to achieve the same result.

JARS & CLASSPATH
================
  
  lib/openadaptor.jar          // the openadaptor code
  lib/openadaptor-depends.jar  // subset of 3rd party jars upon which our core code depends
  lib/openadaptor-spring.jar   // spring specific helper classes
  lib/openadaptor-stub.jar     // stub implementations of APIs we don't want to or cannot ship
  lib/3rdparty/...             // dir containing all the individual jars we depend upon

Unless the individual example README states otherwise, you need to set your CLASSPATH as follows...

windows

   set CLASSPATH=lib;lib\openadaptor.jar;lib\openadaptor-spring.jar;lib\openadaptor-depends.jar
   
unix (bash)

   export CLASSPATH=lib;lib/openadaptor.jar;lib/openadaptor-spring.jar;lib/openadaptor-depends.jar
   
SPRING EXAMPLES
===============

openadaptor provide a spring helper class for running adaptors as standalone processes.

  org.oa3.spring.SpringApplication
  
this expects the following arguments

  -config <url>   : where url points to a spring config, defaults to file:
  -bean <id>      : where id is bean id of something that implements Runnable
  -props <url>    : (optional) url for properties file
  -jmx <port>     : (optional) if specified then runs default jmx mbean server and http adaptor
  
Here is how to run your first adaptor
  
  java org.oa3.spring.SpringApplication -config example/spring/adaptor/simple.xml -bean Adaptor

(assumes your current working directory is openadaptor root dir).
    
For more details on this specific adaptor look and the README in that directory and the comments
in the config file.

CODE EXAMPLES
=============

// TODO  
  
  
   