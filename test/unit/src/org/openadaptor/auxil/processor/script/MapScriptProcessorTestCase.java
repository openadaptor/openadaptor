package org.openadaptor.auxil.processor.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.AbstractTestIDataProcessor;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.RecordFormatException;

public class MapScriptProcessorTestCase extends AbstractTestIDataProcessor {
  private static final Log log =LogFactory.getLog(MapScriptProcessorTestCase.class);

  protected HashMap inputMap;

  public void setUp() throws Exception {
    super.setUp();
    inputMap=new HashMap();
    inputMap.put("Foo", "foo");
    inputMap.put("Bar", "bar");
  }
  public void tearDown() throws Exception {
    inputMap=null;
    super.tearDown();
  }
  protected IDataProcessor createProcessor() {
    return new MapScriptProcessor();
  }

  public void testValidMap() {
    log.debug("--- BEGIN testValidMap ---");
    MapScriptProcessor processor=(MapScriptProcessor)testProcessor;
    processor.setScript("data;");
    try {
      processor.process("Not a map");
      fail("Non Map data should have caused a RecordFormatException");
    }
    catch (RecordFormatException rfe) {} //Expected   
    log.debug("--- END testValidMap ---");
  }

  public void testProcessRecord() {
    log.debug("--- BEGIN testProcessRecord ---");
    ArrayList exceptions=new ArrayList();
    MapScriptProcessor processor=(MapScriptProcessor)testProcessor;
    processor.setScript("data;");
    processor.validate(exceptions);//This also implicitly initialises the script engine.
    //First script leaves it untouched;
    checkResult(inputMap, processor.process(inputMap.clone()));
    
    //Calculate a result, add as new field
    Map expected=(Map)inputMap.clone();
    expected.put("FooBar","foobar");
    processor.setScript("data.put('FooBar',Foo+Bar);");
    processor.validate(exceptions);
    checkResult(expected,processor.process(inputMap));
    
    //Modify existing field
    expected.put("FooBar","foobar_foobar");
    processor.setScript("FooBar=FooBar+'_'+Foo+Bar;");
    processor.validate(exceptions);
    checkResult(expected,processor.process(inputMap));
    
    //Delete field
    expected.remove("Foo");
    processor.setScript("data.remove('Foo');");
    processor.validate(exceptions);
    checkResult(expected,processor.process(inputMap));  
    log.debug("--- END testProcessRecord ---");
  }

  private void checkResult(Object expected,Object[] returned) {
    checkResult(new Object[] {expected},returned);
  }
  /**
   * Check that the returned result exactly matches the expected result.
   * @param expected
   * @param returned
   */
  private void checkResult(Object[] expected,Object[] returned) {
    if (expected==null) {
      assertNull(returned);
    }
    else {
      assertNotNull(returned);
      assertTrue(expected.length==returned.length);
      for (int i=0;i<expected.length;i++) {
        assertEquals(expected[i], returned[i]);
      }
    }
  }

}
