package org.openadaptor.thirdparty.json;

import java.util.ArrayList;

import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.RecordException;

/**
 * Sanity unit test case for JSONToOrderedMapConvertor
 * @author higginse
 *
 */
public class JSONToOrderedMapConvertorTestCase extends TestAbstractJSONConvertor {
  
  protected IDataProcessor createProcessor() {
    return new JSONToOrderedMapConvertor();
  }
  
  /**
   * Test conversion from JSONObject to OrderedMap
   */
  public void testProcessRecord() {
    testProcessor.validate(new ArrayList());
    try {
      Object[] maps = testProcessor.process(jsonObject);
      assertEquals(maps.length, 1);
      assertEquals(generateFlatOrderedMap(), maps[0]);
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }
  }

}