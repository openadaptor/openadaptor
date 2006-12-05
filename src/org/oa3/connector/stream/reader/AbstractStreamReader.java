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
package org.oa3.connector.stream.reader;

/*
 * File: $Header: /cvs/oa3/src/org/oa3/connector/stream/reader/AbstractStreamReader.java,v 1.4 2006/10/18 17:09:05
 * higginse Exp $ Rev: $Revision: 1.4 $ Created Jan 10, 2006 by Eddy Higgins
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.OAException;
import org.oa3.connector.stream.RFC2279;

/**
 * Generic abstract class to allow input from any Stream-oriented source of data.
 * <p>
 * Broadly, it utilises an <code>InputStream</code> from the underlying data source, and then wraps it in an
 * <code>InputStreamReader</code>.
 * <p>
 * Ultimately this reader will be used by a StreamReadConnector, in conjunction witn an <code>IRecordReader</code>
 * implementation to split the data into records
 * 
 * @see StreamReadConnector
 * @see IRecordReader
 * @author Eddy higgins
 */
public abstract class AbstractStreamReader implements IStreamReader {
  private static final Log log = LogFactory.getLog(AbstractStreamReader.class);

  /**
   * This is the reader which is derived from the underlying InputStream
   */
  protected Reader _reader;

  /**
   * Underlying InputStream (to be assigned by concrete sub-classes)
   */
  protected InputStream _inputStream;

  /**
   * Reader encoding. Default is <tt>RFC2279.UTF_8</tt>
   */
  protected String _encoding = RFC2279.UTF_8;

  /**
   * This (optionally) provides hints as to where the current data stream is coming from.
   * <p>
   * Makes most sense in a context where the stream is actually coming from multiple sources (e.g. a composition of
   * several files).
   */
  private Object readerContext = null;

  /**
   * Flag to indicate whether or not this reader is currently connected.
   */
  protected boolean connected = false;

  // BEGIN Bean getters/setters

  public Reader getReader() {
    return _reader;
  }

  public void setEncoding(String encoding) {
    _encoding = encoding;
  }

  public String getEncoding() {
    return _encoding;
  }

  protected void setReaderContext(final Object readerContext) {
    log.info("Setting readerContext to " + (readerContext == null ? "<null>" : readerContext));
    this.readerContext = readerContext;
  }

  public Object getReaderContext() {
    return readerContext;
  }

  // END Bean getters/setters

  /**
   * Connect this StreamReader making it ready for use.
   * <p>
   * Generally this should be called by a sub-class implementation of connect() which has already made the underlying
   * <code>InputStream</code> available. It then derives an <code>InputStreamReader</code> for use, and sets the
   * state to connected.
   * 
   * @throws OAException
   *           if the underlying <code>InputStream</code> is <tt>null</tt> or an unsupported encoding has been
   *           configured for the reader.
   */
  public void connect() throws OAException {
    log.debug("Getting a reader using encoding " + _encoding);

    if (_inputStream == null) {
      throw new OAException("InputStream is not initialised");
    }
    try {
      _reader = new InputStreamReader(_inputStream, _encoding);
    } catch (UnsupportedEncodingException uee) {
      throw new OAException("Unsupported Encoding - " + _encoding);
    }
    connected = true;
    log.info("Connected (Reader created)");
  }

  /**
   * Disconnect this reader.
   * <p>
   * It does this by closing the <code>Reader</code> and, in turn, the underlying <code>InputStream</code> (if they
   * are not null).
   */
  public void disconnect() throws OAException {
    log.debug("Disconnecting (closing reader)");
    if (_reader != null) {
      try {
        _reader.close();
        log.info("Reader closed");
      } catch (IOException ioe) {
        log.warn(ioe.getMessage());
      }
    }
    if (_inputStream != null) {
      try {
        _inputStream.close();
        log.info("InputStream closed");
      } catch (IOException ioe) {
        log.warn(ioe.getMessage());
      }
    }
    connected = false;
  }
}
