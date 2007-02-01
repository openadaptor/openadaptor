package org.openadaptor.core.adaptor;

import java.util.HashMap;
import java.util.Map;

import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.connector.TestReadConnector;
import org.openadaptor.core.connector.TestWriteConnector;
import org.openadaptor.core.processor.TestProcessor;
import org.openadaptor.core.router.Router;
import org.openadaptor.core.router.RoutingMap;

import junit.framework.TestCase;

public class TransactionTestCase extends TestCase {

  /**
   * test that we get the correct number of commits on transactional resources
   *
   */
  public void testCommitAll() {

    TestReadConnector readNode = new TestReadConnector("i");
    readNode.setDataString("x");
    readNode.setMaxSend(5);
    readNode.setTransactional(true);
    readNode.setExpectedCommitCount(5);
    
    TestProcessor processor = new TestProcessor("p");

    TestWriteConnector writeNode = new TestWriteConnector("o");
    writeNode.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", 5));
    writeNode.setTransacted(true);
    writeNode.setExpectedCommitCount(5);
   
    // create router
    RoutingMap routingMap = new RoutingMap();
    
    Map processMap = new HashMap();
    processMap.put(readNode, processor);
    processMap.put(processor, writeNode);
    routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor = new Adaptor();
    adaptor.setMessageProcessor(router);
    adaptor.setRunInCallingThread(true);

    // run adaptor
    adaptor.run();
    
    assertTrue(adaptor.getExitCode() == 0);

  }

  /**
   * test that we get the correct number of commits on transactional resources
   *
   */
  public void testCommitAllWithBatch() {

    TestReadConnector readNode = new TestReadConnector("i");
    readNode.setDataString("x");
    readNode.setMaxSend(5);
    readNode.setBatchSize(2);
    readNode.setTransactional(true);
    readNode.setExpectedCommitCount(5);
    
    TestProcessor processor = new TestProcessor("p");

    TestWriteConnector writeNode = new TestWriteConnector("o");
    writeNode.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", 5));
    writeNode.setTransacted(true);
    writeNode.setExpectedCommitCount(5);
   
    // create router
    RoutingMap routingMap = new RoutingMap();
    
    Map processMap = new HashMap();
    processMap.put(readNode, processor);
    processMap.put(processor, writeNode);
    routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor = new Adaptor();
    adaptor.setMessageProcessor(router);
    adaptor.setRunInCallingThread(true);

    // run adaptor
    adaptor.run();
    
    assertTrue(adaptor.getExitCode() == 0);

  }

  /**
   * test that write connector exceptions are trapped and routed correctly
   *
   */
  public void testCaughtWriteNodeException() {

    TestReadConnector readNode = new TestReadConnector("i");
    readNode.setDataString("x");
    readNode.setMaxSend(5);
    readNode.setTransactional(true);
    readNode.setExpectedCommitCount(5);

    TestProcessor processor = new TestProcessor("p");

    TestWriteConnector writeNode = new TestWriteConnector("o");
    writeNode.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", 3));
    writeNode.setExceptionFrequency(2);
    writeNode.setTransacted(true);
    writeNode.setExpectedCommitCount(3);

    TestWriteConnector errorWriteNode = new TestWriteConnector("e");
    errorWriteNode.setExpectedOutput(AdaptorTestCase.createStringList("java.lang.RuntimeException:test:p(x)", 2));

    // create router
    RoutingMap routingMap = new RoutingMap();
    
    Map processMap = new HashMap();
    processMap.put(readNode, processor);
    processMap.put(processor, writeNode);
    routingMap.setProcessMap(processMap);
    
    Map exceptionMap = new HashMap();
    exceptionMap.put("java.lang.Exception", errorWriteNode);
    routingMap.setExceptionMap(exceptionMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor = new Adaptor();
    adaptor.setMessageProcessor(router);
    adaptor.setRunInCallingThread(true);

    // run adaptor
    adaptor.run();
    
    assertTrue(adaptor.getExitCode() == 0);

  }

  /**
   * test that write connector exception causes adaptor to fail if there
   * is no exception mapping
   *
   */
  public void testUncaughtWriteNodeException() {

    TestReadConnector readNode = new TestReadConnector("i");
    readNode.setDataString("x");
    readNode.setMaxSend(5);
    readNode.setBatchSize(2);
    readNode.setTransactional(true);
    readNode.setExpectedCommitCount(5);

    TestProcessor processor = new TestProcessor("p");

    TestWriteConnector writeNode = new TestWriteConnector("o");
    writeNode.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", 3));
    writeNode.setExceptionFrequency(4);
    readNode.setTransactional(true);
    readNode.setExpectedCommitCount(2);

    // create router
    RoutingMap routingMap = new RoutingMap();
    
    Map processMap = new HashMap();
    processMap.put(readNode, processor);
    processMap.put(processor, writeNode);
    routingMap.setProcessMap(processMap);
    
    Map exceptionMap = new HashMap();
    routingMap.setExceptionMap(exceptionMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor = new Adaptor();
    adaptor.setMessageProcessor(router);
    adaptor.setRunInCallingThread(true);

    // run adaptor
    adaptor.run();
    
    assertTrue(adaptor.getExitCode() != 0);
  }
  

}
