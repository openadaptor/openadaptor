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

package org.openadaptor.thirdparty.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openadaptor.auxil.convertor.AbstractConvertor;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.exception.ProcessingException;
import org.openadaptor.core.exception.RecordException;

import java.util.Iterator;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Feb 19, 2007 by oa3 Core Team
 */

/**
 * Convert from a JSONObject, JSONArray or string representing JSON to an OrderedMap. Conversion is a bit cheap
 * and cheerful but should work given correct JSON. JSON arrays are converted to OrderedMaps with auto-generated keys.
 *
 * @author Kevin Scully
 */
public class JSONToOrderedMapConvertor extends AbstractConvertor {

  /**
   * Performs the the actual conversion. Returns the successfully converted record or throws a ProcessingException.
   *
   * @param record
   *          the JSON string, JSONObject or JSONArray.
   *
   * @return an IOrderedMap.
   *
   * @throws org.openadaptor.core.exception.ProcessingException
   *           if the record is null, not a valid JSON String, or not a JSONObject or JSONArray.
   */
  protected Object convert(Object record) throws RecordException {
    if (record == null)
      throw new ProcessingException("Null values not permitted", this);

    if (!((record instanceof JSONObject) || (record instanceof JSONArray) || (record instanceof String)) )
      throw new ProcessingException("Record is not a JSONOBJECT, JSONArray or String: " + record.getClass().toString(), this);

    Object jsonRecord = record;
    // If we have a String attempt to generate a JSONObject or JSONArray from it.
    if (jsonRecord instanceof String) {
      jsonRecord = jsonFromString((String)jsonRecord);
    }
    // At this point we have either a JSONObject or a JSONArray to convert to an OrderedMap
    IOrderedMap map ;
    if(jsonRecord instanceof JSONObject) {
      map = mapFromJSONObject((JSONObject)jsonRecord);
    }
    else {
      map = mapFromJSONArray((JSONArray)jsonRecord);
    }
    return map;
  }

  /**
   * Parse a JSONObject or JSONArray from a String.
   * @param jsonString
   * @return JSONObject or JSONArray
   */
  private Object jsonFromString(String jsonString) {
    Object jsonRecord;
    String trimmed = jsonString.trim();
    if (trimmed.startsWith("{")) {
      try {
        jsonRecord = new JSONObject(trimmed);
      } catch (JSONException e) {
        throw new ProcessingException("Error parsing JSON format String as Object: [" + e.getMessage() + "]", e, this);
      }
    } else if (!trimmed.startsWith("[")) {
      try {
        jsonRecord = new JSONArray(trimmed);
      } catch (JSONException e) {
        throw new ProcessingException("Error parsing JSON format String as Array", e, this);
      }
    } else {
      throw new ProcessingException("Not a valid JSON String. Received: " + jsonString, this);
    }
    return jsonRecord;
  }

  /**
   * Converts a JSONObject into an IOrderedMap. We create a new map, get the keys from the JSONObject and add an
   * attribute to the map for each one. If the key points to a JSONArray then the corresponding map attribute will be a
   * nested ordered map structure with auto-generated keys to mimic it.
   *
   * @param jsonObject
   *
   * @return equivalent IOrderedMap
   *
   * @throws ProcessingException
   *           if there was a problem parsing the JSONObject or the key obtained from the JSONObject cannot be found
   */
  private IOrderedMap mapFromJSONObject(JSONObject jsonObject) {
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
        throw new ProcessingException("Error getting key [" + nextKey + "] from parsed JSON Object", e, this);
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
   * @throws ProcessingException
   *           if there was an error getting the next element from the JSON array
   */
  private IOrderedMap mapFromJSONArray(JSONArray jsonArray)  {
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
        throw new ProcessingException("Error getting next element from parsed JSON Array", e, this);
      }
    }

    return map;
  }
}
