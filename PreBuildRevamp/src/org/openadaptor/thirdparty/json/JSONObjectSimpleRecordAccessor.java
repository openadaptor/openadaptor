/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.thirdparty.json;

import org.json.JSONException;
import org.json.JSONObject;
import org.openadaptor.auxil.simplerecord.ISimpleRecord;
import org.openadaptor.auxil.simplerecord.ISimpleRecordAccessor;
import org.openadaptor.core.exception.RecordException;

/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Feb 14, 2007 by oa3 Core Team
 */

/**
 * ISimpleRecordAccessor for JSONObjects. Works for JSONObjects only not JSONArrays.
 */
public class JSONObjectSimpleRecordAccessor implements ISimpleRecordAccessor, ISimpleRecord {

  private JSONObject underlyer;

  /**
   * Generate an <code>ISimpleRecord</code> representation of the supplied <code>JSONObject</code>.
   * <p/>
   * Note: To get back the underlying Record object, use ISimpleRecord.getRecord()
   *
   * @param record the object to be accessed as an ISimpleRecord
   * @return a view of the record as an ISimpleRecord.
   * @throws org.openadaptor.core.exception.RecordException
   *          if the supplied record cannot be viewed as an <code>ISimpleRecord</code>
   */
  public ISimpleRecord asSimpleRecord(Object record) throws RecordException {
    if (! (record instanceof JSONObject) ) {
      throw new RecordException("Can only wrap JSONOBJECTS");
    }
    underlyer = (JSONObject)record;
    return this;
  }

  /**
   * Return the type of the record being presented as an ISimpleRecord.
   *
   * @return Class
   */
  public Class getUnderlyingType() {
    if (underlyer != null)
      return underlyer.getClass();
    else
      return null;
  }

  /**
   * Fetch an attribute using the supplied key value.
   * <p/>
   * The key value might, for example, specify:
   * <UL>
   * <LI>An attribute name </LI>
   * <LI>An XPath expression to locate the value within an XML document
   * </UL>
   *
   * @param key Object which will be used to locate the required attribute
   * @return the value corresponding to the supplied key. <tt>Null</tt> if not found or if returned value is
   *         <tt>null</tt>
   * @throws org.openadaptor.core.exception.RecordException
   *          if the operation cannot be performed.
   */
  public Object get(Object key) throws RecordException {
    try {
      return underlyer.get((String)key);
    } catch (JSONException e) {
      throw new RecordException("Unable to get JSONObject value at ["+ key +"]", e);
    }
  }

  /**
   * Store an attribute value using the supplied key value.
   * <p/>
   * If the attribute already exists, then it's value should be overwritten with the supplied value.
   * <p/>
   * If the attribute does not exist, then it should be added (if possible) to the record.
   * <p/>
   * The key value might, for example, specify:
   * <UL>
   * <LI>An attribute name </LI>
   * <LI>An XPath-like expression to locate the value within an XML document
   * </UL>
   *
   * @param key   Object which will be used to locate the required attribute
   * @param value which is the object which is to be associated with the key.
   * @return the previous value corresponding to the supplied key, or <tt>null</tt> if no value was previously bound
   *         to the key.
   * @throws org.openadaptor.core.exception.RecordException
   *          if the operation cannot be performed.
   */
  public Object put(Object key, Object value) throws RecordException {
    try {
      return underlyer.put((String)key, value);
    } catch (JSONException e) {
      throw new RecordException("Unable to put [" + value + "] in JSONObject at ["+ key +"]", e);
    }
  }

  /**
   * Create a clone of this object.
   *
   * @return clone of this object.
   */
  public Object clone() {
    JSONObject clonedUnderLyer = null;
    try {
      clonedUnderLyer = (new JSONObject(underlyer.toString())); // JSONObject doesn't seem to be cloneable.
    } catch (JSONException e) {
      throw new RecordException("Unable to clone JSONObject ["+ underlyer +"]", e);
    }
    return (new JSONObjectSimpleRecordAccessor()).asSimpleRecord(clonedUnderLyer);
  }

  /**
   * Return the underlying record object which this accessor is fronting.
   * <p/>
   * for <code>OrderedHashMap</code> implementations, it will return <tt>this</tt>.
   *
   * @return underlying record object which this accessor is fronting.
   */
  public Object getRecord() {
    return underlyer;
  }

  /**
   * Clear the underlying Record.
   * <p/>
   * Actual meaning depends on the underlying fronted implementation.
   */
  public void clear() {
    underlyer = new JSONObject();
  }

  /**
   * Remove (and return) the attribute from this <code>ISimpleRecord</code> which corresponds to the supplied key.
   * <p/>
   * If the record does not contains any corresponding attribute, then <tt>null</tt> is returned. If the underlying
   * implementation is a <code>Map</code>, then it should behave exactly as Map.remove().
   *
   * @param key The key associated with the attribute to be removed
   * @return The attribute associated with the key, or <code>null</code> if there was no value associated or that
   *         value was itself null.
   * @throws org.openadaptor.core.exception.RecordException
   *          if the operation cannot be performed.
   */
  public Object remove(Object key) throws RecordException {
    return underlyer.remove((String)key);
  }

  /**
   * Returns <tt>true</tt> if this record contains a mapping for the specified key.
   * <p/>
   * More formally, returns <tt>true</tt> if and only if this map contains at a mapping for a key k such that
   * (key==null ? k==null : key.equals(k)). (There can be at most one such mapping.)
   *
   * @param key The key to locate within the Record
   * @return <tt>true</tt> if this map contains a mapping for the specified
   */
  public boolean containsKey(Object key) {
    return underlyer.has((String)key);
  }
}
