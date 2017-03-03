package org.openadaptor.thirdparty.json;

import java.util.ArrayList;

import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.RecordException;

/**
 * Sanity unit test case for OrderedMapToJSONConvertor 
 * @author higginse
 *
 */
public class OrderedMapToJSONConvertorTestCase extends TestAbstractJSONConvertor {
  
  protected IDataProcessor createProcessor() {
    return new OrderedMapToJSONConvertor();
  }
  
  /**
   * Test conversion from OrderedMap to JSONObject
   */
  public void testProcessRecord() {
    testProcessor.validate(new ArrayList());
    try {
      Object[] maps = testProcessor.process(generateFlatOrderedMap());
      assertEquals(maps.length, 1);
      assertEquals(jsonObject.toString(), maps[0].toString());
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }
  }

}