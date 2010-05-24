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

package org.openadaptor.auxil.convertor;

import java.util.List;

import org.openadaptor.core.Component;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.NullRecordException;
import org.openadaptor.core.exception.RecordException;

/**
 * Abstract implementation of IDataProcessor that converts from one data representation to another
 * 
 * @author Eddy Higgins
 */
public abstract class AbstractConvertor extends Component implements IDataProcessor {

  // private static final Log log = LogFactory.getLog(AbstractRecordConvertorProcessor.class);

  protected AbstractConvertor() {
    super();
  }
  
  protected AbstractConvertor(String id) {
    super(id);
  }
  
  /**
   * If true, then converted values will always be wrapped
   * in an enclosing Object[], even if the result is already
   * an array.
   * The default value for this have been changed (after 3.3)
   * to false.
   * Note: Individual subclasses may well override this default
   * for their own purposes.
   * 
   */
  protected boolean boxReturnedArrays = false;
  
  /**
   * Flag to wrap returned Arrays in an enclosing Object[].
   * If true, converted result will be wrapped in an Object[]
   * even if it is already an array.
   * Note: The default value has been changed from true to
   * false after release 3.3
   * @param boxReturnedArrays
   */
  public void setBoxReturnedArrays(boolean boxReturnedArrays) {
    this.boxReturnedArrays=boxReturnedArrays;
  }
  
  /**
   * Gets flag indicating if returned values must always be wrapped in an Object[].
   * 
   * @return true if boxing is enabled, or false otherwise
   */
  public boolean getBoxReturnedArrays() {
    return boxReturnedArrays;
  }

  // IRecordPrcessor implementation
  /**
   * Process a single input record. <p/>
   * 
   * Note: A no-op implementation would be just to return the record unmolested (but wrapped in an Object[]). If a null
   * object is provided, then a NullRecordException should be thrown. <p/>
   * 
   * Note that implementations should always return an Object[], even if empty. Currently a null return value is treated
   * similarily, but this behaviour is considered deprecated and is very likely to change in future.
   * 
   * @param record
   *          the input record to be processed.
   * 
   * @return Object[] with zero or more records, resulting from the processing operation.
   * 
   * @throws RecordException
   */
  public Object[] process(Object record) {
    if (record == null) { //Enforce IDataProcessor contract 
      throw new NullRecordException("Null record not permitted.");
    }
    Object result = convert(record);

    if (result == null) { // Never return null - just return an empty array.
      result = new Object[] {};
    } 
    else { // Wrap in Object[] unless boxReturnedArrays is false and result i
      if (boxReturnedArrays || (!(result instanceof Object[])))
        result = new Object[] { result }; //Wrap it in an Object array.
    }

    return (Object[]) result;
  }

  public void validate(List exceptions) {
    if (exceptions==null) { //IDataProcessor requires a non-null List
      throw new IllegalArgumentException("exceptions List may not be null");
    }
  }

  public void reset(Object context) {
  }

  // End IRecordPrcessor implementation

  // Inheritable ConvertorProcessor methods

  /**
   * Performs the the actual conversion. Returns the successfully converted record or throw a RecordException.
   * 
   * @param record
   * 
   * @return Converted Record
   * 
   * @throws RecordException
   *           if there was a problem converting the record
   */
  protected abstract Object convert(Object record);

}
