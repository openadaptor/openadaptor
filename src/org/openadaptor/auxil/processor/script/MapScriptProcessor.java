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

package org.openadaptor.auxil.processor.script;

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.exception.NullRecordException;
import org.openadaptor.core.exception.RecordFormatException;
/**
 * ScriptProcessor which executes scripts in the context of a Map data record.
 * 
 * @author higginse
 *
 */
public class MapScriptProcessor extends ScriptProcessor {
  private static final Log log =LogFactory.getLog(MapScriptProcessor.class);

  public MapScriptProcessor() {
    super();
  }

  public MapScriptProcessor(String id) {
    super(id);
  }

  /**
   * Process a Map data item.
   * It bind each key->value pair in the supplied map into
   * the scriptEngine.
   * Actual processing is delegated to the superclass.
   *
   */
  public synchronized Object[] process(Object data) {
    if (data==null) {
      throwNullRecordException();
    }
    if (!(data instanceof Map)) {
      throwRecordFormatException(getClass().getName()+" expects Map data, but got "+data.getClass().getName());
    }
    Map map=(Map)data;

    Set keySet=map.keySet();
    Object[] keys=keySet.toArray(new Object[keySet.size()]);
    for (int i=0;i<keys.length;i++) {
      Object key=keys[i];
      if (key!=null) {
        String boundName=key.toString();
        Object value=map.get(key);
        scriptEngine.put(boundName,value);
        if (log.isDebugEnabled()) {
          log.debug("binding "+boundName+" -> "+value);
        }
      }
      else {
        log.warn("ScriptEngine cannot bind null Map key");
      }
    }

    Object[] result=super.process(data);
    if (result.length>0) {
      updateValues((Map)scriptEngine.get(dataBinding),keys);
    }
    return result;
  }

  /**
   * update the named keys from the engine binding
   * @param outputMap
   * @param keys
   */
  private void updateValues(Map outputMap,Object[] keys) { 
    for (int i=0;i<keys.length;i++) {
      Object key=keys[i];
      if (key!=null) { //Check if we need to extract an updated value.
        if (outputMap.containsKey(key)) { //Only update keys that still exist!
          String boundName=key.toString();
          Object value=scriptEngine.get(boundName);
          scriptEngine.put(boundName,value);
          outputMap.put(key,value);
          if (log.isDebugEnabled()) {
            log.debug("updated "+boundName+" -> "+value);
          }
        }
      }
      else {
        log.warn("ScriptEngine cannot bind null Map key");
      }
    }
  }

  private void throwRecordFormatException(String msg) {
    log.warn(msg);
    throw new RecordFormatException(msg);
  }

  private void throwNullRecordException() {
    String msg="Record may not be null";
    log.warn(msg);
    throw new NullRecordException(msg);
  }

}
