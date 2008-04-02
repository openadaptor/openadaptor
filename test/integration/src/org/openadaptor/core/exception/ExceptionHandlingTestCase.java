/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in the
 Software without restriction, including without limitation the rights to use, copy,
 modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 and to permit persons to whom the Software is furnished to do so, subject to the
 following conditions:

 The above copyright notice and this permission notice shall be included in all 
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Nothing in this notice shall be deemed to grant any rights to trademarks, copyrights,
 patents, trade secrets or any other intellectual property of the licensor or any
 contributor except as expressly stated herein. No patent license is granted separate
 from the Software, for code that you delete from the Software, or for combinations
 of the Software with other software or hardware.
 */

package org.openadaptor.core.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.IWriteConnector;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.node.WriteNode;
import org.openadaptor.core.processor.ProcessorGroup;
import org.openadaptor.core.router.Router;
import org.openadaptor.util.TestComponent;

import junit.framework.TestCase;

/**
 * Integration tests of {@link ExceptionHandlerProxy} and exception handling in general.
 * Runs real adaptors which are assembled 'by hand' (not using Spring). Runs adaptors
 * in various exception handling config scenarios - with and without exceptionProcessor
 * set up, adaptors with different types and numbers of nodes throwing exceptions.
 * Tests different types of exceptionProcessors.
 * 
 * @author Kris Lachor
 */
public class ExceptionHandlingTestCase extends TestCase {
  
  private Router router = new Router();
  
  private Map processMap = new HashMap();
  
  private Adaptor adaptor = new Adaptor();
 
  protected void setUp() throws Exception {
    adaptor.setMessageProcessor(router);
  }

  /**
   * Starts a simple adaptor that does not throw exceptions,
   * ensures there were no calls to the exceptionProcessor.
   */
  public void testNoException(){
    processMap.put(new TestComponent.TestReadConnector(), new TestComponent.TestWriteConnector());
    router.setProcessMap(processMap);
    TestComponent.DummyExceptionHandler eHandler = new TestComponent.DummyExceptionHandler();
    assertTrue(eHandler.counter == 0);
    router.setExceptionProcessor(eHandler);
    adaptor.run();
    assertTrue(eHandler.counter == 0);
  }

//  Test commented out, pending a fix in ReadNode and enabling exception handling for read
//  connectors.
//  
//  22/2/08 KLA - ReadNodes nodes are unlikely to be included in the exception handling. 
//  Typical exception is ConnectionException not related to any data and very likely to re-occur.
//  
//  /**
//   * Starts a simple adaptor with a read connector node that throws a RuntimeException.
//   * Ensures the exceptionProcessor {@link IExceptionHandler} was used one time.
//   */
//  public void testOneExceptionFromReadConnector(){
//    processMap.put(new TestComponent.ExceptionThrowingReadConnector(), new TestComponent.TestWriteConnector());
//    checkOneException();
//  }
  
  /**
   * Starts a simple adaptor with a data processor node that throws a RuntimeException.
   * Ensures the exceptionProcessor {@link IExceptionHandler} was used one time.
   */
  public void testOneExceptionFromDataProcessor(){
    processMap.put(new TestComponent.TestReadConnector(), new TestComponent.ExceptionThrowingDataProcessor());
    checkOneException();
  }
  
  /**
   * Starts a simple adaptor with two parallel data processors, one of which throws a RuntimeException.
   * Ensures the exceptionProcessor {@link IExceptionHandler} was used one time.
   * Ensures the writer did write one message (which came from the 'good' data processor).
   */
  public void testOneExceptionFromDataProcessorWithFanout(){
    IReadConnector reader = new TestComponent.TestReadConnector();
    IDataProcessor processor1 = new TestComponent.ExceptionThrowingDataProcessor();
    IDataProcessor processor2 = new TestComponent.DummyDataProcessor();
    TestComponent.TestWriteConnector writer = new TestComponent.TestWriteConnector();
    List processors = new ArrayList();
    processors.add(processor1);
    processors.add(processor2);
    processMap.put(reader, processors);
    processMap.put(processor1, writer);
    processMap.put(processor2, writer);
    checkOneException();
    assertTrue(writer.counter==1);
  }
  
  /**
   * Starts a simple adaptor with a write node that throws a RuntimeException.
   * Ensures the exceptionProcessor {@link IExceptionHandler} was used one time.
   */
  public void testOneExceptionFromWriteConnector(){
    processMap.put(new TestComponent.TestReadConnector(), new TestComponent.ExceptionThrowingWriteConnector());
    checkOneException();
  }
  
  private void checkOneException(){
    router.setProcessMap(processMap);
    TestComponent.DummyExceptionHandler eHandler = new TestComponent.DummyExceptionHandler();
    assertTrue(eHandler.counter == 0);
    router.setExceptionProcessor(eHandler);
    adaptor.setRunInCallingThread(true);
    adaptor.run();
    assertTrue(eHandler.counter == 1);
  }
  
  /**
   * Starts a simple adaptor with a write node that throws a RuntimeException.
   * Ensures the exceptionProcessor {@link IDataProcessor + IWriteConnector} was used one time.
   */
  public void testOneExceptionFromWriteConnector2(){
    /* normal flow */
    processMap.put(new TestComponent.TestReadConnector(), new TestComponent.ExceptionThrowingWriteConnector());
    /* exception handling flow */
    TestComponent.DummyDataProcessor eHandler = new TestComponent.DummyDataProcessor();
    processMap.put(eHandler, new TestComponent.TestWriteConnector());
    router.setProcessMap(processMap);
    assertTrue(eHandler.counter == 0);
    router.setExceptionProcessor(eHandler);
    adaptor.setRunInCallingThread(true);
    adaptor.run();
    assertTrue(eHandler.counter == 1);
  }

  /**
   * Starts a simple adaptor with a node that throws a RuntimeException.
   * The exceptionProcessor (perhaps clumsily written or assembled) throws another Exception itself.
   * The test ensures the exceptionProcessor  {@link IExceptionHandler} was used one time, and that it 
   * was *not* used to handle the exception it threw itself (would cause an endless catch - rethrow loop).
   */
  public void testNoEndlessExceptionThrowingLoop(){
    processMap.put(new TestComponent.TestReadConnector(), new TestComponent.ExceptionThrowingWriteConnector());
    router.setProcessMap(processMap);
    ExceptionThrowingExceptionHandler eHandler = new ExceptionThrowingExceptionHandler();
    assertTrue(eHandler.counter == 0);
    router.setExceptionProcessor(eHandler);
    adaptor.run();
    assertTrue(eHandler.counter == 1);
    assertTrue(adaptor.getExitCode()==1);
  }
  
  /**
   * Same as testNoEndlessExceptionThrowingLoop but uses {@link IWriteConnector} for the exception
   * handler.
   */
  public void testNoEndlessExceptionThrowingLoop2(){
    processMap.put(new TestComponent.TestReadConnector(), new TestComponent.ExceptionThrowingWriteConnector());
    router.setProcessMap(processMap);
    TestComponent.ExceptionThrowingWriteConnector eHandler = new TestComponent.ExceptionThrowingWriteConnector();
    assertTrue(eHandler.counter == 0);
    router.setExceptionProcessor(eHandler);
    adaptor.run();
    assertTrue(eHandler.counter == 1);
    assertTrue(adaptor.getExitCode()==1);
  }
   
  /**
   * Same as testNoEndlessExceptionThrowingLoop but uses an the exception
   * handler made up of multiple rather than a single node. Uses {@link IWriteConnector} for the exception
   * handler.
   */
  public void testNoEndlessExceptionThrowingLoop3(){
    TestComponent.DummyExceptionHandler eHandler = new TestComponent.DummyExceptionHandler();
    /* normal flow */
    processMap.put(new TestComponent.TestReadConnector(), new TestComponent.ExceptionThrowingWriteConnector());
    /* exception handling flow */
    processMap.put(eHandler, new TestComponent.ExceptionThrowingWriteConnector());
    
    router.setProcessMap(processMap);
    assertTrue(eHandler.counter == 0);
    router.setExceptionProcessor(eHandler);
    adaptor.run();
    assertTrue(eHandler.counter == 1);
    assertTrue(adaptor.getExitCode()==1);
  }
  
  
  /**
   * Same as testNoEndlessExceptionThrowingLoop2 - uses {@link IWriteConnector} for the exception
   * handler. It has a new ignoreExceptionProcessorErrors flag set to true. Exceptions from the exception 
   * handler should be logged only - they should not cause the adaptor to shut down (adaptor's exit error code
   * should be 0).
   */
  public void testNoEndlessExceptionThrowingLoop4(){
    processMap.put(new TestComponent.TestReadConnector(), new TestComponent.ExceptionThrowingWriteConnector());
    router.setProcessMap(processMap);
    TestComponent.ExceptionThrowingWriteConnector eHandler = new TestComponent.ExceptionThrowingWriteConnector();
    assertTrue(eHandler.counter == 0);
    router.setExceptionProcessor(eHandler);
    router.setIgnoreExceptionProcessorErrors(true);
    adaptor.run();
    assertTrue(eHandler.counter == 1);
    assertTrue(adaptor.getExitCode()==0);
  }
  
  /**
   * Sets {@link ExceptionHandlerProxy} with a custom exception map as the exceptionProcessor.
   */
  public void testExceptionHandlerProxy(){
    processMap.put(new TestComponent.TestReadConnector(), new TestComponent.ExceptionThrowingWriteConnector());
    router.setProcessMap(processMap);
    ExceptionHandlerProxy exceptionHandlerProxy = new ExceptionHandlerProxy();
    TestComponent.DummyExceptionHandler handler1 = new TestComponent.DummyExceptionHandler();
    TestComponent.DummyExceptionHandler handler2 = new TestComponent.DummyExceptionHandler();
    Map exceptionMap = new HashMap();
    exceptionMap.put(Exception.class.getName(), handler1);
    exceptionMap.put(RuntimeException.class.getName(), handler2);
    exceptionHandlerProxy.setExceptionMap(exceptionMap);
    router.setExceptionProcessor(exceptionHandlerProxy);
    adaptor.run();
    assertTrue(handler1.counter == 0);
    assertTrue(handler2.counter == 1);
  }
  
  /**
   * Starts an adapter with no exceptionProcessor set. One of the nodes
   * throws an exception. This verifies the errorCode is non-zero and exception is 
   * available in the adapter. 
   */
  public void testNoExceptionProcessorWithOneReader(){
    processMap.put(new TestComponent.TestReadConnector(), new TestComponent.ExceptionThrowingWriteConnector());
    runAdaptorWithNoExceptionProcessor();
  }
  
  public void testNoExceptionProcessorWithOneReader2(){
    processMap.put(new TestComponent.TestReadConnector(), new TestComponent.ExceptionThrowingWriteConnector2());
    runAdaptorWithNoExceptionProcessor();
  }
  
  /**
   * Tests WriteNode#suppressDisconnectionErrors(false) property.
   * Set to 'false' it prevents WriteNode from catching exceptions at connector's 
   * disconnection phase.
   */
  public void testNoExceptionProcessorWithOneReader3(){
    WriteNode writeNode = new WriteNode();
    writeNode.setSuppressDisconnectionErrors(false);
    writeNode.setConnector( new TestComponent.ExceptionThrowingWriteConnector3());
    processMap.put(new TestComponent.TestReadConnector(),writeNode);
    runAdaptorWithNoExceptionProcessor();
  }
  
  private void runAdaptorWithNoExceptionProcessor(){
    router.setProcessMap(processMap);
    assertTrue(adaptor.getExitCode()==0);
    assertNotNull(adaptor.getExitErrors());
    assertTrue(adaptor.getExitErrors().isEmpty());
    adaptor.run();
    assertTrue(adaptor.getExitCode()==1);
    assertTrue(adaptor.getExitErrors().size()==1);
    assertTrue(adaptor.getExitErrors().get(0) instanceof RuntimeException);
    RuntimeException re = (RuntimeException) adaptor.getExitErrors().get(0);
    assertTrue(re.getMessage().indexOf(TestComponent.TEST_ERROR_MESSAGE) != -1);
  }
  
  /**
   * Same as {@link #testNoExceptionProcessorWithOneReader()} but with two readers -
   * expects two exceptions after the adapter completes.   
   * 
   * @todo sometimes fails with the adaptor returning exitCode==1, need to investigate.
   */
//  public void testNoExceptionProcessorWithTwoReaders1() throws Exception {
//    processMap.put(new TestComponent.TestReadConnector(), testComponent.new ExceptionThrowingWriteConnector());
//    processMap.put(new TestComponent.TestReadConnector(), testComponent.new ExceptionThrowingWriteConnector());
//    router.setProcessMap(processMap);
//    assertTrue(adaptor.getExitCode()==0);
//    assertNotNull(adaptor.getExitErrors());
//    assertTrue(adaptor.getExitErrors().isEmpty());
//    adaptor.run();
//    assertTrue(adaptor.getExitCode()==2);
//    assertTrue(adaptor.getExitErrors().size()==2);
//    for(int i=0; i<2; i++){
//      assertTrue(adaptor.getExitErrors().get(i) instanceof RuntimeException);
//      RuntimeException re = (RuntimeException) adaptor.getExitErrors().get(i);
//      assertTrue(re.getMessage().indexOf(TestComponent.TEST_ERROR_MESSAGE) != -1);
//    }
//  }
 
  /**
   * Same as {@link #testNoExceptionProcessorWithOneReader()} but with two readers only
   * one of which deals with an exception. Expects one exception after the adapter completes.  
   */
  public void testNoExceptionProcessorWithTwoReaders2(){
    processMap.put(new TestComponent.TestReadConnector(), new TestComponent.ExceptionThrowingWriteConnector());
    processMap.put(new TestComponent.TestReadConnector(), new TestComponent.TestWriteConnector());
    router.setProcessMap(processMap);
    assertTrue(adaptor.getExitCode()==0);
    assertNotNull(adaptor.getExitErrors());
    assertTrue(adaptor.getExitErrors().isEmpty());
    adaptor.run();
    assertTrue(adaptor.getExitCode()==1);
    assertTrue(adaptor.getExitErrors().size()==1);
    assertTrue(adaptor.getExitErrors().get(0) instanceof RuntimeException);
    RuntimeException re = (RuntimeException) adaptor.getExitErrors().get(0);
    assertTrue(re.getMessage().indexOf(TestComponent.TEST_ERROR_MESSAGE) != -1);
  }
  
  /**
   * Tests exception handling when a ProcessorGroup is involved. A message that enters
   * a processor group should be logically treated as one message.
   * 
   * Here the ProcessorGroup is made up of a processor that turns one messages into 
   * multiple messages, followed by a processor that throws an exception. The exception 
   * handler is set up. The exception handler should receive the original message that
   * enters the processor group, although the actual exception happens futher down 
   * the stream at a sub-message level.
   */
  public void testExceptionFromProcessorGroup(){
    ProcessorGroup processorGroup = new ProcessorGroup();
    IDataProcessor [] processors = new IDataProcessor[2];
    processors[0] = new TestComponent.MultipleOutputsDataProcessor();
    processors[1] = new TestComponent.ExceptionThrowingDataProcessor();
    processorGroup.setProcessors(processors);
    TestComponent.TestWriteConnector writeConnector = new TestComponent.TestWriteConnector();
    processMap.put(new TestComponent.TestReadConnector(), processorGroup);
    processMap.put(processorGroup, writeConnector);
    router.setProcessMap(processMap);
    
    TestComponent.DummyExceptionHandler eHandler = new TestComponent.DummyExceptionHandler();
    assertTrue(eHandler.counter == 0);
    router.setExceptionProcessor(eHandler);
    
    adaptor.run();
    assertTrue(eHandler.counter == 1);
    assertTrue(writeConnector.counter == 0);
  }

  /**
   * An exception handler that throws an exception itself. Has a calls counter.
   */
  public static final class ExceptionThrowingExceptionHandler extends ExceptionHandlerProxy {
    public int counter = 0;
    
    public Object[] process(Object data) {
      counter++;
      throw new RuntimeException("Test exception from the exception handler");
    }
  }
}



