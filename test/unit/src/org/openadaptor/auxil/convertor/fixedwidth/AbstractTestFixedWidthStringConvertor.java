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
import org.openadaptor.core.AbstractTestIDataProcessor;
import org.openadaptor.core.exception.RecordException;

/**
 * Suite of tests for the classes that handle fixed width string conversions.
 * 
 * @author Russ Fennell
 */
public abstract class AbstractTestFixedWidthStringConvertor extends AbstractTestIDataProcessor {

  public static final Log log = LogFactory.getLog(AbstractTestFixedWidthStringConvertor.class);

  FixedWidthFieldDetail fd1, fd2, fd3, fd4, fd5;

  FixedWidthFieldDetail[] details;

  protected void setUp() throws Exception {
    super.setUp();
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
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testNoFieldDetailsDefined() {
    // we need to use a concrete example of a AbstractFixedWidthStringConvertorProcessor
    // as this will have the methods to test the FieldDetail class
    AbstractFixedWidthStringConvertor cnvtr = (AbstractFixedWidthStringConvertor)testProcessor;

    // no field details defined
    assertEquals(null, cnvtr.getFieldDetails());
    assertEquals(false, cnvtr.hasFieldNames());
    assertEquals(0, cnvtr.getTotalFieldWidth());    
  }

  public void testFieldWidthsMinusOne() {
    // we need to use a concrete example of a AbstractFixedWidthStringConvertorProcessor
    // as this will have the methods to test the FieldDetail class
    AbstractFixedWidthStringConvertor cnvtr = (AbstractFixedWidthStringConvertor)testProcessor;
    // field widths - 1
    FixedWidthFieldDetail[] fieldDetails = new FixedWidthFieldDetail[] { fd1 };
    
    cnvtr.setFieldDetails(fieldDetails);
    assertEquals(1, cnvtr.getFieldDetails().length);
    assertEquals(3, cnvtr.getTotalFieldWidth());
  }

  public void testFieldWidthsMultiple() {
    // we need to use a concrete example of a AbstractFixedWidthStringConvertorProcessor
    // as this will have the methods to test the FieldDetail class
    AbstractFixedWidthStringConvertor cnvtr = (AbstractFixedWidthStringConvertor)testProcessor;

    // field widths - multiple
    FixedWidthFieldDetail[] fieldDetails = new FixedWidthFieldDetail[] { fd1, fd2, fd3 };
    cnvtr.setFieldDetails(fieldDetails);
    assertEquals(3, cnvtr.getFieldDetails().length);
    assertEquals(213, cnvtr.getTotalFieldWidth());
  }

  public void testFieldNamesNone() {
    // we need to use a concrete example of a AbstractFixedWidthStringConvertorProcessor
    // as this will have the methods to test the FieldDetail class
    AbstractFixedWidthStringConvertor cnvtr = (AbstractFixedWidthStringConvertor)testProcessor;

    // field names - none
    FixedWidthFieldDetail[] fieldDetails = new FixedWidthFieldDetail[] { fd5 };
    cnvtr.setFieldDetails(fieldDetails);
    assertEquals(false, cnvtr.hasFieldNames());
    assertEquals(null, cnvtr.getFieldDetails()[0].getFieldName());
  }

  public void testFieldNamesMultiple() {
    // we need to use a concrete example of a AbstractFixedWidthStringConvertorProcessor
    // as this will have the methods to test the FieldDetail class
    AbstractFixedWidthStringConvertor cnvtr = (AbstractFixedWidthStringConvertor)testProcessor;    // field names - multiple
    FixedWidthFieldDetail[] fieldDetails = new FixedWidthFieldDetail[] { fd1, fd2, fd3 };
    cnvtr.setFieldDetails(fieldDetails);
    assertEquals(true, cnvtr.hasFieldNames());
    assertEquals("id", cnvtr.getFieldDetails()[0].getFieldName());
    assertEquals("name", cnvtr.getFieldDetails()[1].getFieldName());
    assertEquals("address", cnvtr.getFieldDetails()[2].getFieldName());
  }

  public void testTrimDefault() {
    // we need to use a concrete example of a AbstractFixedWidthStringConvertorProcessor
    // as this will have the methods to test the FieldDetail class
    AbstractFixedWidthStringConvertor cnvtr = (AbstractFixedWidthStringConvertor)testProcessor;

    // trim - default
    FixedWidthFieldDetail[] fieldDetails = new FixedWidthFieldDetail[] { fd4 };
    cnvtr.setFieldDetails(fieldDetails);
    assertEquals(false, cnvtr.getFieldDetails()[0].isTrim());
  }

  public void testTrimValuesSet() {
    // we need to use a concrete example of a AbstractFixedWidthStringConvertorProcessor
    // as this will have the methods to test the FieldDetail class
    AbstractFixedWidthStringConvertor cnvtr = (AbstractFixedWidthStringConvertor)testProcessor;

    // trim - values set
    FixedWidthFieldDetail[] fieldDetails = new FixedWidthFieldDetail[] { fd1, fd2, fd3 };
    cnvtr.setFieldDetails(fieldDetails);
    assertEquals(false, cnvtr.getFieldDetails()[0].isTrim());
    assertEquals(false, cnvtr.getFieldDetails()[1].isTrim());
    assertEquals(true, cnvtr.getFieldDetails()[2].isTrim());    
  }

  public void testFieldDetailsRightAlignDefault() {
    // we need to use a concrete example of a AbstractFixedWidthStringConvertorProcessor
    // as this will have the methods to test the FieldDetail class
    AbstractFixedWidthStringConvertor cnvtr = (AbstractFixedWidthStringConvertor)testProcessor;

    // right align - default
    FixedWidthFieldDetail[] fieldDetails = new FixedWidthFieldDetail[]{fd1};
    cnvtr.setFieldDetails(fieldDetails);
    assertEquals(false, cnvtr.getFieldDetails()[0].isRightAlign());
  }

  public void testFieldDetailsRightAlignValuesSet() {
    // we need to use a concrete example of a AbstractFixedWidthStringConvertorProcessor
    // as this will have the methods to test the FieldDetail class
    AbstractFixedWidthStringConvertor cnvtr = (AbstractFixedWidthStringConvertor)testProcessor;

    // right align - values set
    fd1.setRightAlign(true);
    FixedWidthFieldDetail[] fieldDetails = new FixedWidthFieldDetail[]{fd1};
    cnvtr.setFieldDetails(fieldDetails);
    assertEquals(true, cnvtr.getFieldDetails()[0].isRightAlign());
    fd1.setRightAlign(false);
  }

  public void testFieldDetailsNotAllNamesDefined() {

    // we need to use a concrete example of a AbstractFixedWidthStringConvertorProcessor
    // as this will have the methods to test the FieldDetail class
    AbstractFixedWidthStringConvertor cnvtr = (AbstractFixedWidthStringConvertor)testProcessor;

        // not all field names defined
    try {
      FixedWidthFieldDetail[] fieldDetails = new FixedWidthFieldDetail[] { fd1, fd5 };
      cnvtr.setFieldDetails(fieldDetails);
      cnvtr.process("");
      fail("Failed to detect that not all field names were supplied");
    } catch (RecordException e) {
    }
  }

  /**
   * Tests the abstract fixed width convertor class. More specifically, the code that manipulates the
   * FixedWidthFieldDetail list.
   */
  public void testAbstractFixedWidthStringConverterNullRecord() {

    // we need to use a concrete example of a AbstractFixedWidthStringConvertorProcessor
    // as this will have the methods to test the FieldDetail class
    AbstractFixedWidthStringConvertor cnvtr = (AbstractFixedWidthStringConvertor)testProcessor;

    // null field details
    // todo check for NullRecordException
    try {
      cnvtr.setFieldDetails(null);
      cnvtr.process(null);
      fail("Failed to detect a null field details");
    } catch (RecordException e) {
    }

  }


 }
