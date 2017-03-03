package org.openadaptor.thirdparty.json;

import java.util.ArrayList;

import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.RecordException;

/**
 * Sanity unit test case for JSONToStringConvertor
 * @author higginse
 *
 */
public class JSONToStringTestCase extends TestAbstractJSONConvertor {
  
  protected IDataProcessor createProcessor() {
    return new JSONToStringConvertor();
  }
  
  /**
   * Test conversion from JSONObject to OrderedMap
   */
  public void testProcessRecord() {
    testProcessor.validate(new ArrayList());
    try {
      Object[] results = testProcessor.process(jsonObject);
      assertEquals(results.length, 1);
      assertEquals(jsonObject.toString(), results[0]);
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }
  }

}