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

package org.openadaptor.auxil.processor.script;

import java.util.List;
import java.util.Map;

import org.openadaptor.core.Component;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.IMetadataAware;
import org.openadaptor.core.exception.ProcessingException;
import org.openadaptor.core.exception.ValidationException;

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
public class ScriptFilterProcessor extends Component implements IDataProcessor, IMetadataAware {

  private boolean filterOnMatch = true;
  private ScriptProcessor scriptProcessor; //Delegate ScriptProcessor
  private Map metadata;

  public ScriptFilterProcessor() {
    super();
  }

  public ScriptFilterProcessor(String id) {
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

  public void setFilterOnMatch(boolean filterOnMatch) {
    this.filterOnMatch = filterOnMatch;
  }

  /**
   * get scriptProcessor to process data, and filter based on
   * result.
   */
  public synchronized Object[] process(Object data) {
    scriptProcessor.setMetadata(metadata);
    Object[] output = scriptProcessor.process(data);
    Object result=scriptProcessor.getLastResult();
    if (result instanceof Boolean) { 
      if (((Boolean)result).booleanValue() == filterOnMatch) {
        output=new Object[] {}; //filter out the result and return empty array. 
      }
    } else {
      throw new ProcessingException("script result is not boolean", this);
    }
    return output;
  }

  public void reset(Object context) {
    scriptProcessor.reset(context);   
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
  }

  /**
   * Method will be called by the OA framework for each messages that passes through
   * the {@link IMetadataAware} component.
   * 
   * @param metadata - a set of key value pairs that may be used to pass information
   *        to components down the adaptor pipeline.
   */
  public void setMetadata(Map metadata) {
    this.metadata = metadata;    
  }
}
