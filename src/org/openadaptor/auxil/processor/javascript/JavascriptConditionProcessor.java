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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.ProcessingException;

import java.util.List;

/**
 * Processor which uses the evaluated result of javascript to 
 * implement an if/then/else construct.
 * <br>
 *  The evaluated javascript result returns true the 'then' processor
 *  is executed, otherwise the 'else' processor is executed.
 *  If the selected (then/else) processor is not configured, the record 
 *  will pass through without further change.
 * <br>
 * Note: If the javascript does not return a Boolean result, a 
 * RuntimeException will be thrown.
 * 
 * @author higginse
 * @deprecated replaced by more flexible map based script processors: {@link org.openadaptor.auxil.processor.script.ScriptConditionProcessor}
 *
 */
public  class JavascriptConditionProcessor extends JavascriptProcessor {

  private static final Log log = LogFactory.getLog(JavascriptConditionProcessor.class);

  // BEGIN Bean getters/setters
  /**
   * Processor which will be invoked if javascript evaluates to true
   */
  protected IDataProcessor thenProcessor;

  /**
   * Processor which will be invoked if javascript evaluates to false
   */
  protected IDataProcessor elseProcessor;

  /**
   * Assign an optional processor which will be invoked if the
   * javascript evaluates to true.
   * @param thenProcessor Any IDataProcessor instance.
   */
  public void setThenProcessor(IDataProcessor thenProcessor) {
    this.thenProcessor = thenProcessor;
  }

  /**
   * Return the processor, if any which will be invoked if the
   * javascript evaluates to true.
   * @return IDataProcessor instance (or null)
   */
  public IDataProcessor getThenProcessor() {
    return thenProcessor;
  }

  /**
   * Assign an optional processor which will be invoked if the
   * javascript evaluates to false.
   * @param elseProcessor Any IDataProcessor instance.
   */
 public void setElseProcessor(IDataProcessor elseProcessor) {
    this.elseProcessor = elseProcessor;
  }

 /**
  * Return the processor, if any which will be invoked if the
  * javascript evaluates to false.
  * @return IDataProcessor instance (or null)
  */
  public IDataProcessor getElseProcessor() {
    return elseProcessor;
  }
  // END Bean getters/setters


  /**
   * Checks that the mandatory properties have been set. Will call validate() on the
   * super class to ensure that it's mandatory properties have also been set.
   * <p/>
   *
   * Although it's permissible to not define the <em>then</em> and <em>else</em>
   * processors, we write out warnings to give the user some chance when debugging
   * adaptors.
   *
   * @param exceptions list of exceptions that any validation errors will be appended to
   */
  public void validate(List exceptions) {
    super.validate(exceptions);

    if ( thenProcessor == null && elseProcessor == null ) {
      log.warn("No [thenProcessor] or [elseProcessor] defined. The data will not be modified");
    }

    if ( thenProcessor == null && elseProcessor != null ) {
      log.warn("No [thenProcessor] defined. Only records that don't match the condition script will be modified");
    }

    if ( thenProcessor != null && elseProcessor == null ) {
      log.debug("No [elseProcessor] defined. Only records that match the condition filter will be modified");
    }
  }


  /**
   * Invoke thenProcessor or ElseProcessor depending on the evaluation result
   * of the supplied JavaScriptResult.
   * <br>
   * It will return the output of the called processor, or will wrap the 
   * result from the JavascriptResult in an Object[] if no processor has been
   * configured.
   *
   * @throws ProcessingException if a null was passed
   */
  protected Object[] generateOutput(JavascriptResult jsResult) {
    if ( jsResult == null ) {
      throw new ProcessingException("Null result passed. Unable to process", this);
    }

    Object scriptResult=jsResult.executionResult;
    if (!(scriptResult instanceof Boolean)) {
      String msg="Script should return a Boolean result. Instead it has returned: ";
      msg += scriptResult==null?"<null>":scriptResult.getClass().getName();
      log.warn(msg);
      throw new RuntimeException(msg);
    }
    Object record=jsResult.outputRecord.getRecord();

    if (((Boolean)scriptResult).booleanValue()){
      return thenProcessor == null ? new Object[] { record } : thenProcessor.process(record);
    }
    else {
      return elseProcessor == null ? new Object[] { record } : elseProcessor.process(record);
    }
  }
}
