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

import org.jmock.Mock;
import org.openadaptor.auxil.simplerecord.AbstractSimpleRecordProcessor;
import org.openadaptor.auxil.simplerecord.ISimpleRecord;
import org.openadaptor.auxil.simplerecord.ISimpleRecordAccessor;
import org.openadaptor.core.AbstractTestIDataProcessor;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;

/**
 * Abstracts what's common to testing SimpleRecordProcessors.
 * 
 * @author Kevin Scully
 */
public abstract class AbstractTestAbstractSimpleRecordProcessor extends AbstractTestIDataProcessor {
  /** Mock instance for a SimpleRecord */
  protected Mock recordMock;

  /** Mock proxy cast to ISimpleRecord used as a test Record */
  protected ISimpleRecord record;

  protected Mock clonedRecordMock;

  protected ISimpleRecord clonedRecord;

  protected Mock simpleRecordAccessorMock;

  protected ISimpleRecordAccessor simpleRecordAccessor;

  /** Instantiate the mock test objects. Used by setUp(). */
  protected void createMocks() {
    // Mock the incoming record.
    recordMock = mock(ISimpleRecord.class);
    record = (ISimpleRecord) recordMock.proxy();
    // mock the cloned outgoing record.
    clonedRecordMock = new Mock(ISimpleRecord.class);
    clonedRecord = (ISimpleRecord) clonedRecordMock.proxy();
    // Mock the ISimpleRecordAccessor
    simpleRecordAccessorMock = new Mock(ISimpleRecordAccessor.class);
    simpleRecordAccessor = (ISimpleRecordAccessor) simpleRecordAccessorMock.proxy();
  }

  protected void deleteMocks() {
    recordMock = null;
    record = null;
    clonedRecordMock = null;
    clonedRecord = null;
    simpleRecordAccessorMock = null;
    simpleRecordAccessor = null;
  }

  /**
   * Utility method that returns the test processor cast to an AbstractSimpleRecordProcessor.
   * 
   * @return test processor cast to an AbstractSimpleRecordProcessor.
   */
  protected AbstractSimpleRecordProcessor getAbstractSimpleRecordProcessor() {
    return (AbstractSimpleRecordProcessor) testProcessor;
  }

  //
  // Tests
  //

  /**
   * All AbstractSimplerecordProcessors expect a record instance that implements ISimpleRecord.
   * <p>
   * This test ensures that the correct exception is thrown when that is not the case.
   */
  public void testProcessNonISimpleRecord() {
    // Expect a RecordFormatException
    try {
      testProcessor.process(new Object());
    } catch (RecordFormatException e) {
      return;
    } catch (RecordException e) {
      fail("Unexpected RecordException [" + e + "]");
    }
    fail("Did not catch expected RecordFormatException");
  }

  // Needs to be renamed testProcess further up the hierarchy.
  public  void testProcessRecord() {}

  /**
   * Test the basic process record functionality. This is the only test which tests with an accessor set and tests that
   * the accessor is used and that the corresponding getRecord() call is made. All other tests use a mock ISimpleRecord
   * as data and don't assume anything about accessors.
   */
  public abstract void testProcessAccessorSet();

  /**
   * Test the basic process record functionality. This test should be identical to testProcessAccessorSet but with no
   * accessor and ISimpleRecord data. It should test that getRecord is never used.
   */
  public abstract void testProcessNoAccessorSet();
}
