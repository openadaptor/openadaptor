package org.openadaptor.core.connector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.connector.QueuingReadConnector;
import org.openadaptor.core.processor.TestProcessor;
import org.openadaptor.core.router.Router;
import org.openadaptor.core.router.RoutingMap;

public class SocketQueuedReadConnectorTestCase extends TestCase {
	public static final long DEFAULT_PROCESS_DELAY_MS=300;
	
    //Somewhere to store pseudoCommit responses
    private ArrayList commitList = new ArrayList();

  public void testQueuing() {
    Object[] testData = new Object[] {"foo", "bar", "foobar"};
    
    // create readNode
    MyTestReadConnector readNode = new MyTestReadConnector("in", testData);
    readNode.setTransacted(false);
    
    // create writeNode
    TestWriteConnector writeNode = new TestWriteConnector("out");
    ArrayList list = new ArrayList();
    for (int i = 0; i < testData.length; i++) {
      list.add(testData[i]);
    }
    writeNode.setExpectedOutput(list);
    
    // create router
    RoutingMap routingMap = new RoutingMap();
    Map processMap = new HashMap();
    processMap.put(readNode, writeNode);
    routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
    adaptor.setRunInCallingThread(true);
    
    // run adaptor
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);
  }
  
  public void testPseudoCommit() {
	    Object[] testData = new Object[] {"foo", "bar", "foobar"};
	    
	    // create readNode
	    MyTestReadConnector readNode = new MyTestReadConnector("in", testData);
	    readNode.setTransacted(false);
	    readNode.setPseudoTransaction(true);
	    
	    commitList.clear();
	    
	    // create writeNode
	    TestWriteConnector writeNode = new TestWriteConnector("out");
	        
	    ArrayList list = new ArrayList();
	    for (int i = 0; i < testData.length; i++) {
	      list.add(testData[i]);
	    }
	    writeNode.setExpectedOutput(list);
	    
	    // create router
	    RoutingMap routingMap = new RoutingMap();
	    Map processMap = new HashMap();
	    processMap.put(readNode, writeNode);
	    routingMap.setProcessMap(processMap);
	    Router router = new Router(routingMap);
	    
	    // create adaptor
	    Adaptor adaptor =  new Adaptor();
	    adaptor.setMessageProcessor(router);
	    adaptor.setRunInCallingThread(true);
	    
	    // run adaptor
	    adaptor.run();
	    assertTrue(adaptor.getExitCode() == 0);
	    assertTrue(commitList.size() == 3);
	  }
  
  public void testTransactedPseudoCommit() {
	    Object[] testData = new Object[] {"foo", "bar", "foobar"};
	    
	    // create readNode
	    MyTestReadConnector readNode = new MyTestReadConnector("in", testData);
	    readNode.setTransacted(true);
	    readNode.setPseudoTransaction(true);
	    
	    commitList.clear();
	    
	    // create writeNode
	    TestWriteConnector writeNode = new TestWriteConnector("out");
	        
	    ArrayList list = new ArrayList();
	    for (int i = 0; i < testData.length; i++) {
	      list.add(testData[i]);
	    }
	    writeNode.setExpectedOutput(list);
	    
	    // create router
	    RoutingMap routingMap = new RoutingMap();
	    Map processMap = new HashMap();
	    processMap.put(readNode, writeNode);
	    routingMap.setProcessMap(processMap);
	    Router router = new Router(routingMap);
	    
	    // create adaptor
	    Adaptor adaptor =  new Adaptor();
	    adaptor.setMessageProcessor(router);
	    adaptor.setRunInCallingThread(true);
	    
	    // run adaptor
	    adaptor.run();
	    assertTrue(adaptor.getExitCode() == 0);
	    assertTrue(commitList.size() == 3);
	  }
  
  public void testQueueLimit() {
    Object[] testData = new Object[] {"foo", "bar", "foobar"};
    
    // create readNode
    MyTestReadConnector readNode = new MyTestReadConnector("in", testData);
    readNode.setTransacted(false);
    readNode.setQueueLimit(1);
    
    // create processor that introduces delay
    MySlowProcessor processor = new MySlowProcessor(DEFAULT_PROCESS_DELAY_MS);
    
    // create writeNode
    TestWriteConnector writeNode = new TestWriteConnector("out");
    ArrayList list = new ArrayList();
    for (int i = 0; i < testData.length; i++) {
      list.add(testData[i]);
    }
    writeNode.setExpectedOutput(list);
    
    // create router
    RoutingMap routingMap = new RoutingMap();
    Map processMap = new HashMap();
    processMap.put(readNode, processor);
    processMap.put(processor, writeNode);
    routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
    
    // run adaptor
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);
  }
  
  public void testQueueLimitNonBlocking() {
    Object[] testData = new Object[] {"foo", "bar", "foobar"};
    
    // create readNode
    MyTestReadConnector readNode = new MyTestReadConnector("in", testData);
    readNode.setQueueLimit(1);
    readNode.setBlockOnQueue(false);
    readNode.setTransacted(false);
    
    // create processor that introduces delay
    MySlowProcessor processor = new MySlowProcessor(DEFAULT_PROCESS_DELAY_MS);
    
    // create router
    RoutingMap routingMap = new RoutingMap();
    Map processMap = new HashMap();
    processMap.put(readNode, processor);
    routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
    
    // run adaptor
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);
  }
  
  public void testTransactedNoProblems() {
    Object[] testData = new Object[] {"foo", "bar", "foobar"};
    
    // create readNode
    MyTestReadConnector readNode = new MyTestReadConnector("in", testData);
    readNode.setTransacted(true);
    
    // create writeNode
    TestWriteConnector writeNode = new TestWriteConnector("out");
    ArrayList list = new ArrayList();
    for (int i = 0; i < testData.length; i++) {
      list.add(testData[i]);
    }
    writeNode.setExpectedOutput(list);
    
    // create router
    RoutingMap routingMap = new RoutingMap();
    Map processMap = new HashMap();
    processMap.put(readNode, writeNode);
    routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
    
    // run adaptor
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);
  }
  
  public void testTransactedWithProblems() {
    Object[] testData = new Object[] {"foo", "bar", "foobar"};
    
    // create readNode
    MyTestReadConnector readNode = new MyTestReadConnector("i", testData);
    readNode.setTransacted(true);
    
    // create processor that throws exception
    TestProcessor processor = new TestProcessor("p");
    processor.setExceptionFrequency(3);
    
    // create writeNode
    TestWriteConnector writeNode = new TestWriteConnector("o");
    ArrayList list = new ArrayList();
    for (int i = 0; i < testData.length; i++) {
      if (((i+1) % 3) != 0) {
        list.add("p(" + testData[i] + ")");
      }
    }
    writeNode.setExpectedOutput(list);
    writeNode.setTransacted(true);
    writeNode.setExpectedCommitCount(2);
    
    // create router
    RoutingMap routingMap = new RoutingMap();
    Map processMap = new HashMap();
    processMap.put(readNode, processor);
    processMap.put(processor, writeNode);
    routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
    
    // run adaptor
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 1);
  }
  
  public class MyTestReadConnector extends SocketQueuingReadConnector {

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

    public void validate(List exceptions) {
    }
    
    
    protected void sendCommitRollback(String string) {
  	  commitList.add(string);
    }
    
  }
  
  class MySlowProcessor implements IDataProcessor {
  	long processDelayMS=1000;
  	
  	public MySlowProcessor(long processDelayMS) {
  		this.processDelayMS=processDelayMS;
  	}

    public Object[] process(Object data) {
      try {
        Thread.sleep(processDelayMS);
      } catch (InterruptedException e) {
      }
      return new Object[] {data};
    }

    public void reset(Object context) {
    }

    public void validate(List exceptions) {
    }
    
  }

}
