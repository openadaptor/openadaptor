package org.oa3.core.connector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.oa3.auxil.connector.QueuingReadConnector;
import org.oa3.core.adaptor.Adaptor;
import org.oa3.core.router.Router;
import org.oa3.core.router.RoutingMap;

public class QueuedReadConnectorTestCase extends TestCase {

  public void test() {
    Object[] testData = new Object[] {"foo", "bar", "foobar"};
    
    // create inpoint
    MyTestReadConnector inpoint = new MyTestReadConnector("in", testData);
    
    // create outpoint
    TestWriteConnector outpoint = new TestWriteConnector("out");
    ArrayList list = new ArrayList();
    for (int i = 0; i < testData.length; i++) {
      list.add(testData[i]);
    }
    outpoint.setExpectedOutput(list);
    
    // create router
    RoutingMap routingMap = new RoutingMap();
    Map processMap = new HashMap();
    processMap.put(inpoint, outpoint);
    routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
    adaptor.setRunInpointsInCallingThread(true);
    
    // run adaptor
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);
  }
  
  public void testTransactedNoProblems() {
    Object[] testData = new Object[] {"foo", "bar", "foobar"};
    
    // create inpoint
    MyTestReadConnector inpoint = new MyTestReadConnector("in", testData);
    inpoint.setTransacted(true);
    
    // create outpoint
    TestWriteConnector outpoint = new TestWriteConnector("out");
    ArrayList list = new ArrayList();
    for (int i = 0; i < testData.length; i++) {
      list.add(testData[i]);
    }
    outpoint.setExpectedOutput(list);
    
    // create router
    RoutingMap routingMap = new RoutingMap();
    Map processMap = new HashMap();
    processMap.put(inpoint, outpoint);
    routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
    
    // run adaptor
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);
  }
  
  public class MyTestReadConnector extends QueuingReadConnector {

    private Object[] data;
    private Thread thread;
    
    public MyTestReadConnector(final String id, final Object[] data) {
      super(id);
      this.data = data;
    }
    
    public void connect() {
      thread = new Thread("test") {
        public void run() {
          for (int i = 0; i < data.length; i++) {
            enqueue(data[i]);
          }
          data = null;
        }
      };
      thread.start();
    }

    public boolean isDry() {
      return data == null && queueIsEmpty();
    }
    
    public void disconnect() {
      try {
        thread.join();
      } catch (InterruptedException e) {
      }
    }
    
  }
}
