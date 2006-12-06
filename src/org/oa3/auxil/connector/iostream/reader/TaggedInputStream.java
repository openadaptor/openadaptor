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
 * File: $Header: /cvs/oa3/src/org/oa3/connector/stream/reader/TaggedInputStream.java,v 1.2 2006/10/19 14:24:26 higginse
 * Exp $ Rev: $Revision: 1.2 $ Created Apr 12, 2006 by Eddy Higgins
 */
import java.io.IOException;
import java.io.InputStream;

/**
 * This is a utility class, which amounts to a wrapper around an <code>InputStream</code> but which optionally retains
 * information about it's origin.
 * <p>
 * It can also notify a StreamReader when it is first used.
 * 
 * @author Eddy Higgins
 * 
 */
public class TaggedInputStream extends InputStream {
  
  //private static final Log log = LogFactory.getLog(TaggedInputStream.class);

  private InputStream inputStream;

  private Object sourceInfo = null;

  /**
   * Provide information about the origins of this <code>InputStream</code>.
   * <p>
   * The source information could be, for example, a String contining a filename or url.
   * 
   * @param sourceInfo
   *          Object with sourceInformation
   */
  public void setSourceInfo(Object sourceInfo) {
    this.sourceInfo = sourceInfo;
  }

  /**
   * Returns the sourceInformation, if any, for this <code>InputStream</code>.
   * 
   * @return Object containing sourceInfo. May be <tt>null</tt>
   */
  public Object getSourceInfo() {
    return sourceInfo;
  }

  // END Bean getters/setters
  // BEGIN Constructors

  /**
   * Create an <code>TaggedInputStream</code> with accociated source information, and associated
   * <code>IStreamReader</code>.
   * <p>
   * If the <code>IStreamReader</code> is assigned, then it will be notified via <tt>setCurrentRecordOriginInfo()</tt>
   * the first time a read occurs on this <code>TaggedInputStream</code> Note: As soon as the reader has been
   * notified, it is forgotten, and will never be notified again by this instance.
   * 
   * @param inputStream
   *          The underlying InputStream.
   * @param sourceInfo
   *          Optional information about the source of the <code>InputStream</code>
   * @param reader
   *          optional InputStreamReader to associate with.
   */
  public TaggedInputStream(InputStream inputStream, Object sourceInfo, IStreamReader reader) {
    this.inputStream = inputStream;
    if (sourceInfo != null) {
      setSourceInfo(sourceInfo);
    }
    if (reader != null) {
    }
  }

  /**
   * Create an <code>TaggedInputStream</code> with accociated source information.
   * <p>
   * This is equivalent to <code>new TaggedInputStream(inputStream,sourceInfo,null)</code>
   * 
   * @param inputStream
   *          The underlying InputStream.
   * @param sourceInfo
   *          Optional information about the source of the <code>InputStream</code>
   */
  public TaggedInputStream(InputStream inputStream, Object sourceInfo) {
    this(inputStream, sourceInfo, null);
  }

  /**
   * Create an <code>TaggedInputStream</code> from an <code>InputStream</code>.
   * <p>
   * This is equivalent to <code>new TaggedInputStream(inputStream,null,null)</code>
   * 
   * @param inputStream
   *          The underlying InputStream.
   */
  public TaggedInputStream(InputStream inputStream) {
    this(inputStream, null, null);
  }

  // END Constructors
  // BEGIN Implementation of InputStream methods

  public int available() throws IOException {
    return inputStream.available();
  }

  public void close() throws IOException {
    inputStream.close();
  }

  public void mark(int readlimit) {
    inputStream.mark(readlimit);
  }

  public boolean markSupported() {
    return inputStream.markSupported();
  }

  public int read() throws IOException {
    return inputStream.read();
  }

  public int read(byte[] b) throws IOException {
    return inputStream.read(b);
  }

  public int read(byte[] b, int off, int len) throws IOException {
    return inputStream.read(b, off, len);
  }

  public void reset() throws IOException {
    inputStream.reset();
  }

  public long skip(long n) throws IOException {
    return inputStream.skip(n);
  }
}
