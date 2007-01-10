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
package org.oa3.auxil.convertor;

import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.auxil.convertor.fixedwidth.AbstractFixedWidthStringConvertor;
import org.oa3.auxil.convertor.fixedwidth.FixedWidthFieldDetail;
import org.oa3.auxil.convertor.fixedwidth.FixedWidthStringToOrderedMapConvertor;
import org.oa3.auxil.convertor.fixedwidth.OrderedMapToFixedWidthStringConvertor;
import org.oa3.auxil.orderedmap.IOrderedMap;
import org.oa3.auxil.orderedmap.OrderedHashMap;
import org.oa3.core.exception.NullRecordException;
import org.oa3.core.exception.RecordException;
import org.oa3.core.exception.RecordFormatException;

/**
 * Suite of tests for the classes that handle fixed width string conversions.
 * 
 * @author Russ Fennell
 */
public class FixedWidthStringConvertorTestCase extends TestCase {

  public static final Log log = LogFactory.getLog(FixedWidthStringConvertorTestCase.class);

  FixedWidthFieldDetail fd1, fd2, fd3, fd4, fd5;

  FixedWidthFieldDetail[] details;

  protected void setUp() throws Exception {

    fd1 = new FixedWidthFieldDetail();
    fd1.setFieldWidth(3);
    fd1.setFieldName("id");
    fd1.setTrim(false);

    fd2 = new FixedWidthFieldDetail();
    fd2.setFieldWidth(10);
    fd2.setFieldName("name");
    fd2.setTrim(false);

    fd3 = new FixedWidthFieldDetail();
    fd3.setFieldWidth(200);
    fd3.setFieldName("address");
    fd3.setTrim(true);

    fd4 = new FixedWidthFieldDetail();
    fd4.setFieldWidth(20);
    fd4.setFieldName("short address");

    fd5 = new FixedWidthFieldDetail();
    fd5.setFieldWidth(20);
  }

  /**
   * Tests the abstract fixed width convertor class. More specifically, the code that manipulates the
   * FixedWidthFieldDetail list.
   */
  public void testAbstractFixedWidthStringConverter() {

    // we need to use a concrete example of a AbstractFixedWidthStringConvertorProcessor
    // as this will have the methods to test the FieldDetail class
    AbstractFixedWidthStringConvertor cnvtr = new FixedWidthStringToOrderedMapConvertor();

    // no field details defined
    assertEquals(null, cnvtr.getFieldDetails());
    assertEquals(false, cnvtr.hasFieldNames());
    assertEquals(0, cnvtr.getTotalFieldWidth());

    // field widths - 1
    details = new FixedWidthFieldDetail[] { fd1 };
    cnvtr.setFieldDetails(details);
    assertEquals(1, cnvtr.getFieldDetails().length);
    assertEquals(3, cnvtr.getTotalFieldWidth());

    // field widths - multiple
    details = new FixedWidthFieldDetail[] { fd1, fd2, fd3 };
    cnvtr.setFieldDetails(details);
    assertEquals(3, cnvtr.getFieldDetails().length);
    assertEquals(213, cnvtr.getTotalFieldWidth());

    // field names - none
    details = new FixedWidthFieldDetail[] { fd5 };
    cnvtr.setFieldDetails(details);
    assertEquals(false, cnvtr.hasFieldNames());
    assertEquals(null, cnvtr.getFieldDetails()[0].getFieldName());

    // field names - multiple
    details = new FixedWidthFieldDetail[] { fd1, fd2, fd3 };
    cnvtr.setFieldDetails(details);
    assertEquals(true, cnvtr.hasFieldNames());
    assertEquals("id", cnvtr.getFieldDetails()[0].getFieldName());
    assertEquals("name", cnvtr.getFieldDetails()[1].getFieldName());
    assertEquals("address", cnvtr.getFieldDetails()[2].getFieldName());

    // trim - default
    details = new FixedWidthFieldDetail[] { fd4 };
    cnvtr.setFieldDetails(details);
    assertEquals(false, cnvtr.getFieldDetails()[0].isTrim());

    // trim - values set
    details = new FixedWidthFieldDetail[] { fd1, fd2, fd3 };
    cnvtr.setFieldDetails(details);
    assertEquals(false, cnvtr.getFieldDetails()[0].isTrim());
    assertEquals(false, cnvtr.getFieldDetails()[1].isTrim());
    assertEquals(true, cnvtr.getFieldDetails()[2].isTrim());

    // right align - default
    details = new FixedWidthFieldDetail[]{fd1};
    cnvtr.setFieldDetails(details);
    assertEquals(false, cnvtr.getFieldDetails()[0].isRightAlign());


    // right align - values set
    fd1.setRightAlign(true);
    details = new FixedWidthFieldDetail[]{fd1};
    cnvtr.setFieldDetails(details);
    assertEquals(true, cnvtr.getFieldDetails()[0].isRightAlign());
    fd1.setRightAlign(false);

    // null field details
    // todo check for NullRecordException
    try {
      cnvtr.setFieldDetails(null);
      cnvtr.process(null);
      fail("Failed to detect a null field details");
    } catch (RecordException e) {
    }

    // not all field names defined
    try {
      details = new FixedWidthFieldDetail[] { fd1, fd5 };
      cnvtr.setFieldDetails(details);
      cnvtr.process("");
      fail("Failed to detect that not all field names were supplied");
    } catch (RecordException e) {
    }
  }

  /**
   * tests the fixed width to ordered map converter
   */
  public void testConvertFixedWidthToOrderMap() {

    FixedWidthStringToOrderedMapConvertor cnvtr = new FixedWidthStringToOrderedMapConvertor();
    details = new FixedWidthFieldDetail[] { fd1, fd2, fd3 };
    IOrderedMap result;
    String rec1 = "01 Russ      Riverbank House";
    String rec2 = "02 Steve     Riverbank House, London, UK";

    List keys = null;

    // null record
    try {
      cnvtr.process(null);
      fail("Failed to detect a null record");
    } catch (NullRecordException e) {
    } catch (Exception e) {
      fail("Wrong exception thrown [" + e + "]");
    }

    // non-String record
    try {
      cnvtr.process(new Integer(42));
      fail("Failed to detect a non-String record");
    } catch (RecordFormatException e) {
    } catch (Exception e) {
      fail("Wrong exception thrown [" + e + "]");
    }

    // no details defined
    try {
      cnvtr.process(rec1);
      fail("Failed to detect that no field details were defined");
    } catch (RecordException e) {
    }

    // record too short
    try {
      cnvtr.setFieldDetails(details);
      cnvtr.process("foo bar");
      fail("Failed to detect a null record");
    } catch (RecordFormatException e) {
     } catch (Exception e) {
      fail("Wrong exception thrown [" + e + "]");
    }

    // record ok - as last field can be trimmed
    try {
      Object[] resultArray = cnvtr.process(rec1);
      assertTrue(resultArray.length == 1);
      result = (IOrderedMap) resultArray[0];
      assertEquals(3, result.size());
      assertEquals("01 ", result.get(0)); // trim == false
      assertEquals("Russ      ", result.get(1)); // trim == false
      assertEquals("Riverbank House", result.get(2));
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }

    // record fails as last field cannot be trimmed and is too short
    try {
      details = new FixedWidthFieldDetail[] { fd1, fd2, fd4 };
      cnvtr.setFieldDetails(details);
      cnvtr.process(rec1);
      fail("Failed to detect that the record was too short as the last field cannot be trimmed");
    } catch (RecordFormatException e) {
    } catch (Exception e) {
      fail("Wrong exception thrown [" + e + "]");
    }

    // record too long - fd4: last field must be 25 chars long
    try {
      cnvtr.setFieldDetails(details);
      Object[] resultArray = cnvtr.process(rec2);
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

    // some fields names are defined but not all - fd5: no name
    try {
      details = new FixedWidthFieldDetail[] { fd1, fd2, fd5 };
      cnvtr.setFieldDetails(details);
      cnvtr.process(rec1);
      fail("Failed to detect that not all field names were defined");
    } catch (RecordException e) {
    }

    // no field names defined - that's ok
    try {
      details = new FixedWidthFieldDetail[] { fd5 };
      cnvtr.setFieldDetails(details);
      Object[] resultArray = cnvtr.process(rec2);
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

    // only field widths defined - ok, uses default field attributes
    // (trim = false, name auto-generated)
    Integer[] widths = new Integer[] { new Integer(3), new Integer(10), new Integer(15) };
    try {
      cnvtr.setFieldDetails(null);
      cnvtr.setFieldWidths(widths);
      Object[] resultArray = cnvtr.process(rec1);
      assertTrue(resultArray.length == 1);
      result = (IOrderedMap) resultArray[0];
      assertEquals(3, result.size());
      assertEquals("01 ", result.get(0));
      assertEquals("Russ      ", result.get(1));
      assertEquals("Riverbank House", result.get(2));
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }

    // you can't have both fieldDetails and fieldWidths defined
    try {
      cnvtr.setFieldDetails(details);
      fail("Failed to detect that both fieldDetails and fieldWidths have been defined");
    } catch (Exception e) {
    }

    try {
      cnvtr = new FixedWidthStringToOrderedMapConvertor();
      cnvtr.setFieldDetails(details);
      cnvtr.setFieldWidths(widths);
      fail("Failed to detect that both fieldWidths and fieldDetails have been defined");
    } catch (Exception e) {
    }
  }

  /**
   * tests the ordered map to fixed width converter
   */
  public void testConvertOrderedMapToFixedWidth() {

    IOrderedMap map = new OrderedHashMap();
    OrderedMapToFixedWidthStringConvertor cnvtr = new OrderedMapToFixedWidthStringConvertor();

    // null record
    try {
      cnvtr.process(null);
      fail("Failed to detect null record");
    } catch (NullRecordException e) {
    } catch (Exception e) {
      fail("Wrong exception thrown [" + e + "]");
    }

    // non-IOrderedMap record
    try {
      cnvtr.process(new Integer(42));
      fail("Failed to detect that record wasn't an IOrderedMap");
    } catch (RecordFormatException e) {
    } catch (Exception e) {
      fail("Wrong exception thrown [" + e + "]");
    }

    // no details defined
    try {
      cnvtr.process(map);
      fail("Failed to detect that no field details were defined");
    } catch (RecordException e) {
    }

    // field name not defined - output is blank string 10 chars long
    try {
      map.put("id", "01 ");
      details = new FixedWidthFieldDetail[] { fd2 };
      cnvtr.setFieldDetails(details);
      Object[] resultArray = cnvtr.process(map);
      assertTrue(resultArray.length == 1);
      String s = (String) resultArray[0];
      assertEquals(fd2.getFieldWidth(), s.length());

      // single field name
      map = new OrderedHashMap();
      map.put("id", "01 ");
      details = new FixedWidthFieldDetail[] { fd1 };
      cnvtr.setFieldDetails(details);
      resultArray = cnvtr.process(map);
      assertTrue(resultArray.length == 1);
      s = (String) resultArray[0];
      assertEquals("01 ", s);

      // multiple field names
      map = new OrderedHashMap();
      map.put("id", "01 ");
      map.put("name", "1234567890");
      details = new FixedWidthFieldDetail[] { fd1, fd2 };
      cnvtr.setFieldDetails(details);
      resultArray = cnvtr.process(map);
      assertTrue(resultArray.length == 1);
      s = (String) resultArray[0];
      assertEquals("01 1234567890", s);

      // multiple field names, only one defined - some output
      map = new OrderedHashMap();
      map.put("id", "01 ");
      map.put("name", "1234567890");
      details = new FixedWidthFieldDetail[] { fd2 };
      cnvtr.setFieldDetails(details);
      resultArray = cnvtr.process(map);
      assertTrue(resultArray.length == 1);
      s = (String) resultArray[0];
      assertEquals("1234567890", s);

      // padding
      map = new OrderedHashMap();
      map.put("id", "01 ");
      map.put("name", "1234");
      details = new FixedWidthFieldDetail[] { fd2 };
      cnvtr.setFieldDetails(details);
      resultArray = cnvtr.process(map);
      assertTrue(resultArray.length == 1);
      s = (String) resultArray[0];
      assertEquals(fd2.getFieldWidth(), s.length());

      // trimming
      map = new OrderedHashMap();
      map.put("name", "1234567890");
      map.put("id", "12345");
      details = new FixedWidthFieldDetail[] { fd1 };
      cnvtr.setFieldDetails(details);
      resultArray = cnvtr.process(map);
      assertTrue(resultArray.length == 1);
      s = (String) resultArray[0];
      assertEquals(fd1.getFieldWidth(), s.length());

      // more field names than ordered map entries - should be spaces for second field
      map = new OrderedHashMap();
      map.put("id", "01 ");
      details = new FixedWidthFieldDetail[] { fd1, fd2 };
      cnvtr.setFieldDetails(details);
      resultArray = cnvtr.process(map);
      assertTrue(resultArray.length == 1);
      s = (String) resultArray[0];
      assertEquals(fd1.getFieldWidth() + fd2.getFieldWidth(), s.length());
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }

    // field widths defined only - too few
    map = new OrderedHashMap();
    map.put("name", "1234567890");
    map.put("id", "12345");
    details = new FixedWidthFieldDetail[] { fd5 };
    cnvtr.setFieldDetails(details);
    try {
      cnvtr.process(map);
      fail("Failed to detect that not enough field widths were defined");
    } catch (RecordException e) {
    }

    // field widths defined only - too many
    map = new OrderedHashMap();
    map.put("name", "1234567890");
    map.put("id", "12345");
    details = new FixedWidthFieldDetail[] { fd5, fd5, fd5, fd5 };
    cnvtr.setFieldDetails(details);
    try {
      cnvtr.process(map);
      fail("Failed to detect that not enough field widths were defined");
    } catch (RecordException e) {
    }

    // field widths defined only - correct
    try {
      map = new OrderedHashMap();
      map.put("name", "1234567890");
      map.put("id", "12345");
      details = new FixedWidthFieldDetail[] { fd5, fd5 };
      cnvtr.setFieldDetails(details);
      Object[] resultArray = cnvtr.process(map);
      assertTrue(resultArray.length == 1);
      String s = (String) resultArray[0];
      assertEquals(fd5.getFieldWidth() + fd5.getFieldWidth(), s.length());
    } catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }

  }
}
