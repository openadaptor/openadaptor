/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

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
import org.openadaptor.auxil.orderedmap.OrderedMapUtils;
import org.openadaptor.core.exception.ProcessingException;
import org.openadaptor.core.exception.RecordException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Feb 19, 2007 by oa3 Core Team
 */

/**
 * Represent an OrderedMap as a JSON Object. Always converts an OrderedMap to a JSON Object. Makes no attempt to
 * work out if a JSON Array would be more appropriate. Can be optionally configured to produce a JSON String rather than a
 * JSONObject.
 *
 * @author Kevin Scully
 */
public class OrderedMapToJSONConvertor extends AbstractConvertor {

  /** Defaults to <code>false</code> and if true produce JSON formatted text rather than JSON Objects. */
  protected boolean asText = false;

  /** Defaults to 0 and is size of tab to use when pretty printing text. Default (0) means don't pretty print. */
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
   *          Default (0) means don't pretty print
   */
  public void setTabSize(int tabSize) {
    this.tabSize = tabSize;
  }

  /**
   * Defaults to false but if true produce JSON formatted text rather than JSON Objects.
   * @return true is text required.
   */
  public boolean isAsText() {
    return asText;
  }

  /**
   * Set true if JSON formatted text rather than JSON Objects required.
   * @param asText
   */
  public void setAsText(boolean asText) {
    this.asText = asText;
  }


  /**
   * Performs the the actual conversion. Returns the successfully converted record or throws a RecordException.
   *
   * @param record
   *          IOrderedMap to be converted
   *
   * @return JSON Object or string equivalent
   *
   * @throws org.openadaptor.core.exception.ProcessingException
   *           if the record is null or is not an IOrderedMap or the conversion fails.
   */
  protected Object convert(Object record) throws RecordException {
    IOrderedMap map = OrderedMapUtils.extractOrderedMap(record);
    // Always start with an IOrderedMap which means we can start with a conversion to JSON Object.
    JSONObject jsonObject = jsonObjectFromOM(map);
    if (!isAsText()) {
      return jsonObject;
    } else {
      return jsonObjectAsText(jsonObject);
    }
  }

  protected String jsonObjectAsText(JSONObject jsonObject) {String jsonText;
    try {
      if (tabSize < 1) {
        jsonText = jsonObject.toString();
      } else {
        jsonText = jsonObject.toString(getTabSize());
      }
    } catch (JSONException e) {
      throw new ProcessingException("Unable to render JSON Object as text", e, this);
    }
    return jsonText;
  }

  /**
   * Convert an OrderedMap to a JSON Object. This method trawls through the OrderedMap looking for embedded OrderedMaps
   * and Arrays and converting them to JSONObjects or JSONArrays. This means it won't cope with cycles very well.
   * <br>
   * The reason it recurses through the OrderedMap is that the Map constructor for JSONbject will not convert nested
   * maps.
   * <br
   * One important point about Arrays and OrderedMaps, although OrderedMaps can be trated as Ordered Collections i.e.
   * lists there is no way to determine if it's to be treated primarily as a Map as a List therefore we always create
   * JSON Objects from OrderedMaps rather than Arrays. The only way to get a JSONArray is to have explicitly stored
   * an Array as the value at a key in the OrderedMap.
   * <br>
   * if asText is set thaen this convertor will produce a JSON String rather tham Objects or Arrays.
   *
   * @param record
   *          the OrderedMap to convert
   *
   * @return the equivalent JSONObject (or string)
   */
  protected JSONObject jsonObjectFromOM(IOrderedMap record) {
    // converted record will end up a copy of record with all
    // embedded OMs or Arrays converted to JSON Objects or Arrays.
    Map convertedRecord = new HashMap();

    Iterator keys = record.keys().iterator();
    while (keys.hasNext()) {
      Object nextkey = keys.next();

      // Convert any embedded oms to JSONObjects
      Object nextValue = record.get(nextkey);
      if (nextValue instanceof IOrderedMap)
        convertedRecord.put(nextkey, jsonObjectFromOM((IOrderedMap) nextValue));

      // Convert any Arrays to JSON Arrays.
      // To be honest this is unlikely unless the OM has been constructed using nefarious means.
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
