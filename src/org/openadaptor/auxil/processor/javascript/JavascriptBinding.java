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

import org.openadaptor.core.IComponent;
import org.openadaptor.core.exception.ValidationException;
import org.openadaptor.util.JavascriptEngine;
import org.openadaptor.util.JavascriptEngine.JavascriptResult;

import java.util.List;

/**
 * Allows execution of javascript with Bound Objects.
 * 
 * @author higginse
 *
 */
public  class JavascriptBinding  {

  private String boundName=JavascriptEngine.DEFAULT_BOUND_NAME;
  private String script=null;


  /**
   * JavaScriptEngine which will actually execute the javascript.
   */
  private JavascriptEngine jsEngine=new JavascriptEngine();


  // BEGIN Bean getters/setters
  /**
   * Set the name by which supplied objects may be referred
   * to within the javascript.
   * <br>
   * The default value is JavascriptEngine.DEFAULT_BOUND_NAME
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
   * The default value is JavascriptEngine.DEFAULT_BOUND_NAME
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
   * Mandatory. Assigns the javascript which will be executed on calls to
   * execute(Object input)
   *
   * @param script String containing javascript.
   */
  public void setScript(String script) {
    this.script = script;
  }


  // END Bean getters/setters

  /**
   * Evaluate the configured javascript in the context of the 
   * supplied input Object.
   * @param input Object containing a record which the javascript may
   *              refer to using the boundName.
   * @return JavascriptResult containing the (possibly modified)
   *         record, and the the result of javascript evaluation.
   */
  public JavascriptResult execute(Object input) {
    return jsEngine.execute(script, input,boundName);
  }


  /**
   * Checks that the mandatory properties have been set
   *
   * @param exceptions list of exceptions that any validation errors will be appended to
   * @param comp the component that this class is bound to. Required as part of the
   * constructor for any ValidationExceptions we may raise
   */
  public void validate(List exceptions, IComponent comp) {
    if ( script == null )
      exceptions.add(new ValidationException("You must supply a value for the [script] property", comp));
  }
}
