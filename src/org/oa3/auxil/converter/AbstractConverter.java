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
package org.oa3.auxil.converter;

import java.util.List;

import org.oa3.core.IDataProcessor;
import org.oa3.core.exception.RecordException;

/**
 * Abstract implementation of IDataProcessor that converts from one data representation to another
 * 
 * @author Eddy Higgins
 */
public abstract class AbstractConverter implements IDataProcessor {

  // private static final Log log = LogFactory.getLog(AbstractRecordConvertorProcessor.class);

  protected boolean boxReturnedArrays = true;

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
    Object result = convert(record);

    if (result == null) { // Never return null - just return an empty array.
      result = new Object[0];

    } else { // If it's already an array, do nothing
      if (boxReturnedArrays || (!(result instanceof Object[])))
        result = new Object[] { result };
    }

    return (Object[]) result;
  }

  public void validate(List exceptions) {
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
