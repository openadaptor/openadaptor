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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.util.JavascriptEngine.JavascriptResult;
/**
 * Executes javascript scripts in the context of the data record.
 * <br>
 * This expects to be configured with a JavascriptBinding, and
 * javascript.
 * the binding associates the current record with the javscript
 * for execution.
 * <br>
 * The binding may be omitted, in which case a default binding will 
 * used (see JavascriptBinging for the default binding).
 *  
 * @author higginse
 *
 */
public  class JavascriptProcessor implements IDataProcessor {

  private static final Log log = LogFactory.getLog(JavascriptProcessor.class);

  private JavascriptBinding javascript=new JavascriptBinding();
 
  // BEGIN Bean getters/setters
  /**
   * Associates a JavascriptBinding with this processor.
   * <br>
   * Currently only necessary if you with to override the
   * default bound name for the record.
   */
  public void setJavascriptBinding(JavascriptBinding javascript) {
    this.javascript=javascript;
  }
  /**
   * Returns the binding for this processor.
   */
  public JavascriptBinding getJavascriptBinding() {
    return javascript;
  }

  /**
   * Rgturn the javascript to be executed by this processor.
   * @return String containing the javascript to be executed.
   */
  public String getScript() {
    return javascript.getScript();
  }
  /**
   * Assign  the javascript to be executed by this processor
   * <br>
   * Note that it will implicitly configure a default binding if none 
   * has been explicitly configured.
   * @param script containing Javascript for execution.
   */
  public void setScript(String script) {
    if (javascript==null) {
      javascript=new JavascriptBinding();
    }
    javascript.setScript(script);
  }

  // END Bean getters/setters

  public JavascriptProcessor() {
    super();
  }

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
    JavascriptResult jsResult=javascript.execute(input);
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
    //log.info("Result was: "+context.toString(result));
    return new Object[] {jsResult.outputRecord};
  }
  
  public void validate(List exceptions) {
  }

  public void reset(Object context) {
  }
 
}
