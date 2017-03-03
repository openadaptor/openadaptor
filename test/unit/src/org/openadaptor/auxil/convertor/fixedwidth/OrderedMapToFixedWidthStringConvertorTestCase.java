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
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.NullRecordException;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;

/**
 * Suite of tests for the classes that handle fixed width string conversions.
 * 
 * @author Russ Fennell
 */
public class OrderedMapToFixedWidthStringConvertorTestCase extends AbstractTestFixedWidthStringConvertor {
  public static final Log log = LogFactory.getLog(OrderedMapToFixedWidthStringConvertorTestCase.class);

  protected OrderedMapToFixedWidthStringConvertor convertor;

  protected void setUp() throws Exception {
    super.setUp();
    convertor=(OrderedMapToFixedWidthStringConvertor)testProcessor;
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    convertor = null;
  }

  protected IDataProcessor createProcessor() {
    return new OrderedMapToFixedWidthStringConvertor();
  }


  /**
   * tests the ordered map to fixed width converter
   */
  public void testProcessRecord() {
    IOrderedMap map;

    // field widths defined only - correct
    try {
      map = new OrderedHashMap();
      map.put("name", "1234567890");
      map.put("id", "12345");
      details = new FixedWidthFieldDetail[] { fd5, fd5 };
      convertor.setFieldDetails(details);
      Object[] resultArray = convertor.process(map);
      assertTrue(resultArray.length == 1);
      String s = (String) resultArray[0];
      assertEquals(fd5.getFieldWidth() + fd5.getFieldWidth(), s.length());
    }
    catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }
  }

  /**
   * All IDataProcessors are implemented to expect a non-null record instance.
   * <p/>
   * This test ensures that the correct NullRecordException is thrown when that is not the case.
   */
  public void testProcessNullRecord() {
    // null record
    try {
      convertor.process(null);
      fail("Failed to detect null record");
    }
    catch (NullRecordException e) {
    }
    catch (Exception e) {
      fail("Wrong exception thrown [" + e + "]");
    }
  }

  public void testProcessNonOrderedMap() {
    // non-IOrderedMap record
    try {
      convertor.process(new Integer(42));
      fail("Failed to detect that record wasn't an IOrderedMap");
    }
    catch (RecordFormatException e) {
    }
    catch (Exception e) {
      fail("Wrong exception thrown [" + e + "]");
    }
  }

  public void testNoDetailsDefined() {
    IOrderedMap map = new OrderedHashMap();

    // no details defined
    try {
      convertor.process(map);
      fail("Failed to detect that no field details were defined");
    }
    catch (RecordException e) {
    }
  }

  public void testProcessFieldNameNotDefined() {
    IOrderedMap map = new OrderedHashMap();
    Object[] resultArray;
    String s;

    // field name not defined - output is blank string 10 chars long
    try {
      map.put("id", "01 ");
      details = new FixedWidthFieldDetail[] { fd2 };
      convertor.setFieldDetails(details);
      resultArray = convertor.process(map);
      assertTrue(resultArray.length == 1);
      s = (String) resultArray[0];
      assertEquals(fd2.getFieldWidth(), s.length());
    }
    catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }        
  }

  public void testProcessSingleFieldName() {
    IOrderedMap map;

    Object[] resultArray;
    String s;

    try {
      // single field name
      map = new OrderedHashMap();
      map.put("id", "01 ");
      details = new FixedWidthFieldDetail[] { fd1 };
      convertor.setFieldDetails(details);
      resultArray = convertor.process(map);
      assertTrue(resultArray.length == 1);
      s = (String) resultArray[0];
      assertEquals("01 ", s);
    }
      catch (RecordException re) {
        fail("Unexpected RecordException - " + re);
      }            
  }

  public void testProcessMultipleFieldNames() {
    IOrderedMap map;

    Object[] resultArray;
    String s;

    try {
      // multiple field names
      map = new OrderedHashMap();
      map.put("id", "01 ");
      map.put("name", "1234567890");
      details = new FixedWidthFieldDetail[] { fd1, fd2 };
      convertor.setFieldDetails(details);
      resultArray = convertor.process(map);
      assertTrue(resultArray.length == 1);
      s = (String) resultArray[0];
      assertEquals("01 1234567890", s);
    }
    catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }            
  }

  public void testProcessMultipleFieldNamesOneDefined() {
    IOrderedMap map;

    Object[] resultArray;
    String s;

    try {
      // multiple field names, only one defined - some output
      map = new OrderedHashMap();
      map.put("id", "01 ");
      map.put("name", "1234567890");
      details = new FixedWidthFieldDetail[] { fd2 };
      convertor.setFieldDetails(details);
      resultArray = convertor.process(map);
      assertTrue(resultArray.length == 1);
      s = (String) resultArray[0];
      assertEquals("1234567890", s);
    }
    catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }                
  }

  public void testProcessFieldDetailsPadding() {
    IOrderedMap map;

    Object[] resultArray;
    String s;

    try {
      // padding
      map = new OrderedHashMap();
      map.put("id", "01 ");
      map.put("name", "1234");
      details = new FixedWidthFieldDetail[] { fd2 };
      convertor.setFieldDetails(details);
      resultArray = convertor.process(map);
      assertTrue(resultArray.length == 1);
      s = (String) resultArray[0];
      assertEquals(fd2.getFieldWidth(), s.length());
    }
    catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }
  }

  public void testProcessFieldDetailsTrimming() {
    IOrderedMap map;

    Object[] resultArray;
    String s;

    try {
      // trimming
      map = new OrderedHashMap();
      map.put("name", "1234567890");
      map.put("id", "12345");
      details = new FixedWidthFieldDetail[] { fd1 };
      convertor.setFieldDetails(details);
      resultArray = convertor.process(map);
      assertTrue(resultArray.length == 1);
      s = (String) resultArray[0];
      assertEquals(fd1.getFieldWidth(), s.length());
    }
    catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }
  }

  public void testProcessMoreNamesThanData() {
    IOrderedMap map;

    Object[] resultArray;
    String s;

    try {
      // more field names than ordered map entries - should be spaces for second field
      map = new OrderedHashMap();
      map.put("id", "01 ");
      details = new FixedWidthFieldDetail[] { fd1, fd2 };
      convertor.setFieldDetails(details);
      resultArray = convertor.process(map);
      assertTrue(resultArray.length == 1);
      s = (String) resultArray[0];
      assertEquals(fd1.getFieldWidth() + fd2.getFieldWidth(), s.length());
    }
    catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }    
  }

  public void testFieldNameCaching() {
    // Ensure that getFieldNames() always returns the right answer if fieldDetails is reset a few times.

    FixedWidthFieldDetail[] firstDetails = new FixedWidthFieldDetail[] { fd1, fd2 };
    String[] firstNames = new String[] { fd1.getFieldName(), fd2.getFieldName() };

    convertor.setFieldDetails(firstDetails);
    String[] firstResultArray = (String[])convertor.getFieldNames().toArray(new String[convertor.getFieldNames().size()]);
    assertTrue(firstResultArray.length == 2);
    assertEquals(firstNames[0], firstResultArray[0]);
    assertEquals(firstNames[1], firstResultArray[1]);

    FixedWidthFieldDetail[] secondDetails = new FixedWidthFieldDetail[] { fd3, fd4 };
    String[] secondNames = new String[] { fd3.getFieldName(), fd4.getFieldName() };

    convertor.setFieldDetails(secondDetails);
    String[] secondResultArray = (String[])convertor.getFieldNames().toArray(new String[convertor.getFieldNames().size()]);
    assertTrue(secondResultArray.length == 2);
    assertEquals(secondNames[0], secondResultArray[0]);
    assertEquals(secondNames[1], secondResultArray[1]);

    // fd5 has no name set so number of names is one less than number of fieldDetails.
    FixedWidthFieldDetail[] thirdDetails = new FixedWidthFieldDetail[] { fd3, fd4, fd5 };
    String[] thirdNames = new String[] { fd3.getFieldName(), fd4.getFieldName(), fd5.getFieldName() };

    convertor.setFieldDetails(thirdDetails);
    String[] thirdResultArray = (String[])convertor.getFieldNames().toArray(new String[convertor.getFieldNames().size()]);
    assertTrue("Result is wrong length", thirdResultArray.length == 2);
    assertEquals(thirdNames[0], thirdResultArray[0]);
    assertEquals(thirdNames[1], thirdResultArray[1]);    
  }

  public void testTooFewFieldWidths() {
    // field widths defined only - too few
    IOrderedMap map = new OrderedHashMap();
    map.put("name", "1234567890");
    map.put("id", "12345");
    details = new FixedWidthFieldDetail[] { fd5 };
    convertor.setFieldDetails(details);
    try {
      convertor.process(map);
      fail("Failed to detect that not enough field widths were defined");
    }
    catch (RecordException e) {}        
  }

  public void testProcessTooManyFieldWidths() {
    // field widths defined only - too many
    IOrderedMap map = new OrderedHashMap();
    map.put("name", "1234567890");
    map.put("id", "12345");
    details = new FixedWidthFieldDetail[] { fd5, fd5, fd5, fd5 };
    convertor.setFieldDetails(details);
    try {
      convertor.process(map);
      fail("Failed to detect that not enough field widths were defined");
    }
    catch (RecordException e) {}
  }


  /**
   * tests the ordered map to fixed width converter
   */
  public void testProcessFieldWidthsDefined() {
    // field widths defined only - correct
    try {
      IOrderedMap map = new OrderedHashMap();
      map.put("name", "1234567890");
      map.put("id", "12345");
      details = new FixedWidthFieldDetail[] { fd5, fd5 };
      convertor.setFieldDetails(details);
      Object[] resultArray = convertor.process(map);
      assertTrue(resultArray.length == 1);
      String s = (String) resultArray[0];
      assertEquals(fd5.getFieldWidth() + fd5.getFieldWidth(), s.length());
    }
    catch (RecordException re) {
      fail("Unexpected RecordException - " + re);
    }
  }

}
