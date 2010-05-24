/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.expression.Expression;
import org.openadaptor.auxil.expression.ExpressionException;
import org.openadaptor.auxil.expression.IExpression;
import org.openadaptor.auxil.simplerecord.AbstractSimpleRecordProcessor;
import org.openadaptor.auxil.simplerecord.ISimpleRecord;
import org.openadaptor.core.exception.RecordException;

/**
 * Pass or discard records depending on the evaluation of a boolean Expression.
 * <p>
 * Note that pass/discard logic may be inverted via discardMatches property.
 * 
 * @author Eddy Higgins
 * @deprecated ScriptFilterProcessor should be used instead
 */
public class FilterProcessor extends AbstractSimpleRecordProcessor {

  private static final Log log = LogFactory.getLog(FilterProcessor.class);

  /**
   * Boolean expression whose
   */
  protected IExpression filterExpression;

  /**
   * Flag indicating if matches are discarded or retained (discarded or passed).
   * <p>
   * By default, matches are discarded, and non-matches are passed.
   */
  protected boolean discardMatches = true;

  // BEGIN Bean getters/setters

  /**
   * This specifies the expression which will be evaluated.
   * <p>
   * The expression must evaluate to a <code>Boolean</code>
   * 
   * @param filterExpression
   *          expression to be evaluated.
   */
  public void setFilterExpression(IExpression filterExpression) {
    this.filterExpression = filterExpression;
  }

  /**
   * Return the expression to be evaluated.
   * 
   * @return <code>IExpression</code> which will be evaluated.
   */
  public IExpression getFilterExpression() {
    return filterExpression;
  }

  /**
   * Convenience accessor for setExpression, which only specifies the expression String.
   * <p>
   * If other expression properties (e.g. type mapping) are required, then the alternative accessor should be used, or
   * the properties must be set subsequently through other means (programmatic or whatever)
   * <p>
   * The expression must generate a <code>Boolean</code> result.
   * 
   * @param filterExpressionString
   *          String containing the expression to be evaluated.
   * @throws ExpressionException
   *           if an <code>IExpression</code> cannot be created from the expression String.
   */
  public void setFilterExpressionString(String filterExpressionString) throws ExpressionException {
    setFilterExpression(Expression.createExpressionFromString(filterExpressionString));
  }

  /**
   * Set flag indicating if matches will cause records to be passed or discarded.
   * <p>
   * By default, matches are discarded, and non-matches are passed.
   * 
   * @param discardMatches
   *          boolean flag, where <tt>true</tt> means discard matching records.
   */
  public void setDiscardMatches(boolean discardMatches) {
    this.discardMatches = discardMatches;
  }

  /**
   * Get flag indicating if matches will cause records to be passed or discarded.
   * 
   * @return boolean, <tt>true</tt> if matches are to be discarded, <tt>false</tt> otherwise.
   */
  public boolean getDiscardMatches() {
    return discardMatches;
  }

  // END Bean getters/setters

  /**
   * Apply the filter expression to an incoming record, and discard or pass the record based on the result.
   * <p>
   * Note alreadyCloned is ignored, as no modification should be made to the incoming record anyway.
   * 
   * @param simpleRecord
   *          record to be checked.
   * @param alreadyCloned
   *          flag which is ignored here
   * @return Object[] which will be empty if the record is discarded, or contain the record if not.
   * @throws RecordException
   *           if expression cannot be evalueated, or doesn't return a <code>Boolean</code>
   */
  public Object[] processSimpleRecord(ISimpleRecord simpleRecord, boolean alreadyCloned) throws RecordException {
    if (discardMatches != testCondition(simpleRecord)) {
      log.debug("Passing record");
      return new Object[] { simpleRecord };
    } else {
      log.debug("Discarding record");
      return new Object[] {};
    }
  }


  /**
   * Test the filter expression against an <code>ISimpleRecord</code>.
   * <p>
   * Expression should be returning a Boolean value.
   * 
   * @param simpleRecord
   *          the record under test
   * @return boolean result of expression evaluation
   * @throws RecordException
   *           if 'filterExpression' cannot be evaluated, or doesn't return a </code>Boolean</code>
   */
  protected boolean testCondition(ISimpleRecord simpleRecord) throws RecordException {
    Object result = filterExpression.evaluate(simpleRecord);
    if (result instanceof Boolean) {
      return ((Boolean) result).booleanValue();
    } else {
      throw new ExpressionException("Expression failed to return a Boolean. Evaluation result was: " + result);
    }
  }

  public void validate(List exceptions) {
    super.validate(exceptions);
    Exception e = checkMandatoryProperty("filterExpression", filterExpression != null);
    if (e != null) {
      exceptions.add(e);
    }
  }

  protected String conditionAsString() {
    return (filterExpression == null ? "<null>" : filterExpression.toString());
  }

  /**
   * Return a <code>String</code> view of this filter 'statement'.
   * <p>
   * Returns pseudo-code which is java-like to a point.
   * 
   * @return <code>String</code> representation of this filter.
   */
  public String toString() {
    StringBuffer sb = new StringBuffer(discardMatches ? "DISCARD " : "PASS ");
    sb.append("if (");
    sb.append(conditionAsString());
    sb.append(")");
    return sb.append(")").toString();
  }

}
