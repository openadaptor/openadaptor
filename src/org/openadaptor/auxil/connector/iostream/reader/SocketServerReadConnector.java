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
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.iostream.reader.string.LineReader;

/**
 * Simple StreamReader to listen on a socket, and provide a StreamReader based on the first connection that receives.
 * <p>
 * If the streamReader is read before any incoming connection is received, then it blocks until something connects.
 * 
 * Note: In it's current form it is very primitive.
 * 
 * @author OA3 Eddy Higgins
 */

public class SocketServerReadConnector extends AbstractStreamReadConnector {

  protected SocketServerReadConnector() {
    super();
    setDataReader(new LineReader());
  }

  protected SocketServerReadConnector(String id) {
    super(id);
    setDataReader(new LineReader());
  }

  private static final Log log = LogFactory.getLog(SocketServerReadConnector.class);

  private int port;

  private ServerSocket serverSocket;
  
  private String clientInfo;

  public void setPort(int port) {
    this.port = port;
  }

  public void disconnect() {
    super.disconnect();
    if (serverSocket != null) {
      try {
        log.info(getId() + " closing socket server on " + port);
        serverSocket.close();
        serverSocket = null;
        clientInfo = null;
      } catch (IOException e) {
        log.warn(getId() + " failed to close socket, " + e.getMessage());
      }
    }
  }

  public Object getReaderContext() {
    return clientInfo;
  }
  
  protected InputStream getInputStream() throws IOException {
    serverSocket = new ServerSocket(port);
    return new DeferredInputStream(serverSocket);
  }

  class DeferredInputStream extends InputStream {
    private InputStream realInputStream;

    private ServerSocket serverSocket;

    public DeferredInputStream(ServerSocket serverSocket) {
      this.serverSocket = serverSocket;
    }

    public int read() throws IOException {
      if (realInputStream == null) {
        realInputStream = waitForRealConnection();
      }
      return realInputStream.read();
    }

    private InputStream waitForRealConnection() {
      try {
        log.info(getId() + " waiting for connection on " + serverSocket.getLocalPort());
        Socket socket = serverSocket.accept();
        clientInfo = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        log.info(getId() + " Got connection from " + clientInfo);
        return socket.getInputStream();
      } catch (IOException e) {
        log.error(getId() + " IOException accepting connection, " + e.getMessage());
      }
      return null;
    }
  }
}
