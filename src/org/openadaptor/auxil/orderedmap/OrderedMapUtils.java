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
package org.openadaptor.auxil.orderedmap;

import org.openadaptor.core.exception.NullRecordException;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;

/**
 * Utility methods for Ordered Maps
 * 
 * @author Eddy Higgins
 */
public class OrderedMapUtils {
  
  // private static Log log = LogFactory.getLog(OrderedMapUtils.class);

  private OrderedMapUtils() {
  } // No instantiation allowed.

  /**
   * This method simply converts a record into an Ordered Map, probably just by casting it. If it cannot, then a
   * RecordException will be thrown.
   * 
   * @param record
   *          A candidate object which might be an ordered map
   * @return the object as an ordered Map (probably just cast).
   * @throws NullRecordException
   *           if the incoming record is <tt>null</tt>
   * @throws RecordFormatException
   *           if the incoming record is not an <code>IOrderedMap</code>
   */
  public static IOrderedMap extractOrderedMap(Object record) throws RecordException {
    IOrderedMap result = null;
    if (record instanceof IOrderedMap) {
      result = (IOrderedMap) record;
    } else {
      if (record == null) {
        throw new NullRecordException("Expected IOrderedMap. Null record not permitted.");
      } else {
        throw new RecordFormatException("Expected IOrderedMap . Got [" + record.getClass().getName() + "]");
      }
    }
    return result;
  }
}
