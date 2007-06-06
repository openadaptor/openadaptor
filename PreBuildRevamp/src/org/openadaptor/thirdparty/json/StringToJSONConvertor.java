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

package org.openadaptor.thirdparty.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openadaptor.auxil.convertor.AbstractConvertor;
import org.openadaptor.core.exception.NullRecordException;
import org.openadaptor.core.exception.ProcessingException;
import org.openadaptor.core.exception.RecordFormatException;

/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Feb 14, 2007 by oa3 Core Team
 */

/**
 * Simple convertor that parses text as JSON.
 */
public class StringToJSONConvertor extends AbstractConvertor {
  /**
   * Performs the the actual conversion. Returns the successfully converted record or throw a RecordException.
   *
   * @param record
   * @return Converted Record
   * @throws org.openadaptor.core.exception.RecordException
   *          if there was a problem converting the record
   */
  protected Object convert(Object record) {
    if (record == null)
      throw new NullRecordException("Null values not permitted");

    if (!(record instanceof String))
      throw new RecordFormatException("Record is not a String: " + record.getClass().toString());

    String trimmed = ((String) record).trim();
    Object convertedObject;
    try {
      if (trimmed.startsWith("{")) {
        convertedObject = new JSONObject(trimmed);
      } else if (trimmed.startsWith("[")) {
        convertedObject = new JSONArray(trimmed);
      } else {
        throw new ProcessingException("Unable to parse JSON String: " + trimmed, this);
      }
    } catch (JSONException e) {
      throw new RecordFormatException("Unable to create JSON from: " + trimmed, e);
    }

    return convertedObject;
  }
}
