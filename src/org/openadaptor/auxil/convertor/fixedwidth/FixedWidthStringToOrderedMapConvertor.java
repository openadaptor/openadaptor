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

package org.openadaptor.auxil.convertor.fixedwidth;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.exception.NullRecordException;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;

/**
 * Converts a fixed width string into an ordered map <p/>
 * 
 * If the record is too short for the field widths defined then an exception is thrown. Conversely, if the record is
 * longer than the defined fields then the fields are produced as required, a warning is recorded and the surplus is
 * thrown away!! <p/>
 * 
 * The names for fields are optional and if omitted, the ordered map will assign arbitrary sequential values for them.
 * However, if you define any of them then you must define all of them.
 * 
 * @author Russ Fennell
 * 
 * @see IOrderedMap
 * @see FixedWidthFieldDetail
 */
public class FixedWidthStringToOrderedMapConvertor extends AbstractFixedWidthStringConvertor {
  private static final Log log = LogFactory.getLog(FixedWidthStringToOrderedMapConvertor.class);

  /**
   * Converts the supplied string into fields based on the widths defined in the properties file. Then creates an
   * IOrderedMap and adds an attribute for each field. If defined, the field names are obtained from the
   * <code>fieldDetails</code> defined in the config.
   * 
   * @param record
   *          a fixed width String
   * 
   * @return an IOrderedMap with attributes corresponding to the fields in the supplied record
   * 
   * @throws NullRecordException
   *           if the record is null
   * @throws RecordFormatException
   *           if the record is not a String.
   * @throws RecordException
   *           if the record is too short for the widths defined. Also if there are multiple fields with the same name
   *           defined. Or if the number of field names defined is not equal to the number of fields.
   */
  protected Object convert(Object record) throws RecordException {
    if (record == null)
      throw new NullRecordException("Null values not permitted");

    if (!(record instanceof String))
      throw new RecordFormatException("Record is not a String: " + record.getClass().toString());

    if (hasFieldNames() && getFieldNames().size() != fieldDetails.length)
      throw new RecordException("You must a name for ALL fields if you define any of them");

    String rec = (String) record;

    if (rec.length() < getMinimumRecordSize())
      throw new RecordFormatException("Fixed width record [" + rec + "] of length " + rec.length()
          + " is too short to be split into " + getFieldDetails().length + " fields of" + " total length "
          + getTotalFieldWidth());

    // if the record is longer than the total size then we thrown away the extra data :-)
    if (rec.length() > getTotalFieldWidth()) {
      FixedWidthStringToOrderedMapConvertor.log
          .warn("The record is longer than the defined total length of the fields." + " Will ignore the final "
              + (rec.length() - getTotalFieldWidth()) + " characters.");
      rec = rec.substring(0, getTotalFieldWidth());
    }

    // whoops, here's a problem
    if (fieldDetails == null)
      throw new RecordException("Error, no field details defined");

    IOrderedMap map;

    ArrayList values = new ArrayList();
    String field;

    int j = 0;
    for (int i = 0; i < fieldDetails.length; i++) {
      int wdth = fieldDetails[i].getFieldWidth();

      try {
        // if we are processing the last field then we need to allow
        // for the fact that it might be shorter than the field length
        if (i == fieldDetails.length - 1)
          field = rec.substring(j);
        else
          field = rec.substring(j, j + wdth);

        if (fieldDetails[i].isTrim())
          field = field.trim();

        values.add(field);

        j = j + wdth;
      } catch (Exception e) {
        throw new RecordException("Error chopping record: " + e.getMessage());
      }
    }

    // add details to the map - if there field names specified, then we
    // use them
    if (!hasFieldNames()) {
      map = new OrderedHashMap(values);
    } else {
      map = new OrderedHashMap();

      for (int i = 0; i < fieldDetails.length; i++) {
        FixedWidthFieldDetail detail = fieldDetails[i];
        String key = detail.getFieldName();

        if (isValidFieldName(key))
          map.put(key, values.get(i));
        else
          map.add(values.get(i));
      }
    }

    return map;
  }
}
