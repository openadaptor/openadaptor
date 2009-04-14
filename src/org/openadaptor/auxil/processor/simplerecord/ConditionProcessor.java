/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in the
 Software without restriction, including without limitation the rights to use, copy,
 modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 and to permit persons to whom the Software is furnished to do so, subject to the
 following conditions:

 The above copyright notice and this permission notice shall be included in all 
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Nothing in this notice shall be deemed to grant any rights to trademarks, copyrights,
 patents, trade secrets or any other intellectual property of the licensor or any
 contributor except as expressly stated herein. No patent license is granted separate
 from the Software, for code that you delete from the Software, or for combinations
 of the Software with other software or hardware.
*/

package org.openadaptor.auxil.processor.simplerecord;

import java.util.List;

import org.openadaptor.auxil.expression.Expression;
import org.openadaptor.auxil.expression.ExpressionException;
import org.openadaptor.auxil.expression.IExpression;
import org.openadaptor.auxil.simplerecord.AbstractSimpleRecordProcessor;
import org.openadaptor.auxil.simplerecord.ISimpleRecord;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.ValidationException;

/**
 * Modifies a SimpleRecord based on the result of evaluating a conditional expression. <p/> The three main parts of a
 * ConditionProcessor are the <b>ifExpression</b>, the <b>thenProcessor</b> and the <b>elseProcessor</b>. <p/> Based
 * on whether the condition expression evaluates to true or false, the incoming record is passed to either the
 * <b>thenProcessor</b> or the <b>elseProcessor</b>. These two processors can be any <code>IDataProcessor</code>
 * implementation. <p/> No further action will be taken if one of <b>thenProcessor</b> or <b>elseProcessor</b> is
 * selected, but is <tt>null</tt>.
 * 
 * @author Eddy Higgins
 */
public class ConditionProcessor extends AbstractSimpleRecordProcessor {

  //private static final Log log = LogFactory.getLog(ConditionProcessor.class);

  /**
   * The expression to be evaluated. Must generate a Boolean result.
   */
  protected IExpression ifExpression;

  /**
   * processor used if ifExpression evaluates to true.
   */
  protected IDataProcessor thenProcessor;

  /**
   * processor used if ifExpression evaluates to false.
   */
  protected IDataProcessor elseProcessor;

  // BEGIN Bean getters/setters

  /**
   * This specifies the expression to be evaluated. <p/> The expression must generate a <code>Boolean</code> result.
   * 
   * @param ifExpression
   *          The conditional expression which will be evaluated.
   */
  public void setIfExpression(IExpression ifExpression) {
    this.ifExpression = ifExpression;
  }

  /**
   * The conditional expression which will be evaluated.
   * 
   * @return The conditional expression which will be evaluated.
   */
  public IExpression getIfExpression() {
    return ifExpression;
  }

  /**
   * Convenience accessor for setExpression, which only specifies the expression String. <p/> If other expression
   * properties (e.g. type mapping) are required, then the alternative accessor should be used, or the properties must
   * be set subsequently through other means (programmatic or whatever) <p/> The expression must generate a
   * <code>Boolean</code> result.
   * 
   * @param ifExpressionString
   *          String containing the expression to be evaluated.
   * @throws ExpressionException
   *           if an <code>IExpression</code> cannot be created from the expression String.
   */
  public void setIfExpressionString(String ifExpressionString) throws ExpressionException {
    setIfExpression(Expression.createExpressionFromString(ifExpressionString));
  }

  /**
   * Assign processor which will get called if the expression evaluates <tt>true</tt>. <p/> It may be null, resulting
   * in no action.
   * 
   * @param thenProcessor
   *          <code>IDataProcessor</code> instance
   */
  public void setThenProcessor(IDataProcessor thenProcessor) {
    this.thenProcessor = thenProcessor;
  }

  /**
   * Return processor which will get called if the expression evaluates <tt>true</tt>. <p/> It may be null, resulting
   * in no action.
   * 
   * @return <code>IDataProcessor</code> instance, or <tt>null</tt>
   */
  public IDataProcessor getThenProcessor() {
    return thenProcessor;
  }

  /**
   * Assign processor which will get called if the expression evaluates <tt>false</tt>. <p/> It may be null,
   * resulting in no action.
   * 
   * @param elseProcessor
   *          <code>IDataProcessor</code> instance
   */
  public void setElseProcessor(IDataProcessor elseProcessor) {
    this.elseProcessor = elseProcessor;
  }

  /**
   * Return processor which will get called if the expression evaluates <tt>false</tt>. <p/> It may be null,
   * resulting in no action.
   * 
   * @return <code>IDataProcessor</code> instance, or <tt>null</tt>
   */

  public IDataProcessor getElseProcessor() {
    return elseProcessor;
  }

  // END Bean getters/setters

  /**
   * Evaluate 'ifExpression', and call 'thenProcessor' or 'elseProcessor' as appropriate.
   * 
   * @param simpleRecord
   *          <code>ISimpleRecord</code> against which expression is evaluated and subsequent processor
   *          ('thenProcessor' or 'elseProcessor') is invoked.
   * @param alreadyCloned
   *          flag to indicate whenter this record has alread been cloned. Ignored here as this processor doesn't modify
   *          the record, so we don't care.
   * @return Object[] with results of processing.
   * @throws RecordException
   */
  public Object[] processSimpleRecord(ISimpleRecord simpleRecord, boolean alreadyCloned) throws RecordException {
    if (testCondition(simpleRecord)) {
      return thenProcessor == null ? new Object[] { simpleRecord } : thenProcessor.process(simpleRecord);
    } else {
      return elseProcessor == null ? new Object[] { simpleRecord } : elseProcessor.process(simpleRecord);
    }
  }

  public void validate(List exceptions) {
    super.validate(exceptions);

    Exception e = checkMandatoryProperty("ifExpression", ifExpression != null);
    if (e != null) {
      exceptions.add(e);
    }
    e = checkAtLeastOneOfProperty(new String[] { "thenProcessor", "elseProcessor" }, new boolean[] {
        (thenProcessor != null), (elseProcessor != null) });
    if (e != null) {
      exceptions.add(e);
    }
  }

  /**
   * Loops through the list of property names supplied and if the corresponding test fails then adds the name to a list
   * of failures and returns an exception. For example: <p/> <blockquote>
   * 
   * <pre>
   * ComponentException e = checkAtLeastOneOfProperty(new String[] { attributeName, &quot;expression&quot; }, new boolean[] {
   *     attributeName != null, expression != null });
   * if (e != null)
   *   throw e;
   * </pre>
   * 
   * </blockquote> <p/> The exception would contain the message "At least one of attributeName,expression must be set"
   * (if both failed the tests).
   * 
   * @param names
   *          array of attribute names
   * @param tests
   *          array of test (eg. attribute != null )
   * @return ComponentException containing the list of attributes test. or null
   */
  protected Exception checkAtLeastOneOfProperty(String[] names, boolean[] tests) {
    Exception result = null;
    int count = 0;
    for (int i = 0; i < names.length; i++) {
      if (tests[i])
        count++;
    }

    if (count < 1) {
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < names.length - 1; i++)
        sb.append(names[i]).append(",");

      sb.append(names[names.length - 1]);

      result = new ValidationException("At least one of " + sb.toString() + " must be set.", this);
    }

    return result;
  }

  /**
   * Return a <code>String</code> view of this condition 'statement'. <p/> Returns pseudo-code which is java-like to a
   * point.
   * 
   * @return <code>String</code> representation of this condition.
   */
  public String toString() {
    StringBuffer sb = new StringBuffer("if (");
    sb.append(conditionAsString());
    sb.append(") then (");
    sb.append(thenProcessor == null ? "<null>" : thenProcessor.toString());
    sb.append(") else (");
    sb.append(elseProcessor == null ? "<null>" : elseProcessor.toString());
    return sb.append(")").toString();
  }

  /**
   * Test the condition expression against an <code>ISimpleRecord</code>. <p/> Expression should be returning a
   * Boolean value.
   * 
   * @param simpleRecord
   *          the record under test
   * @return boolean result of expression evaluation
   * @throws RecordException
   *           if 'ifExpression' cannot be evaluated, or doesn't return a </code>Boolean</code>
   */
  protected boolean testCondition(ISimpleRecord simpleRecord) throws RecordException {
    Object result = ifExpression.evaluate(simpleRecord);
    if (result instanceof Boolean) {
      return ((Boolean) result).booleanValue();
    } else {
      throw new ExpressionException("Expression failed to return a Boolean. Evaluation result was: " + result);
    }
  }

  /**
   * String representation of condition expression.
   * 
   * @return String representation of condition expression.
   */
  protected String conditionAsString() {
    return (ifExpression == null ? "<null>" : ifExpression.toString());
  }

}
