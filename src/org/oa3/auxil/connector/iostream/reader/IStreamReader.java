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
 * File: $Header: /cvs/oa3/src/org/oa3/connector/stream/reader/IStreamReader.java,v 1.3 2006/10/18 17:09:05 higginse Exp $
 * Rev: $Revision: 1.3 $ Created Jan 27, 2006 by Eddy Higgins
 */
import java.io.Reader;

import org.oa3.auxil.connector.iostream.RFC2279;
import org.oa3.core.exception.ComponentException;

/**
 * Interface by which Listeners can extract individual records from a Reader.
 * 
 * @author Eddy Higgins
 */
public interface IStreamReader extends RFC2279 {

  /**
   * Get the underlying <code>Reader</code>.
   * 
   * @return Underlying <code>Reader</code> instance.
   */
  public Reader getReader();

  /**
   * Sets the character encoding for this Reader.
   * 
   * @param encoding
   *          <code>String</code> containing the character encoding to use
   * @see RFC2279 for some well-known encodings.
   */
  public void setEncoding(String encoding);

  /**
   * Returns the character encoding in use.
   * 
   * @return <code>String</code> with the encoding.
   * @see RFC2279 for some well-known encodings.
   */
  public String getEncoding();

  /**
   * Get information on the current origin of records for this connector. For example, a file reader might use this to
   * hold the name of the file currently being processed; for a socket, it might be the host:port. This information may
   * be used downstream to allow, for example, convertors to reset their header processing in preparation for a new
   * file.
   * 
   * @return Object containing information about current Record Source. May be null if not in use.
   * 
   */
  public Object getReaderContext();

  /**
   * Connects the underlying data stream and makes this reader available for use.
   * 
   * @throws ComponentException
   *           if a problem occurs during connection
   */
  public void connect() throws ComponentException;

  /**
   * Disconnect the underlying data stream.
   * 
   * @throws ComponentException
   *           if a problem occurs on disconnect
   */
  public void disconnect() throws ComponentException;
}
