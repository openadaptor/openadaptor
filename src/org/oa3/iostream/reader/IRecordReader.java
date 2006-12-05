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
 * File: $Header: /cvs/oa3/src/org/oa3/connector/stream/reader/IRecordReader.java,v 1.4 2006/10/18 17:09:05 higginse Exp $
 * Rev: $Revision Created Jan 27, 2006 by Eddy Higgins
 */
import java.io.IOException;
import java.io.Reader;

/**
 * Interface for classes which extract individual records from a supplied <code>Reader</code>.
 * <p>
 * Implementations should be able to take a <code>Reader</code>, and supply record objects on repeated calls to
 * next(). When all records have been read, next() should then return <tt>null</tt>
 * 
 * @author Eddy Higgins
 */
public interface IRecordReader {
  /**
   * Assign a <code>Reader</code> for use by this reader.
   * <p>
   * Currently setReader() also trigger a call to initialise() (hence an IOException can be thrown).
   * 
   * @param reader
   *          <code>Reader</code> which is to be split into records.
   * @throws IOException
   *           if there is any problem.
   */
  public void setReader(Reader reader) throws IOException; // ToDo: decouple initialise() call from here.

  /**
   * Return the underlying <code>Reader</code> that this record reader uses.
   * 
   * @return <code>Reader</code> which is used by the record reader
   */
  public Reader getReader();

  /**
   * Initialise this recordReader.
   * 
   * @throws IOException
   *           if the <code>Reader</code> is <tt>null</tt>
   */
  public void initialise() throws IOException;

  /**
   * Fetch the next record from this reader.
   * <p>
   * If the reader has no more records, then it should return <tt>null</tt>
   * 
   * @return Object containing a record, or <tt>null</tt> if reader is exhausted.
   * @throws IOException
   *           if a problem occurs when reading a record.
   */
  public Object next() throws IOException;

  public boolean isDry();
}
