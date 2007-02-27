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

package org.openadaptor.auxil.connector.iostream.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.lifecycle.LifecycleComponent;

/**
 * Base implementation for Read Connector that read from java.io.InputStream.
 * This implements IReadConnector by delegating to a {@link IDataReader} which
 * does the work of reading discrete data from the stream. Subclasses of this
 * base class need to implement {@link #getInputStream}. This class also
 * implements utiliy method that the subclass may need.
 * 
 * @author perryj
 * @see IDataReader
 */
public abstract class AbstractStreamReadConnector extends LifecycleComponent implements IReadConnector {

  private InputStream inputStream;

  private IDataReader dataReader;

  private boolean isDry = false;

  private int batchSize = 1;

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  protected AbstractStreamReadConnector() {
    super();
  }

  protected AbstractStreamReadConnector(String id) {
    super(id);
  }

  public void setDataReader(final IDataReader dataReader) {
    this.dataReader = dataReader;
  }

  public void connect() {
    try {
      inputStream = getInputStream();
    } catch (IOException e) {
      throw new RuntimeException("IOException, " + e.getMessage(), e);
    }
    if (!(isDry = (inputStream == null))) {
      setInputStream(inputStream);
    }
  }

  protected abstract InputStream getInputStream() throws IOException;

  protected void setInputStream(final InputStream inputStream) {
    this.inputStream = inputStream;
    dataReader.setInputStream(inputStream);
    isDry = false;
  }

  protected void closeInputStream() {
    if (inputStream != null && inputStream != System.in) {
      try {
        inputStream.close();
        inputStream = null;
      } catch (IOException e) {
      }
      try {
        dataReader.close();
      } catch (IOException e) {
      }
    }
  }

  public void disconnect() {
    closeInputStream();
  }

  public Object getReaderContext() {
    return null;
  }

  public boolean isDry() {
    return isDry;
  }

  public Object[] next(long timeoutMs) {
    try {
      ArrayList batch = new ArrayList();
      for (int i = 0; i < batchSize; i++) {
        Object data = dataReader.read();
        if (data != null) {
          batch.add(data);
        } else {
          isDry = true;
          break;
        }
      }
      return batch.toArray();
    } catch (IOException e) {
      throw new ConnectionException("IOException, " + e.getMessage(), e, this);
    }
  }

}
