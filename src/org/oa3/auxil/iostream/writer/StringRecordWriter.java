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
package org.oa3.auxil.iostream.writer;

/*
 * File: $Header: /cvs/oa3/src/org/oa3/connector/stream/writer/StringRecordWriter.java,v 1.3 2006/08/31 10:01:37 shirea
 * Exp $ Rev: $Revision: 1.3 $ Created Feb 24, 2005 by Eddy Higgins
 */
import org.apache.log4j.Logger;

import java.io.*;

/**
 * Very simplistic String Record Writer. Basically treats cr and lf as record delimiters.
 * 
 * @author OA3 Core Team
 */
public class StringRecordWriter implements IRecordWriter {

  static Logger log = Logger.getLogger(StringRecordWriter.class.getName());

  static final String DEFAULT_DELIMITER = System.getProperty("line.separator", "\n");

  // Bean properties:
  private Writer writer;

  private String recordDelimiter = DEFAULT_DELIMITER;

  // BEGIN Bean getters/setters
  public void setWriter(Writer writer) {
    this.writer = writer;
  }

  public Writer getWriter() {
    return writer;
  }

  /**
   * Optional: specify the record delimiter string output after each record (defaults to runtime
   * <code>line.separator</code> system property value).
   * 
   * If explicitly set then the exact string provided will be used.
   * 
   * To get ASCII CR and LF you will need to use entites such as: <verbatim>&#x0D;&#x0A;</verbatim>.
   * 
   * Note the XML character code entity for form feed (x0c) is <i>illegal</i> in XML 1.0, so you would need to extend
   * this class if you had a requirement to use it.
   * 
   * @param delimiter
   */
  public void setRecordDelimiter(String delimiter) {
    // Store actual value (so getter/setter will play nicely with bean editors):
    this.recordDelimiter = delimiter;
  }

  public String getRecordDelimiter() {
    return recordDelimiter;
  }

  // END Bean getters/setters

  public void write(Object record) throws IOException {
    if (writer != null) {
      if (record != null) {
        writer.write(record.toString());
        writer.write(recordDelimiter);
      } else {
        log.warn("Ignoring null record");
      }
    } else {
      throw new IOException("Writer not initialised");
    }
  }

  public void flush() throws IOException {
    if (writer != null) {
      writer.flush();
    }
  }
}
