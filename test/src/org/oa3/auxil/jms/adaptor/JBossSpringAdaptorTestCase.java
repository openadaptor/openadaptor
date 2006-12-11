package org.oa3.auxil.jms.adaptor;

import junit.framework.TestCase;

import org.oa3.core.adaptor.Adaptor;
import org.oa3.spring.SpringApplication;
import org.oa3.util.ResourceUtil;
import org.springframework.beans.factory.ListableBeanFactory;

public class JBossSpringAdaptorTestCase extends TestCase {

  public void test() {
    String configFile = ResourceUtil.getResourcePath(this, "spring.xml");
    
    // run publisher
    SpringApplication.runXml(configFile, null, "Publisher");
    
    // create and start thread to stop subscriber after 5 secs
    ListableBeanFactory factory = SpringApplication.getBeanFactory(configFile, null);
    final Adaptor adaptor = (Adaptor) factory.getBean("Subscriber");
    Thread t = new Thread() {
      public void run() {
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        adaptor.stop();
      }
    };
    t.start();
    
    // run subscriber
   adaptor.run();
  }
}
