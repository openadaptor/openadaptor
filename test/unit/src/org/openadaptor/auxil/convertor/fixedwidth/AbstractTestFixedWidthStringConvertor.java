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

  /**
   * Tests the abstract fixed width convertor class. More specifically, the code that manipulates the
   * FixedWidthFieldDetail list.
   */
  public void testAbstractFixedWidthStringConverter() {

    // we need to use a concrete example of a AbstractFixedWidthStringConvertorProcessor
    // as this will have the methods to test the FieldDetail class
    AbstractFixedWidthStringConvertor cnvtr = (AbstractFixedWidthStringConvertor)testProcessor;

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


 }
