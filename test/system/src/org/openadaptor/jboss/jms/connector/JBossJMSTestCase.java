package org.openadaptor.jboss.jms.connector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.openadaptor.auxil.connector.jms.JMSReadConnector;
import org.openadaptor.auxil.connector.jms.JMSWriteConnector;
import org.openadaptor.auxil.connector.jms.JMSConnection;
import org.openadaptor.auxil.connector.jndi.JNDIConnection;

/**
 * assumes standard JBOSS distro is runnning.
 * 
 * @author perryj
 *
 */
public class JBossJMSTestCase extends TestCase {

  private List inputs = new ArrayList();
  
  public JBossJMSTestCase() {
    inputs.add("larry");
    inputs.add("curly");
    inputs.add("mo");
  }
  
  public void testQueuePush() {
    try {
      JMSConnection connection = getConnection();
      connection.setDestinationName("queue/testQueue");
      //connection.setQueue(true);
      connection.setDurable(true);
      connection.setClientID("push");

      JMSWriteConnector connector = new JMSWriteConnector();
      connector.setJmsConnection(connection);
      connector.connect();
      connector.deliver((Object[]) inputs.toArray()); 
      for (Iterator iter = inputs.iterator(); iter.hasNext();) {
        String s = (String) iter.next();
        connector.deliver(new Object[] {s});
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testQueuePop() {
    List outputs = new ArrayList();
    try {
      JMSConnection connection = getConnection();
      connection.setDestinationName("queue/testQueue");
      //connection.setQueue(true);
      connection.setDurable(true);
      connection.setClientID("pop");

      JMSReadConnector connector = new JMSReadConnector();
      connector.setJmsConnection(connection);
      connector.connect();

      while (!connector.isDry() && outputs.size() < inputs.size()) {
        Object[] data = connector.next(1000);
        for (int i = 0; i < data.length; i++) {
          outputs.add(data[i]);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
    if (!outputs.equals(inputs)) {
      System.err.println("expected output...");
      for (Iterator iter = inputs.iterator(); iter.hasNext();) {
        System.err.println(iter.next());
        
      }
      System.err.println("actual output...");
      for (Iterator iter = outputs.iterator(); iter.hasNext();) {
        System.err.println(iter.next());
        
      }
      fail("output differs from expected output");
    }
    
  }

  public static JMSConnection getConnection() {
    JNDIConnection jndiConnection = new JNDIConnection();
    jndiConnection.setInitialContextFactory("org.jnp.interfaces.NamingContextFactory");
    jndiConnection.setProviderUrl("jnp://localhost:1099");

    JMSConnection connection = new JMSConnection();
    connection.setConnectionFactoryName("ConnectionFactory");
    connection.setJndiConnection(jndiConnection);
    return connection;
  }
}
