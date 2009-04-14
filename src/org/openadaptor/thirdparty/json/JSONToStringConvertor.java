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

package org.openadaptor.thirdparty.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openadaptor.auxil.convertor.AbstractConvertor;
import org.openadaptor.core.exception.ProcessingException;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Feb 19, 2007 by oa3 Core Team
 */

/**
 * Convert JSON Objects or Arrays to JSON formatted strings.
 */
public class JSONToStringConvertor extends AbstractConvertor {

  /** Size of tab to use when pretty print which defaults to 0. Default is don't pretty print. */
  protected int tabSize = 0;

  /**
   * @return the tab size to use when pretty print which defaults to 0. Default is don't pretty print.
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
   * Performs the the actual conversion. Returns the successfully formatted string or throws a ProcessingException.
   *
   * @param record
   * @return Converted Record
   * @throws org.openadaptor.core.exception.ProcessingException
   *          if there was a problem converting the record
   */
  protected Object convert(Object record) {
    if (record == null) {
      throw new ProcessingException("Incoming data is null.", this);
    }
    if (! (( record instanceof JSONObject ) || ( record instanceof JSONArray ))) {
      throw new ProcessingException("Expected a JSONObject or a JSONArray. Got [" + record.getClass().getName() + "] instead", this);
    }

    String jsonText;

    try {
      if (tabSize < 1) {
          jsonText = record.toString();
      }
      else if (record instanceof JSONObject) {
        jsonText = ((JSONObject)record).toString(getTabSize());
      }
      else {
        jsonText = ((JSONArray)record).toString(getTabSize());
      }
    } catch (JSONException e) {
      throw new ProcessingException("Unable to render JSON as Text", e, this);
    }

    return jsonText;
  }
}
