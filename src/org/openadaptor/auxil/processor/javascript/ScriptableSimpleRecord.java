/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved. 
                                                                                     
 Permission is hereby granted, free of charge, to any person obtaining a             
 copy of this software and associated documentation files (the                       
"Software"), to deal in the Software without restriction, including                
 without limitation the rights to use, copy, modify, merge, publish,                 
 distribute, sublicense, and/or sell copies of the Software, and to                  
 permit persons to whom the Software is furnished to do so, subject to               
 the following conditions:                                                           
                                                                                     
 The above copyright notice and this permission notice shall be included             
 in all copies or substantial portions of the Software.                              
                                                                                     
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS           
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                          
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                               
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE              
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION              
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION               
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                     
                                                                                     
 Nothing in this notice shall be deemed to grant any rights to                       
 trademarks, copyrights, patents, trade secrets or any other intellectual            
 property of the licensor or any contributor except as expressly stated              
 herein. No patent license is granted separate from the Software, for                
 code that you delete from the Software, or for combinations of the                  
 Software with other software or hardware.                                           
*/
package org.openadaptor.auxil.processor.javascript;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.*;
import org.openadaptor.auxil.simplerecord.ISimpleRecord;
import org.openadaptor.core.exception.RecordFormatException;

public class ScriptableSimpleRecord extends ScriptableObject  {
  public static final long serialVersionUID = 0x00;
  private static final Log log = LogFactory.getLog(ScriptableSimpleRecord.class);
  public static final String CLASSNAME=ScriptableSimpleRecord.class.getName();
  private ISimpleRecord simpleRecord;
  private boolean modified=false;
  
  // The zero-argument constructor used by Rhino runtime to create instances
  public ScriptableSimpleRecord() {}

  // Method jsConstructor defines the JavaScript constructor
  public void jsConstructor(Object simpleRecord) { 
    if (simpleRecord instanceof ISimpleRecord){
      this.simpleRecord=(ISimpleRecord)simpleRecord;
    }
    else {
      log.warn("Supplied record is not an ISimpleRecord instance, but "+simpleRecord.getClass().getName());
      throw new RecordFormatException("Only ISimpleRecord instances may be used within scripts");
    }
  }

  // The class name is defined by the getClassName method
  public String getClassName() { 
    return CLASSNAME; 
    }

  // Exposed methods are be defined using the jsFunction_ prefix. Here we define
  //  get for JavaScript.
  public Object jsFunction_get(Object key) {
    log.debug("get("+key+") invoked");
    Object value=simpleRecord.get(key);

    return value;
  }

  //Note: Always forces modify flag, even if new value = old value.
  public void jsFunction_put(Object key,Object value) {
    log.debug("put("+key+","+value+") invoked");
    modify();
    simpleRecord.put(key, value);
  }

  //Note: Always forces modify flag, even if it didn't contain the key.
    public Object jsFunction_remove(Object key) {
    log.debug("remove("+key+") invoked");
    modify();
    return simpleRecord.remove(key);
  }

  public String jsFunction_toString() {
    return simpleRecord.toString();
  }

  public boolean js_Function_containsKey(Object key) {
    log.debug("containsKey("+key+") invoked");
    return simpleRecord.containsKey(key);
  }
  
  public ISimpleRecord getSimpleRecord() {
    return simpleRecord;
  }
  
  private void modify() {
    if (!modified) {
      log.info("Modifying a simpleRecord - cloning original");
      simpleRecord=(ISimpleRecord)simpleRecord.clone();
      modified=true;
    }
  }
}
