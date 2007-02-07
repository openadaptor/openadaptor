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
/**
 * Utility class to provide Javascript execution using Rhino.
 * <br>
 * It allows execution of a javascript, whilst providing a binding to
 * a data record to allow the script to interact with it.
 * 
 * the execute() method may be used to invoke a javascript. It expects an
 * Object which contains an ISimpleRecord implementation.
 * <br>
 * It may optionally be invoked with an String to override the default
 * bound name of DEFAULT_BOUND_NAME.
 * <br>
 * Execution will return a JavascriptResult object which contains both
 * the result returned by the javascript, and an output Object which
 * corresponds to that supplied.
 * <br>
 * Note that if the supplied Object was not modified by the script, it
 * is returned. If the script however modifies it, then a clone() of that
 * Object it taken and the modified clone is returned. This is achieved
 * by the ScriptableSimpleRecord class.
 * 
 * @author higginse
 *
 */
public  class JavascriptEngine  {
  private static final Log log = LogFactory.getLog(JavascriptEngine.class);
  /**
   * This is the default name by which the input object may be referenced
   * from within the javascript script.
   * <br>
   * e.g.   record.set("mid", (record.get("bid") + record.get("offer")) / 2)
   */
  public static final String DEFAULT_BOUND_NAME="record";

  //private Context context;
  private Scriptable scope;

  /**
   * This performs some one-time initialisation of the Javascript Engine.
   * <br>
   * In particular, it sets up an execution scope which is shared between
   * subsequent calls to execute().
   * Note that the Context is associated with a thread, and thus may not
   * be safely reused. However, the Rhino implementation allows redundant calls
   * to Context.enter() on the same thread. The the Rhino documentation for 
   * more detail.
   */
  public JavascriptEngine() {
    super();
    Context context=Context.enter();
    try {
      scope = context.initStandardObjects();
      ScriptableObject.defineClass(scope,ScriptableSimpleRecord.class);
    }
    catch (Exception e) {
      String msg="Failed to define "+ScriptableSimpleRecord.CLASSNAME;
      log.warn(msg);
      throw new RuntimeException(msg,e);
    }
    finally {
      Context.exit();
    }
  }
  /**
   * Trivial wrapper around execute(script,input,DEFAULT_BOUND_NAME).
   * 
   * @param script Javascript to execute
   * @param input  An object which should be an ISimpleRecord implementation
   * @return JavascriptResult object containing the result of the script, and the output Object
   */
  public JavascriptResult execute(String script,Object input) {
    return execute(script,input,DEFAULT_BOUND_NAME);
  }
  
  /**
   * Execute a supplied javascript given an object to be bound for use
   * by the script. The boundName argument should contain the name by which
   * the object may be referenced from within the script.
   * <BR>
   * This will attempt to execute the script, and return the result and
   * the resulting output object wrapped in a JavascriptResult object.
   * 
   * @param script Javascript to execute
   * @param input  An object which should be an ISimpleRecord implementation
   * @param boundName A String containing the name by which the object is to 
   *                  be referenced from within the javascript
   * @return JavascriptResult object containing the result of the script, and the output Object
   */
  public JavascriptResult execute(String script,Object input,String boundName){
    Context context=Context.enter();
    try {
      Scriptable record=getRecord(context,input);
      scope.put(boundName, scope, record);
      Object scriptResult= context.evaluateString(scope,script, "<cmd>", 1, null);
      log.info("Result type:"+(scriptResult==null?"<null>":scriptResult.getClass().getName()));
      log.info("Result: "+scriptResult);
      ISimpleRecord outputRecord= ((ScriptableSimpleRecord)record).getSimpleRecord();
      return new JavascriptResult(scriptResult,outputRecord);
    }
    finally{
      Context.exit();
    }
  }

  /**
   * Convert the supplied Object into a ScriptableSimpleRecord 
   * @param context (Rhino) Context for the conversion
   * @param input Object to be converted
   * @return Scriptable generated from the object.
   * @throws RecordException if the supplied object wasn't an ISimpleRecord
   * @throws RuntimeException if the javascript execution fails for any reason. Exception
   *         will wrap the underlying cause.
   */
  private Scriptable getRecord(Context context,Object input) {
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

  /**
   * Contains the result of execution a javascript script.
   * <BR>
   * It has two parts <BR>
   * <ul>
   *   <li> executionResult - the result from execution.</li>
   *   <li> outputRecord - an ISimpleRecord containing the possibly modified record which was provided for execution.</li>
   * </ul>  
   * <br>
   * Note: As the fields are final they are provided as public.
   * @author higginse
   *
   */
  public class JavascriptResult {
    public final Object executionResult;
    public final ISimpleRecord outputRecord;
    public JavascriptResult(Object executionResult,ISimpleRecord outputRecord){
      this.executionResult=executionResult;
      this.outputRecord=outputRecord;
    }
  }
}
