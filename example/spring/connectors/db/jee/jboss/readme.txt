Examples in this section are intended to help configure a JBoss instance
for deployment of OA as a JBoss service and running the readerDS.xml 
example.

The following libraries need to be put on JBoss server's CLASSPATH:
    lib/openadaptor.jar
    lib/openadaptor-spring.jar
    lib/openadaptor-depends.jar

The XML files in this folder should be dropped into an auto-deploy area of JBoss.
Hypersonic database and JBoss instances need to be started prior to running the
adaptor.

oahsqldb-ds.xml - defines a Hypersonic DataSource
oa-service.xml - exposes openadaptor as JBoss service

When the JBoss instance is running, an adaptor exposed as JMX managed bean should be 
available via JBoss web console at http://localhost:8080/web-console (under openadaptor.org).
Adaptor can be kicked of via runSpringAdaptor() method run with the name of 
adaptor config file (readerDS.xml) as argument.



        