/*
 * [[
 * Copyright (C) 2001 - 2006 The Software Conservancy as Trustee. All rights
 * reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Nothing in this notice shall be deemed to grant any rights to
 * trademarks, copyrights, patents, trade secrets or any other intellectual
 * property of the licensor or any contributor except as expressly stated
 * herein. No patent license is granted separate from the Software, for
 * code that you delete from the Software, or for combinations of the
 * Software with other software or hardware.
 * ]]
 */
package org.oa3.connector.stream.writer;

/*
 * File: $Header: /cvs/oa3/src/org/oa3/connector/stream/writer/StreamWriteConnector.java,v 1.2 2006/10/16 17:51:18
 * higginse Exp $ Rev: $Revision: 1.2 $ Created Feb 23, 2006 by Eddy Higgins
 */
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.OAException;
import org.oa3.connector.AbstractWriteConnector;

/**
 * This is the main Connector for Stream based record writers. It requires: IStreamWriter- Writer to do the actual
 * writing. IRecordWriter- Writes a record at a time
 * 
 * @author OA3 Core Team
 */
public class StreamWriteConnector extends AbstractWriteConnector {
  
  private static final Log log = LogFactory.getLog(StreamWriteConnector.class);

  protected IStreamWriter streamWriter;

  protected Writer writer; // Convenience handle (it's obtained from the streamWriter)

  protected IRecordWriter recordWriter;

  private IRecordWriter defaultRecordWriter = new StringRecordWriter();

  // BEGIN Bean getters/setters

  public void setStreamWriter(IStreamWriter streamWriter) {
    this.streamWriter = streamWriter;
  }

  public void setRecordWriter(IRecordWriter recordWriter) {
    this.recordWriter = recordWriter;
  }

  public IStreamWriter getStreamWriter() {
    return streamWriter;
  }

  public IRecordWriter getRecordWriter() {
    return recordWriter;
  }

  // END Bean getters/setters

  /**
   * Establish a connection to external message transport. If already connected then do nothing.
   * 
   * @throws org.oa3.control.OAException
   */
  public void connect() {
    log.debug("Connector: [" + getId() + "] connecting ....");
    streamWriter.connect();
    writer = streamWriter.getWriter();// ( convenient access to writer)
    if (recordWriter == null) {
      log.info("No Record Writer assigned - assigning default of " + defaultRecordWriter.getClass().getName());
      recordWriter = defaultRecordWriter;
    }
    recordWriter.setWriter(writer);
    connected = true;
    log.info("Connector: [" + getId() + "] successfully connected.");
  }

  /**
   * Disconnect from the external message transport. If already disconnected then do nothing.
   * 
   * @throws org.oa3.control.OAException
   */
  public void disconnect() {
    log.debug("Connector: [" + getId() + "] disconnecting ....");
    if (streamWriter != null) {
      streamWriter.disconnect();
      connected = false;
      log.info("Connector: [" + getId() + "] disconnected");
    } else {
      log.warn("Connector: [" + getId() + "] ignoring disconnect attempt, as the StreamWriteConnector is null");
    }
  }

  public Object deliver(Object[] records) throws OAException {
    Object result = null;
    try {
      if (records != null) {
        for (int i = 0; i < records.length; i++) {
          recordWriter.write(records[i]);
        }
      }
      recordWriter.flush();
    } catch (IOException ioe) {
      throw new OAException(ioe.getMessage(), ioe);
    }
    return result;// ToDo: Revisit this - can't return anything sensible here yet.
  }

}
