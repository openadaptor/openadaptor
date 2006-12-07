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
package org.oa3.thirdparty.json;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oa3.auxil.convertor.AbstractConvertor;
import org.oa3.auxil.orderedmap.IOrderedMap;
import org.oa3.auxil.orderedmap.OrderedHashMap;
import org.oa3.auxil.orderedmap.OrderedMapUtils;
import org.oa3.core.exception.RecordException;
import org.oa3.core.exception.RecordFormatException;

/**
 * Represent an OrderedMap as a valid JSON String. Always convert an OrderedMap to a JSON Object. Makes no attempt to
 * work out if an Array would be more appropriate.
 * 
 * @author Kevin Scully
 */
public class OrderedMapToJsonStringConvertor extends AbstractConvertor {

  // Size of tab to use when pretty print. 0 means don't pretty print.
  protected int tabSize = 0;

  /**
   * @return the tab size when using pretty print. Default (0) means don't pretty print.
   */
  public int getTabSize() {
    return tabSize;
  }

  /**
   * Sets the tab size to use when pretty printing.
   * 
   * @param tabSize
   *          0 means don;t pretty print
   */
  public void setTabSize(int tabSize) {
    this.tabSize = tabSize;
  }

  /**
   * Performs the the actual conversion. Returns the successfully converted record or throws a RecordException.
   * 
   * @param record
   *          IOrderedMap to be convered
   * 
   * @return JSON string equivalent
   * 
   * @throws RecordFormatException
   *           or the conversion fails
   * @throws RecordException
   *           if the record is null or is not an IOrderedMap
   */
  protected Object convert(Object record) throws RecordException {
    IOrderedMap map = OrderedMapUtils.extractOrderedMap(record);

    // Always start with an IOrderedMap which means we can start with a conversion to JSON Object.
    JSONObject jsonObj = jsonObjectFromOM(map);
    String jsonText;
    try {
      if (tabSize < 1)
        jsonText = jsonObj.toString();
      else
        jsonText = jsonObj.toString(getTabSize());

    } catch (JSONException e) {
      throw new RecordFormatException("Unable to render JSON as Text", e);
    }

    return jsonText;
  }

  /**
   * Convert an OrderedMap to a JSON Object. This method trawls through the OrderedMap looking for embedded OrderedMaps
   * and Arrays and converting them to JSONObjects or JSONArrays. This means it won't cope with cycles very well.
   * 
   * @param record
   *          the OrderedMap to convert
   * 
   * @return the equivalent JSONObject
   */
  protected JSONObject jsonObjectFromOM(IOrderedMap record) {
    // converted record will end up a copy of record with all embedded OMs or Arrays
    // converted to JSON Objects or Arrays.
    IOrderedMap convertedRecord = new OrderedHashMap();

    Iterator keys = record.keys().iterator();
    while (keys.hasNext()) {
      Object nextkey = keys.next();

      // Convert any embedded oms to JJSONObjects
      Object nextValue = record.get(nextkey);
      if (nextValue instanceof IOrderedMap)
        convertedRecord.put(nextkey, jsonObjectFromOM((IOrderedMap) nextValue));

      // Convert any Arrays to JSON Arrays.
      else if ((record.get(nextkey)) instanceof Object[])
        convertedRecord.put(nextkey, jsonArrayFrom((Object[]) nextValue));

      // Add value as is
      else
        convertedRecord.put(nextkey, nextValue);
    }

    // Finally convert the converted record.
    return new JSONObject(convertedRecord);
  }

  /**
   * Convert an Array into a JSONArray. Iterate through the array and convert any IOrderedMaps or Arrays. If the array
   * element being processed is itself an array then we add it as a nested element to the JSONArray.
   * 
   * @param array
   *          arry of IOrderedMaps
   * 
   * @return JSONArray
   */
  protected JSONArray jsonArrayFrom(Object[] array) {
    JSONArray jsonArray = new JSONArray();

    for (int i = 0; i < array.length; i++) {
      Object nextvalue = array[i];
      if (nextvalue instanceof IOrderedMap)
        jsonArray.put(jsonObjectFromOM((IOrderedMap) nextvalue));

      else if (nextvalue instanceof Object[])
        jsonArray.put(jsonArrayFrom((Object[]) nextvalue));

      else
        jsonArray.put(nextvalue);
    }

    return jsonArray;
  }

}
