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

package org.openadaptor.auxil.convertor.delimited;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;

/**
 * Convert OrderedMaps to DelimitedStrings
 * 
 * @author Eddy Higgins
 */
public class OrderedMapToDelimitedStringConvertor extends AbstractDelimitedStringConvertor {

  private static final Log log = LogFactory.getLog(OrderedMapToDelimitedStringConvertor.class);

  // internal state:
  protected boolean writeHeaderBeforeNextRecord = false;

  // bean properties:
  protected boolean outputHeader = false;

  // BEGIN Properties

  /**
   * Optional: whether enclosing quotes should be output around a field when it contains the field delimiter character
   * (default is <code>false</code> meaning do not add any quotes).
   * 
   * @param addNeededQuotes
   */
  public void setAddNeededEnclosingQuotes(boolean addNeededQuotes) {
    addNeededEnclosingQuotes = addNeededQuotes;
  }
  
  /**
   * Optional: whether enclosing quotes should be always output around a field 
   * (default is <code>false</code> meaning do not add any quotes).
   * 
   * @param forceEnclosingQuotes
   * @see #setAddNeededEnclosingQuotes
   */
  public void setForceEnclosingQuotes(boolean forceEnclosingQuotes) {
    this.forceEnclosingQuotes = forceEnclosingQuotes;
  }

  /**
   * @return true if the elcosing quotes should be added to the field
   */
  public boolean getAddNeededEnclosingQuotes() {
    return addNeededEnclosingQuotes;
  }

  /**
   * Whether a header should be output as the first row (default is <code>false</code> meaning no header).
   * 
   * @return <code>true</code> if a header is to be written out as the first row; <code>false</code> otherwise.
   */
  public boolean isOutputHeader() {
    return outputHeader;
  }

  /**
   * Optional: whether a header should be output as the first row (default is <code>false</code> meaning no header).
   * 
   * @param outputHeader
   *          <code>true</code> if a header is to be written out as the first row; <code>false</code> otherwise.
   */
  public void setOutputHeader(boolean outputHeader) {
    this.outputHeader = outputHeader;
    writeHeaderBeforeNextRecord = outputHeader;
  }

  // END Properties

  // BEGIN IRecordProcessor implementation

  /**
   * calls super class reset() and sets the flag to indicate that the next record is a header one (dependant on the
   * <code>outputHeader</code> property in the config file).
   */
  public void reset() {
    super.reset();
    writeHeaderBeforeNextRecord = outputHeader;
  }

  // END IRecordProcessor implementation

  /**
   * Converts the ordered map to a delimited string. Loops through each map attribute and gets its value. The values are
   * staiched together using the <code>delimiter</code> and returned. <p/>
   * 
   * If necessary, a header record will be generated. In this case an Object array is created with the first element
   * being the header String and the second on the data values resulting from converting the map.
   * 
   * @param record
   *          IOrderedMap to be converted
   * 
   * @return delimited String (or Object[] if a header is produced)
   * 
   * @throws RecordFormatException
   *           if the record is not an IOrderedMap
   * @throws RecordException
   *           if the field conversion fails
   * 
   * @see AbstractDelimitedStringConvertor
   */
  protected Object convert(Object record) throws RecordException {
    if (!(record instanceof IOrderedMap))
      throw new RecordFormatException("Record is not an IOrderedMap. Record is " + record);

    Object result = null;
    IOrderedMap map = (IOrderedMap) record;
    if (nextRecordContainsFieldNames) {
      StringBuffer sb = new StringBuffer();
      List keys = map.keys();
      if (keys.size() > 0) {
        for (int i = 0; i < keys.size() - 1; i++) {
          String key = (String) keys.get(i);
          sb.append(key).append(delimiter);
        }
        sb.append(keys.size() - 1);
        result = sb.toString();
      } else {
        log.warn("Unable to use empty ordered map to generate field names");
      }
    } else {
      result = convertOrderedMapToDelimitedString(map);
    }

    if (writeHeaderBeforeNextRecord) {
      Object[] resultObj = new Object[2];
      resultObj[0] = convertOrderedMapToDelimitedStringHeader(map);
      resultObj[1] = result;
      writeHeaderBeforeNextRecord = false;
      result = resultObj;
    }

    return result;
  }
}
