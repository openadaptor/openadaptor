Examples in this section are intended to help configure a JBoss instance
for deployment of OA as a JBoss service and running the readerDS.xml 
example.

The following libraries need to be put on JBoss server's CLASSPATH:
    lib/openadaptor.jar
    lib/openadaptor-spring.jar
    lib/openadaptor-depends.jar

The XML files in this folder should be dropped into an auto-deploy area of JBoss.

When the JBoss instance is running, a JMX managed spring adaptor should be 
available via JMXConsole at http://localhost:8080  (under openadaptor.org).



        