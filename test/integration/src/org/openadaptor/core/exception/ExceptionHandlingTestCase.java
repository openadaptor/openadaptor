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
import java.util.List;
import java.util.Map;

import org.openadaptor.core.Component;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.IWriteConnector;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.router.Router;

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
  
  protected void setUp() throws Exception {
    adaptor.setMessageProcessor(router);
  }

  /**
   * Starts a simple adaptor that does not throw exceptions,
   * ensures the exceptionProcessor wasn't used.
   */
  public void testNoException(){
    processMap.put(new TestReadConnector(), new TestWriteConnector());
    router.setProcessMap(processMap);
    DummyExceptionHandler eHandler = new DummyExceptionHandler();
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
    processMap.put(new TestReadConnector(), new ExceptionThrowingWriteConnector());
    router.setProcessMap(processMap);
    DummyExceptionHandler eHandler = new DummyExceptionHandler();
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
    processMap.put(new TestReadConnector(), new ExceptionThrowingWriteConnector());
    router.setProcessMap(processMap);
    ExceptionThrowingExceptionHandler eHandler = new ExceptionThrowingExceptionHandler();
    assertTrue(eHandler.counter == 0);
    router.setExceptionProcessor(eHandler);
    adaptor.run();
    assertTrue(eHandler.counter == 1);
  }
  
  /**
   * Sets ExceptionHandlerProxy with a custom exception map as the exceptionProcessor.
   *
   */
  public void testExceptionHandlerProxy(){
    processMap.put(new TestReadConnector(), new ExceptionThrowingWriteConnector());
    router.setProcessMap(processMap);
    ExceptionHandlerProxy exceptionHandlerProxy = new ExceptionHandlerProxy();
    DummyExceptionHandler handler1 = new DummyExceptionHandler();
    DummyExceptionHandler handler2 = new DummyExceptionHandler();
    Map exceptionMap = new HashMap();
    exceptionMap.put(Exception.class.getName(), handler1);
    exceptionMap.put(RuntimeException.class.getName(), handler2);
    exceptionHandlerProxy.setExceptionMap(exceptionMap);
    router.setExceptionProcessor(exceptionHandlerProxy);
    adaptor.run();
    assertTrue(handler1.counter == 0);
    assertTrue(handler2.counter == 1);
  }
  
  
  
  //
  // Helper test classes
  //
  
  /**
   * Simple write connector that throws an exception.
   */
  public final class ExceptionThrowingWriteConnector extends Component implements IWriteConnector {
    public void connect() {}
    public void disconnect() {}
    public Object deliver(Object[] data) {
       throw new RuntimeException();
    }
    public void validate(List exceptions) {}
  }
  
  /**
   * Simple write connector that throws an exception.
   */
  public static final class DummyExceptionHandler extends ExceptionHandlerProxy {
    int counter = 0;
    
    public Object[] process(Object data) {
      counter++;
      return super.process(data);
    } 
  }
  
  /**
   * Simple write connector that throws an exception.
   */
  public static final class ExceptionThrowingExceptionHandler extends ExceptionHandlerProxy {
    static int counter = 0;
    
    public Object[] process(Object data) {
      counter++;
      throw new RuntimeException("Test exception from the exception handler");
    }
  }
  
  /**
   * Simple read connector that returns one item of data then becomes dry.
   */
  public final class TestReadConnector implements IReadConnector {
    private boolean isDry = false;
    
    public void connect() {}
    public void disconnect() {}
    public Object getReaderContext() {return null;}
   
    public boolean isDry() { 
      boolean result = isDry;
      isDry = true;
      return result;
    }
   
    public Object[] next(long timeoutMs) { 
      return new String[]{"Dummy read connector test data"}; 
    }
    
    public void validate(List exceptions) {}
  }
  
  /**
   * Write connector that verifies the hospital data
   */
  public static final class TestWriteConnector extends Component implements IWriteConnector {
    public void connect() {}
    public void disconnect() {}
    public Object deliver(Object[] data) {
       if(data == null || data.length == 0){
         throw new RuntimeException("Hospital data empty");
       }
       for (int i = 0; i < data.length; i++) {
         System.out.println(data[i]);
       }
       return null;
    }
    public void validate(List exceptions) {}
  }

}
