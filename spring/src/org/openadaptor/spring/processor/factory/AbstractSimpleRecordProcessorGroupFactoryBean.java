/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.spring.processor.factory;

import java.util.List;
import java.util.Map;

import org.openadaptor.auxil.processor.simplerecord.SimpleRecordAttributeExistsProcessor;
import org.openadaptor.auxil.simplerecord.ISimpleRecordAccessor;

/**
 * Abstract superclass for FactoryBeans that instantiate ProcessorGroups of AbstractSimpleRecordProcessor subclasses.
 * <p>
 * The ProcessorGroup can be optionally prepended with an insatance of SimpleRecordAttributesExistProcessor which can
 * be used to ensure that given attributes will always exist in incoming records so that they can be safelt referenced
 * in any expressions that reference the incoming record.
 *
 * @author Kevin Scully
 */
public abstract class AbstractSimpleRecordProcessorGroupFactoryBean extends AbstractProcessorGroupFactoryBean {

  /** SimpleRecordAccessor to be used by all Processors in the group. */
  protected ISimpleRecordAccessor simpleRecordAccessor = null;

  /**
   * This map may be used to provide type 'hints' for arguments to an expression
   * Currently supported types are Double,Long,Date,String. All ExpressionProcessors
   * in the ProcessorGroup are given the same TypeMap.
   */
  protected Map attributeTypeMap = null;

  /** List of attributes that must exist in the incoming records. */
  protected List mandatoryIncomingRecordAttributes = null;

  // Bean Definition

  /**
   * SimpleRecordAccessor to be used by all Processors in the group.
   * @return  SimpleRecordAccessor to be used by all Processors in the group.
   */
  public ISimpleRecordAccessor getSimpleRecordAccessor() {
    return simpleRecordAccessor;
  }

  /**
   * SimpleRecordAccessor to be used by all Processors in the group.
   * @param simpleRecordAccessor Used by all Processors in the group.
   */
  public void setSimpleRecordAccessor(ISimpleRecordAccessor simpleRecordAccessor) {
    this.simpleRecordAccessor = simpleRecordAccessor;
  }

  /**
   * This map may be used to provide type 'hints' for arguments to an expression
   * Currently supported types are Double,Long,Date,String.
   * @return The attribute type map.
   */
  public Map getAttributeTypeMap() {
    return attributeTypeMap;
  }

  /**
   * This map may be used to provide type 'hints' for arguments to an expression
   * Currently supported types are Double,Long,Date,String.
   *
   * @param attributeTypeMap  The attribute type map.
   */
  public void setAttributeTypeMap(Map attributeTypeMap) {
    this.attributeTypeMap = attributeTypeMap;
  }

  /**
   * List of attributes that must exist in the incoming records.
   * @return List of attributes that must exist in the incoming records.
   */
  public List getMandatoryIncomingRecordAttributes() {
    return mandatoryIncomingRecordAttributes;
  }

  /**
   * List of attributes that must exist in the incoming records.
   * @param mandatoryIncomingRecordAttributes List of attributes that must exist in the incoming records.
   */
  public void setMandatoryIncomingRecordAttributes(List mandatoryIncomingRecordAttributes) {
    this.mandatoryIncomingRecordAttributes = mandatoryIncomingRecordAttributes;
  }

  // End Bean Definition

  /**
   * Add any processors that need to go to the front of the ProcessorGroup.
   */
  public void addPreProcessors(List processorList) {
    if (mandatoryIncomingRecordAttributes != null) {
      SimpleRecordAttributeExistsProcessor processor = new SimpleRecordAttributeExistsProcessor();
      processor.setCreateOnMissingAttribute(true);
      processor.setMandatoryAttributes(mandatoryIncomingRecordAttributes);
    }
  }
}
