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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Mock;
import org.openadaptor.auxil.expression.Expression;
import org.openadaptor.auxil.expression.ExpressionException;
import org.openadaptor.auxil.expression.IExpression;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic tests for NewFilterProcessor.
 * 
 * @author Kevin Scully
 */
public class FilterProcessorTestCase extends AbstractTestAbstractSimpleRecordProcessor {

  private static final Log log = LogFactory.getLog(FilterProcessorTestCase.class);

  protected static final String REFERENCE_EXPRESSION_STRING = "{attributeName} = 'joe bloggs'";

  protected Mock expressionMock;

  protected IExpression expression;

  /**
   * Instantiate the mock test objects. Used by setUp().
   */
  protected void createMocks() {
    super.createMocks();
    // Set up condition mocks
    expressionMock = mock(IExpression.class);
    expression = (IExpression) expressionMock.proxy();
  }

  protected void deleteMocks() {
    super.deleteMocks();
    expressionMock = null;
    expression = null;
  }

  /**
   * Override to create the basic test processor instance.
   * 
   * @return The test processor.
   */
  protected IDataProcessor createProcessor() {
    FilterProcessor processor = new FilterProcessor();
    initialiseTestProcessor(processor);
    return processor;
  }

  protected void initialiseTestProcessor(FilterProcessor processor) {
    processor.setFilterExpression(expression);
  }

  //
  // Tests
  //

  /**
   * Implement to perform the basic process record functionality.
   * <p>
   * Condition is configured to return true and everything else set as default. Record should be discarded.
   */
  public void testProcessAccessorSet() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(simpleRecordAccessor);
    recordMock.expects(never()).method("getRecord"); // Matches are discarded
    simpleRecordAccessorMock.expects(once()).method("asSimpleRecord").with(eq(record)).will(returnValue(record));
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(true)); // Expect evaluating the
    Object[] returnedArray = null;
    try {
      returnedArray = testProcessor.process(record);
    } catch (RecordException e) {
      fail("Unexpected Exception: [" + e + "]");
    } catch (NullPointerException npe) {
      fail("Unexpected NullPointerException: [" + npe + "]");
    }
    assertTrue("Expected an array with one record", (returnedArray.length == 0));
  }

  public void testProcessNoAccessorSet() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(null);
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(true)); // Expect evaluating the
    Object[] returnedArray = null;
    try {
      returnedArray = testProcessor.process(record);
    } catch (RecordException e) {
      fail("Unexpected Exception: [" + e + "]");
    } catch (NullPointerException npe) {
      fail("Unexpected NullPointerException: [" + npe + "]");
    }
    assertTrue("Expected an array with one record", (returnedArray.length == 0));

  }

  /**
   * Implement to perform the basic process record functionality.
   * <p>
   * Condition is configured to return true and discardMatches set to false and everything else set as default. Record
   * should be passed.
   */
  public void testConditionTrueDiscardMatchesFalse() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(null);
    ((FilterProcessor) testProcessor).setDiscardMatches(false); // true is the default.
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(true));
    Object[] returnedArray = null;
    try {
      returnedArray = testProcessor.process(record);
    } catch (RecordException e) {
      fail("Unexpected Exception: [" + e + "]");
    } catch (NullPointerException npe) {
      fail("Unexpected NullPointerException: [" + npe + "]");
    }
    assertTrue("Expected an array with one record", (returnedArray.length == 1));
    assertTrue("Expected outgoing record to be identical to incoming record", (record == returnedArray[0]));
  }

  /**
   * Test set to return true, discardMatches to true.
   * <p>
   * Expect the record to be discarded.
   */
  public void testConditionTrueDiscardMatchesTrue() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(null);
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(true)); // Expect evaluating the
    recordMock.expects(never()).method("getRecord");
    Object[] returnedArray = null;
    try {
      returnedArray = testProcessor.process(record);
    } catch (RecordException e) {
      fail("Unexpected Exception: [" + e + "]");
    } catch (NullPointerException npe) {
      fail("Unexpected NullPointerException: [" + npe + "]");
    }
    assertTrue("Return value from processor should be either null or an empty array.", (returnedArray == null)
        || (returnedArray.length == 0));
  }

  /**
   * Test set to return false, discardMatches to false.
   * <p>
   * Expect the record to be discarded.
   */
  public void testConditionFalseDiscardMatchesFalse() {
    ((FilterProcessor) testProcessor).setDiscardMatches(false); // true is the default
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(false));
    recordMock.expects(never()).method("getRecord");
    Object[] returnedArray = null;
    try {
      returnedArray = testProcessor.process(record);
    } catch (RecordException e) {
      fail("Unexpected Exception: [" + e + "]");
    } catch (NullPointerException npe) {
      fail("Unexpected NullPointerException: [" + npe + "]");
    }
    assertTrue("Return value from processor should be either null or an empty array.", (returnedArray == null)
        || (returnedArray.length == 0));
  }

  /**
   * Test set to return false, discardMatches to true.
   * <p>
   * Expect the record to be passed.
   */
  public void testConditionFalseDiscardMatchesTrue() {
    getAbstractSimpleRecordProcessor().setSimpleRecordAccessor(null);
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(false));
    recordMock.expects(never()).method("getRecord");
    Object[] returnedArray = null;
    try {
      returnedArray = testProcessor.process(record);
    } catch (RecordException e) {
      fail("Unexpected Exception: [" + e + "]");
    } catch (NullPointerException npe) {
      fail("Unexpected NullPointerException: [" + npe + "]");
    }
    assertTrue("Expected an array with one record", (returnedArray.length == 1));
    assertTrue("Expected outgoing record to be identical to incoming record", (record == returnedArray[0]));
  }

  /**
   * Test behaviour when condition expression evaluates to null.
   * <p>
   * Expect an ExpressionException.
   */
  public void testConditionEvaluatesToNull() {
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(null));
    try {
      testProcessor.process(record);
    } catch (ExpressionException ee) {
      log.info("Got expected ExpresionException with message: [" + ee.getMessage() + "]");
      return; // This is what we expected
    } catch (Exception e) {
      fail("Unexpected Exception: [" + e + "]");
    }
    fail("Didn't get expected ExpressionException.");
  }

  /**
   * Test behaviour when condition expression evaluates to object not a boolean.
   * <p>
   * Expect an ExpressionException.
   */
  public void testConditionEvaluatesToObject() {
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(new Object()));
    try {
      testProcessor.process(record);
    } catch (ExpressionException ee) {
      log.info("Got expected ExpresionException with message: [" + ee.getMessage() + "]");
      return; // This is what we expected
    } catch (Exception e) {
      fail("Unexpected Exception: [" + e + "]");
    }
    fail("Didn't get expected ExpressionException.");
  }

  /**
   * Check if setFilterExpressionString produces the expression expected.
   */
  public void testSetFilterExpressionString() {
    String expressionString = REFERENCE_EXPRESSION_STRING;
    IExpression referenceExpression = new Expression();
    try {
      referenceExpression.setExpression(expressionString);
    } catch (ExpressionException e) {
      fail("Error setting reference expression string. Exception [" + e + "]");
    }

    FilterProcessor processor = (FilterProcessor) testProcessor;
    try {
      processor.setFilterExpressionString(expressionString);
    } catch (ExpressionException e) {
      fail("Error setting processor filter expression string. Exception [" + e + "]");
    }

    // NB Testing equality of the expression's strings 'cos equality for expressions isn't defined.
    assertEquals("The reference expression and the processor filter expression should be equal.", referenceExpression
        .getExpression(), processor.getFilterExpression().getExpression());
  }

  /** Test validate returns ok when processor correctly configured. */
  public void testValidate() {
    FilterProcessor processor = (FilterProcessor) testProcessor;
    processor.setFilterExpression(expression);

    List exceptions = new ArrayList();
    processor.validate(exceptions);
    assertTrue("Expected no validate exceptions.", exceptions.isEmpty());
  }

  /** Test validate returns one exception when expression is not configured. */
  public void testValidateNoExpression() {
    FilterProcessor processor = (FilterProcessor) testProcessor;
    processor.setFilterExpression(null);

    List exceptions = new ArrayList();
    processor.validate(exceptions);
    assertTrue("Expected one validate exceptions, got " + exceptions.size(), exceptions.size() == 1);
  }

  /**
   * Test behaviour on using a NewFilterProcessor not with configured with an expression.
   * <p>
   * Expect validate to return one OAException with message "property filterExpression is mandatory".
   */
  public void testValidateNullCondition() {
    ((FilterProcessor) testProcessor).setFilterExpression(null);
    List exceptions = new ArrayList();
    testProcessor.validate(exceptions);
    assertTrue("Expected one validate exceptions.", exceptions.size() == 1);
    Exception e = (Exception) exceptions.get(0);
    assertTrue("Expect validate to return one OAException", e instanceof ValidationException);
  }

}
