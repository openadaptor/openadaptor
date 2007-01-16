package org.openadaptor.auxil.jms.adaptor;

import junit.framework.TestCase;

import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.spring.SpringApplication;
import org.openadaptor.util.ResourceUtil;
import org.springframework.beans.factory.ListableBeanFactory;

public class JBossSpringAdaptorTestCase extends TestCase {

  public void xtest() {
    String configFile = ResourceUtil.getResourcePath(this, "test/src/", "spring.xml");

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

  public void test2() {
    String configFile = ResourceUtil.getResourcePath(this, "test/src/", "spring2.xml");
    
    ListableBeanFactory publisherFactory = SpringApplication.getBeanFactory(configFile, null);
    final Adaptor publisher = (Adaptor) publisherFactory.getBean("Publisher");
    
    ListableBeanFactory subscriberFactory = SpringApplication.getBeanFactory(configFile, null);
    final Adaptor subscriber = (Adaptor) subscriberFactory.getBean("Subscriber");

    // create and start thread to stop subscriber after 5 secs
    Thread subscriberRunThread = new Thread("subscribe") {
      public void run() {
        subscriber.run();
      }
    };

    // create and start thread to stop subscriber after 5 secs
    Thread subscriberStopThread = new Thread("stop") {
      public void run() {
        try {
          Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
        }
        subscriber.stop();
      }
    };
    subscriberRunThread.start();
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e1) {
    }
    subscriberStopThread.start();

    // run publisher
    publisher.run();
    try {
      subscriberRunThread.join();
    } catch (InterruptedException e) {
    }
    
  }
}
