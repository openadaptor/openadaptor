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
package org.oa3.auxil.converter.fixedwidth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.auxil.orderedmap.IOrderedMap;
import org.oa3.core.exception.NullRecordException;
import org.oa3.core.exception.RecordException;
import org.oa3.core.exception.RecordFormatException;

/**
 * Converts an ordered map to a fixed width string. <p/>
 * 
 * To do this you must supply the widths of the fields that you wish to be outputted. Additionally you may supply the
 * field names which will be used to select the corresponding values from the map. <p/>
 * 
 * If you supply the names then they will be used as keys to "search" for the corresponding map values. Note, that you
 * do not have to supply names for all the map elements and can output some or all of them. Additionally, if you the map
 * does not contain the field name then a "blank" field will be generated. A side effect of this is that you can
 * completely change the order that fields are output. <p/>
 * 
 * If you supply just the widths then each element in the map will the output in turn. You do not have any choice over
 * the order. If you are using just the field widths then there are some fairly draconian requirements that are imposed -
 * chief amongst thenm is the fact that the number of widths defiend must match the number of elements in the map.
 * Further explaination of this is provided below. <p/>
 * 
 * In both cases, the string output will be trimmed/padded to the appropriate width.
 * 
 * @author Russ Fennell
 * @see org.oa3.collections.IOrderedMap
 */
public class OrderedMapToFixedWidthStringConvertor extends AbstractFixedWidthStringConvertor {
  private static final Log log = LogFactory.getLog(OrderedMapToFixedWidthStringConvertor.class);

  /**
   * Convert the supplied record from an ordered map to a fixed width string. There are two methods to achieve this - by
   * defining the field names, widths, etc. or by defining just the widths.
   * 
   * @param record
   *          must be an instance of an IOrderedMap
   * 
   * @return a String containing the fixed width representation of the ordered map supplied
   * 
   * @throws RecordException
   *           if the record is null or if the record is not an instance of an IOrderedMap
   * 
   * @throws RecordException
   *           if the record is null or not an IOrderedMap. Also if there are no details/widths defined.
   * 
   */
  protected Object convert(Object record) throws RecordException {
    if (record == null)
      throw new NullRecordException("Null values not permitted");

    if (!(record instanceof IOrderedMap))
      throw new RecordFormatException("Record is not an ordered map: " + record.getClass().toString());

    // whoops, here's a problem
    if (fieldDetails == null)
      throw new RecordException("Error, no field details defined");

    IOrderedMap map = (IOrderedMap) record;

    // we process the map using the field names as keys if they are supplied
    // otherwise we use the field widths
    if (getFieldNames().size() == 0)
      return convertUsingFieldWidths(map);
    else
      return convertUsingFieldNames(map);
  }

  /**
   * Takes each field name defined and uses it as a key to get the corresponding values from the map. Will also trim/pad
   * the value to the appropriate width. <p/>
   * 
   * As you don't have to defined a name for each element in the map we list out the elements that will not be included
   * in the output. We are nice like that. <p/>
   * 
   * A side effect of this is that the order of the map elements are outputted is completely configurable by you and
   * will follow the order that the field names are defined. <p/>
   * 
   * If a defined field name is not one of the keys in the map then a "blank" field of the correct size will be used
   * instead (ie. X spaces).
   * 
   * @param map
   *          the ordered map to process
   * 
   * @return the corresponding String
   */
  private String convertUsingFieldNames(IOrderedMap map) {

    String rec = "";

    // loop through the elements of the ordered map and log a warning about
    // any that will not be included in the outputted record (ie. any where
    // there is not a corresponding field definition)
    for (int i = 0; i < map.size(); i++) {

      String field = (String) map.keys().get(i);

      // check that this field is to be outputted
      if (!getFieldNames().contains(field)) {
        OrderedMapToFixedWidthStringConvertor.log.info("Field [" + field
            + "] is not defined in the fieldDetails and will be skipped");
      }
    }

    // Loop through all the field definitions and build the string from the
    // corresponding values stored in the ordered map. The field name should
    // be the key in the map. If the field is not found in the map then the
    // string will contain an empty string of the correct length. Also, we
    // need to trim/pad the elements to fit the field widths defined.
    for (int i = 0; i < fieldDetails.length; i++) {
      int width = fieldDetails[i].getFieldWidth();
      String key = fieldDetails[i].getFieldName();
      String value = "";

      if (map.containsKey(key)) {
        Object o = map.get(key);
        value = o.toString();
      } else {
        OrderedMapToFixedWidthStringConvertor.log.warn("Field [" + key
            + "] not found in the map. Will use blank field");
      }

      rec += trimPadValue(value, width);
    }

    return rec;
  }

  /**
   * Takes each width defined and trims/pads the corresponding ordered map value and adds it to the output. Thus the
   * order of the output is defined by the order of elements in the map. <p/>
   * 
   * In order for this to make sense you must supply a width for all elements of the map. For example, if you supply 3
   * widths then which of the 10 elements of the map do we output? More confusing, if you supply 10 widths and there are
   * only 3 elements then which width applies to which elements? You could say that we process the first 3 widths but it
   * all gets to confusing so we just dictate that they must match up.
   * 
   * @param map
   *          the ordered map to process
   * 
   * @return the corresponding String
   * 
   * @throws RecordException
   *           if the number of widths defined does not match the number of elements in the map
   */
  private String convertUsingFieldWidths(IOrderedMap map) throws RecordException {
    String rec = "";

    // if we are just using field widths then we must have the same number
    // as there are values in the map
    if (fieldDetails.length != map.size())
      throw new RecordFormatException("The number of elements in the map [" + map.size() + "]"
          + " does not match the number of widths defined [" + fieldDetails.length + "]");

    // loop through the widths and trim/pad the corresponding map value
    // which is then added to the record
    for (int i = 0; i < fieldDetails.length; i++) {
      int width = fieldDetails[i].getFieldWidth();
      String value = (String) map.get(i);

      rec += trimPadValue(value, width);
    }

    return rec;
  }

  /**
   * Either trims the supplied string or pads it out (using spaces) to the required width and returns the new string
   * 
   * @param s
   *          the string to pad/trim
   * @param width
   *          the width that the resulting string will be
   * 
   * @return the original string but now at the required length
   */
  private String trimPadValue(String s, int width) {

    // pad
    if (s.length() < width) {
      StringBuffer buffer = new StringBuffer(s);
      for (int i = width - s.length(); i > 0; --i)
        buffer.append(" ");
      return buffer.toString();
    }

    // trim
    if (s.length() > width)
      return s.substring(0, width);

    // the string is already at the required length
    return s;
  }
}
