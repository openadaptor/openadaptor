/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openadaptor.auxil.connector.iostream.EncodingAwareObject;
import org.openadaptor.auxil.connector.iostream.reader.IDataReader;

public class RegexReader extends EncodingAwareObject implements IDataReader {

  private InputStreamReader reader;

  private Matcher matcher;

  public void setRegex(String regex) {
    matcher = Pattern.compile(regex, Pattern.DOTALL | Pattern.MULTILINE).matcher("");
    if (matcher.groupCount() > 1) {
      throw new RuntimeException("regex is invalid, can only have 0 or 1 groups");
    }
  }

  public Object read() throws IOException {
    StringBuffer buffer = null;
    int c;
    while ((c = reader.read()) != -1) {
      buffer = buffer == null ? new StringBuffer() : buffer;
      buffer.append((char) c);
      if (matcher.reset(buffer.toString()).matches()) {
        return matcher.group(matcher.groupCount());
      }
    }
    return null;
  }

  public void setInputStream(final InputStream inputStream) {
    reader = createInputStreamReader(inputStream);
  }

  public void close() throws IOException {
    reader.close();
  }
}
