These spring configurations illustrate the basic structure and functionality of adaptors.

The place to start is 

  simple.xml
  
To run this you first need to set your classpath correctly (see top level README.txt).

Once you have done this you can run from the command line like this...

  java org.oa3.spring.SpringApplication -config simple.xml -bean Adaptor

This contrived example reads input from stdin, capitalizes it and writes it to stdout.

Try it out by typing some input into the console.

You can adjust the logging by tweaking example/lo4j.properties.

The other config files show core adaptor functionality that you may want to take advantage of.