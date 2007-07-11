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

import java.util.HashMap;
import java.util.Map;

import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.router.Router;
import org.openadaptor.util.TestComponent;

import junit.framework.TestCase;


/**
 * Integration tests of exception handling in general.
 * Uses real adaptors, which are assembled 'by hand' (not using Spring).
 * 
 * @author Kris Lachor
 */
public class ExceptionHandlingTestCase extends TestCase {
  
  private Router router = new Router();
  
  private Map processMap = new HashMap();
  
  private Adaptor adaptor = new Adaptor();
  
  private TestComponent testComponent = new TestComponent();
  
  protected void setUp() throws Exception {
    adaptor.setMessageProcessor(router);
  }

  /**
   * Starts a simple adaptor that does not throw exceptions,
   * ensures the exceptionProcessor wasn't used.
   */
  public void testNoException(){
    processMap.put(testComponent.new TestReadConnector(), testComponent.new TestWriteConnector());
    router.setProcessMap(processMap);
    TestComponent.DummyExceptionHandler eHandler = new TestComponent.DummyExceptionHandler();
    assertTrue(eHandler.counter == 0);
    router.setExceptionProcessor(eHandler);
    adaptor.run();
    assertTrue(eHandler.counter == 0);
  }
  
  /**
   * Starts a simple adaptor with a write node that throws a RuntimeException,
   * ensures the exceptionProcessor was used one time.
   */
  public void testOneException(){
    processMap.put(testComponent.new TestReadConnector(), testComponent.new ExceptionThrowingWriteConnector());
    router.setProcessMap(processMap);
    TestComponent.DummyExceptionHandler eHandler = new TestComponent.DummyExceptionHandler();
    assertTrue(eHandler.counter == 0);
    router.setExceptionProcessor(eHandler);
    adaptor.run();
    assertTrue(eHandler.counter == 1);
  }
  
  /**
   * Starts a simple adaptor with a node that throws a RuntimeException.
   * The exceptionProcessor was badly written or assembled - itself it throws another Exception.
   * Ensures the exceptionProcessor was used one time, and that it was *not* used
   * to handle the exception it threw itself (would cause an endless catch - rethrow loop).
   */
  public void testNoEndlessExceptionThrowingLoop(){
    processMap.put(testComponent.new TestReadConnector(), testComponent.new ExceptionThrowingWriteConnector());
    router.setProcessMap(processMap);
    TestComponent.ExceptionThrowingExceptionHandler eHandler = new TestComponent.ExceptionThrowingExceptionHandler();
    assertTrue(eHandler.counter == 0);
    router.setExceptionProcessor(eHandler);
    adaptor.run();
    assertTrue(eHandler.counter == 1);
  }
  
  /**
   * Sets ExceptionHandlerProxy with a custom exception map as the exceptionProcessor.
   */
  public void testExceptionHandlerProxy(){
    processMap.put(testComponent.new TestReadConnector(), testComponent.new ExceptionThrowingWriteConnector());
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
   * Starts an adapter without exceptionProcessor. One of the nodes
   * throws an exception. This verifies the errorCode is non-zero and exception is 
   * available in the adapter. 
   */
  public void testNoExceptionProcessorWithOneReader(){
    processMap.put(testComponent.new TestReadConnector(), testComponent.new ExceptionThrowingWriteConnector());
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
   */
  public void testNoExceptionProcessorWithTwoReaders1(){
    processMap.put(testComponent.new TestReadConnector(), testComponent.new ExceptionThrowingWriteConnector());
    processMap.put(testComponent.new TestReadConnector(), testComponent.new ExceptionThrowingWriteConnector());
    router.setProcessMap(processMap);
    assertTrue(adaptor.getExitCode()==0);
    assertNotNull(adaptor.getExitErrors());
    assertTrue(adaptor.getExitErrors().isEmpty());
    adaptor.run();
    assertTrue(adaptor.getExitCode()==2);
    assertTrue(adaptor.getExitErrors().size()==2);
    for(int i=0; i<2; i++){
      assertTrue(adaptor.getExitErrors().get(i) instanceof RuntimeException);
      RuntimeException re = (RuntimeException) adaptor.getExitErrors().get(i);
      assertTrue(re.getMessage().indexOf(TestComponent.TEST_ERROR_MESSAGE) != -1);
    }
  }
  
  /**
   * Same as {@link #testNoExceptionProcessorWithOneReader()} but with two readers only
   * one of which deals with an exception. Expects one exception after the adapter completes.  
   */
  public void testNoExceptionProcessorWithTwoReaders2(){
    processMap.put(testComponent.new TestReadConnector(), testComponent.new ExceptionThrowingWriteConnector());
    processMap.put(testComponent.new TestReadConnector(), testComponent.new TestWriteConnector());
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

}
