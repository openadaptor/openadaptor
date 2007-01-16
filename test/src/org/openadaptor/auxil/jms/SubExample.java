package org.openadaptor.auxil.jms;

import org.openadaptor.auxil.connector.jms.JMSReadConnector;
import org.openadaptor.auxil.connector.jms.JMSConnection;
import org.openadaptor.auxil.connector.jndi.JNDIConnection;

public class SubExample {

  public static void main(String[] args) {
    JNDIConnection jndiConnection = new JNDIConnection();
    jndiConnection.setInitialContextFactory("org.jnp.interfaces.NamingContextFactory");
    jndiConnection.setProviderUrl("jnp://localhost:1099");

    JMSConnection connection = new JMSConnection();
    connection.setConnectionFactoryName("ConnectionFactory");
    connection.setJndiConnection(jndiConnection);
    connection.setDestinationName("topic/testTopic");
    //connection.setTopic(true);

    JMSReadConnector inpoint = new JMSReadConnector();
    inpoint.setJmsConnection(connection);
    inpoint.connect();
    
    while (inpoint.isConnected()) {
      Object[] data = inpoint.next(1000);
      for (int i = 0; data != null && i < data.length; i++) {
        System.err.println(data[i]);
      }
    }
  }
}
