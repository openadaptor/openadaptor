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

import org.apache.log4j.Logger;
import org.oa3.auxil.expression.ExpressionException;
import org.oa3.core.IDataProcessor;
import org.oa3.core.exception.RecordException;

/**
 * Basic tests for AttributeSetProcessor.
 * 
 * @author Kevin Scully
 */
public class AttributeSetProcessorTestCase extends AbstractTestAttributeModifyProcessor {

  static Logger log = Logger.getLogger(AttributeSetProcessorTestCase.class);

  protected static final String TARGET_ATTRIBUTE_NAME = "target";

  protected static final String EXPRESSION_EVAL_RESULT = "id";

  /**
   * Test Processor is an instance of AttributeSetProcessor.
   * <P>
   * AttributeName is set to "target"<br>
   * ExpressionString is set to "'id'"/
   * 
   * @return The Test Processor.
   */
  protected IDataProcessor createProcessor() {
    AttributeSetProcessor processor = new AttributeSetProcessor();
    return processor;
  }

  /** Test setting an attribute named "target" with the value "id". */
  public void testProcessRecord() {
    // Set expectations
    getAttributeModifyProcessor().setAttributeName(TARGET_ATTRIBUTE_NAME);
    getAttributeModifyProcessor().setExpression(expression);
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(returnValue(EXPRESSION_EVAL_RESULT));
    recordMock.expects(once()).method("clone").will(returnValue(record));
    recordMock.expects(once()).method("getRecord").will(returnValue(record));
    recordMock.expects(once()).method("put").with(eq(getAttributeModifyProcessor().getAttributeName()),
        eq(EXPRESSION_EVAL_RESULT));
    // test
    try {
      testProcessor.process(record);
    } catch (RecordException e) {
      fail("Unexpected Exception [" + e + "]");
    }
  }

  /** See what happens when the expression throws an exception. We expect an ExpressionException thrown by the processor. */
  public void testExpressionThrowsException() {
    // Set expectations
    getAttributeModifyProcessor().setAttributeName(TARGET_ATTRIBUTE_NAME);
    getAttributeModifyProcessor().setExpression(expression);
    expressionMock.expects(once()).method("evaluate").with(eq(record)).will(
        throwException(new ExpressionException("Deliberately thrown by the mock expression.")));
    recordMock.expects(once()).method("clone").will(returnValue(record));
    recordMock.expects(never()).method("getRecord").will(returnValue(record));
    recordMock.expects(never()).method("put").with(eq(getAttributeModifyProcessor().getAttributeName()),
        eq(EXPRESSION_EVAL_RESULT));
    // test
    try {
      testProcessor.process(record);
    } catch (ExpressionException e) {
      // fail("Unexpected Exception [" + e + "]");
      log.info("Caught expected ExpressionException: [" + e + "]");
    } catch (Exception e) {
      fail("Unexpected Exception [" + e + "]");
    }
  }

  public void testValidateAllSet() {
    // Set Expectations
    getAttributeModifyProcessor().setAttributeName(TARGET_ATTRIBUTE_NAME);
    getAttributeModifyProcessor().setExpression(expression);
    // test
    List exceptions = new ArrayList();
    getAttributeModifyProcessor().validate(exceptions);
    assertTrue("Should have no validate exceptions", exceptions.isEmpty());
  }

  public void testValidateAttributeSet() {
    // Set Expectations
    getAttributeModifyProcessor().setAttributeName(TARGET_ATTRIBUTE_NAME);
    getAttributeModifyProcessor().setExpression(null);
    // test
    List exceptions = new ArrayList();
    getAttributeModifyProcessor().validate(exceptions);
    assertTrue("Should have one validate exception.", exceptions.size() == 1);
  }

  public void testValidateExpressionSet() {
    // Set Expectations
    getAttributeModifyProcessor().setAttributeName(null);
    getAttributeModifyProcessor().setExpression(expression);
    // test
    List exceptions = new ArrayList();
    getAttributeModifyProcessor().validate(exceptions);
    assertTrue("Should have one validate exception.", exceptions.size() == 1);
  }

  public void testValidateNothingSet() {
    // Set Expectations
    getAttributeModifyProcessor().setAttributeName(null);
    getAttributeModifyProcessor().setExpression(null);
    // test
    List exceptions = new ArrayList();
    getAttributeModifyProcessor().validate(exceptions);
    assertTrue("Should have 2 validate exception.", exceptions.size() == 2);
  }
}
