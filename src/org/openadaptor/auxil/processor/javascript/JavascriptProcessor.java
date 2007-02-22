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

package org.openadaptor.auxil.processor.javascript;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.auxil.simplerecord.ISimpleRecord;
import org.openadaptor.core.Component;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.ValidationException;
/**
 * Executes javascript scripts in the context of the data record.
 * <br>
 * This expects to be configured with a script containing javascript
 * to be executed in the context of supplied records.
 * <br>
 *
 * @author higginse
 *
 */
public  class JavascriptProcessor extends Component implements IDataProcessor {

  private static final Log log = LogFactory.getLog(JavascriptProcessor.class);

  /**
   * This is the default name by which the input object may be referenced
   * from within the javascript script.
   * <br>
   * e.g.   record.set("mid", (record.get("bid") + record.get("offer")) / 2)
   */
  public static final String DEFAULT_BOUND_NAME="record";

  private String boundName=DEFAULT_BOUND_NAME;
  private String script=null;
  private Script compiledScript=null;
  private Scriptable scope;

  // BEGIN Bean getters/setters
  /**
   * Set the name by which supplied objects may be referred
   * to within the javascript.
   * <br>
   * The default value is DEFAULT_BOUND_NAME
   */
  public void setBoundName(String name) {
    if ((name==null) ||(name.trim().length()==0)) {
      throw new RuntimeException("Null or empty value for boundName is not permitted");
    }
    boundName=name;
  }

  /**
   * Return the name by which supplied objects may be referred
   * to within the javascript.
   * <br>
   * The default value is DEFAULT_BOUND_NAME
   */
  public String getBoundName() {
    return boundName;
  }

  /**
   * Return the javascript which will be executed on
   * calls to execute(Object input)
   * @return String containing javascript.
   */
  public String getScript() {
    return script;
  }

  /**
   * Assign the javascript to be executed by this processor
   *
   * @param script containing Javascript for execution.
   */
  public void setScript(String script) {
    this.script = script;
  }
  // END Bean getters/setters

  /**
   * Execute javascript in the context of the supplied record.
   * <br>
   * Note that any javascript specific exceptions will manifest
   * themselves as RuntimeExceptions.
   * 
   * @return Object[] containing the result of executing the
   * javascript with the supplied input record.
   */
  public Object[] process(Object input) {

    JavascriptResult jsResult=execute(input);
    return generateOutput(jsResult);
  }

  /**
   * Generate an output record array from a returned JavascriptResult.
   * <br>
   * Default behaviour is just to wrap the outputRecord in an Object[].
   * <br>
   * This method exists primarily as a hook to allow it to be overridden
   * (for example, to allow the javascript result value to be returned instead).
   */
  protected Object[] generateOutput(JavascriptResult jsResult) {
    Object result=jsResult.executionResult;
    log.debug("Result :"+result+" [type="+(result==null?"<null>":result.getClass().getName())+"]");
    return new Object[] {jsResult.outputRecord};
  }

  /**
   * Checks that the mandatory properties have been set. 
   * script is a mandatory property.
   * <p/>
   *
   * While it is valid to supply a blank script we write a warning to the logs as the
   * processor will not modify the data.
   *
   * @param exceptions list of exceptions that any validation errors will be appended to
   */
  public void validate(List exceptions) {
    if (script == null )
      exceptions.add(new ValidationException("No script configured. Fatal error", this));
    if (script.trim().length()==0) {
      log.warn("Configured javascript is empty.");
    }
    try {
    compiledScript=compile(script);
    }
    catch (RuntimeException re) {
      exceptions.add(new ValidationException("Failed to compile javascript. Fatal error", this));
    }
  }


  public void reset(Object context) {}

  private Script compile(String script) {
    Script compiledScript=null;
    Context context=Context.enter();
    try {
      scope = context.initStandardObjects();
      ScriptableObject.defineClass(scope,ScriptableSimpleRecord.class);
      compiledScript=context.compileString(script, "oa script", 1, null);
      log.debug("Javascript has been compiled from source script");
    }
    catch (Exception e) {
      String msg="Failed to define "+ScriptableSimpleRecord.CLASSNAME;
      log.warn(msg);
      throw new RuntimeException(msg,e);
    }
    finally {
      Context.exit();
    }
    return compiledScript;
  }

  private JavascriptResult execute(Object input){
    Context context=Context.enter();
    try {
      Scriptable record=getRecord(context,input);
      scope.put(boundName, scope, record);
      Object scriptResult= compiledScript.exec(context, scope);
      ISimpleRecord outputRecord= ((ScriptableSimpleRecord)record).getSimpleRecord();
      return new JavascriptResult(scriptResult,outputRecord);
    }
    catch (org.mozilla.javascript.EvaluatorException ee) {
      log.error("Javascript execution failed: "+ee.toString());
      throw new RuntimeException("Javascript execution failed: "+ee.toString(),ee);
    }
    catch (org.mozilla.javascript.EcmaError ece) {
      log.error("Javascript execution failed: "+ece.toString());
      throw new RuntimeException("Javascript execution failed: "+ece.toString(),ece);
    }    
    finally{
      //Rhino has repectfully requested that we always make sure we exit the context!
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
   * Convenience mechanism to allow ad-hoc execution of javascript (for debugging only).
   * 
   * Note: It is very inefficient - instantiating a new processor for each executed script.
   * 
   * @param argv argumens of the form name=value for pre-populating the test map.
   * @throws IOException on I/O problems.
   */
  public static void main(String[] argv) throws IOException{
    ISimpleRecord map=new OrderedHashMap(); 
    for (int i=0;i<argv.length;i++){
      try {
      String[] pair=argv[i].split("=");
      String name=pair[0];
      String value=pair[1];
      map.put(name, value);
      System.out.println("Primed input record with "+name+"->"+value);
      }
      catch (Exception e) {
        System.err.println("Failed to process arg: "+argv[i]);
      }
    }
    BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
    String line;
    StringBuffer script=new StringBuffer();
    System.out.println("Enter javascript to be executed.");
    System.out.println("Two empty lines signfies end of input. ");
    System.out.println();
    System.out.println("The input map may be populated (top level) from command line arguments of the form 'name=value'");
    System.out.println();
    do  {
      System.out.print("javascript > "); 
      script.setLength(0);
      while ((line=br.readLine()).length()>0) {
        script.append(line);
        System.out.print(" ctd. > ");
      }  
      System.out.println("Result: ");
      try {
        JavascriptProcessor jsp=new JavascriptProcessor();
        jsp.setScript(script.toString());
        JavascriptResult jsr=jsp.execute(map);
        System.out.println(jsr.executionResult+" [type="+(jsr.executionResult==null?"<null>":jsr.executionResult.getClass().getName())+"]");
        String mod="unchanged";
        if (map!=jsr.outputRecord) {
          mod="modified";
          map=jsr.outputRecord;
        }
        System.out.println("Map ["+mod+"] is: "+map);
      }
      catch (org.mozilla.javascript.EvaluatorException ee) {
        System.out.println("<error> : "+ee.toString());
      }
      catch (org.mozilla.javascript.EcmaError ece) {
        System.out.println("<error>: "+ece.toString());
      }
    }
    while (script.length()>0);
    System.out.println("Exiting.");
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
