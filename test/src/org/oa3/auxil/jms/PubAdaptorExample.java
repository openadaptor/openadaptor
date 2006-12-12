package org.oa3.auxil.jms;

import java.util.HashMap;
import java.util.Map;

import org.oa3.auxil.connector.jms.JMSConnection;
import org.oa3.auxil.connector.jms.JMSWriteConnector;
import org.oa3.auxil.connector.jndi.JNDIConnection;
import org.oa3.core.adaptor.Adaptor;
import org.oa3.core.connector.TestReadConnector;
import org.oa3.core.router.RoutingMap;

public class PubAdaptorExample {

  public static void main(String[] args) {
    
    // create jndi connection
    JNDIConnection jndiConnection = new JNDIConnection();
    jndiConnection.setInitialContextFactory("org.jnp.interfaces.NamingContextFactory");
    jndiConnection.setProviderUrl("jnp://localhost:1099");

    // create jms connection
    JMSConnection connection = new JMSConnection();
    connection.setConnectionFactoryName("ConnectionFactory");
    connection.setJndiConnection(jndiConnection);
    connection.setDestinationName("topic/testTopic");
    connection.setTopic(true);

    // create test data generator
    TestReadConnector in = new TestReadConnector("in");
    in.setMaxSend(100);
    in.setBatchSize(5);
    
    // create write connector
    JMSWriteConnector out = new JMSWriteConnector("out");
    out.setJmsConnection(connection);

    // create routing map
    RoutingMap routingMap = new RoutingMap();
    Map processMap = new HashMap();
    processMap.put(in, out);
    routingMap.setProcessMap(processMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setRoutingMap(routingMap);
    adaptor.setRunInpointsInCallingThread(true);
    
    // run adaptor
    adaptor.run();

  }
}
