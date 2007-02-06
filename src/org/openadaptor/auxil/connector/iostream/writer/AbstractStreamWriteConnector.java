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

package org.openadaptor.auxil.connector.iostream.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.openadaptor.core.connector.AbstractWriteConnector;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.ValidationException;

/**
 * @author Fred Perry
 */
public abstract class AbstractStreamWriteConnector extends AbstractWriteConnector {

  private IDataWriter dataWriter;

  private OutputStream outputStream;

  public AbstractStreamWriteConnector() {
    super();
  }

  public AbstractStreamWriteConnector(String id) {
    super(id);
  }

  public void setDataWriter(IDataWriter dataWriter) {
    this.dataWriter = dataWriter;
  }

  public void validate(List exceptions) {
    super.validate(exceptions);
    if (dataWriter == null) {
      exceptions.add(new ValidationException("dataWriter property not set", this));
    }
  }
  
  /**
   * Note: you must set the <em>dataWriter</em> field prior to calling connect().
   *
   * @throws ComponentException if the dataWriter field has not been set
   * @throws ConnectionException if there was a problem retrieving the comms
   * stream to the remote server
   */
  public void connect() {
    try {
      outputStream = getOutputStream();
      dataWriter.setOutputStream(outputStream);
    } catch (IOException e) {
      throw new ConnectionException("IOException, " + e.getMessage(), e, this);
    }
  }

  public Object deliver(Object[] data) {
    try {
      for (int i = 0; i < data.length; i++) {
        dataWriter.write(data[i]);
      }
      dataWriter.flush();
      return null;
    } catch (IOException e) {
      throw new ConnectionException("IOException, " + e.getMessage(), e, this);
    }
  }

  public void disconnect() {
    if (outputStream != null && outputStream != System.out) {
      try {
        outputStream.close();
      } catch (IOException e) {
      } finally {
        outputStream = null;
      }
    }
  }

  protected abstract OutputStream getOutputStream() throws IOException;
}
