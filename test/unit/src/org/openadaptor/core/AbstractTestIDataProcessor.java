/*
 * [[
 * Copyright (C) 2001 - 2006 The Software Conservancy as Trustee. All rights
 * reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Nothing in this notice shall be deemed to grant any rights to
 * trademarks, copyrights, patents, trade secrets or any other intellectual
 * property of the licensor or any contributor except as expressly stated
 * herein. No patent license is granted separate from the Software, for
 * code that you delete from the Software, or for combinations of the
 * Software with other software or hardware.
 * ]]
 */
package org.openadaptor.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.MockObjectTestCase;
import org.json.JSONException;
import org.json.JSONObject;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.exception.NullRecordException;
import org.openadaptor.core.exception.RecordException;

/**
 * Abstracts whats common to testing IDataProcessor implementations.
 * 
 * @author Kevin Scully
 */
public abstract class AbstractTestIDataProcessor extends MockObjectTestCase {
  private static final Log log =LogFactory.getLog(AbstractTestIDataProcessor.class);

  protected static final String[] TEST_NAMES = { "F-1", "F-2", "F-3", "F-4" };
  protected static final String[] TEST_VALUES = { "Apples", "Oranges", "Bananas", "Pears" };

  /**
   * The test processor.
   */
  protected IDataProcessor testProcessor;

  // Junit setup and teardown methods

  protected void setUp() throws Exception {
    super.setUp();
    createMocks();
    testProcessor = createProcessor();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    testProcessor = null;
  }

  /**
   * Abstract method overridden to create any required mock objects.
   */
  protected void createMocks() {} //Default implementation doesn't do anything.

  protected void deleteMocks() {} //Default implementation doesn't do anything.

  /**
   * Override to create the basic test processor instance.
   * 
   * @return The test processor.
   */
  protected abstract IDataProcessor createProcessor();

  /**
   * Implement to perform the basic process record functionality.
   */
  abstract public void testProcessRecord();

  //abstract public void testValidation();

  /**
   * All IDataProcessors are implemented to expect a non-null record instance.
   * <p>
   * This test ensures that the correct NullRecordException is thrown when that is not the case.
   */
  public void testProcessNullRecord() {
    log.debug("--- BEGIN testProcessNullRecord ---");
    try {
      testProcessor.process(null);
    } catch (NullRecordException e) {
      log.debug("--- END testProcessNullRecord ---");
      return;
    } catch (RecordException e) {
      fail("Unexpected RecordException [" + e + "]");
    }
    fail("Did not catch expected NullRecordException");

    log.debug("--- END testProcessNullRecord ---");
  }

  public void testValidateNull() {
    log.debug("--- BEGIN testValidate(null) ---");
    try {
      testProcessor.validate(null);
    } catch (IllegalArgumentException e) {
      return;
    } catch (Throwable t) {
      fail("Unexpected Exception [" + t + "]");
    }
    finally {
      log.debug("--- END testValidate(null) ---");
    }
    fail("Did not catch expected IllegalArgumentException");
  }
  //Utility methods
  
  /**
   * Utility method to generate a DelimitedString from supplied data
   */
  protected static String generateDelimitedString(String delimiter, Object[] data) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < data.length; i++) {
      sb.append(data[i]);
      if (i < data.length - 1) {
        sb.append(delimiter);
      }
    }
    return sb.toString();
  }

  protected static String generateTestDelimitedString(String delimiter) {
    return generateDelimitedString(delimiter, TEST_VALUES);
  }

  /**
   * Utility to generate OrderedMaps from supplied data
   */
  protected static IOrderedMap generateOrderedMap(Object[] names, Object[] values) {
    // Create using Map add(key, value)
    IOrderedMap map = new OrderedHashMap(values.length);
    for (int i = 0; i < values.length; i++) {
      map.put(names[i], values[i]);
    }
    return map;
  }
  protected static IOrderedMap generateFlatOrderedMap() {
    return generateOrderedMap(TEST_NAMES,TEST_VALUES);
  }
 
  protected static JSONObject generateJSON(Object[] names, Object[] values) {
    StringBuffer sb=new StringBuffer("{");
    for (int i=0;i<names.length;i++) {
      sb.append('"').append(names[i]).append('"');
      sb.append(":").append('"').append(values[i]).append('"');
      sb.append(",");
    }
    sb.setCharAt(sb.length()-1, '}');
    try {
    return new JSONObject(sb.toString());
    }
    catch (JSONException je) {
      throw new RuntimeException(je);
    }
  }
  
  protected static JSONObject generateTestJSON() {
    return generateJSON(TEST_NAMES, TEST_VALUES);
  }
}
