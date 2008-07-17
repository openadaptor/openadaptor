Examples in this section are intended to illustrate basic usage of the JMS
connectors.

The properties are in jms.properties and are setup to work with jboss. These
should be edited as required to work with other jms providers.

These examples rely on a default instance of jboss (http://www.jboss.org)
running on the same machine that the example is being run.

Please ensure that the jboss client and server jars used are from compatible releases.

Run these examples like this...

java org.openadaptor.spring.SpringAdaptor -config example.xml -props jms.properties

Suggested Classpath

    lib
    lib/openadaptor.jar
    lib/openadaptor-spring.jar
    lib/openadaptor-depends.jar
    jbossall-client.jar (this is not distributed with openadaptor)
    
The jars for JOTM are included in lib/openadaptor-depends.jar by default.

        