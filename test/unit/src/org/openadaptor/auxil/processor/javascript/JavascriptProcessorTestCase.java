/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved. 
                                                                                     
 Permission is hereby granted, free of charge, to any person obtaining a             
 copy of this software and associated documentation files (the                       
"Software"), to deal in the Software without restriction, including                
 without limitation the rights to use, copy, modify, merge, publish,                 
 distribute, sublicense, and/or sell copies of the Software, and to                  
 permit persons to whom the Software is furnished to do so, subject to               
 the following conditions:                                                           
                                                                                     
 The above copyright notice and this permission notice shall be included             
 in all copies or substantial portions of the Software.                              
                                                                                     
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS           
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                          
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                               
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE              
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION              
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION               
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                     
                                                                                     
 Nothing in this notice shall be deemed to grant any rights to                       
 trademarks, copyrights, patents, trade secrets or any other intellectual            
 property of the licensor or any contributor except as expressly stated              
 herein. No patent license is granted separate from the Software, for                
 code that you delete from the Software, or for combinations of the                  
 Software with other software or hardware.                                           
*/

package org.openadaptor.auxil.processor.javascript;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.auxil.simplerecord.ISimpleRecord;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;

public class JavascriptProcessorTestCase extends TestCase {
  static Log log = LogFactory.getLog(JavascriptProcessorTestCase.class);

  protected ISimpleRecord record;
  protected JavascriptProcessor processor;
  
  private static final String STRING_ONE="One";
  private static final String STRING_TWO="Two";
  private static final Integer FIVE=new Integer(5);
  private static final Integer SIX=new Integer(6);
  //private static final Double PI=new Double(3.14159);
  
  protected ISimpleRecord createTestRecord() {
    OrderedHashMap map=new OrderedHashMap();
    map.put("s1", STRING_ONE);
    map.put("s2", STRING_TWO);
    map.put("i1", FIVE);
    map.put("i2", SIX);
    map.put("pi", new Double(3.1415));
    return map;
  }
  public void setUp() {
    processor=new JavascriptProcessor();
    record=createTestRecord();
  }
  public void tearDown() {
    record.clear();
    record=null;
  }

  //
  // Tests
  //

  /**
   * All AbstractSimplerecordProcessors expect a record instance that implements ISimpleRecord.
   * <p>
   * This test ensures that the correct exception is thrown when that is not the case.
   */
  public void testProcessNonISimpleRecord() {
    log.info("testProcessNonISimpleRecord()");
    // Expect a RecordFormatException
    try {
      processor.setScript("3+4"); //Any non null script;
      List validationExceptions=new ArrayList();
      processor.validate(validationExceptions);
      
      processor.process(new Object());
    } catch (RecordFormatException e) {
      return;
    } catch (RecordException e) {
      fail("Unexpected RecordException [" + e + "]");
    }
    fail("Did not catch expected RecordFormatException");
  }

  // Needs to be renamed testProcess further up the hierarchy.
  public  void testProcessRecord() {
    log.info("testProcessRecord()");
    execute("record.get('i1') + record.get('i2')",11);
    //Test
  }
  public void testStringProcessing() {
    execute("record.get('s1') + record.get('s2')",STRING_ONE+STRING_TWO); 
    execute("record.get('s1').substring(2)",STRING_ONE.substring(2));
  }
  /*
  public void testFiltering(){
    processor.setDiscardMatches(true);
    processor.setScript("record.get('i1')=="+FIVE);
    Object[] result=processor.process(record);
    assertTrue("Record should have been filtered",result.length==0);
    processor.setScript("record.get('i1')==3");
    result=processor.process(record);
    assertTrue("Expected a single result record",result.length==1);
    Object output=result[0];
    assertTrue("Record should have passed through unchanged",record.equals(output));
    
    processor.setScript("record.get('i1')");
    try {
    result=processor.process(record);
    fail("Should throw a RuntimeException if filterOnResult is true, but script does not return a Boolean");
    }
    catch (RuntimeException re) {}
  }
  */
  //Convenience methods for testing.
  private Object exec(String script){
    String key="__key__";
    String fullScript=("record.put('"+key+"',"+script+")");
    log.info("FullScript is:"+fullScript);
    ISimpleRecord result=execRaw(fullScript);
    return result.get(key);
  }
  
  private ISimpleRecord execRaw(String script) {
    processor.setScript(script);
    List validationExceptions=new ArrayList();
    processor.validate(validationExceptions);
    assertTrue("Script should validate ok",validationExceptions.isEmpty());

    Object[] result=processor.process(record);
    assertTrue("expected a result array with one entry",result.length==1);
    ISimpleRecord output=(ISimpleRecord)result[0];
    return output;
  }

  private void execute(String script,int expected) {
    Object value=exec(script);
    assertTrue("Expected a numeric result",value instanceof Number);
    Number num=(Number)value;
    assertTrue("Expected int result of "+expected,num.intValue()==expected);
 }
  private void execute(String script,String expected) {
    Object value=exec(script).toString();
    assertTrue("Expected String result of "+expected,value.equals(expected));
 }

}
