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

package org.openadaptor.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;
import org.openadaptor.auxil.processor.javascript.ScriptableSimpleRecord;
import org.openadaptor.auxil.simplerecord.ISimpleRecord;
import org.openadaptor.core.exception.RecordException;

public  class JavascriptEngine  {
  private static final Log log = LogFactory.getLog(JavascriptEngine.class);

  public static final String DEFAULT_BOUND_NAME="record";

  private Context context;
  private Scriptable scope;

  public JavascriptEngine() {
    super();
    context=Context.enter(); //Should have matching Context.exit!
    try {
      scope = context.initStandardObjects();
      ScriptableObject.defineClass(scope,ScriptableSimpleRecord.class);
    }
    catch (Exception e) {
      String msg="Failed to define "+ScriptableSimpleRecord.CLASSNAME;
      log.warn(msg);
      throw new RuntimeException(msg,e);
    }
  }
  
  public JavascriptResult execute(String script,Object input) {
    return execute(script,input,DEFAULT_BOUND_NAME);
  }
  public JavascriptResult execute(String script,Object input,String boundName){
    Scriptable record=getRecord(input);
    scope.put(boundName, scope, record);
    Object scriptResult= context.evaluateString(scope,script, "<cmd>", 1, null);
    log.info("Result type:"+(scriptResult==null?"<null>":scriptResult.getClass().getName()));
    log.info("Result: "+scriptResult);
    ISimpleRecord outputRecord= ((ScriptableSimpleRecord)record).getSimpleRecord();
    return new JavascriptResult(scriptResult,outputRecord);
  }

  private Scriptable getRecord(Object input) {
    Scriptable record=null;
    try {
      record=context.newObject(scope,ScriptableSimpleRecord.CLASSNAME,new Object[] {input});
    }
    catch (WrappedException we){
      log.warn("Javascript exception: "+we);
      Throwable cause=we.getCause();
      if (cause instanceof RecordException){
        throw (RecordException)cause;
      }
      else {
        throw new RuntimeException(cause);
      }
    }
    return record;
  }

  public class JavascriptResult {
    public final Object executionResult;
    public final ISimpleRecord outputRecord;
    public JavascriptResult(Object executionResult,ISimpleRecord outputRecord){
      this.executionResult=executionResult;
      this.outputRecord=outputRecord;
    }
  }
}
