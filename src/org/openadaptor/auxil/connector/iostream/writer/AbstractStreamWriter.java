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

package org.openadaptor.auxil.connector.iostream.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.exception.ComponentException;

/**
 * 
 * @author OA3 Core Team
 */
public abstract class AbstractStreamWriter extends Component implements IStreamWriter {
  
  private static final Log log = LogFactory.getLog(AbstractStreamWriter.class);

  protected Writer writer;

  protected OutputStream outputStream;

  protected String encoding = UTF_8;

  // BEGIN Bean getters/setters
  public Writer getWriter() {
    return writer;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public String getEncoding() {
    return encoding;
  }

  // END Bean getters/setters

  public void connect() throws ComponentException {
    log.debug("Getting a writer using encoding " + encoding);

    if (outputStream == null) {
      throw new ComponentException("OutputStream is not initialised", this);
    }
    try {
      writer = new OutputStreamWriter(outputStream, encoding);
    } catch (UnsupportedEncodingException uee) {
      throw new ComponentException("Unsupported Encoding - " + encoding, this);
    }
    log.info("Connected (reader created)");
  }

  public void disconnect() throws ComponentException {
    log.debug("Disconnecting (closing writer)");
    if (writer != null && outputStream != System.out) {
      try {
        writer.close();
        log.info("Writer closed");
      } catch (IOException ioe) { // ToDo: Validate decision that this can safely be ignored.
        log.warn(ioe.getMessage());
      }
    }
    if (outputStream != null && outputStream != System.out) {
      try {
        outputStream.close();
        log.info("OutputStream closed");
      } catch (IOException ioe) { // ToDo: Validate decision that this can safely be ignored.
        log.warn(ioe.getMessage());
      }
    }
  }
}
