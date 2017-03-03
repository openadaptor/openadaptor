package org.openadaptor.thirdparty.json;

import java.util.ArrayList;

import org.json.JSONObject;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.RecordException;

/**
 * Sanity unit test case for StringToJSONConvertor 
 * @author higginse
 *
 */
public class StringToJSONConvertorTestCase extends TestAbstractJSONConvertor {
  
  protected IDataProcessor createProcessor() {
    return new StringToJSONConvertor();
  }
  
  /**
   * Test conversion from OrderedMap to JSONObject
   */
  public void testProcessRecord() {
    testProcessor.validate(new ArrayList());
    try {
      Object[] maps = testProcessor.process(jsonObject.toString());
      assertEquals(maps.length, 1);
      Object result=maps[0];
      assertTrue("process() should have generated a JSONObject instance",result instanceof JSONObject);
      //Bit if a cheat really - using String representation to check equality.
      assertEquals(jsonObject.toString(), result.toString());
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }
  }

}