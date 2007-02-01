/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved. 
                                                                                     
 Permission is hereby granted, free of charge, to any person obtaining a             
 copy of this software and associated documentation files (the                       
 "Software"), to deal in the Software without restriction, including               
 without limitation the rights to use, copy, modify, merge, publish,                 
 distribute, sublicense, and/or sell copies of the Software, and to                  
 permit persons to whom the Software is furnished to do so, subject to               
 the following conditions:                                                           
                                                                                     
 The above copyright notice and this permission notice shall be included             
 in all copies or substantial portions of the Software.                              
                                                                                     
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS           
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                          
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                               
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE              
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION              
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION               
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                     
                                                                                     
 Nothing in this notice shall be deemed to grant any rights to                       
 trademarks, copyrights, patents, trade secrets or any other intellectual            
 property of the licensor or any contributor except as expressly stated              
 herein. No patent license is granted separate from the Software, for                
 code that you delete from the Software, or for combinations of the                  
 Software with other software or hardware.                                           
*/

package org.openadaptor.auxil.processor.simplerecord;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.expression.Expression;
import org.openadaptor.auxil.expression.ExpressionException;
import org.openadaptor.auxil.expression.IExpression;
import org.openadaptor.auxil.simplerecord.AbstractSimpleRecordProcessor;
import org.openadaptor.auxil.simplerecord.ISimpleRecord;
import org.openadaptor.core.exception.RecordException;

/**
 * Abstract base class for any processor that modifies an attribute within an <code>ISimpleRecord</code>.
 * 
 * @author Eddy Higgins
 */
public abstract class AttributeModifyProcessor extends AbstractSimpleRecordProcessor {

  public static final Log log = LogFactory.getLog(AttributeModifyProcessor.class);

  /**
   * Attribute which is to be modified.
   */
  protected String attributeName;

  /**
   * Expression which defines an expression to describe the modification.
   */
  protected IExpression expression;

  // BEGIN Bean getters/setters

  /**
   * Set the name of the attribute which will hold the result of expression evaluation.
   * <p>
   * Note that depending on the underlying ISimpleRecord, this might in fact specify a path to the attribute relative to
   * the root of the document.
   * 
   * @param attributeName
   */
  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  /**
   * Get the name of the attribute which will hold the result of expression evaluation.
   * 
   * <p>
   * Note that depending on the underlying ISimpleRecord, this might in fact specify a path to the attribute relative to
   * the root of the document.
   * 
   * @return the name (or path) of attribute to will hold the result of expression evaluation
   */
  public String getAttributeName() {
    return attributeName;
  }

  /**
   * This specifies the expression which will be evaluated as part of the modification.
   * 
   * @param expression
   *          The expression which will be evaluated.
   */
  public void setExpression(IExpression expression) {
    this.expression = expression;
  }

  /**
   * Convenience accessor for setExpression, which only specifies the expression String.
   * <p>
   * If other expression properties (e.g. type mapping) are required, then the alternative accessor should be used, or
   * the properties must be set subsequently through other means (programmatic or whatever)
   * <p>
   * 
   * @param expressionString
   *          <code>String</code> representing the expression to be evaluated
   * @throws ExpressionException
   *           if the expression could not be created.
   */
  public void setExpressionString(String expressionString) throws ExpressionException {
    setExpression(Expression.createExpressionFromString(expressionString));
  }

  /**
   * Returns the expression being evaluated.
   * 
   * @return <code>IExpression</code> instance.
   */
  public IExpression getExpression() {
    return expression;
  }

  // END Bean getters/setters

  /**
   * This will apply the modification to an <code>ISimpleRecord</code>.
   * <p>
   * It proceeds as follows:
   * <UL>
   * <LI>It will clone the record, if it hasn't already been cloned, as it is definitely going to be modified.</LI>
   * <LI>It will call (sub-class implementation of) modifySimpleRecord() to do the actual modification</LI>
   * <LI>Finally, it will wrap the resulting record in an Object[]</LI>
   * </UL>
   * 
   * @param simpleRecord
   *          The record to be modified.
   * @param alreadyCloned
   *          Flag to indicate if the record has already been cloned. If not, we'll have to.
   * @return An Object[] containing the modified record.
   * @throws RecordException
   *           if the modification failed.
   */
  public final Object[] processSimpleRecord(ISimpleRecord simpleRecord, boolean alreadyCloned) throws RecordException {
    // Clone it if it hasn't already been done - we're DEFINITELY going to modify it.
    ISimpleRecord outgoing = modifySimpleRecord(alreadyCloned ? simpleRecord : (ISimpleRecord) simpleRecord.clone());
    return new Object[] { outgoing };
  }

  /**
   * Apply a modification to an ISimpleRecord. Note that the incoming record is normally the same as that returned.
   * 
   * @param simplerecord
   *          SimpleRecord to modify.
   * @return The modified record
   * @throws RecordException
   *           if the modification fails.
   */
  protected abstract ISimpleRecord modifySimpleRecord(ISimpleRecord simplerecord) throws RecordException;

  /**
   * Provide a<code>String</code> representation of the modification
   * 
   * @return <code>String</code> representation of the modification.
   */
  public abstract String toString();
}
