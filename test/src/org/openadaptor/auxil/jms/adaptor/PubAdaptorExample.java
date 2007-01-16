package org.openadaptor.auxil.jms.adaptor;

import java.util.HashMap;
import java.util.Map;

import org.openadaptor.auxil.connector.jms.JMSWriteConnector;
import org.openadaptor.auxil.connector.jms.JMSConnection;
import org.openadaptor.auxil.connector.jndi.JNDIConnection;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.connector.TestReadConnector;
import org.openadaptor.core.router.Router;
import org.openadaptor.core.router.RoutingMap;

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
    //connection.setTopic(true);

    // create test data generator
    TestReadConnector in = new TestReadConnector("in");
    in.setMaxSend(100);
    in.setBatchSize(5);
    
    // create write connector
    JMSWriteConnector out = new JMSWriteConnector("out");
    out.setJmsConnection(connection);

    // create router
    RoutingMap routingMap = new RoutingMap();
    Map processMap = new HashMap();
    processMap.put(in, out);
    routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
    adaptor.setRunInpointsInCallingThread(true);
    
    // run adaptor
    adaptor.run();

  }
}
