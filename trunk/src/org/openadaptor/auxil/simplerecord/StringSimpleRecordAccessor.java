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

package org.openadaptor.auxil.simplerecord;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;

// TODO: This should really be a general purpose Object wrapper - not just Strings.
/**
 * Utility class to represent <code>String</code> objects as <code>ISimpleRecord</code> instances.
 * <p>
 * This allows String objects to be represented as ISimpleRecords. This may be useful if, say an entire record is to be
 * used in a processor (as opposed to some of it's attributes).
 * <p>
 * For example, when reading Delimited String records, it might first be necessary to filter out records which are
 * comments, and don't have the delimited string format. It has a strict flag (true by default) which mandates that
 * supplied objects must be Strings. If false, Any object may be represented.
 * 
 * @author Eddy Higgins
 */
public class StringSimpleRecordAccessor implements ISimpleRecordAccessor, ISimpleRecord {
  
  private static final Log log = LogFactory.getLog(StringSimpleRecordAccessor.class);

  /**
   * This is the underlying object for the accessor.
   */
  private Object record;

  /**
   * Internal flag to remember incoming record format.
   */
  protected boolean incomingWasString = false;

  private boolean strict = true;

  // BEGIN Bean getters/setters

  /**
   * Flag to indicate whether processor should apply toString() to records which are not already Strings.
   * 
   * @return boolean True if toString() should be applied, false otherwise
   */
  public boolean getStrict() {
    return strict;
  }

  /**
   * Sets flag which indicates whether processor should apply toString() to records which are not already Strings.
   * 
   * @param strict
   *          True if toString() should be applied, false otherwise
   */
  public void setStrict(boolean strict) {
    this.strict = strict;
  }

  private void setRecord(Object value) throws RecordFormatException {
    if ((strict) && (value != null) && (!(value instanceof String))) {
      throw new RecordFormatException("Expeced a String (as strict=" + strict + "). Got " + value.getClass().getName());
    }
    record = value;
  }

  // END Accessors
  private StringSimpleRecordAccessor(Object record) throws RecordFormatException {
    this();
    setRecord(record);
  }

  /**
   * Default constructor.
   * <p>
   * In normal circumstances this should <em>only</em> be used by Spring.
   */
  public StringSimpleRecordAccessor() {
  } // Default constructor is for bean use only.

  // BEGIN Implementation of ISimpleRecord

  /**
   * Retrieve a <code>String</code> representation of the underlying record.
   * <p>
   * Note that the supplied key is ignored.
   * 
   * 
   * @param key
   *          Ignored as it is not needed.
   * @return The object associated with the supplied key
   * @throws org.openadaptor.core.processor.RecordFormatException
   *           if strict is false, and the record is not <tt>null</tt> and not a <code>String</code>.
   */
  public Object get(Object key) throws RecordFormatException {
    return record;
  }

  /**
   * Store the suppplied object in the underlying record.
   * <p>
   * Note: The supplied key is ignored completely.
   * <p>
   * If <code>strict</code> property is true, then only <code>String</code> or <tt>null</tt>values may be
   * supplied, otherwise a RecordFormatException will result.
   * 
   * @param key
   *          Not used and will be ignored.
   * @param value
   *          Object to be stored. Must be a <code>String</code> if strict is set.
   * @return The Object which has just been stored.
   * @throws org.openadaptor.core.processor.RecordFormatException
   *           if supplied value is not <tt>null</tt> and not a <code>String</code>, and <code>strictM/code>
   *                                                  is set.
   */
  public Object put(Object key, Object value) throws RecordFormatException {
    setRecord(value);
    return record;
  }

  /**
   * This will set the underlying record to null.
   * <p>
   * Note: The supplied key is ignored.
   * 
   * @param key
   *          ignored
   * @return Object containing the previously stored record value.
   * @throws org.openadaptor.core.processor.RecordException
   *           if the operation cannot be completed.
   */
  public Object remove(Object key) throws RecordException {
    Object value = record;
    record = null;
    return value;
  }

  /**
   * Always returns <tt>false</tt>.
   * 
   * <p>
   * 
   * @param key
   *          Ignored
   * @return <tt>false</tt>
   */
  public boolean containsKey(Object key) {
    return false;
  }

  /**
   * Shallow copy this accessor.
   * <p>
   * Note that the underlying record itself is not cloned().
   * 
   * @return StringSimpleRecordAccessor with identical properties, and reference to underlying record.
   */
  public Object clone() {
    StringSimpleRecordAccessor clone = new StringSimpleRecordAccessor();
    if ((record == null) || (record instanceof String)) {
      clone.record = record;
    } else {
      clone.record = getClone(record);
    }
    clone.setStrict(getStrict());
    return clone;
  }

  /**
   * This returns the underlying Object that this class is fronting as an <code>ISimpleRecord</code>.
   * 
   * @return Object containing record.
   */
  public Object getRecord() {
    return record;
  }

  /**
   * Set the underlying record to <tt>null</tt>.
   */
  public void clear() {
    record = null;
  }

  // END Implementation of ISimpleRecord

  /**
   * Get an <code>ISimpleRecord</code>view on the supplied record Object.
   * <p>
   * This approximates the inverse of getRecord(), subject to the influence (of preserveIncomingXMLFormat)
   * 
   * @param record
   *          The object to be represented as an ISimpleRecord. Must be an XMLString or a Dom4J <code>Document</code>
   * @return ISimpleRecord view on the underlying XML Document.
   * @throws RecordException
   *           if the record cannot be represented as an ISimpleRecord
   */
  public ISimpleRecord asSimpleRecord(Object record) throws RecordException {
    return new StringSimpleRecordAccessor(record);
  }

  /**
   * Return the type of the record being presented as an ISimpleRecord.
   * @return  Class
   */
  public Class getUnderlyingType() {
    return record.getClass();
  }

  /**
   * Returns a clone of the object, or a reference if clone is not possible.
   * <p>
   * Note: This doesn't expect a String
   * 
   * @param o
   *          The object to be cloned.
   * @return Object containing a clone() of the original if possible, or a reference if not.
   */
  private Object getClone(Object o) {
    Object result = o;
    if (result != null) {
      if (result instanceof Cloneable) {
        try {
          Method m = result.getClass().getMethod("clone", (Class[])null);
          result = m.invoke(o, (Object[])null);
        } catch (NoSuchMethodException nsme) {
          log.warn("Object is cloneable, but has no clone() method. Reference to original will be returned.");
        } catch (IllegalAccessException iae) {
          log.warn("clone() method failed, reference to original will be returned. Exception: " + iae);
        } catch (InvocationTargetException ite) {
          log.warn("clone() method failed, reference to original will be returned. Exception: " + ite);
        }
      }
    }
    return result;
  }
}
