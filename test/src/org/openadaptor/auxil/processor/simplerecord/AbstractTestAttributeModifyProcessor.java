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

import org.jmock.Mock;
import org.oa3.auxil.expression.Expression;
import org.oa3.auxil.expression.ExpressionException;
import org.oa3.auxil.expression.IExpression;

/**
 * Abstract tests and test utilities common to AttributeModifyProcessors.
 * 
 * @author Kevin Scully
 */
abstract public class AbstractTestAttributeModifyProcessor extends AbstractTestAbstractSimpleRecordProcessor {

  protected static final String REFERENCE_EXPRESSION_STRING = "{attributeName} = 'joe bloggs'";

  protected Mock expressionMock;

  protected IExpression expression;

  /**
   * Instantiate the mock test objects. Used by setUp().
   */
  protected void createMocks() {
    super.createMocks();

    // Expression Mock
    expressionMock = mock(IExpression.class);
    expression = (IExpression) expressionMock.proxy();
  }

  protected void deleteMocks() {
    super.deleteMocks();
    expressionMock = null;
    expression = null;
  }

  /**
   * Utility method that returns the test processor cast to an AttributeModifyProcessor.
   * 
   * @return test processor cast to an AttributeModifyProcessor.
   */
  protected AttributeModifyProcessor getAttributeModifyProcessor() {
    return (AttributeModifyProcessor) testProcessor;
  }

  /**
   * Chsck if setExpressionString produces teh expression expected.
   */
  public void testSetExpressionString() {
    String expressionString = REFERENCE_EXPRESSION_STRING;
    IExpression referenceExpression = new Expression();
    try {
      referenceExpression.setExpression(expressionString);
    } catch (ExpressionException e) {
      fail("Error setting expression string. Exception [" + e + "]");
    }

    AttributeModifyProcessor processor = getAttributeModifyProcessor();
    try {
      processor.setExpressionString(expressionString);
    } catch (ExpressionException e) {
      fail("Error setting processor expression string. Exception [" + e + "]");
    }

    // NB Testing equality of the expression's strings 'cos equality for expressions isn't defined.
    assertEquals("The reference expression and the processor expression should be equal.", referenceExpression
        .getExpression(), processor.getExpression().getExpression());
  }
}
