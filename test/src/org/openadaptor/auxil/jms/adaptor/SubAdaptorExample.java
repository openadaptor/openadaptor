package org.oa3.auxil.jms.adaptor;

import java.util.HashMap;
import java.util.Map;

import org.oa3.auxil.connector.iostream.writer.FileWriter;
import org.oa3.auxil.connector.iostream.writer.StreamWriteConnector;
import org.oa3.auxil.connector.jms.JMSConnection;
import org.oa3.auxil.connector.jms.JMSReadConnector;
import org.oa3.auxil.connector.jndi.JNDIConnection;
import org.oa3.core.adaptor.Adaptor;
import org.oa3.core.router.Router;
import org.oa3.core.router.RoutingMap;

public class SubAdaptorExample {

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

    // create read connector
    JMSReadConnector in = new JMSReadConnector("in");
    in.setJmsConnection(connection);

    // create test write connector
    StreamWriteConnector out = new StreamWriteConnector("out");
    out.setStreamWriter(new FileWriter());
    
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
