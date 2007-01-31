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
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.util.JavascriptEngine.JavascriptResult;

public  class JavascriptConditionProcessor extends JavascriptProcessor {

  private static final Log log = LogFactory.getLog(JavascriptConditionProcessor.class);

  // BEGIN Bean getters/setters
  protected IDataProcessor thenProcessor;

  protected IDataProcessor elseProcessor;
  public void setThenProcessor(IDataProcessor thenProcessor) {
    this.thenProcessor = thenProcessor;
  }

  public IDataProcessor getThenProcessor() {
    return thenProcessor;
  }

  public void setElseProcessor(IDataProcessor elseProcessor) {
    this.elseProcessor = elseProcessor;
  }

  public IDataProcessor getElseProcessor() {
    return elseProcessor;
  }
  // END Bean getters/setters

  /**
   * Generate an output record array.
   * Default behaviour is just to wrap the outputRecord in an Object[].
   * @param outputRecord
   * @param scriptResult
   * @return
   */
  protected Object[] generateOutput(JavascriptResult jsResult) {
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
