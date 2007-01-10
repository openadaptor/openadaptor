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
package org.oa3.auxil.processor.simplerecord;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Mock;
import org.oa3.auxil.expression.Expression;
import org.oa3.auxil.expression.ExpressionException;
import org.oa3.auxil.expression.IExpression;
import org.oa3.core.IDataProcessor;
import org.oa3.core.exception.RecordException;

/**
 * Tests for the ConditionProcessor.
 */
public class ConditionProcessorTestCase extends AbstractTestAbstractSimpleRecordProcessor {

  private static final Log log = LogFactory.getLog(ConditionProcessorTestCase.class);

  protected static final String REFERENCE_EXPRESSION_STRING = "{attributeName} = 'joe bloggs'";

  protected Mock thenProcessorMock;

  protected IDataProcessor thenProcessor;

  protected Mock elseProcessorMock;

  protected IDataProcessor elseProcessor;

  protected Mock expressionMock;

  protected IExpression expression;

  /**
   * Instantiate the mock test objects. Used by setUp().
   */
  protected void createMocks() {
    super.createMocks();
    // Mock IDataProcessor for use as the Then Processor.
    thenProcessorMock = mock(IDataProcessor.class);
    thenProcessor = (IDataProcessor) thenProcessorMock.proxy();
    // Mock IDataProcessor for use as the Else Processor.
    elseProcessorMock = mock(IDataProcessor.class);
    elseProcessor = (IDataProcessor) elseProcessorMock.proxy();
    // Expression Mock
    expressionMock = mock(IExpression.class);
    expression = (IExpression) expressionMock.proxy();
  }

  protected void deleteMocks() {
    super.deleteMocks();
    thenProcessorMock = null;
    thenProcessor = null;
    elseProcessorMock = null;
    elseProcessor = null;
    expressionMock = null;
    expression = null;
  }

  /**
   * Override to create the basic test processor instance.
   * 
   * @return The test processor.
   */
  protected IDataProcessor createProcessor() {
    ConditionProcessor processor = new ConditionProcessor();
    processor.setIfExpression(expression);
    processor.setThenProcessor(thenProcessor);
    processor.setElseProcessor(elseProcessor);
    return processor;
  }

  /**
   * Test the basic process record functionality. In this case the 'then' functionality. This is the only test which
   * tests with an accessor set and tests that the accessor is used and that the corresponding getRecord() call is made.
   * All other tests use a mock ISimpleRecord as data and don't assume anything about accessors.
   */
  public void testProcessAccessorSet() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(simpleRecordAccessor);
    simpleRecordAccessorMock.expects(once()).method("asSimpleRecord").with(eq(record)).will(returnValue(record));
    recordMock.expects(atLeastOnce()).method("getRecord").will(returnValue(record));
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(true));
    thenProcessorMock.expects(once()).method("process").with(eq(record)).will(returnValue(new Object[] { record }));
    elseProcessorMock.expects(never()).method("process");
    try {
      testProcessor.process(record);
    } catch (RecordException e) {
      fail("Unexpected Exception: [" + e + "]");
    }
  }

  public void testProcessNoAccessorSet() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(null);
    recordMock.expects(never()).method("getRecord");
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(true));
    thenProcessorMock.expects(once()).method("process").with(eq(record)).will(returnValue(new Object[] { record }));
    elseProcessorMock.expects(never()).method("process");
    try {
      testProcessor.process(record);
    } catch (RecordException e) {
      fail("Unexpected Exception: [" + e + "]");
    }
  }

  /**
   * Ensure that the else processor is exercised if the condition evaluates to false.
   */
  public void testProcessElsePath() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(null);
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(false));
    thenProcessorMock.expects(never()).method("process");
    elseProcessorMock.expects(once()).method("process").with(eq(record)).will(returnValue(new Object[] { record }));
    try {
      testProcessor.process(record);
    } catch (RecordException e) {
      fail("Unexpected Exception: [" + e + "]");
    }
  }

  /**
   * Test that the correct exceptions are raised when the test expression evaluates to null. <p/> We expect the
   * ConditionProcessor to throw an ExpressionException.
   */
  public void testConditionEvaluatesToNull() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(null);
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(null));
    thenProcessorMock.expects(never()).method("process");
    elseProcessorMock.expects(never()).method("process");
    try {
      testProcessor.process(record);
    } catch (ExpressionException e) {
      log.info("Got ExpressionException with message: [" + e.getMessage() + "]");
      return; // This is what we expected.
    } catch (RecordException e) {
      fail("Unexpected RecordException: [" + e + "]");
    }
    fail("Expected an ExpressionException");
  }

  /**
   * Test that the correct exceptions are raised when the test expression evaluates to something that isn't a boolean.
   * <p/> We expect the ConditionProcessor to throw an ExpressionException.
   */
  public void testConditionEvaluatesToNonBooleanObject() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(null);
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(new Object()));
    thenProcessorMock.expects(never()).method("process");
    elseProcessorMock.expects(never()).method("process");
    try {
      testProcessor.process(record);
    } catch (ExpressionException e) {
      log.info("Got ExpressionException with message: [" + e.getMessage() + "]");
      return; // This is what we expected.
    } catch (RecordException e) {
      fail("Unexpected RecordException: [" + e + "]");
    }
    fail("Expected an ExpressionException");
  }

  /**
   * Test that an ExpressionException thrown by an Expression Object is handled properly. <p/> We expect the
   * ConditionProcessor to pass on the ExpressionException.
   */
  public void testConditionThrowsExpressionException() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(null);
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(
        throwException(new ExpressionException("Thrown by Mock Expression")));
    thenProcessorMock.expects(never()).method("process");
    elseProcessorMock.expects(never()).method("process");
    try {
      testProcessor.process(record);
    } catch (ExpressionException e) {
      log.info("Got ExpressionException with message: [" + e.getMessage() + "]");
      return; // This is what we expected.
    } catch (RecordException e) {
      fail("Unexpected RecordException: [" + e + "]");
    }
    fail("Expected an ExpressionException");
  }

  /**
   * Test that a NullPointerException thrown by an Expression Object is handled properly. <p/> We expect the
   * ConditionProcessor to pass on the NullPointerException.
   */
  public void testConditionThrowsNullPointerException() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(null);
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(
        throwException(new NullPointerException("Thrown by Mock Expression")));
    thenProcessorMock.expects(never()).method("process");
    elseProcessorMock.expects(never()).method("process");
    try {
      testProcessor.process(record);
    } catch (RecordException e) {
      fail("Unexpected RecordException: [" + e + "]");
    } catch (NullPointerException npe) {
      log.info("Got NullPointerException with message: [" + npe.getMessage() + "]");
      return; // This is what we expected.
    }

    fail("Expected a NullPointerException");
  }

  /**
   * Test that what happens when the Then Processor throws a RecordException is as expected. <p/> We expect the
   * ConditionProcessor to pass on the RecordException.
   */
  public void testThenProcessorThowsRecordException() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(null);
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(true));
    thenProcessorMock.expects(once()).method("process").with(eq(record)).will(
        throwException(new RecordException("Exception thrown by Then Processor")));
    elseProcessorMock.expects(never()).method("process").with(eq(record));
    try {
      testProcessor.process(record);
    } catch (RecordException e) {
      log.info("Got RecordException with message: [" + e.getMessage() + "]");
      return; // This is what we expected.
    }
    fail("Expected a RecordException");
  }

  /**
   * Test that what happens when the Else Processor throws a RecordException is as expected. <p/> We expect the
   * ConditionProcessor to pass on the RecordException.
   */
  public void testElseProcessorThrowsRecordException() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(null);
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(false));
    thenProcessorMock.expects(never()).method("process").with(eq(record));
    elseProcessorMock.expects(once()).method("process").with(eq(record)).will(
        throwException(new RecordException("Exception thrown by Else Processor")));
    try {
      testProcessor.process(record);
    } catch (RecordException e) {
      log.info("Got RecordException with message: [" + e.getMessage() + "]");
      return; // This is what we expected.
    }
    fail("Expected a RecordException");
  }

  /**
   * Test that what happens when the Then Processor throws a NullPointerException is as expected. <p/> We expect the
   * ConditionProcessor to pass on the NullPointerException.
   */
  public void testThenProcessorThrowsNullPointerException() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(null);
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(true));
    thenProcessorMock.expects(once()).method("process").with(eq(record)).will(
        throwException(new NullPointerException("NullPointerException thrown by Then Processor")));
    elseProcessorMock.expects(never()).method("process").with(eq(record));
    try {
      testProcessor.process(record);
    } catch (RecordException e) {
      fail("Got RecordException with message: [" + e.getMessage() + "]");
    } catch (NullPointerException npe) {
      log.info("Got NullPointerException with message: [" + npe.getMessage() + "]");
      return; // This is what we expected.
    }
    fail("Expected a RecordException");
  }

  /**
   * Test that what happens when the Then Processor throws a NullPointerException is as expected. <p/> We expect the
   * ConditionProcessor to pass on the NullPointerException.
   */
  public void testElseProcessorThrowsNullPointerException() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(null);
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(false));
    thenProcessorMock.expects(never()).method("process").with(eq(record));
    elseProcessorMock.expects(once()).method("process").with(eq(record)).will(
        throwException(new NullPointerException("NullPointerException thrown by Else Processor")));
    try {
      testProcessor.process(record);
    } catch (RecordException e) {
      fail("Got RecordException with message: [" + e.getMessage() + "]");
    } catch (NullPointerException npe) {
      log.info("Got NullPointerException with message: [" + npe.getMessage() + "]");
      return; // This is what we expected.
    }
    fail("Expected a NullPointerException");
  }

  /**
   * Check if setIfExpressionString produces the expression expected.
   */
  public void testSetIfExpressionString() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(null);
    String expressionString = REFERENCE_EXPRESSION_STRING;
    IExpression referenceExpression = new Expression();
    try {
      referenceExpression.setExpression(expressionString);
    } catch (ExpressionException e) {
      fail("Error setting reference expression string. Exception [" + e + "]");
    }

    ConditionProcessor processor = (ConditionProcessor) testProcessor;
    try {
      processor.setifExpressionString(expressionString);
    } catch (ExpressionException e) {
      fail("Error setting processor if expression string. Exception [" + e + "]");
    }

    // NB Testing equality of the expression's strings 'cos equality for expressions isn't defined.
    assertEquals("The reference expression and the processor if expression should be equal.", referenceExpression
        .getExpression(), processor.getIfExpression().getExpression());
  }

  // Tests for processing given various null processors.

  /**
   * Check that record is passed through if expression is false and else processor null.
   */
  public void testPassThruOnNullElseProcessor() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(null);
    ConditionProcessor processor = (ConditionProcessor) testProcessor;
    processor.setElseProcessor(null);
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(false));
    thenProcessorMock.expects(never()).method("process");

    Object[] processedRecords = new Object[] {};
    try {
      processedRecords = testProcessor.process(record);
    } catch (RecordException e) {
      fail("Got RecordException with message: [" + e.getMessage() + "]");
    }

    assertTrue("This test should return just one record.", processedRecords.length == 1);
    assertEquals("Record should have passed through untouched.", record, processedRecords[0]);
  }

  /**
   * Check that record is processed if expression is true and else processor null.
   */
  public void testProcessViaThenOnNullElseProcessor() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(null);
    ConditionProcessor processor = (ConditionProcessor) testProcessor;
    processor.setElseProcessor(null);
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(true));
    thenProcessorMock.expects(once()).method("process").with(eq(record)).will(
        returnValue(new Object[] { clonedRecord })); // Proves pprocess record was called

    Object[] processedRecords = new Object[] {};
    try {
      processedRecords = testProcessor.process(record);
    } catch (RecordException e) {
      fail("Got RecordException with message: [" + e.getMessage() + "]");
    }

    assertTrue("This test should return just one record.", processedRecords.length == 1);
    assertEquals("Record should have been processed.", clonedRecord, processedRecords[0]);
  }

  /**
   * Check that record is passed through if expression is true and then processor null.
   */
  public void testPassThruOnNullThenProcessor() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(null);
    ConditionProcessor processor = (ConditionProcessor) testProcessor;
    processor.setThenProcessor(null);
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(true));
    elseProcessorMock.expects(never()).method("process");

    Object[] processedRecords = new Object[] {};
    try {
      processedRecords = testProcessor.process(record);
    } catch (RecordException e) {
      fail("Got RecordException with message: [" + e.getMessage() + "]");
    }

    assertTrue("This test should return just one record.", processedRecords.length == 1);
    assertEquals("Record should have passed through untouched.", record, processedRecords[0]);
  }

  /**
   * Check that record is processed via else if expression is false and then processor null.
   */
  public void testProcessViaElseOnNullThenProcessor() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(null);
    ConditionProcessor processor = (ConditionProcessor) testProcessor;
    processor.setThenProcessor(null);
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(false));
    // Proves process record was called
    elseProcessorMock.expects(once()).method("process").with(eq(record)).will(
        returnValue(new Object[] { clonedRecord }));

    Object[] processedRecords = new Object[] {};
    try {
      processedRecords = testProcessor.process(record);
    } catch (RecordException e) {
      fail("Got RecordException with message: [" + e.getMessage() + "]");
    }

    assertTrue("This test should return just one record.", processedRecords.length == 1);
    assertEquals("Record should have been processed.", clonedRecord, processedRecords[0]);
  }

  /**
   * Test what happens to processing if both procesors are null and expression true.
   */
  public void testProcessIfBothProcessorsNullExpressionTrue() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(null);
    ConditionProcessor processor = (ConditionProcessor) testProcessor;
    processor.setThenProcessor(null);
    processor.setElseProcessor(null);
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(true));

    Object[] processedRecords = new Object[] {};
    try {
      processedRecords = testProcessor.process(record);
    } catch (RecordException e) {
      fail("Got RecordException with message: [" + e.getMessage() + "]");
    }
    assertTrue("This test should return just one record.", processedRecords.length == 1);
    assertEquals("Record should have passed through untouched.", record, processedRecords[0]);
  }

  /**
   * Test what happens to processing if both processors are null and expression false.
   */
  public void testProcessIfBothProcessorsNullExpressionFalse() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(null);
    ConditionProcessor processor = (ConditionProcessor) testProcessor;
    processor.setThenProcessor(null);
    processor.setElseProcessor(null);
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(false));

    Object[] processedRecords = new Object[] {};
    try {
      processedRecords = testProcessor.process(record);
    } catch (RecordException e) {
      fail("Got RecordException with message: [" + e.getMessage() + "]");
    }
    assertTrue("This test should return just one record.", processedRecords.length == 1);
    assertEquals("Record should have passed through untouched.", record, processedRecords[0]);
  }

  // Test validation options

  /**
   * Test Validation with fully configured processor. For ConditionProcessor this means setting the ifExpression and
   * both of the processors.
   */
  public void testValidation() {
    ConditionProcessor processor = (ConditionProcessor) testProcessor;
    processor.setIfExpression(expression);
    processor.setThenProcessor(null);
    processor.setElseProcessor(elseProcessor);

    List exceptions = new ArrayList();
    processor.validate(exceptions);
    assertTrue("Should have no validate exceptions", exceptions.isEmpty());
  }

  /**
   * Test Validation with minimally configured processor. For ConditionProcessor this means setting the ifExpression and
   * one of the processors. For this test we set the thenProcessor.
   */
  public void testValidationThenSet() {
    ConditionProcessor processor = (ConditionProcessor) testProcessor;
    processor.setIfExpression(expression);
    processor.setThenProcessor(thenProcessor);
    processor.setElseProcessor(null);

    List exceptions = new ArrayList();
    processor.validate(exceptions);
    assertTrue("Should have no validate exceptions", exceptions.isEmpty());
  }

  /**
   * Test Validation with minimally configured processor. For ConditionProcessor this means setting the ifExpression and
   * one of the processors. For this test we set the elseProcessor.
   */
  public void testValidationElseSet() {
    ConditionProcessor processor = (ConditionProcessor) testProcessor;
    processor.setIfExpression(expression);
    processor.setThenProcessor(null);
    processor.setElseProcessor(elseProcessor);

    List exceptions = new ArrayList();
    processor.validate(exceptions);
    assertTrue("Should have no validate exceptions", exceptions.isEmpty());
  }

  /**
   * Test Validation with procesor configured with no IfExpression and no processors. Should fail on two counts.
   */
  public void testValidationNoIfExpression() {
    ConditionProcessor processor = (ConditionProcessor) testProcessor;
    processor.setIfExpression(null);
    processor.setThenProcessor(null);
    processor.setElseProcessor(null);

    List exceptions = new ArrayList();
    processor.validate(exceptions);
    assertTrue("Expected two validate exceptions.", exceptions.size() == 2);
  }

  /**
   * Test Validation with procesor configured with no IfExpression and one processor set. Should fail on one count.
   */
  public void testValidationNoIfExpressionThenProcessorSet() {
    ConditionProcessor processor = (ConditionProcessor) testProcessor;
    processor.setIfExpression(null);
    processor.setThenProcessor(thenProcessor);
    processor.setElseProcessor(null);

    List exceptions = new ArrayList();
    processor.validate(exceptions);
    assertTrue("Expected two validate exceptions.", exceptions.size() == 1);
  }

  /**
   * Test Validation with procesor configured with no IfExpression and one processor set. Should fail on one count.
   */
  public void testValidationNoIfExpressionElseProcessorSet() {
    ConditionProcessor processor = (ConditionProcessor) testProcessor;
    processor.setIfExpression(null);
    processor.setThenProcessor(null);
    processor.setElseProcessor(thenProcessor);

    List exceptions = new ArrayList();
    processor.validate(exceptions);
    assertTrue("Expected one validate exceptions.", exceptions.size() == 1);
  }

}
