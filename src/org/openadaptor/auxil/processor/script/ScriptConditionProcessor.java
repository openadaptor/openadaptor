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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.exception.ProcessingException;
import org.openadaptor.core.exception.ValidationException;

/**
 * Wrapper around a ScriptProcessor which allows alternate processing
 * depending on the result of the executed script
 * 
 * Note: This has been changed to use delegation rather than
 * inheritance to allow the use of alternate ScriptProcessor types,
 * such as MapFilterProcessor.
 * 
 * @author higginse
 * 
 */

public class ScriptConditionProcessor extends Component implements IDataProcessor {
  private static final Log log =LogFactory.getLog(ScriptConditionProcessor.class);

  private ScriptProcessor scriptProcessor; //Delegate ScriptProcessor
  private IDataProcessor ifProcessor;
  private IDataProcessor thenProcessor;

  public ScriptConditionProcessor() {
    super();
  }

  public ScriptConditionProcessor(String id) {
    super(id);
  }
  
  /**
   * Assign the delegate ScriptProcessor which while actually
   * execute the script.
   * 
   * @param scriptProcessor
   */
  public void setScriptProcessor(ScriptProcessor scriptProcessor) {
    this.scriptProcessor=scriptProcessor;
  }
  public ScriptProcessor getScriptProcessor() {
    return scriptProcessor;
  }

  public void setIfProcessor(IDataProcessor ifProcessor) {
    this.ifProcessor = ifProcessor;
  }

  public void setThenProcessor(IDataProcessor thenProcessor) {
    this.thenProcessor = thenProcessor;
  }
  
  public synchronized Object[] process(Object data) {
    Object[] output=null;
    scriptProcessor.process(data);//ToDo: Don't care about the result. Is this sensible?
    Object result=scriptProcessor.getLastResult();
    if (result instanceof Boolean) {
      boolean choice=((Boolean)result).booleanValue();
      if (log.isDebugEnabled()) {
        log.debug((choice?"if":"then")+" processor has been chosen");
      }
      output=choice ? ifProcessor.process(data) : thenProcessor.process(data);
    } else {
      throw new ProcessingException("script result is not boolean", this);
    }
    return output;
  }
  
  public void validate(List exceptions) {
    if (exceptions==null) { //IDataProcessor requires a non-null List
      throw new IllegalArgumentException("exceptions List may not be null");
    }
    if (scriptProcessor == null) {
      exceptions.add(new ValidationException("Property scriptProcessor must be configured", this));
    }
    else {
      scriptProcessor.validate(exceptions);
    }
   if (ifProcessor == null) {
      exceptions.add(new ValidationException("ifProcessor property not set", this));
    }
    if (thenProcessor == null) {
      exceptions.add(new ValidationException("thenProcessor property not set", this));
    }
  }
  public void reset(Object context) {
    scriptProcessor.reset(context);   
  }

}
