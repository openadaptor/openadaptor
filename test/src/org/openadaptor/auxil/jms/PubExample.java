package org.openadaptor.auxil.jms;

import org.openadaptor.auxil.connector.jms.JMSConnection;
import org.openadaptor.auxil.connector.jms.JMSWriteConnector;
import org.openadaptor.auxil.connector.jndi.JNDIConnection;

public class PubExample {

  public static void main(String[] args) {
    JNDIConnection jndiConnection = new JNDIConnection();
    jndiConnection.setInitialContextFactory("org.jnp.interfaces.NamingContextFactory");
    jndiConnection.setProviderUrl("jnp://localhost:1099");

    JMSConnection connection = new JMSConnection();
    connection.setConnectionFactoryName("ConnectionFactory");
    connection.setJndiConnection(jndiConnection);
    connection.setDestinationName("topic/testTopic");
    connection.setTopic(true);

    JMSWriteConnector outpoint = new JMSWriteConnector();
    outpoint.setJmsConnection(connection);
    outpoint.connect();
    outpoint.deliver(new Object[] {"one", "two", "three"});
  }
}
