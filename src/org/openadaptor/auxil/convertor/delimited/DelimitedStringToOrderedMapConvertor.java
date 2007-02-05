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

package org.openadaptor.auxil.convertor.delimited;

import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;

/**
 * Converts delimited string records to {@link IOrderedMap} implementations.
 * 
 * @author Eddy Higgins
 */
public class DelimitedStringToOrderedMapConvertor extends AbstractDelimitedStringConvertor {

  public DelimitedStringToOrderedMapConvertor() {
    super();
  }

  public DelimitedStringToOrderedMapConvertor(String id) {
    super(id);
  }

  /**
   * Set flag to remove enclosing quotes from fields where necessary. This is applied to all fields
   * 
   * @param stripQuotes
   *          true to remove quotes
   */
  public void setStripEnclosingQuotes(boolean stripQuotes) {
    stripEnclosingQuotes = stripQuotes;
  }

  /**
   * @return true if enclosing quotes are to be removed from all fields
   */
  public boolean getStripEnclosingQuotes() {
    return stripEnclosingQuotes;
  }

  /**
   * Uses the convertDelimitedStringToOrderedMap() call in the super class to convert the supplied string.
   * 
   * @param record
   *          delimited string
   * 
   * @return IOrderedMap containing attributes corresponding to the fields in the delimited string passed
   * 
   * @throws RecordFormatException
   *           if the record is not a string
   * @throws RecordException
   *           if the
   */
  protected Object convert(Object record) {
    Object result = null;

    if (!(record instanceof String)) {
      throw new RecordFormatException("Record is not a String. Record: " + record);
    }

    String stringVal = (String) record;
    if (nextRecordContainsFieldNames) {
      setFieldNames(extractValues(stringVal));
      nextRecordContainsFieldNames = false;
    } else {
      result = convertDelimitedStringToOrderedMap(stringVal);
    }

    return result;
  }
}
