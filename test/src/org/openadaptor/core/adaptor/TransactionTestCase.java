package org.oa3.core.adaptor;

import java.util.HashMap;
import java.util.Map;

import org.oa3.core.adaptor.Adaptor;
import org.oa3.core.connector.TestReadConnector;
import org.oa3.core.connector.TestWriteConnector;
import org.oa3.core.processor.TestProcessor;
import org.oa3.core.router.Router;
import org.oa3.core.router.RoutingMap;

import junit.framework.TestCase;

public class TransactionTestCase extends TestCase {

  /**
   * test that we get the correct number of commits on transactional resources
   *
   */
  public void testCommitAll() {

    TestReadConnector inpoint = new TestReadConnector("i");
    inpoint.setDataString("x");
    inpoint.setMaxSend(5);
    inpoint.setTransactional(true);
    inpoint.setExpectedCommitCount(5);
    
    TestProcessor processor = new TestProcessor("p");

    TestWriteConnector outpoint = new TestWriteConnector("o");
    outpoint.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", 5));
    outpoint.setTransacted(true);
    outpoint.setExpectedCommitCount(5);
   
    // create router
    RoutingMap routingMap = new RoutingMap();
    
    Map processMap = new HashMap();
    processMap.put(inpoint, processor);
    processMap.put(processor, outpoint);
    routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor = new Adaptor();
    adaptor.setMessageProcessor(router);
    adaptor.setRunInpointsInCallingThread(true);

    // run adaptor
    adaptor.run();
    
    assertTrue(adaptor.getExitCode() == 0);

  }

  /**
   * test that we get the correct number of commits on transactional resources
   *
   */
  public void testCommitAllWithBatch() {

    TestReadConnector inpoint = new TestReadConnector("i");
    inpoint.setDataString("x");
    inpoint.setMaxSend(5);
    inpoint.setBatchSize(2);
    inpoint.setTransactional(true);
    inpoint.setExpectedCommitCount(5);
    
    TestProcessor processor = new TestProcessor("p");

    TestWriteConnector outpoint = new TestWriteConnector("o");
    outpoint.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", 5));
    outpoint.setTransacted(true);
    outpoint.setExpectedCommitCount(5);
   
    // create router
    RoutingMap routingMap = new RoutingMap();
    
    Map processMap = new HashMap();
    processMap.put(inpoint, processor);
    processMap.put(processor, outpoint);
    routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor = new Adaptor();
    adaptor.setMessageProcessor(router);
    adaptor.setRunInpointsInCallingThread(true);

    // run adaptor
    adaptor.run();
    
    assertTrue(adaptor.getExitCode() == 0);

  }

  /**
   * test that write connector exceptions are trapped and routed correctly
   *
   */
  public void testCaughtOutpointException() {

    TestReadConnector inpoint = new TestReadConnector("i");
    inpoint.setDataString("x");
    inpoint.setMaxSend(5);
    inpoint.setTransactional(true);
    inpoint.setExpectedCommitCount(5);

    TestProcessor processor = new TestProcessor("p");

    TestWriteConnector outpoint = new TestWriteConnector("o");
    outpoint.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", 3));
    outpoint.setExceptionFrequency(2);
    outpoint.setTransacted(true);
    outpoint.setExpectedCommitCount(3);

    TestWriteConnector errorOutpoint = new TestWriteConnector("e");
    errorOutpoint.setExpectedOutput(AdaptorTestCase.createStringList("java.lang.RuntimeException:test:p(x)", 2));

    // create router
    RoutingMap routingMap = new RoutingMap();
    
    Map processMap = new HashMap();
    processMap.put(inpoint, processor);
    processMap.put(processor, outpoint);
    routingMap.setProcessMap(processMap);
    
    Map exceptionMap = new HashMap();
    exceptionMap.put("java.lang.Exception", errorOutpoint);
    routingMap.setExceptionMap(exceptionMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor = new Adaptor();
    adaptor.setMessageProcessor(router);
    adaptor.setRunInpointsInCallingThread(true);

    // run adaptor
    adaptor.run();
    
    assertTrue(adaptor.getExitCode() == 0);

  }

  /**
   * test that write connector exception causes adaptor to fail if there
   * is no exception mapping
   *
   */
  public void testUncaughtOutpointException() {

    TestReadConnector inpoint = new TestReadConnector("i");
    inpoint.setDataString("x");
    inpoint.setMaxSend(5);
    inpoint.setBatchSize(2);
    inpoint.setTransactional(true);
    inpoint.setExpectedCommitCount(5);

    TestProcessor processor = new TestProcessor("p");

    TestWriteConnector outpoint = new TestWriteConnector("o");
    outpoint.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", 3));
    outpoint.setExceptionFrequency(4);
    inpoint.setTransactional(true);
    inpoint.setExpectedCommitCount(2);

    // create router
    RoutingMap routingMap = new RoutingMap();
    
    Map processMap = new HashMap();
    processMap.put(inpoint, processor);
    processMap.put(processor, outpoint);
    routingMap.setProcessMap(processMap);
    
    Map exceptionMap = new HashMap();
    routingMap.setExceptionMap(exceptionMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor = new Adaptor();
    adaptor.setMessageProcessor(router);
    adaptor.setRunInpointsInCallingThread(true);

    // run adaptor
    adaptor.run();
    
    assertTrue(adaptor.getExitCode() != 0);
  }
  

}
