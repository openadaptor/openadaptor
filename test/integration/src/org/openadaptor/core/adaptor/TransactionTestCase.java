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

/**
 * Integration tests for {@link Transaction}. 
 */
public class TransactionTestCase extends TestCase {

  TestReadConnector readNode = new TestReadConnector("TestReader");
  
  TestProcessor processor = new TestProcessor("TestProcessor");
  
  TestWriteConnector writeNode = new TestWriteConnector("TestWriter");
  
  /**
   * Test that we get the correct number of commits on transactional resources.
   */
  public void testCommitAll() {

    readNode.setDataString("x");
    readNode.setMaxSend(5);
    readNode.setTransactional(true);
    readNode.setExpectedCommitCount(5);

    writeNode.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", 5));
    writeNode.setTransacted(true);
    writeNode.setExpectedCommitCount(5);
   
    Map processMap = new HashMap();
    processMap.put(readNode, processor);
    processMap.put(processor, writeNode);
    
    Adaptor adaptor = Adaptor.run(processMap);
    
    assertTrue(adaptor.getExitCode() == 0);
    writeNode.checkCommitCount();
  }

  /**
   * Test that we get the correct number of commits on transactional resources.
   */
  public void testCommitAllWithBatch() {

    readNode.setDataString("x");
    readNode.setMaxSend(5);
    readNode.setBatchSize(2);
    readNode.setTransactional(true);
    readNode.setExpectedCommitCount(5);

    writeNode.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", 5));
    writeNode.setTransacted(true);
    writeNode.setExpectedCommitCount(5);
    
    Map processMap = new HashMap();
    processMap.put(readNode, processor);
    processMap.put(processor, writeNode);
    
    Adaptor adaptor = Adaptor.run(processMap);
    
    assertTrue(adaptor.getExitCode() == 0);
    writeNode.checkCommitCount();
  }

  /**
   * test that write connector exceptions are trapped and routed correctly
   *
   */
  public void testCaughtWriteNodeException() {

    readNode.setDataString("x");
    readNode.setMaxSend(5);
    readNode.setTransactional(true);
    readNode.setExpectedCommitCount(5);

    writeNode.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", 3));
    
    /* Every second record will cause an exception */
    writeNode.setExceptionFrequency(2);
    writeNode.setTransacted(true);
    
    /* Three recordes should be committed, two rolled back */
    writeNode.setExpectedCommitCount(3);
    writeNode.setExpectedRollbackCount(2);

    TestWriteConnector errorWriteNode = new TestWriteConnector("e");
    errorWriteNode.setExpectedOutput(AdaptorTestCase.createStringList("java.lang.RuntimeException:test:p(x)", 2));
    
    Map processMap = new HashMap();
    processMap.put(readNode, processor);
    processMap.put(processor, writeNode);
    
    /* Run the adaptor */
    Adaptor adaptor = Adaptor.run(processMap, errorWriteNode);
    
    assertTrue(adaptor.getExitCode() == 0);
    writeNode.checkCommitCount();
    writeNode.checkRollbackCount();
  }

  /**
   * test that write connector exception causes adaptor to fail if there
   * is no exception mapping
   *
   * @todo revisit. A never-failing test.
   */
  public void testUncaughtWriteNodeException() {

    readNode.setDataString("x");
    readNode.setMaxSend(5);
    readNode.setBatchSize(2);
    readNode.setTransactional(true);
    readNode.setExpectedCommitCount(5);

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
