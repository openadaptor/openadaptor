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
package org.oa3.auxil.expression;

/*
 * File: $Header: /cvs/oa3/src/org/oa3/expression/IExpression.java,v 1.4 2006/11/02 10:14:12 higginse Exp $ Rev:
 * $Revision: 1.4 $ Created Sep 26 2006 by Eddy Higgins
 */
import java.util.Map;

import org.oa3.auxil.simplerecord.ISimpleRecord;
import org.oa3.core.exception.RecordException;

/**
 * Common interface for openadaptor Expressions.
 * <p>
 * In general, openadaptor3 expressions look like normal algebraic expressions, the difference being that the variables
 * used within the expressions are taken as placeholders for attribute values within a supplied record. Expressions
 * operate on ISimpleRecord objects, which in turn provide implementation neutral access to underlying record attributes
 * for use within expressions.
 * <p>
 * In addition, expression variables may have type conversion applied before they are used in expressions. This is
 * achieved by providing an attribute type map, which provides hints as to how an attribute from a record should be
 * treated if it being referenced in the expression
 * 
 * For examples of expressions in use, see the Cookbook examples include with openadaptor3
 * 
 * @author Eddy Higgins
 */
public interface IExpression {
  /**
   * Set the expression which will be evaluated against incoming records. If the expression is set to <tt>null</tt>,
   * then evaluation will always return <tt>null</tt>
   * 
   * @param expression
   *          <code>String</code> containing an expression
   * @throws ExpressionException
   *           if the expression is not valid.
   */
  public void setExpression(String expression) throws ExpressionException;

  /**
   * Get the expression which will be evaluated against incoming records.
   * 
   * @return <code>String</code> containing an expression
   */
  public String getExpression();

  /**
   * This map may be used to provide type 'hints' for conversion of arguments to an expression.
   * <p>
   * For example, assume a record has a String attribute called 'price'. If we wish to do a calculation using this
   * attribute, we can provide a map which contains an entry mapping "price" to"Double" which will have the effect of
   * converting the price attribute to a Double value before use in the expression.
   * <p>
   * Currently mapping supports "Long","Double","String","Date". Unknown types will be converted as Strings.
   * 
   * @return Map of attribute name to java type mappings.
   * @see org.oa3.util.Dom4jUtils this is where the actual convertion takes place.
   */
  public Map getAttributeTypeMap();

  /**
   * This map may be used to provide type 'hints' for conversion of arguments to an expression.
   * 
   * For example, assume a record has a String attribute called 'price'. If we wish to do a calculation using this
   * attribute, we can provide a map which contains an entry mapping "price" to"Double" which will have the effect of
   * converting the price attribute to a Double value before use in the expression.
   * <p>
   * Currently mapping supports "Long","Double","String","Date". Unknown types will be converted as Strings.
   * 
   * @param attributeTypeMap
   * @see org.oa3.util.Dom4jUtils this is where the actual convertion takes place.
   */
  public void setAttributeTypeMap(Map attributeTypeMap);

  /**
   * Flag to indicate if short-circuit expression evaluation should be applied.
   * <p>
   * The default value should be <tt>true</tt> if the implementation supports it.
   * 
   * @param enabled
   *          short circuit evaluation is enabled if this is <tt>true</tt>
   */
  public void setShortCircuitEvaluation(boolean enabled);

  /**
   * Flag to indicate if short-circuit expression evaluation should be applied.
   * <p>
   * The default value should be <tt>true</tt> if the implementation supports it.
   * 
   * @return <tt>true</tt> if short circuit evaluation is enabled.
   */
  public boolean getShortCircuitEvaluation();

  /**
   * Get flag which indicates if an exception should be thrown when an attribute is referenced within an expression, but
   * doesn't exist within the <code>ISimpleRecord</code> instance.
   * <p>
   * Implementation note: It should default this to false, whereby references should just return <tt>null</tt>
   * instead.
   * 
   * @return boolean true means throw exception. False means return nulls.
   */
  public boolean getThrowExceptionOnMissingAttribute();

  /**
   * Set flag which indicates if an exception should be thrown when an attribute is referenced within an expression, but
   * doesn't exist within the <code>ISimpleRecord</code> instance.
   * <p>
   * Implementation note: It should default this to false, whereby references should just return <tt>null</tt>
   * instead. The exception thrown should be an instance of RecordException
   * 
   * @param throwExceptionOnMissingAttribute
   *          Boolean flag to indicate that an exception should be thrown when an expression refers to an attribute
   *          which does not exist within the record.
   */
  public void setThrowExceptionOnMissingAttribute(boolean throwExceptionOnMissingAttribute);

  /**
   * Evaluate the expression using the supplied record (an <code>ISimpleRecord</code>) to supply variable values.
   * <p>
   * Note that if the expression itself is <tt>null</tt>, then this should return <tt>null</tt>. <br>
   * If the supplied record is null then an ExpressionException may be thrown, but only if the expression contains any
   * attribute references (as they cannot be resolved).
   * 
   * @param record
   *          an instance of <code>ISimpleRecord</code>
   * @return Object containing the result of the expression, or <tt>null</tt> if the expression is <tt>null</tt>.
   * @throws RecordException
   *           if the expression fails to evaluate.
   */
  public Object evaluate(ISimpleRecord record) throws RecordException;
}
