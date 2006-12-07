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
import org.oa3.core.exception.NullRecordException;
import org.oa3.core.exception.RecordException;
import org.oa3.core.exception.RecordFormatException;

/**
 * Convert from a string representing JSON to an OrderedMap. Converter is a bit cheap and cheerful but should work given
 * correct JSON. JSON arrays are converted to OrderedMaps with auto-generated keys.
 * 
 * @author Kevin Scully
 */
public class JsonStringToOrderedMapConvertor extends AbstractConvertor {

  /**
   * Performs the the actual conversion. Returns the successfully converted record or throw a RecordException.
   * 
   * @param record
   *          the JSON string to process
   * 
   * @return Converted Record
   * 
   * @throws NullRecordException
   *           if the record is null
   * @throws RecordFormatException
   *           if the record is not a String or there was an error parsing it
   * @throws RecordException
   *           if the conversion fails
   */
  protected Object convert(Object record) throws RecordException {
    if (record == null)
      throw new NullRecordException("Null values not permitted");

    if (!(record instanceof String))
      throw new RecordFormatException("Record is not a String: " + record.getClass().toString());

    String trimmed = ((String) record).trim();
    if (trimmed.startsWith("{")) {
      JSONObject jsonObject;
      try {
        jsonObject = new JSONObject(trimmed);
      } catch (JSONException e) {
        throw new RecordFormatException("Error parsing JSON format String: [" + e.getMessage() + "]", e);
      }
      return mapFromJSONObject(jsonObject);
    }

    if (!trimmed.startsWith("["))
      throw new RecordFormatException("Not valid JSON. Received: " + record);

    JSONArray jsonArray;
    try {
      jsonArray = new JSONArray(trimmed);
    } catch (JSONException e) {
      throw new RecordFormatException("Error parsing JSON format String", e);
    }

    return mapFromJSONArray(jsonArray);
  }

  /**
   * Converts a JSONObject into an IOrderedMap. We create a new map, get the keys from the JSONObject and add an
   * attribute to the map for each one. If the key points to a JSONArray then the corresponding map attribute will be a
   * nested ordered map structure to mimic it.
   * 
   * @param jsonObject
   * 
   * @return equivalent IOrderedMap
   * 
   * @throws RecordException
   *           if there was aproblem parsing the JSONarry or the key obtained from the JSONObject cannot be found
   */
  protected Object mapFromJSONObject(JSONObject jsonObject) throws RecordException {
    IOrderedMap map = new OrderedHashMap();
    Iterator keys = jsonObject.keys();

    while (keys.hasNext()) {
      String nextKey = (String) keys.next();
      try {
        String nextValue = jsonObject.getString(nextKey).trim();

        if (nextValue.startsWith("{"))
          map.put(nextKey, mapFromJSONObject(jsonObject.getJSONObject(nextKey)));

        else if (nextValue.startsWith("["))
          map.put(nextKey, mapFromJSONArray(jsonObject.getJSONArray(nextKey)));

        else
          map.put(nextKey, nextValue);

      } catch (JSONException e) {
        throw new RecordFormatException("Error getting key [" + nextKey + "] from parsed JSON Object", e);
      }
    }

    return map;
  }

  /**
   * Converts a JSONArray into an IOrderedMap. Essentailly adds an attribute for each element of the JSON array. Nested
   * elements are converted directly into nested maps.
   * 
   * @param jsonArray
   *          the array to process
   * 
   * @return the equivalent IOrderedMap
   * 
   * @throws RecordException
   *           if there was an error getting the next element from the JSON array
   */
  protected Object mapFromJSONArray(JSONArray jsonArray) throws RecordException {
    IOrderedMap map = new OrderedHashMap();

    for (int i = 0; i < jsonArray.length(); i++) {
      try {
        String nextElement = jsonArray.getString(i).trim();

        if (nextElement.startsWith("{"))
          map.add(mapFromJSONObject(jsonArray.getJSONObject(i)));

        else if (nextElement.startsWith("["))
          map.add(mapFromJSONArray(jsonArray.getJSONArray(i)));

        else
          map.add(nextElement);

      } catch (JSONException e) {
        throw new RecordFormatException("Error getting next element from parsed JSON Array", e);
      }
    }

    return map;
  }
}
