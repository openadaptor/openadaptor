package org.openadaptor.spring;

import java.io.PrintStream;

import org.openadaptor.core.adaptor.Adaptor;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * helper class for launch openadaptor adaptor processes based on spring.
 * 
 * @author perryj
 *
 */
public class SpringAdaptor extends SpringApplication {

  public static void main(String[] args) {
    try {
      SpringAdaptor app = new SpringAdaptor();
      app.parseArgs(args);
      app.run();
      System.exit(0);
    } catch (Exception e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
      usage(System.err);
      System.exit(1);
    }
  }
  
  protected Runnable getRunnableBean(ListableBeanFactory factory) {
    if (getBeanId() == null) {
      String[] ids = factory.getBeanNamesForType(Adaptor.class);
      if (ids.length == 1) {
        setBeanId(ids[0]);
      } else if (ids.length == 0){
        throw new RuntimeException("No Adaptor bean found in config");
      } else if (ids.length > 1) {
        throw new RuntimeException("Mulitple Adaptor beans found in config");
      }
    }
    return (Adaptor) factory.getBean(getBeanId());
  }

  protected static void usage(PrintStream ps) {
    ps.println("usage: java " + SpringApplication.class.getName() 
        + "\n  -config <url> [ -config <url>  | ... ]" 
        + "\n  [-bean <id>] "
        + "\n  [-jmxport <http port>]"
        + "\n\n"
        + " e.g. java " + SpringAdaptor.class.getName() + " -config file:test.xml");
  }

}
