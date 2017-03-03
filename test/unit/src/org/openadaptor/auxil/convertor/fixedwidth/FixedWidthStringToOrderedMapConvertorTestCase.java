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
package org.openadaptor.auxil.convertor.fixedwidth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.NullRecordException;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;

import java.util.List;

/**
 * Suite of tests for the classes that handle fixed width string conversions.
 *
 * @author Russ Fennell
 */
public class FixedWidthStringToOrderedMapConvertorTestCase extends AbstractTestFixedWidthStringConvertor {
  public static final Log log = LogFactory.getLog(FixedWidthStringToOrderedMapConvertorTestCase.class);

  protected FixedWidthStringToOrderedMapConvertor convertor;

  protected void setUp() throws Exception {
    super.setUp();
    convertor = (FixedWidthStringToOrderedMapConvertor) testProcessor;
  }

  protected void tearDown() throws Exception {
    convertor = null;
    super.tearDown();
  }

  protected IDataProcessor createProcessor() {
    return new FixedWidthStringToOrderedMapConvertor();
  }

  /**
   * tests the fixed width to ordered map converter
   */
  public void testProcessRecord() {
    FixedWidthFieldDetail[] fieldDetails = new FixedWidthFieldDetail[]{fd1, fd2, fd3};
    String rec1 = "01 Russ      Riverbank House";

    try {
      convertor = new FixedWidthStringToOrderedMapConvertor();
      convertor.setFieldDetails(fieldDetails);
      convertor.process(rec1);

    } catch (Exception e) {
      fail("Didn't expect any exceptions: " + e);
    }
  }

  public void testProcessNullRecord() {
    // null record
    try {
      convertor.process(null);
      fail("Failed to detect a null record");
    } catch (NullRecordException e) {
    } catch (Exception e) {
      fail("Wrong exception thrown [" + e + "]");
    }

  }

  public void testProcessNoDetailsDefined() {
    // no details defined
    String rec1 = "01 Russ      Riverbank House";
    try {
      convertor.process(rec1);
      fail("Failed to detect that no field details were defined");
    } catch (RecordException e) {
    }

  }

  public void testProcessNonStringRecord() {
    // non-String record
    try {
      convertor.process(new Integer(42));
      fail("Failed to detect a non-String record");
    } catch (RecordFormatException e) {
    } catch (Exception e) {
      fail("Wrong exception thrown [" + e + "]");
    }
  }


  public void testProcessTooShort() {
    // record too short
    FixedWidthFieldDetail[] fieldDetails = new FixedWidthFieldDetail[]{fd1, fd2, fd3};
    try {
      convertor.setFieldDetails(fieldDetails);
      convertor.process("foo bar");
      fail("Failed to detect a null record");
    } catch (RecordFormatException e) {
    } catch (Exception e) {
      fail("Wrong exception thrown [" + e + "]");
    }
  }

  public void testProcessLastFieldTrimmed() {
    // record ok - as last field can be trimmed
    FixedWidthFieldDetail[] fieldDetails = new FixedWidthFieldDetail[]{fd1, fd2, fd3};
    IOrderedMap result;
    String rec1 = "01 Russ      Riverbank House";
    try {
      convertor.setFieldDetails(fieldDetails);
      Object[] resultArray = convertor.process(rec1);
      assertTrue(resultArray.length == 1);
      result = (IOrderedMap) resultArray[0];
      assertEquals(3, result.size());
      assertEquals("01 ", result.get(0)); // trim == false
      assertEquals("Russ      ", result.get(1)); // trim == false
      assertEquals("Riverbank House", result.get(2));
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }
  }

  public void testProcessLastFieldNotTrimmed() {
    // record fails as last field cannot be trimmed and is too short
    FixedWidthFieldDetail[] fieldDetails = new FixedWidthFieldDetail[]{fd1, fd2, fd4};
    String rec1 = "01 Russ      Riverbank House";
    try {
      convertor.setFieldDetails(fieldDetails);
      convertor.process(rec1);
      fail("Failed to detect that the record was too short as the last field cannot be trimmed");
    } catch (RecordFormatException e) {
    } catch (Exception e) {
      fail("Wrong exception thrown [" + e + "]");
    }
  }

  public void testProcessTooLong() {
    // record too long - fd4: last field must be 25 chars long
    FixedWidthFieldDetail[] fieldDetails = new FixedWidthFieldDetail[]{fd1, fd2, fd4};
    String rec2 = "02 Steve     Riverbank House, London, UK";
    IOrderedMap result;
    List keys = null;
    try {
      convertor.setFieldDetails(fieldDetails);
      Object[] resultArray = convertor.process(rec2);
      assertTrue(resultArray.length == 1);
      result = (IOrderedMap) resultArray[0];
      assertEquals(3, result.size());
      assertEquals("02 ", result.get(0)); // trim == false
      assertEquals("Steve     ", result.get(1)); // trim == false
      assertEquals("Riverbank House, Lon", result.get(2)); // only 1st 20 chars returned

      // test the names defined in the ordered map
      keys = result.keys();
      assertEquals(3, keys.size());
      assertEquals("id", keys.get(0));
      assertEquals("name", keys.get(1));
      assertEquals("short address", keys.get(2));
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }
  }

  public void testProcessMissingFieldNames() {
    // some fields names are defined but not all - fd5: no name
    FixedWidthFieldDetail[] fieldDetails = new FixedWidthFieldDetail[]{fd1, fd2, fd5};
    String rec1 = "01 Russ      Riverbank House";
    try {

      convertor.setFieldDetails(fieldDetails);
      convertor.process(rec1);
      fail("Failed to detect that not all field names were defined");
    } catch (RecordException e) {
    }
  }

  public void testProcessNoFieldsDefined() {
    // no field names defined - that's ok
    
    String rec2 = "02 Steve     Riverbank House, London, UK";
    IOrderedMap result;
    List keys = null;
    try {
      details = new FixedWidthFieldDetail[]{fd5};
      convertor.setFieldDetails(details);
      Object[] resultArray = convertor.process(rec2);
      assertTrue(resultArray.length == 1);
      result = (IOrderedMap) resultArray[0];
      assertEquals(1, result.size());
      assertEquals("02 Steve     Riverba", result.get(0));

      // test the names defined in the ordered map
      keys = result.keys();
      assertEquals(1, keys.size());
      assertEquals("_auto_1", keys.get(0));
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }
  }

  public void testProcessWidthsOnlyDefined() {
    // only field widths defined - ok, uses default field attributes
    // (trim = false, name auto-generated)
    String rec1 = "01 Russ      Riverbank House";
    IOrderedMap result;
    Integer[] widths = new Integer[]{new Integer(3), new Integer(10), new Integer(15)};
    try {
      convertor.setFieldDetails(null);
      convertor.setFieldWidths(widths);
      Object[] resultArray = convertor.process(rec1);
      assertTrue(resultArray.length == 1);
      result = (IOrderedMap) resultArray[0];
      assertEquals(3, result.size());
      assertEquals("01 ", result.get(0));
      assertEquals("Russ      ", result.get(1));
      assertEquals("Riverbank House", result.get(2));
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }
  }

  public void testProcessDetailsAndWidthsSet() {
    Integer[] widths = new Integer[]{new Integer(3), new Integer(10), new Integer(15)};
    FixedWidthFieldDetail[] fieldDetails = new FixedWidthFieldDetail[]{fd5};

    // you can't have both fieldDetails and fieldWidths defined
    try {
      convertor.setFieldDetails(fieldDetails);
      convertor.setFieldWidths(widths);
      fail("Failed to detect that both fieldDetails and fieldWidths have been defined");
    } catch (Exception e) {
    }
  }


}
