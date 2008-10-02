/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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
package org.openadaptor.auxil.connector.iostream.writer;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.processor.script.ScriptProcessor;
import org.openadaptor.core.IWriteConnector;
import org.openadaptor.core.exception.ValidationException;

/**
 * A file write connector that opens and closes output stream for every message 
 * it receives. It allows for deriving the name of the output file dynamically
 * with script using massage payload.
 * 
 * @author OA3 Core Team
 */
public class DynamicFileWriteConnector extends FileWriteConnector {

  private static final Log log = LogFactory.getLog(DynamicFileWriteConnector.class);
  
  private ScriptProcessor scriptProcessor = new ScriptProcessor();
  
  private boolean scriptProvided = false;
    
  /**
   * Constructor.
   */
  public DynamicFileWriteConnector() {
    super();
  }
  
  /**
   * Constructor.
   * 
   * @param id
   */
  public DynamicFileWriteConnector(String id) {
    super(id);
  }
  
  /**
   * No need to do anything.
   */
  public void connect() {
  }
  
  /**
   * Derives a dynamic filename based on message payload. Uses a standard 
   * {@link ScriptProcessor} to execute/evaluate the script.
   * 
   * @param data message payload
   * @return a file name
   */
  protected String deriveFilename(Object[] data){
    
    String filename = getFilename();
  
    /* null checks */
    if(data==null || data.length==0){
      log.debug("Returning default filename: " + filename);
      return filename;
    }
    
    /* We're using the first element of the payload array for now.. this may well change soon. */
    Object [] scriptResArray = scriptProcessor.process(data[0]);
    
    if(null != scriptResArray && scriptResArray.length>0) {
      log.debug("Size of objects = " + scriptResArray.length);
      Object dynamicFilename = scriptResArray[0];
      if (dynamicFilename!=null) {
        filename=dynamicFilename.toString();
      }
      log.info("dynamicFilename="+filename);
    }
    else{
      log.debug("Script return no file names.");
    }
    return filename;
  }
 
  /**
   * Writes data to a file with a dynamically derived name.
   */
  public Object deliver(Object[] data) {  
    
    /* Derives file name from message payload */
    setFilename(deriveFilename(data));
    
    /* Opens stream, writes data, closes stream. */
    super.connect();
    Object result = super.deliver(data);
    disconnect();
    
    return result;
  }
  
  /**
   * In addition to FileWriteConnector validation checks if 'script' was set.
   * 
   * @see FileWriteConnector#validate(List)
   * @see IWriteConnector#validate(List)
   */
  public void validate(List exceptions) {
    super.validate(exceptions);
    if (! scriptProvided) {
      exceptions.add(new ValidationException("script property not set", this));
    }
  }
  
  /**
   * Sets the script that will derive a dynamic filename based on message payload.
   * 
   * @param script
   */
  public void setScript(String script) {
    scriptProvided = true;
    scriptProcessor.setScript(script);
    scriptProcessor.validate(new java.util.ArrayList());
  }
}
