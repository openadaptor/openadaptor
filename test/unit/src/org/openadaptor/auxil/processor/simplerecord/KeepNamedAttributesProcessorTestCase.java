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
package org.openadaptor.auxil.processor.simplerecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.processor.simplerecord.KeepNamedAttributesProcessor;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;

/**
 * Basic tests for the KeepNamedAttributesProcessor.
 */
public class KeepNamedAttributesProcessorTestCase extends AbstractTestAbstractSimpleRecordProcessor {

  private static final Log log = LogFactory.getLog(KeepNamedAttributesProcessorTestCase.class);

  private static final String X = "x";

  private static final String Y = "y";

  private static final String Z = "z";

  private static final String VALUE_OF_X = "value of x";

  private static final String VALUE_OF_Y = "value of y";

  private static final String VALUE_OF_Z = "value of z";

  private static final Object[] attributesToKeepArray = new Object[] { X, Y, Z };

  /**
   * Override to create the basic test processor instance.
   * 
   * @return The test processor.
   */
  protected IDataProcessor createProcessor() {
    KeepNamedAttributesProcessor processor = new KeepNamedAttributesProcessor();
    processor.setAttributesToKeep(Arrays.asList(attributesToKeepArray));
    return processor;
  }

  /**
   * Implement to perform the basic process record process functionality.
   * <P>
   * Test that the configured attributes (and no others) are transferred between records.
   */
  public void testProcessAccessorSet() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(simpleRecordAccessor);

    simpleRecordAccessorMock.expects(once()).method("asSimpleRecord").with(eq(record)).will(returnValue(record));
    recordMock.expects(once()).method("clone").will(returnValue(clonedRecord));
    clonedRecordMock.expects(once()).method("clear");
    clonedRecordMock.expects(atLeastOnce()).method("getRecord").will(returnValue(clonedRecord));

    recordMock.expects(once()).method("get").with(eq(X)).will(returnValue(VALUE_OF_X));
    recordMock.expects(once()).method("get").with(eq(Y)).will(returnValue(VALUE_OF_Y));
    recordMock.expects(once()).method("get").with(eq(Z)).will(returnValue(VALUE_OF_Z));

    clonedRecordMock.expects(once()).method("put").with(eq(X), eq(VALUE_OF_X));
    clonedRecordMock.expects(once()).method("put").with(eq(Y), eq(VALUE_OF_Y));
    clonedRecordMock.expects(once()).method("put").with(eq(Z), eq(VALUE_OF_Z));

    try {
      testProcessor.process(record);
    } catch (Exception e) {
      fail("Unexpected Exception: [" + e + "]");
    }
  }

  public void testProcessNoAccessorSet() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(null);

    recordMock.expects(once()).method("clone").will(returnValue(clonedRecord));
    clonedRecordMock.expects(once()).method("clear");
    clonedRecordMock.expects(never()).method("getRecord");

    recordMock.expects(once()).method("get").with(eq(X)).will(returnValue(VALUE_OF_X));
    recordMock.expects(once()).method("get").with(eq(Y)).will(returnValue(VALUE_OF_Y));
    recordMock.expects(once()).method("get").with(eq(Z)).will(returnValue(VALUE_OF_Z));

    clonedRecordMock.expects(once()).method("put").with(eq(X), eq(VALUE_OF_X));
    clonedRecordMock.expects(once()).method("put").with(eq(Y), eq(VALUE_OF_Y));
    clonedRecordMock.expects(once()).method("put").with(eq(Z), eq(VALUE_OF_Z));

    try {
      testProcessor.process(record);
    } catch (Exception e) {
      fail("Unexpected Exception: [" + e + "]");
    }
  }

  /**
   * Test that behaviour on attempting to keep an attribute NOT in the incoming record works as expected.
   */
  public void testKeepingNonExistentAttribute() {
    recordMock.expects(once()).method("clone").will(returnValue(clonedRecord));
    clonedRecordMock.expects(once()).method("clear");
    clonedRecordMock.expects(never()).method("getRecord"); // Should never get this far.

    // Set things up so that "x" is treated as never being in the incoming record.
    recordMock.expects(once()).method("get").with(eq(X)).will(returnValue(null));
    clonedRecordMock.expects(never()).method("put").with(eq(X), eq(null));

    try {
      testProcessor.process(record);
    } catch (RecordFormatException e) {
      log.info("Expected RecordFormatException thrown with message: [" + e.getMessage() + "]");
      return;
    } catch (RecordException e) {
      fail("Expected a RecordFormatException. Not: [" + e + "]");
    }
    fail("Expected a RecordException.");
  }

  /**
   * Test Validation with fully configured processor. For KeepNamedAttributesProcessor this just means setting
   * attributesToKeep.
   */
  public void testValidation() {
    KeepNamedAttributesProcessor processor = (KeepNamedAttributesProcessor) testProcessor;
    processor.setAttributesToKeep(Arrays.asList(attributesToKeepArray));

    List exceptions = new ArrayList();
    processor.validate(exceptions);
    assertTrue("Expected no validate exceptions.", exceptions.isEmpty());
  }

  /**
   * Test Validation with processor configured with no attributesToKeep. For KeepNamedAttributesProcessor this is the
   * only configuration that will fail validation.
   */
  public void testValidationNoAttributesToKeep() {
    KeepNamedAttributesProcessor processor = (KeepNamedAttributesProcessor) testProcessor;
    processor.setAttributesToKeep(null);

    List exceptions = new ArrayList();
    processor.validate(exceptions);
    assertTrue("Expected one validate exceptions.", exceptions.size() == 1);
  }
}
