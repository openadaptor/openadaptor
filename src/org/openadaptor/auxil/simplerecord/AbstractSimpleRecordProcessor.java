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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.NullRecordException;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;
import org.openadaptor.core.exception.ValidationException;

/**
 * Common functionality for processors which deal with ISimpleRecords.
 *
 * @see ISimpleRecordAccessor
 *
 * @author OA3 Core Team
 */
public abstract class AbstractSimpleRecordProcessor extends Component implements IDataProcessor {

  private static final Log log = LogFactory.getLog(AbstractSimpleRecordProcessor.class);

  protected ISimpleRecordAccessor simpleRecordAccessor = null;


  /**
   * @return a method that will cobvert the record into and instance of ISimpleRecord
   */
  public ISimpleRecordAccessor getHelper() {
    return simpleRecordAccessor;
  }

  /**
   * @return a method that will cobvert the record into and instance of ISimpleRecord
   */
  public ISimpleRecordAccessor getSimpleRecordAccessor() {
    return simpleRecordAccessor;
  }

  /**
   * Sets the method that will convert records into ISimpleRecords
   *
   * @param simpleRecordAccessor
   */
  public void setSimpleRecordAccessor(ISimpleRecordAccessor simpleRecordAccessor) {
    this.simpleRecordAccessor = simpleRecordAccessor;
  }

  /**
   * Process a single record, which must be an instance of an ISimpleRecord. Final
   * because this is where the ISimpleRecord checking is done. If you don't want
   * this, implement IRecordProcessor directly instead.
   *
   * @param data the record to be processed
   *
   * @return the array of records which result from processing the incoming record.
   *
   * @throws NullRecordException if the record is null
   * @throws RecordFormatException if the record is not an instance of ISimpleRecord
   * @throws RecordException if the processing fails
   */
  public final Object[] process(Object data) {

    boolean wrappedLocally = true; // Current default behaviour is to always wrap and unwrap.

    if (data == null) {
      throw new NullRecordException("Null record not permitted.");
    }

    ISimpleRecord simpleRecord = null;
    if (simpleRecordAccessor != null) {
      simpleRecord = simpleRecordAccessor.asSimpleRecord(data);
    } else {
      if (data instanceof ISimpleRecord) { //Easy. We're done.
        simpleRecord = (ISimpleRecord) data;
        wrappedLocally = false; // We don't want to unwrap 'cos we received the data already wrapped.
      } else {
        log.warn("Incoming record is not an ISimpleRecord - perhaps a SimpleRecordAccessor must be specified?");
        throw new RecordFormatException("Expected ISimpleRecord . Got [" + data.getClass().getName() + "]");
      }
    }

    Object[] outputArray = processSimpleRecord(simpleRecord, false); //Don't clone unless we need to
    if (wrappedLocally) {
      for (int i = 0; i < outputArray.length; i ++ ) {
        outputArray[i] = ((ISimpleRecord)outputArray[i]).getRecord();
      }
    }
    return outputArray;
  }

  /**
   * Process a simpleRecord, nothing if it has already been cloned.
   *
   * @param simpleRecord the record to be processed
   * @param alreadyCloned true if the record has been cloned
   *
   * @return Object[] containing the results of processing the incoming record
   *
   * @throws RecordException if processing fails
   */
  public abstract Object[] processSimpleRecord(ISimpleRecord simpleRecord, boolean alreadyCloned)
      throws RecordException;

  public void validate(List exceptions) {
  }

  /**
   * Essentially allows you to wrap a test on a property and return an exception
   * if it fails. For example:
   * <p/>
   *
   * <blockquote><pre>
   *      ComponentException e = checkMandatoryProperty(foo, foo!=null);
   *      if ( e != null )
   *          throw e;
   * </pre></blockquote>
   *
   * @param name the property name
   * @param propTest the result of a test (eg. name!=null)
   *
   * @return null if the test suceeded or an ComponentException if it failed
   */
  protected Exception checkMandatoryProperty(String name, boolean propTest) {
    return propTest ? null : new ValidationException("property " + name + " is mandatory", this);
  }

  /**
   * Loops through the list of property names supplied and if the corresponding test
   * fails then adds the name to a list of failures and returns an exception. For
   * example:
   * <p/>
   *
   * <blockquote><pre>
   *      ComponentException e = checkExactlyOneOfProperty(
   *          new String[] {attributeName,"expression"},
   *          new boolean[] {attributeName!=null,expression!=null} );
   *      if ( e != null )
   *          throw e;
   * </pre></blockquote>
   *
   * The exception would contain the message "Exactly one of attributeName,expression
   * must be set" (if both failed the tests).
   *
   * @param names array of attribute names
   * @param tests array of test (eg. attribute != null )
   *
   * @return ComponentException containing the names of any attributes that fail the tests
   * or null
   */
  protected Exception checkExactlyOneOfProperty(String[] names, boolean[] tests) {
    Exception result = null;
    int count = 0;
    for (int i = 0; i < names.length; i++) {
      if (tests[i])
        count++;
    }

    if (count != 1) {
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < names.length - 1; i++)
        sb.append(names[i]).append(",");

      sb.append(names[names.length - 1]);

      result = new ValidationException("Exactly one of " + sb.toString() + " must be set", this);
    }

    return result;
  }

  public void reset(Object context) {
  }
}
