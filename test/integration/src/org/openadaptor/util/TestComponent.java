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

package org.openadaptor.util;

import java.util.ArrayList;
import java.util.List;

import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.Component;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.IEnrichmentReadConnector;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.IWriteConnector;
import org.openadaptor.core.exception.ExceptionHandlerProxy;

/**
 * Utility class provides several components - connectors, processors, for the use
 * in integration/system tests. 
 * 
 * Test components are defined as *static* inner so that they can be easily instantiated via 
 * Spring, where bean names are binary class names, e.g.:
 * org.openadaptor.util.TestComponent$ExceptionThrowingDataProcessor
 * 
 * @author Kris Lachor
 */
public class TestComponent {
  
  public static String TEST_ERROR_MESSAGE = "THIS IS A TEST ERROR/EXCEPTION AND SHOULD BE IGNORED.";
  
  //
  // IReadConnectors
  //
  //
  
  /**
   * A read connector that returns one item of data (a String) then becomes dry.
   */
  public static class TestReadConnector implements IReadConnector {
    private boolean isDry = false;
    
    public void connect() {}
    public void disconnect() {}
    public Object getReaderContext() {return null;}
    public void setReaderContext(Object context) {}
   
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
   * An enrichment read connector based on TestReadConnector.
   */
  public static class TestEnrichmentReadConnector extends TestReadConnector implements IEnrichmentReadConnector{
    public Object[] next(IOrderedMap inputParameters, long timeout) {
      return next(timeout);
    }
  }
  
  
  /**
   * A write connector that throws a RuntimeException whenever it receives 
   * data to write.
   */
  public static final class ExceptionThrowingReadConnector extends TestReadConnector {  
    public Object[] next(long timeoutMs) { 
      throw new RuntimeException(TEST_ERROR_MESSAGE);
    }
  }
  
  /**
   * A read connector that returns one item of data (an OrdredMap) then becomes dry.
   */
  public static class TestReadConnectorOM implements IReadConnector {
    private boolean isDry = false;
    
    public void connect() {}
    public void disconnect() {}
    public Object getReaderContext() {return null;}
    public void setReaderContext(Object context) {}
   
    public boolean isDry() { 
      boolean result = isDry;
      isDry = true;
      return result;
    }
   
    public Object[] next(long timeoutMs) { 
      IOrderedMap map = new OrderedHashMap();
      map.put("element1", "2");
      return new Object[]{map}; 
    }
    
    public void validate(List exceptions) {}
  }
  
  
  
  //
  // IDataProcessors
  //
  //
  
  /**
   * A dummy data processor.
   */
  public static final class DummyDataProcessor extends Component implements IDataProcessor {
    public int counter = 0;
    
    public Object[] process(Object data) {
      counter++;
      return new Object[]{data};
    }
    
    public void validate(List exceptions) {}
    public void reset(Object context) {}
  }
  
  /**
   * A data processor that throws a RuntimeException whenever it receives 
   * data to write.
   */
  public static final class ExceptionThrowingDataProcessor extends Component implements IDataProcessor {
    public int counter = 0;
    
    public Object[] process(Object data) {
      counter++;
      throw new RuntimeException(TEST_ERROR_MESSAGE);
    }
    
    public void validate(List exceptions) {}
    public void reset(Object context) {}
  }
  
  /**
   * A data processor that creates multiple output messages from a single input message.
   */
  public static final class MultipleOutputsDataProcessor extends Component implements IDataProcessor {
    public int counter = 0;
    
    public Object[] process(Object data) {
      counter++;
      return new Object[]{data, "otherData1", "otherData2"};
    }
    
    public void validate(List exceptions) {}
    public void reset(Object context) {}
  }
  
  
  //
  // IWriteConnectors
  //
  //
  
  /**
   * A write connector that checks the data it receives is not empty. Does nothing
   * if it is, throws a RuntimeException if it isn't.
   */
  public static final class TestWriteConnector extends Component implements IWriteConnector {
    public int counter = 0;
    
    public List dataCollection = new ArrayList();
    
    public void connect() {}
    
    public void disconnect() {}
    
    public Object deliver(Object[] data) {
       counter++;
       if(data == null || data.length == 0){
         throw new RuntimeException("No data to write");
       }
       dataCollection.add(data);
       return null;
    }
    
    public void validate(List exceptions) {}
  }
  
  /**
   * A write connector that throws a RuntimeException whenever it receives 
   * data to write.
   */
  public static final class ExceptionThrowingWriteConnector extends Component implements IWriteConnector {
    public int counter = 0;
    public void connect() {}
    public void disconnect() {}
    public Object deliver(Object[] data) {
       counter++;
       throw new RuntimeException(TEST_ERROR_MESSAGE);
    }
    public void validate(List exceptions) {}
  }
  
  
  //
  // IExceptionHandlers
  //
  //
  
  /**
   * An exception handler with a calls counter.
   */
  public static final class DummyExceptionHandler extends ExceptionHandlerProxy {
    public int counter = 0;
    
    public Object[] process(Object data) {
      counter++;
      return super.process(data);
    } 
  }
}
