/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.processor.script.ScriptProcessor;
import org.openadaptor.core.IMetadataAware;
import org.openadaptor.core.IWriteConnector;
import org.openadaptor.core.exception.ValidationException;

/**
 * A file write connector that opens and closes output stream for every message 
 * it receives. It allows for deriving the name of the output file dynamically
 * with script using message payload.
 * 
 * @author OA3 Core Team
 */
public class DynamicFileWriteConnector extends FileWriteConnector implements IMetadataAware {

  private static final Log log = LogFactory.getLog(DynamicFileWriteConnector.class);
  
  private ScriptProcessor scriptProcessor = new ScriptProcessor();
  
  private boolean scriptProvided = false;

  private Map metadata = new HashMap(); // Empty to start with. Will be reset for each new message.

  private boolean singleFilenameForBatch = true;
    
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
    
    /* Pass in any supplied metadata. This may need to be more sophisticated. */
    scriptProcessor.setMetadata(metadata);
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
    if (getSingleFilenameForBatch()) {
      /* treat batch as a single unit */
      /* Derives file name from message payload */
      setFilename(deriveFilename(data));
      /* Opens stream, writes data, closes stream. */
      super.connect();
      Object result = super.deliver(data);
      disconnect();
      return result;
    } else {
      /* Iterate through the Batch */
      Object[] resultArray = new Object[data.length];
      for (int i = 0; i < data.length; i++) {
        Object[] batchElement = new Object[] { data[i] };
        /* Derives file name from message payload */
        setFilename(deriveFilename(batchElement));
        /* Opens stream, writes data, closes stream. */
        /* should keep track of filename and connect disconnect only if it changes */
        super.connect();
        resultArray[i] = super.deliver(batchElement);
        disconnect();
      }
      return resultArray;
    }
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

  public void setMetadata(Map metadata) {
    this.metadata = metadata;    
  }

  /**
   * Define whether or not to write all of a batch to a single file. The default is
   * true for backwards compatibility.
   * 
   * @param singleFilenameForBatch the singleFilenameForBatch to set
   */
  public void setSingleFilenameForBatch(boolean singleFilenameForBatch) {
    this.singleFilenameForBatch = singleFilenameForBatch;
  }

  /**
   * Define whether or not to write all of a batch to a single file. The default is
   * true for backwards compatibility.
   * 
   * @return the singleFilenameForBatch
   */
  public boolean getSingleFilenameForBatch() {
    return singleFilenameForBatch;
  }
  
  
}
