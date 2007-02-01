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

package org.openadaptor.spring.processor.factory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openadaptor.auxil.expression.Expression;
import org.openadaptor.auxil.expression.ExpressionException;
import org.openadaptor.auxil.processor.simplerecord.AttributeSetProcessor;
import org.openadaptor.auxil.processor.simplerecord.ConditionProcessor;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.processor.ProcessorGroup;

/**
 * FactoryBean that mimics an old style ValueAliasPipe.
 *
 * The Factory generates a ProcessorGroup of ConditionProcessors. Each procesor is initialised using
 * the attribute and one key/value pair from the aliasmap. Each key in the map defines an expression that
 * evaluates to a SimpleRecord attribute value to alias and each value from the key/value pair is an expression
 * that evaluates to a new value for that same attribute.
 * <p>
 * Properties
 * <p>
 * <pre>
 * attribute        Attribute whose value is being aliased
 * aliasMap         Map of old to new values. Keys and Values are both expressions.
 *</pre>
 */
public class ValueAliasProcessorGroupFactoryBean extends AbstractSimpleRecordProcessorGroupFactoryBean {

  /** Attribute whose value is being aliased. */
  protected String attribute;

  /**
   * Map with entries that define the mapping of expressions defining existing values to expressions
   * used to define the new value.
   */
  protected Map aliasMap;

  // Bean Definition

  /**
   * Attribute whose value is being aliased.
   * @return the attribute name.
   */
  public String getAttribute() {
    return attribute;
  }

  /**
   * Attribute whose value is being aliased.
   * @param attribute the attribute name.
   */
  public void setAttribute(String attribute) {
    this.attribute = attribute;
  }

  /**
   * Map with entries that define the mapping of expressions defining existing values to expressions
   * used to define the new value.
   * @return Map defining aliases.
   */
  public Map getAliasMap() {
    return aliasMap;
  }

  /**
   * Map with entris that define the mapping of expressions defining existing values to expressions
   * used to define the new value.
   * @param aliasMap Map defining aliases.
   */
  public void setAliasMap(Map aliasMap) {
    this.aliasMap = aliasMap;
  }

  // End Bean Definition

  /**
   * Instantiate the ProcessorGroup. This is the Object constructed by this factory. This Object
   * will be cached by the implementation of getObject() in AbstractProcessorGroupFactoryBean.
   *
   * @return Instantiated ProcessorGroup
   */
  protected ProcessorGroup createObject() throws IllegalAccessException, InstantiationException, ExpressionException {
    ProcessorGroup theGroup = (ProcessorGroup) getObjectType().newInstance();
    List processorList = new ArrayList(getAliasMap().size());
    Iterator keyIterator = getAliasMap().keySet().iterator();
    while (keyIterator.hasNext()) {
      String key = (String) keyIterator.next();
      ConditionProcessor groupElement = new ConditionProcessor();
      groupElement.setSimpleRecordAccessor(getSimpleRecordAccessor());
      groupElement.setIfExpression(generateIfExpression(getAttribute(), key));
      groupElement.setThenProcessor(generateThenProcessor(getAttribute(), (String) getAliasMap().get(key)));
      processorList.add(groupElement);
    }
    theGroup.setProcessors((IDataProcessor[]) processorList.toArray(new IDataProcessor[processorList.size()]));
    return theGroup;
  }

  protected AttributeSetProcessor generateThenProcessor(String targetAttribute, String expressionString)
      throws ExpressionException {
    Expression expression = new Expression();
    expression.setExpression(expressionString);
    expression.setAttributeTypeMap(getAttributeTypeMap());
    AttributeSetProcessor thenProcessor = new AttributeSetProcessor();
    thenProcessor.setAttributeName(targetAttribute);
    thenProcessor.setExpression(expression);
    thenProcessor.setSimpleRecordAccessor(getSimpleRecordAccessor());
    return thenProcessor;
  }

  protected Expression generateIfExpression(String attribute, String valueExpression) throws ExpressionException {
    Expression ifExpression = new Expression();
    String expressionString = "{" + attribute + "}" + " = " + valueExpression;
    ifExpression.setExpression(expressionString);
    ifExpression.setAttributeTypeMap(getAttributeTypeMap());
    return ifExpression;
  }
}
