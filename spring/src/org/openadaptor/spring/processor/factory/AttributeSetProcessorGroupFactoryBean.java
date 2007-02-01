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
import org.openadaptor.auxil.expression.IExpression;
import org.openadaptor.auxil.processor.simplerecord.AttributeSetProcessor;
import org.openadaptor.auxil.processor.simplerecord.KeepNamedAttributesProcessor;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.processor.ProcessorGroup;

/**
 * Concrete implementation of a Factorybean that instantiates a ProcessorGroups of AttributeSetProcessors.
 * <p>
 * The ProcessorGroup can be optionally prepended with an insatance of SimpleRecordAttributesExistProcessor which can
 * be used to ensure that given attributes will always exist in incoming records so that they can be safelt referenced
 * in the expressionMap.
 * <p> 
 * The ProcessorGroup is optionally appended with an instance of KeepNamedAttributesProcessor initialised from the
 * property map keys. This processor will work with all ISimpleRecord implementations.
 * <p/>
 * Properties
 * <p/>
 * <pre>
 *  expressionMap               Map         key (result attribute name) value (Expression that resolves to new value).
 *  keepResultAttributesOnly    Boolean     Default false. If true remove all result attributes not explicitly named
 * </pre>
 */
public class AttributeSetProcessorGroupFactoryBean extends AbstractSimpleRecordProcessorGroupFactoryBean {

  /**
   * If true add an KeepNamedAttributesProcessor to the end of the ProcessorGroup.
   */
  protected boolean keepResultAttributesOnly = false;

  /**
   * Map with keys that define the result attribute names to populate and values which contain the expressions
   * used to initialise the ExpressionProcessors in the ProcessorGroup.
   */
  protected Map expressionMap;

  // Bean property support

  /**
   * If true add a KeepNamedAttributesProcessor to the start of the ProcessorGroup.
   * @return value of this flag.
   */
  public boolean isKeepResultAttributesOnly() {
    return keepResultAttributesOnly;
  }

  /**
   * If true add a KeepNamedAttributesProcessor to the start of the ProcessorGroup. This will remove all
   * attributes not explicitly set.
   * @param keepResultAttributesOnly  if true remove all attributes not explicitly set.
   */
  public void setKeepResultAttributesOnly(boolean keepResultAttributesOnly) {
    this.keepResultAttributesOnly = keepResultAttributesOnly;
  }

  /**
   * Map with keys that define the result attribute names to populate and values which contain the expressions
   * used to initialise the ExpressionProcessors in the ProcessorGroup. All AttributeSetProcessors in the
   * ProcessorGroup are given the same TypeMap.
   *
   * @return the expression map in use.
   */
  public Map getExpressionMap() {
    return expressionMap;
  }

  /**
   * Map with keys that define the result attribute names to populate and values which contain the expressions
   * used to initialise the ExpressionProcessors in the ProcessorGroup. All AttributeSetProcessors in the
   * ProcessorGroup are given the same TypeMap.
   *
   * @param expressionMap The expression map to use.
   */
  public void setExpressionMap(Map expressionMap) {
    this.expressionMap = expressionMap;
  }

  // End Bean property support

  // Overridden abstract methods

  /**
   * Instantiate the ProcessorGroup. This is the Object constructed by this factory. This Object
   * will be cached by the implementation of getObject() in AbstractProcessorGroupFactoryBean.
   *
   * @return ProcessorGroup created by this factory.
   */
  protected ProcessorGroup createObject() throws IllegalAccessException, InstantiationException, ExpressionException {
    ProcessorGroup theGroup = (ProcessorGroup) getObjectType().newInstance();
    List processorList = new ArrayList(expressionMap.size());
    addPreProcessors(processorList);
    Iterator mapIterator = expressionMap.entrySet().iterator();
    while (mapIterator.hasNext()) {
      Map.Entry entry = (Map.Entry) mapIterator.next();
      AttributeSetProcessor groupElement = new AttributeSetProcessor();
      if (getSimpleRecordAccessor() != null) {
        groupElement.setSimpleRecordAccessor(getSimpleRecordAccessor());
      }
      IExpression expression = new Expression();
      expression.setAttributeTypeMap(getAttributeTypeMap());
      expression.setExpression((String) entry.getValue());
      groupElement.setExpression(expression);
      groupElement.setAttributeName((String) entry.getKey());
      processorList.add(groupElement);
    }
    addPostProcessors(processorList); // Add them at the end
    theGroup.setProcessors((IDataProcessor[]) processorList.toArray(new IDataProcessor[processorList.size()]));
    return theGroup;
  }

  // End overridden abstract methods

  protected void addPostProcessors(List processorList) {
    if (isKeepResultAttributesOnly()) {
      KeepNamedAttributesProcessor commonProcessor = new KeepNamedAttributesProcessor();
      // The list of result Attributes is the keySet of the property map
      commonProcessor.setAttributesToKeep(new ArrayList(getExpressionMap().keySet()));
      if (getSimpleRecordAccessor() != null) {
        commonProcessor.setSimpleRecordAccessor(getSimpleRecordAccessor());
      }
      processorList.add(commonProcessor);
    }
  }
}
