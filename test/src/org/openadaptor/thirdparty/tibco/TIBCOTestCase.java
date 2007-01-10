package org.openadaptor.thirdparty.tibco;

import org.openadaptor.thirdparty.tibco.TibrvConnection;
import org.openadaptor.thirdparty.tibco.TibrvReadConnector;
import org.openadaptor.thirdparty.tibco.TibrvWriteConnector;

import junit.framework.TestCase;

public class TIBCOTestCase extends TestCase {

  public static void main(String[] args) {
    ((new TIBCOTestCase())).testListen();
  }
  
  public void testSend() {
    TibrvConnection connection = new TibrvConnection();
    TibrvWriteConnector connector = new TibrvWriteConnector();
    connector.setConnection(connection);
    connector.setSubject("test");
    connector.connect();
    connector.deliver(new Object[] {"hello mum"});
    connector.disconnect();
  }
  
  public void testListen() {
    TibrvConnection connection = new TibrvConnection();
    TibrvReadConnector connector = new TibrvReadConnector();
    connector.setConnection(connection);
    connector.setTopic("test");
    connector.connect();
    runDelayedSendThread();
    Object[] data = connector.next(5 * 1000);
    assertTrue(data.length == 1);
    assertTrue(data[0].equals("hello mum"));
    connector.disconnect();
  }
  
  private void runDelayedSendThread() {
    Thread t = new Thread() {
      public void run() {
        try {
          Thread.sleep(2 * 1000);
        } catch (InterruptedException e) {
        }
        testSend();
      }
    };
    t.start();
  }
}
