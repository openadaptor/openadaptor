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

package org.openadaptor.legacy.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.RecordFormatException;
import org.openadaptor.dataobjects.DataObject;

/**
 * Wrapper around a ScriptProcessor which allows filtering based
 * on the result of execution of the script.
 * 
 * Note: This has been changed to use delegation rather than
 * inheritance to allow the use of alternate ScriptProcessor types,
 * such as MapFilterProcessor.
 * 
 * @author higginse
 * 
 */
public class DOFilterProcessor extends Component implements IDataProcessor {
  private static final Log log =LogFactory.getLog(DOFilterProcessor.class);

  private boolean filterOnMatch = true;

  private List matchedTypes=null;

  public DOFilterProcessor() {
    super();
  }

  public DOFilterProcessor(String id) {
    super(id);
  }

  public void setMatchedTypes(List types) {
    this.matchedTypes=types;
  }


  public void setFilterOnMatch(boolean filterOnMatch) {
    this.filterOnMatch = filterOnMatch;
  }

  /**
   * get scriptProcessor to process data, and filter based on
   * result.
   */
  public synchronized Object[] process(Object data) {
    Object[] output;
    if (data instanceof DataObject) {
      if (matches((DataObject)data) == filterOnMatch) {
        output=new Object[] {}; //filter out the result and return empty array. 
      }
      else {
        output=new Object[] {data};
      }
    } else {
      throw new RecordFormatException("record is not a DataObject");
    }
    return output;
  }

  protected boolean matches(DataObject data) {
    String type=data.getType().getName();
    boolean match=matchedTypes.contains(type);
    if (log.isDebugEnabled()) {
       log.debug("type "+type+(match?"matches":"does not match")+" expected types");
      }
    return match;
  }

  public void reset(Object context) {}  

  public void validate(List exceptions) {
    if (matchedTypes==null) {
      log.warn("No match types have been specified - No records will match");
      matchedTypes=new ArrayList();
    }
    if (exceptions==null) { //IDataProcessor requires a non-null List
      throw new IllegalArgumentException("exceptions List may not be null");
    }
  }
}
