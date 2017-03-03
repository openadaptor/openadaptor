package org.openadaptor.thirdparty.json;

import org.json.JSONObject;
import org.openadaptor.auxil.convertor.delimited.AbstractDelimitedStringConvertor;
import org.openadaptor.core.AbstractTestIDataProcessor;
/**
 * Common unit tests for {@link AbstractDelimitedStringConvertor}.
 */
public abstract class TestAbstractJSONConvertor extends AbstractTestIDataProcessor {
  
  protected JSONObject jsonObject=generateTestJSON();
  public void setup() throws Exception {
    super.setUp();
  }
  
  public void tearDown() throws Exception {
    super.tearDown();
  }
 
}
