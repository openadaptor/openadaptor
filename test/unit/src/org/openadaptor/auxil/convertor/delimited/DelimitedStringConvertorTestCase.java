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
package org.openadaptor.auxil.convertor.delimited;

import junit.framework.TestCase;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.exception.RecordException;

/**
 * This tests the DelimitedStringConvertors implementation.
 * 
 * @author OA3 Core Team
 */
public class DelimitedStringConvertorTestCase extends TestCase {
  private static final String[] NAMES = { "F-1", "F-2", "F-3", "F-4" };

  private static final String[] VALUES = { "Apples", "Oranges", "Bananas", "Pears" };

  private static final String[] NAMES_FOR_TRAILING_EMPTY_ELEMENTS = { "F-1", "F-2", "F-3", "F-4", "F-5", "F-6", "F-7" };

  private static final String[] VALUES_TRAILING_EMPTY_ELEMENTS = { "Apples", "Oranges", "Bananas", "Pears", "", "", "" };

  private static final String DELIMITER = ",";

  private String ds;



  private IOrderedMap om;



  private DelimitedStringToOrderedMapConvertor dsom;



  private OrderedMapToDelimitedStringConvertor omds;

  protected void setUp() throws Exception {
    super.setUp();
    ds = generateDelimitedString(DELIMITER, VALUES);
    om = generateOrderedMap(NAMES, VALUES);
    dsom = new DelimitedStringToOrderedMapConvertor();
    dsom.setFieldNames(NAMES);
    omds = new OrderedMapToDelimitedStringConvertor();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  private static String generateDelimitedString(String delimiter, String[] data) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < data.length; i++) {
      sb.append(data[i]);
      if (i < data.length - 1) {
        sb.append(delimiter);
      }
    }
    return sb.toString();
  }

  private static IOrderedMap generateOrderedMap(String[] names, String[] values) {
    // Create using Map add(key, value)
    IOrderedMap map = new OrderedHashMap(values.length);
    for (int i = 0; i < values.length; i++) {
      map.put(names[i], values[i]);
    }
    return map;
  }

  // Test conversion from Delimited String to Ordered Map
  public void testDelimitedStringToOrderedMapConversion() {
    dsom.validate(null);
    try {
      Object[] maps = dsom.process(ds);
      assertEquals(maps.length, 1);
      assertEquals(om, maps[0]);
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }

  }

  public void testDelimitedStringToOrderedMapWithHeadersInFirstRow() {
    dsom.validate(null);
    try {
      dsom.setFirstRecordContainsFieldNames(true);

      String headerDS = generateDelimitedString(DELIMITER, NAMES);
      Object[] maps = dsom.process(headerDS);
      assertEquals(maps.length, 0);
      assertEquals(headerDS, generateDelimitedString(DELIMITER, dsom.getFieldNames()));

      maps = dsom.process(ds);
      assertEquals(maps.length, 1);
      assertEquals(om, maps[0]);
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }

  }

  public void testDelimitedStringToOrderedMapWithTrailingEmptyStrings() {
    // Set Test Data and Expectations
    IOrderedMap expectedOm = generateOrderedMap(NAMES_FOR_TRAILING_EMPTY_ELEMENTS, VALUES_TRAILING_EMPTY_ELEMENTS);
    String testDS = generateDelimitedString(DELIMITER, VALUES_TRAILING_EMPTY_ELEMENTS);
    DelimitedStringToOrderedMapConvertor testDsom = new DelimitedStringToOrderedMapConvertor();
    testDsom.setFieldNames(NAMES_FOR_TRAILING_EMPTY_ELEMENTS);
    // Test
    testDsom.validate(null);
    try {
      Object[] maps = testDsom.process(testDS);
      assertEquals(maps.length, 1);
      assertEquals(expectedOm, maps[0]);
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }

  }

  public void testInvalidInputs() {
    dsom.validate(null);
    try {
      dsom.process(om);
      fail("Convertor should not accept non String value " + om.getClass().getName());
    } catch (RecordException pe) {
    }
    omds.validate(null);
    try {
      omds.process(ds);
      fail("Convertor should not accept non IOrderedMap value " + om.getClass().getName());
    } catch (RecordException pe) {
      ;
    }
  }

  // Test Conversion from OrderedMap to Delimited String
  public void testOrderedMapToDelimitedStringConversion() {
    omds.validate(null);
    try {
      omds.setDelimiter(DELIMITER);
      Object[] dsList = omds.process(om);
      assertEquals(dsList.length, 1);
      assertEquals(ds, (String) dsList[0]);
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }

  }

  public void testOrderedMapToDelimitedStringWithExplicitHeader() {
    try {
      omds.setDelimiter(DELIMITER);
      omds.setOutputHeader(true);
      omds.setFieldNames(NAMES);
      omds.validate(null);

      Object[] dsList = omds.process(om);
      assertEquals(dsList.length, 2);

      assertEquals(generateDelimitedString(DELIMITER, NAMES), (String) dsList[0]);

      assertEquals(ds, (String) dsList[1]);
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }

  }

  public void testOrderedMapToDelimitedStringWithImplicitHeader() {
    try {
      omds.setDelimiter(DELIMITER);
      omds.setOutputHeader(true);
      omds.validate(null);

      Object[] dsList = omds.process(om);
      assertEquals(dsList.length, 2);

      assertEquals(generateDelimitedString(DELIMITER, NAMES), (String) dsList[0]);

      assertEquals(ds, (String) dsList[1]);
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }

  }

}
