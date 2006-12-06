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
package org.oa3.auxil.connector.iostream.reader;

/*
 * File: $Header: /cvs/oa3/src/org/oa3/connector/stream/reader/StreamReadConnector.java,v 1.8 2006/10/18 17:09:05
 * higginse Exp $ Rev: $Revision: 1.8 $ Created Nov 4, 2005 by Eddy Higgins
 */
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.core.connector.AbstractReadConnector;
import org.oa3.core.exception.OAException;

/**
 * This is the main connector implementation for reading streams of records.
 * <p>
 * It delegates behaviour to a stream reader (<code>IStreamReader</code> implementation) which has responsibility to
 * read Streams of data, and to a record reader (<code>IRecordReader</code> implementation) which then splits the
 * incoming stream into data records.
 * 
 * This composition makes is possible to use any record reader with any streamed data source.
 * 
 * @author Eddy Higgins
 */
public class StreamReadConnector extends AbstractReadConnector {

  private static Log log = LogFactory.getLog(StreamReadConnector.class);

  protected IStreamReader streamReader;

  protected Reader _reader; // Convenience handle (it's obtained from the streamReader)

  protected IRecordReader recordReader;

  // BEGIN Bean getters/setters

  /**
   * Associate an <code>IStreamReader</code> with this connector.
   * <p>
   * This will also tell the streamReader to notify <tt>this</tt> of future data origin changes, by calling
   * setConnector(this) on it.
   * 
   * @param streamReader
   *          <code>IStreamReader</code> to be used by this connector
   * @see IStreamReader for more info on the effect of setConnector()
   */
  public void setStreamReader(IStreamReader streamReader) {
    this.streamReader = streamReader;
  }

  /**
   * Return the <code>IStreamReader</code> for this connector.
   * 
   * @return <code>IStreamReader</code> for this connector
   */
  public IStreamReader getStreamReader() {
    return streamReader;
  }

  /**
   * Associate an <code>IRecordReader</code> with this connector.
   * 
   * @param recordReader
   */
  public void setRecordReader(IRecordReader recordReader) {
    this.recordReader = recordReader;
  }

  /**
   * Return the <code>IRecordReader</code> for this connector.
   * 
   * @return <code>IRecordReader</code> for this connector
   */
  public IRecordReader getRecordReader() {
    return recordReader;
  }

  // END Bean getters/setters

  /**
   * Establish a connection to a Stream Data Source.
   * <p>
   * It achieves this as follows:
   * <UL>
   * <LI> Connect the stream reader (obtaining it's <code>Reader</code>)
   * <LI> Prime the record reader (having assigned a <code>DefaultRecordReader</code> if necessary) with the retrieved<code>Reader</code>.
   * </UL>
   * 
   * @throws OAException
   *           if an IOException occurs during the connection attempt.
   */
  public void connect() {
    log.debug("Connector: [" + getId() + "] connecting ....");
    // Connect the StreamReader - thus making its Reader available
    streamReader.connect();
    // Get the Reader
    _reader = streamReader.getReader();
    if (recordReader == null) { // Get a default one
      log.info("No Record Reader assigned - assigning Default one (reads entire stream as a String)");
      recordReader = new DefaultRecordReader();
    }
    try { // Prime the RecordReader with the stream's Reader.
      recordReader.setReader(_reader);
    } catch (IOException ioe) { // Bugger
      log.error("Failed to set the reader for this connector" + ioe.toString());
      throw new OAException("Failed to set the reader on the recordReader - " + ioe.toString(), ioe);
    }
    connected = true;
    log.info("Connector: [" + getId() + "] successfully connected.");
  }

  /**
   * Disconnect this connector.
   * <p>
   * It will disconnect the streamReader if it is not already disconnected.
   * 
   * @throws OAException
   *           if the stream reader disconnect throws one.
   */
  public void disconnect() {
    log.debug("Connector: [" + getId() + "] disconnecting ....");
    if (streamReader != null) {
      streamReader.disconnect();
      connected = false;
      log.info("Connector: [" + getId() + "] disconnected");
    } else {
      log.warn("Connector: [" + getId() + "] ignoring disconnect attempt, as the StreamReader is null");
    }
  }

  /**
   * Attempt to refresh input data (after a poll).
   * <p>
   * Currently, this just crudely calls disconnect() and connect().
   * 
   * @throws OAException
   *           If the disconnect/connect calls throw one
   */
  public void refreshData() throws OAException {
    // ToDo: Make refreshData return a boolean.
    log.info("Refreshing data after poll");
    // boolean result=true;
    try {
      disconnect();
      connect();
    } catch (OAException oae) {
      log.info("Failed to refresh data: " + oae.getMessage());
      // result=false;
    }
    // return result;
  }

  /**
   * Retrieve the next Record from this connector.
   * <P>
   * Returns an Object[] containing the record, or <tt>null</tt> if input is exhausted.
   * 
   * @return Object[] containing the record, or <tt>null</tt> if input is exhausted.
   * @throws OAException
   *           if the underlying recordReader throws an IOException.
   */
  public Object[] nextRecord(long timeoutMs) throws OAException {
    Object[] records = null;
    try {
      Object record = recordReader.next();
      if (record != null) {
        records = new Object[] { record };
      }
    } catch (IOException ioe) {
      throw new OAException(ioe.getMessage(), ioe);
    }
    return records;
  }

  public boolean isDry() {
    return recordReader.isDry();
  }

  public Object getReaderContext() {
    return streamReader.getReaderContext();
  }
}

/**
 * Utility class to read entire input from the reader, as a single record.
 * 
 */
class DefaultRecordReader extends AbstractRecordReader {
  private static int BUFSIZ = 1024;

  private char[] buf = new char[BUFSIZ];

  private boolean isDry = false;

  /**
   * Read the entire input from the reader.
   * 
   * @return String containing the entire input from the reader.
   * @throws IOException
   *           if there is a problem with the data.
   */
  public Object next() throws IOException {
    int totalRead = 0;
    StringBuffer sb = new StringBuffer();
    int charsRead = -1;
    while ((charsRead = getReader().read(buf, 0, BUFSIZ)) != -1) {
      totalRead += charsRead;
      sb.append(buf, 0, charsRead);
    }
    isDry = true;
    return (totalRead > 0) ? sb.toString() : null;
  }

  public boolean isDry() {
    return isDry;
  }

}
