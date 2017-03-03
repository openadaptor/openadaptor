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
import org.openadaptor.auxil.processor.simplerecord.SimpleRecordAttributeExistsProcessor;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;

/**
 * Basic tests for SimpleRecordAttributeExistsProcessor.
 */
public class SimpleRecordAttributeExistsProcessorTestCase extends AbstractTestAbstractSimpleRecordProcessor {

  private static final Log log = LogFactory.getLog(SimpleRecordAttributeExistsProcessorTestCase.class);

  private static final String X = "x";

  private static final String Y = "y";

  private static final String Z = "z";

  private static final Object[] ATTRIBUTES_ARRAY = new Object[] { X, Y, Z };

  /**
   * Override to create the basic test processor instance.
   * 
   * @return The test processor.
   */
  protected IDataProcessor createProcessor() {
    SimpleRecordAttributeExistsProcessor processor = new SimpleRecordAttributeExistsProcessor();
    processor.setMandatoryAttributes(Arrays.asList(ATTRIBUTES_ARRAY));
    processor.setThrowExceptionOnMissingAttribute(false);
    return processor;
  }

  /**
   * Implement to perform the basic process record functionality.
   * <p>
   * In this case the processed record is set up to contain all the named attributes configured in the processor.
   */
  public void testProcessAccessorSet() {
    ((SimpleRecordAttributeExistsProcessor)testProcessor).setSimpleRecordAccessor(simpleRecordAccessor);
    simpleRecordAccessorMock.expects(once()).method("asSimpleRecord").with(eq(record)).will(returnValue(record));
    recordMock.expects(once()).method("containsKey").with(eq(X)).will(returnValue(true));
    recordMock.expects(once()).method("containsKey").with(eq(Y)).will(returnValue(true));
    recordMock.expects(once()).method("containsKey").with(eq(Z)).will(returnValue(true));
    recordMock.expects(once()).method("getRecord").will(returnValue(record));
    Object[] returnedRecords = null;
    try {
      returnedRecords = testProcessor.process(record);
    } catch (RecordException e) {
      fail("Shouldn't throw an exception. Got exception: [" + e + "]");
    }
    assertTrue("Should be one record.", returnedRecords.length == 1);
    assertEquals("Should be the same as the incoming record", record, returnedRecords[0]);
  }

  public void testProcessNoAccessorSet() {
    ((SimpleRecordAttributeExistsProcessor)testProcessor).setSimpleRecordAccessor(null);
    recordMock.expects(once()).method("containsKey").with(eq(X)).will(returnValue(true));
    recordMock.expects(once()).method("containsKey").with(eq(Y)).will(returnValue(true));
    recordMock.expects(once()).method("containsKey").with(eq(Z)).will(returnValue(true));
    //recordMock.expects(once()).method("getRecord").will(returnValue(record));
    Object[] returnedRecords = null;
    try {
      returnedRecords = testProcessor.process(record);
    } catch (RecordException e) {
      fail("Shouldn't throw an exception. Got exception: [" + e + "]");
    }
    assertTrue("Should be one record.", returnedRecords.length == 1);
    assertEquals("Should be the same as the incoming record", record, returnedRecords[0]);
  }

  /**
   * Implement to perform the basic process record functionality with discardMatches set to false.
   * <p>
   * In this case the processed record is set up to contain all the named attributes configured in the processor. As
   * discardMatches is set to false the record should be discarded as it passes the test (i.e. matches).
   */
  public void testProcessRecordDiscardMatchesSetFalse() {
    ((SimpleRecordAttributeExistsProcessor) testProcessor).setDiscardMatches(false);
    recordMock.expects(once()).method("containsKey").with(eq(X)).will(returnValue(true));
    recordMock.expects(once()).method("containsKey").with(eq(Y)).will(returnValue(true));
    recordMock.expects(once()).method("containsKey").with(eq(Z)).will(returnValue(true));
    Object[] returnedRecords = null;
    try {
      returnedRecords = testProcessor.process(record);
    } catch (RecordException e) {
      fail("Shouldn't throw an exception. Got exception: [" + e + "]");
    }
    assertTrue("Should be one record.", returnedRecords.length == 0);
  }

  /**
   * Implement to perform the basic process record functionality with throwExceptionOnMissingAttribute set to true.
   * <p>
   * In this case the processed record is set up to contain all the named attributes configured in the processor. As A
   * result no exception is expected and we expect the record to be passed through.
   */
  public void testProcessRecordWithThrowExceptionSet() {
    recordMock.expects(once()).method("containsKey").with(eq(X)).will(returnValue(true));
    recordMock.expects(once()).method("containsKey").with(eq(Y)).will(returnValue(true));
    recordMock.expects(once()).method("containsKey").with(eq(Z)).will(returnValue(true));
    ((SimpleRecordAttributeExistsProcessor) testProcessor).setThrowExceptionOnMissingAttribute(true);
    //recordMock.expects(once()).method("getRecord").will(returnValue(record));
    Object[] returnedRecords = null;
    try {
      returnedRecords = testProcessor.process(record);
    } catch (RecordException e) {
      fail("Shouldn't throw an exception. Got exception: [" + e + "]");
    }
    assertTrue("Should be one record.", returnedRecords.length == 1);
    assertEquals("Should be the same as the incoming record", record, returnedRecords[0]);
  }

  /**
   * Implement to perform the basic process record functionality with throwExceptionOnMissingAttribute set to true and
   * discardMatches set to false.
   * <p>
   * In this case the processed record is set up to contain all the named attributes configured in the processor. As A
   * result no exception is expected and we expect the record to be passed through.
   */
  public void testProcessRecordWithThrowExceptionSetDiscardMatchesSetFalse() {
    ((SimpleRecordAttributeExistsProcessor) testProcessor).setDiscardMatches(false);
    ((SimpleRecordAttributeExistsProcessor) testProcessor).setThrowExceptionOnMissingAttribute(true);
    recordMock.expects(once()).method("containsKey").with(eq(X)).will(returnValue(true));
    recordMock.expects(once()).method("containsKey").with(eq(Y)).will(returnValue(true));
    recordMock.expects(once()).method("containsKey").with(eq(Z)).will(returnValue(true));
    //recordMock.expects(once()).method("getRecord").will(returnValue(record));
    Object[] returnedRecords = null;
    try {
      returnedRecords = testProcessor.process(record);
    } catch (RecordException e) {
      fail("Shouldn't throw an exception. Got exception: [" + e + "]");
    }
    assertTrue("Should be one record.", returnedRecords.length == 1);
    assertEquals("Should be the same as the incoming record", record, returnedRecords[0]);
  }

  /**
   * Implement to perform the basic process record functionality with CreateOnMissingAttribute set to true and
   * discardMatches set to default (true).
   * <p>
   * In this case the processed record is set up to contain all the named attributes configured in the processor. As A
   * result no exception is expected and we expect the (cloned)record to be passed through. We don't expect the value of
   * discard matches to make any difference.
   */
  public void testProcessRecordWithCreateMissingAttributesSet() {
    ((SimpleRecordAttributeExistsProcessor) testProcessor).setCreateOnMissingAttribute(true);
    recordMock.expects(once()).method("clone").will(returnValue(clonedRecord));
    clonedRecordMock.expects(once()).method("containsKey").with(eq(X)).will(returnValue(true));
    clonedRecordMock.expects(once()).method("containsKey").with(eq(Y)).will(returnValue(true));
    clonedRecordMock.expects(once()).method("containsKey").with(eq(Z)).will(returnValue(true));
    //clonedRecordMock.expects(once()).method("getRecord").will(returnValue(clonedRecord));
    Object[] returnedRecords = null;
    try {
      returnedRecords = testProcessor.process(record);
    } catch (RecordException e) {
      fail("Shouldn't throw an exception. Got exception: [" + e + "]");
    }
    assertTrue("Should be one record.", returnedRecords.length == 1);
    assertEquals("Should be the same as the incoming record", clonedRecord, returnedRecords[0]);
  }

  /**
   * Implement to perform the basic process record functionality with CreateOnMissingAttribute set to true and
   * discardMatches set to false.
   * <p>
   * In this case the processed record is set up to contain all the named attributes configured in the processor. As A
   * result no exception is expected and we expect the (cloned)record to be passed through. We don't expect the value of
   * discard matches to make any difference.
   */
  public void testProcessRecordWithCreateMissingAttributesSetDiscardMatchesSetFalse() {
    ((SimpleRecordAttributeExistsProcessor) testProcessor).setDiscardMatches(false);
    ((SimpleRecordAttributeExistsProcessor) testProcessor).setCreateOnMissingAttribute(true);
    recordMock.expects(once()).method("clone").will(returnValue(clonedRecord));
    clonedRecordMock.expects(once()).method("containsKey").with(eq(X)).will(returnValue(true));
    clonedRecordMock.expects(once()).method("containsKey").with(eq(Y)).will(returnValue(true));
    clonedRecordMock.expects(once()).method("containsKey").with(eq(Z)).will(returnValue(true));
    //clonedRecordMock.expects(once()).method("getRecord").will(returnValue(clonedRecord));
    Object[] returnedRecords = null;
    try {
      returnedRecords = testProcessor.process(record);
    } catch (RecordException e) {
      fail("Shouldn't throw an exception. Got exception: [" + e + "]");
    }
    assertTrue("Should be one record.", returnedRecords.length == 1);
    assertEquals("Should be the same as the incoming record", clonedRecord, returnedRecords[0]);
  }

  /**
   * Test behaviour with missing attributes in the record and everything else set to default values.
   * <p>
   * The incoming record should be discarded.
   */
  public void testWithMissingAttributes() {
    recordMock.expects(once()).method("containsKey").with(eq(X)).will(returnValue(true));
    recordMock.expects(once()).method("containsKey").with(eq(Y)).will(returnValue(false));
    recordMock.expects(once()).method("containsKey").with(eq(Z)).will(returnValue(true));

    Object[] returnedRecords = null;
    try {
      returnedRecords = testProcessor.process(record);
    } catch (RecordFormatException e) {
      fail("Default configuration processor should discard records if missing a required attribute. Got RecordFormatException: "
          + e);
    } catch (RecordException e) {
      fail("Shouldn't throw a RecordException. Got exception: [" + e + "]");
    } catch (Exception e) {
      fail("Shouldn't throw an Exception. Got exception: [" + e + "]");
    }

    assertTrue("Default configuration processor should discard records if missing a required attribute.",
        returnedRecords.length == 0);
  }

  /**
   * Test behaviour with missing attributes in the record and discardMatches set to false.
   * <p>
   * Should get a record in the returned records and it should be the same as the incoming record.
   */
  public void testWithMissingAttributesDiscardMatchesSetFalse() {
    ((SimpleRecordAttributeExistsProcessor) testProcessor).setDiscardMatches(false);
    recordMock.expects(once()).method("containsKey").with(eq(X)).will(returnValue(true));
    recordMock.expects(once()).method("containsKey").with(eq(Y)).will(returnValue(false));
    recordMock.expects(once()).method("containsKey").with(eq(Z)).will(returnValue(true));
    //recordMock.expects(once()).method("getRecord").will(returnValue(record));

    Object[] returnedRecords = null;
    try {
      returnedRecords = testProcessor.process(record);
    } catch (RecordFormatException e) {
      fail("Default configuration processor should discard records if missing a required attribute. Got RecordFormatException: "
          + e);
      // log.info("Got expected RecordFormatException. Got exception: [" + e +"]");
      // assertTrue("Expected to see ["+Y+"] in the exception message", e.getMessage().contains("["+Y+"]"));
    } catch (RecordException e) {
      fail("Shouldn't throw a RecordException. Got exception: [" + e + "]");
    } catch (Exception e) {
      fail("Shouldn't throw an Exception. Got exception: [" + e + "]");
    }

    assertTrue(
        "With discardMatches set false configuration processor should not discard records if missing a required attribute.",
        returnedRecords.length == 1);
    assertEquals("The returned record should be the same as the incoming record.", record, returnedRecords[0]);
  }

  /**
   * Test behaviour with missing attributes in the record and set to throw a RecordFormatException.
   * <p>
   * Should get a RecordFormatException with the missing attribute named in the message.
   */
  public void testWithMissingAttributesAndThrowExceptionSet() {
    recordMock.expects(once()).method("containsKey").with(eq(X)).will(returnValue(true));
    recordMock.expects(once()).method("containsKey").with(eq(Y)).will(returnValue(false));
    recordMock.expects(once()).method("containsKey").with(eq(Z)).will(returnValue(true));
    ((SimpleRecordAttributeExistsProcessor) testProcessor).setThrowExceptionOnMissingAttribute(true);

    try {
      testProcessor.process(record);
    } catch (RecordFormatException e) {
      log.info("Got expected RecordFormatException. Got exception: [" + e + "]");
      return;
    } catch (RecordException e) {
      fail("Shouldn't throw a RecordException. Got exception: [" + e + "]");
    } catch (Exception e) {
      fail("Shouldn't throw an Exception. Got exception: [" + e + "]");
    }
    fail("Should have thrown a RecordFormatException");

  }

  /**
   * Test behaviour with missing attributes in the record, set to throw a RecordFormatException and to discard matches.
   * <p>
   * Should get a RecordFormatException with the missing attribute named in the message. DiscardMatches should be
   * ignored.
   */
  public void testWithMissingAttributesAndThrowExceptionSetDiscardMatchesSetFalse() {
    ((SimpleRecordAttributeExistsProcessor) testProcessor).setDiscardMatches(false);
    recordMock.expects(once()).method("containsKey").with(eq(X)).will(returnValue(true));
    recordMock.expects(once()).method("containsKey").with(eq(Y)).will(returnValue(false));
    recordMock.expects(once()).method("containsKey").with(eq(Z)).will(returnValue(true));
    ((SimpleRecordAttributeExistsProcessor) testProcessor).setThrowExceptionOnMissingAttribute(true);

    try {
      testProcessor.process(record);
    } catch (RecordFormatException e) {
      log.info("Got expected RecordFormatException. Got exception: [" + e + "]");
      // assertTrue("Expected to see ["+Y+"] in the exception message", e.getMessage().contains("["+Y+"]"));
      return;
    } catch (RecordException e) {
      fail("Shouldn't throw a RecordException. Got exception: [" + e + "]");
    } catch (Exception e) {
      fail("Shouldn't throw an Exception. Got exception: [" + e + "]");
    }
    fail("Should have thrown a RecordFormatException");

  }

  /**
   * Test behaviour with missing attributes in the record and set to create missing attribute with a null value.
   * <p>
   * Record should have put(...) invoked with the appropriate arguments. No Exceptions should be thrown. Note that in
   * this case we expect the processor to act as a "Processor" rather than a "Filter". This means that we expect the
   * incoming record to be cloned and the clone to be modified. We also expect a processed return value from
   * processRecord.
   */
  public void testWithMissingAttributesAndCreateOnMissingAttributeSet() {
    recordMock.expects(once()).method("clone").will(returnValue(clonedRecord));
    clonedRecordMock.expects(once()).method("containsKey").with(eq(X)).will(returnValue(true));
    clonedRecordMock.expects(once()).method("containsKey").with(eq(Y)).will(returnValue(false));
    clonedRecordMock.expects(once()).method("containsKey").with(eq(Z)).will(returnValue(true));
    clonedRecordMock.expects(once()).method("put").with(eq(Y), NULL);
    //clonedRecordMock.expects(once()).method("getRecord").will(returnValue(clonedRecord));
    ((SimpleRecordAttributeExistsProcessor) testProcessor).setCreateOnMissingAttribute(true);

    Object[] returnedRecordArray = null;
    try {
      returnedRecordArray = testProcessor.process(record);
    } catch (RecordFormatException e) {
      fail("Shouldn't throw a RecordFormatException. Got exception: [" + e + "]");
      return;
    } catch (RecordException e) {
      fail("Shouldn't throw a RecordException. Got exception: [" + e + "]");
    } catch (Exception e) {
      fail("Shouldn't throw an Exception. Got exception: [" + e + "]");
    }
    assertNotNull("Returned array should not be null", returnedRecordArray);
    assertTrue("Returned array should be length one.", returnedRecordArray.length == 1);
    assertEquals("Returned record should be the cloned record", clonedRecord, returnedRecordArray[0]);
  }

  /**
   * Test behaviour with missing attributes in the record, set to create missing attribute with a null value and discard
   * matches false.
   * <p>
   * Record should have put(...) invoked with the appropriate arguments. No Exceptions should be thrown. Note that in
   * this case we expect the processor to act as a "Processor" rather than a "Filter". This means that we expect the
   * incoming record to be cloned and the clone to be modified. We also expect a processed return value from
   * processRecord.
   */
  public void testWithMissingAttributesAndCreateOnMissingAttributeSetDiscardMatchesFalse() {
    recordMock.expects(once()).method("clone").will(returnValue(clonedRecord));
    clonedRecordMock.expects(once()).method("containsKey").with(eq(X)).will(returnValue(true));
    clonedRecordMock.expects(once()).method("containsKey").with(eq(Y)).will(returnValue(false));
    clonedRecordMock.expects(once()).method("containsKey").with(eq(Z)).will(returnValue(true));
    clonedRecordMock.expects(once()).method("put").with(eq(Y), NULL);
    //clonedRecordMock.expects(once()).method("getRecord").will(returnValue(clonedRecord));
    ((SimpleRecordAttributeExistsProcessor) testProcessor).setCreateOnMissingAttribute(true);
    ((SimpleRecordAttributeExistsProcessor) testProcessor).setDiscardMatches(false);

    Object[] returnedRecordArray = null;
    try {
      returnedRecordArray = testProcessor.process(record);
    } catch (RecordFormatException e) {
      fail("Shouldn't throw a RecordFormatException. Got exception: [" + e + "]");
      return;
    } catch (RecordException e) {
      fail("Shouldn't throw a RecordException. Got exception: [" + e + "]");
    } catch (Exception e) {
      fail("Shouldn't throw an Exception. Got exception: [" + e + "]");
    }
    assertNotNull("Returned array should not be null", returnedRecordArray);
    assertTrue("Returned array should be length one.", returnedRecordArray.length == 1);
    assertEquals("Returned record should be the cloned record", clonedRecord, returnedRecordArray[0]);
  }

  //
  // Validation Tests
  //

  public void testValidateProperlyConfigured() {
    List exceptions = new ArrayList();
    testProcessor.validate(exceptions);
    assertTrue("Expected no validate exceptions.", exceptions.isEmpty());
  }

  public void testValidateMandatoryAttributesNull() {
    ((SimpleRecordAttributeExistsProcessor) testProcessor).setMandatoryAttributes(null);
    List exceptions = new ArrayList();
    testProcessor.validate(exceptions);
    assertTrue("Expected one validate exceptions.", exceptions.size() == 1);
  }

  public void testValidateThrowExceptionOnMissingAttributeSet() {
    ((SimpleRecordAttributeExistsProcessor) testProcessor).setThrowExceptionOnMissingAttribute(true);
    List exceptions = new ArrayList();
    testProcessor.validate(exceptions);
    assertTrue("Expected no validate exceptions.", exceptions.isEmpty());
  }

  public void testValidateCreateOnMissingAtributeSet() {
    ((SimpleRecordAttributeExistsProcessor) testProcessor).setCreateOnMissingAttribute(true);
    List exceptions = new ArrayList();
    testProcessor.validate(exceptions);
    assertTrue("Expected no validate exceptions.", exceptions.isEmpty());
  }

  public void testValidateBothSet() {
    ((SimpleRecordAttributeExistsProcessor) testProcessor).setCreateOnMissingAttribute(true);
    ((SimpleRecordAttributeExistsProcessor) testProcessor).setThrowExceptionOnMissingAttribute(true);
    List exceptions = new ArrayList();
    testProcessor.validate(exceptions);
    assertTrue("Expected one validate exceptions.", exceptions.size() == 1);
  }
}
