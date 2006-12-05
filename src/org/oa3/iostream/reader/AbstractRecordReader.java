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
package org.oa3.iostream.reader;

/*
 * File: $Header: /cvs/oa3/src/org/oa3/connector/stream/reader/AbstractRecordReader.java,v 1.4 2006/10/18 17:23:52
 * higginse Exp $ Rev: $Revision: 1.4 $ Created Oct 31, 2005 by Eddy Higgins
 */

import java.io.IOException;
import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract base class for classes which split data read from a <code>Reader</code> into individual records.
 * 
 * @see java.io.Reader
 * @author Eddy Higgins
 */
public abstract class AbstractRecordReader implements IRecordReader {
  private static final Log log = LogFactory.getLog(AbstractRecordReader.class);

  /**
   * This is the reader which will be used.
   */
  private Reader reader;

  private boolean isDry;
  
  // BEGIN Bean getters/setters

  /**
   * Assign a <code>Reader</code> for use by this <code>RecordReader</code>.
   * <p>
   * Note that it will call initialise() if this method results in the reader being changed.
   * 
   * @param reader
   *          Any <code>Reader</code> implementation
   * @throws IOException
   *           if an exception occurs during initialisation().
   */
  public void setReader(Reader reader) throws IOException { // ToDo: Decouple this from initialise().
    if (this.reader != reader) {
      log.info("Setting reader to " + reader);
      this.reader = reader;
      initialise();
    }
  }

  /**
   * Return the <code>Reader</code> associated with this <code>RecordReader</code>
   * 
   * @return The <code>Reader</code> associated with this <code>RecordReader</code>
   */
  public Reader getReader() {
    return reader;
  }

  
  public boolean isDry() {
    return isDry;
  }
  
  protected void setIsDry(boolean dry) {
    isDry = dry;
  }
  
  // END Bean getters/setters

  /**
   * Initialise this <code>RecordReader</code>.
   * <p>
   * Base implementation has no efffect.
   * 
   * @throws IOException
   *           if a problem occurs during initialisation.
   */
  public void initialise() throws IOException {
  }

  /**
   * Return the next available record, if any.
   * 
   * @return Object containing the next record.
   * @throws IOException
   *           if a problem occurs
   */
  public abstract Object next() throws IOException;
}
