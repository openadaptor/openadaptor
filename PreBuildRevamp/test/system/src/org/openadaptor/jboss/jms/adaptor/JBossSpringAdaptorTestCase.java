package org.openadaptor.jboss.jms.adaptor;

import java.net.MalformedURLException;

import junit.framework.TestCase;

import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.util.ResourceUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.UrlResource;

public class JBossSpringAdaptorTestCase extends TestCase {

  public void xtest() throws BeansException, MalformedURLException {
    String configFile = "file:" + ResourceUtil.getResourcePath(this, "test/src/", "spring.xml");

    ListableBeanFactory factory = new XmlBeanFactory(new UrlResource(configFile));;

    // run publisher
    Adaptor publisher = (Adaptor) factory.getBean("Publisher");
    publisher.run();

    // create and start thread to stop subscriber after 5 secs
    final Adaptor subscriber = (Adaptor) factory.getBean("Subscriber");
    Thread t = new Thread() {
      public void run() {
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        subscriber.stop();
      }
    };
    t.start();

    // run subscriber
    subscriber.run();
  }

  public void test2() {
    // spring2.xml missing
    /*
    String configFile = ResourceUtil.getResourcePath(this, "test/system/src/", "spring2.xml");
    
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
    */
    
  }
}
