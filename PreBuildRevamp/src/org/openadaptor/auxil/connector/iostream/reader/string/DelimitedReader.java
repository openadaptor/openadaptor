/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.auxil.connector.iostream.reader.string;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

import org.openadaptor.auxil.connector.iostream.EncodingAwareObject;
import org.openadaptor.auxil.connector.iostream.reader.IDataReader;

/**
 * This class reads string records based on a fixed sequence of delimiter
 * characters. It's delimiter / delimiter char codes property must be
 * configured. The includeDelimiter property controls whether the delimiter
 * characters are included in the record.
 * 
 * @author perryj
 * 
 */
public class DelimitedReader extends EncodingAwareObject implements IDataReader {

  private BufferedReader reader;
  private char[] delimiter;
  private boolean includeDelimiter = true;
  
  public void setDelimiter(final String s) {
    delimiter = s.toCharArray();
  }
  
  public void setDelimiterCharCodes(final int[] chars) {
    delimiter = new char[chars.length];
    for (int i = 0; i < chars.length; i++) {
      delimiter[i] = (char)chars[i];
    }
  }
  
  /**
   * Optional
   * 
   * @param include
   *          if true then delimiter characters are returned as part of the
   *          record when {@link #read()} is invoked
   */
  public void setIncludeDelimiter(final boolean include) {
    includeDelimiter = include;
  }
  
  public void close() throws IOException {
    reader.close();
  }

  public Object read() throws IOException {
    StringBuffer buffer = null;
    int i = 0;
    for (int c; (c = reader.read()) != -1; ) {
      buffer = buffer == null ? new StringBuffer() : buffer;
      buffer.append((char)c);
      if (c == delimiter[i]) {
        if (++i == delimiter.length) {
          return includeDelimiter ? buffer.toString() : buffer.substring(0, buffer.length() - i);
        }
      } else {
        i = 0;
      }
    }
    return buffer != null ? buffer.toString() : null;
  }

  public void setInputStream(InputStream inputStream) {
    reader = new BufferedReader(createInputStreamReader(inputStream));
  }

}
