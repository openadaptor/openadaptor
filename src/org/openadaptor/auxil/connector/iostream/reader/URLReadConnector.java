/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.auxil.connector.iostream.reader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.openadaptor.auxil.connector.iostream.reader.string.LineReader;
import org.openadaptor.core.exception.ValidationException;

/**
 * Read Connector which connects to, and reads from a URL. Defaults dataReader
 * to {@link LineReader}.
 * 
 * @author Eddy Higgins
 */
public class URLReadConnector extends AbstractStreamReadConnector {

  protected URL url;

  public URLReadConnector() {
    super();
    setDataReader(new LineReader());
  }
  
  public URLReadConnector(String id) {
    super(id);
    setDataReader(new LineReader());
  }
  
  public void setUrl(String url) throws MalformedURLException {
    this.url = new URL(url);
  }

  protected InputStream getInputStream() throws IOException {
    return url.openStream();
  }
  
  /**
   * returns url property
   */
  public Object getReaderContext() {
    return url.toExternalForm();
  }

  public void validate(List exceptions) {
    super.validate(exceptions);
    if (url == null) {
      exceptions.add(new ValidationException("url property not set", this));
    }
  }
}
