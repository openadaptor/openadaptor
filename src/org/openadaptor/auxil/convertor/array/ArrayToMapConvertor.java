/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.auxil.convertor.array;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.convertor.AbstractMapGenerator;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.exception.NullRecordException;
import org.openadaptor.core.exception.RecordFormatException;

/**
 * General purpose convertor for converting Object arrays into OrderedMaps
 * 
 * @author Eddy Higgins
 */
public class ArrayToMapConvertor extends AbstractMapGenerator {
  private final static Log log = LogFactory.getLog(ArrayToMapConvertor.class);

  /**
   * Verivies that records are Object arrays and delegate to
   * array conversion method.
   * @return Object which should contain a Map representation from the incoming array
   * @throws RecordFormatException if incoming record is not an Object[]
   */
  protected Object convert(Object record) {
    if (record instanceof Object[]) {
      return convert((Object[])record);
    }
    else {
      throw new RecordFormatException("Object array expected - got "+record);
    }
  }

  /**
   * Convert an Object array into an OrderedMap of name/value pairs
   * @param values incoming array to be converted
   * @return Map with named values
   */
  protected Map convert(Object[] values) {
    IOrderedMap map=null;
    if (values == null) {
      throw new NullRecordException("Null values not permitted");
    }
    if (nextRecordContainsFieldNames) {
      log.debug("Extracting field names from record");
      setFieldNames(values);
    }
    else {        
      int received = values.length;
      if (fieldNames != null) {
        int count = fieldNames.length;
        if ((received > count) && (!insufficientNamesWarningIssued)) { //Warn once only 
          log.warn(count +" names for "+received + " fields - some will have automatic names");
          insufficientNamesWarningIssued=true;
        }
        else if (received < count) {
          if (padMissingFields) {
            log.debug("Padding "+received+" fields to "+count);
            Object[] padded=new Object[count];
            System.arraycopy(values, 0, padded, 0, values.length);
            values=padded;
            received=padded.length;
          }
          else {
            String msg="Expected "+count+" fields,got "+received+" (enable padMissingFields property to avoid this)";
            log.warn(msg);
            throw new RecordFormatException(msg);
          }
        }
        map = new OrderedHashMap();
        for (int i = 0; i < count; i++) {// Add the named ones
          map.put(fieldNames[i], values[i]);
        }
        for (int j = count; j < received; j++) { // Now add the nameless poor souls.
          //map.put("col "+j,values[j]);
          map.put(nameGenerator.generateName(String.valueOf(j)),values[j]);
        }
      } else { // don't care about names. Ain't got none.
        map = new OrderedHashMap(Arrays.asList(values));
      }
    }
    return map;
  }

}
