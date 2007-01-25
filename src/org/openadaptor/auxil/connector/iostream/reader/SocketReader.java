/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved. 
                                                                                     
 Permission is hereby granted, free of charge, to any person obtaining a             
 copy of this software and associated documentation files (the                       
"Software"), to deal in the Software without restriction, including                
 without limitation the rights to use, copy, modify, merge, publish,                 
 distribute, sublicense, and/or sell copies of the Software, and to                  
 permit persons to whom the Software is furnished to do so, subject to               
 the following conditions:                                                           
                                                                                     
 The above copyright notice and this permission notice shall be included             
 in all copies or substantial portions of the Software.                              
                                                                                     
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS           
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                          
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                               
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE              
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION              
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION               
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                     
                                                                                     
 Nothing in this notice shall be deemed to grant any rights to                       
 trademarks, copyrights, patents, trade secrets or any other intellectual            
 property of the licensor or any contributor except as expressly stated              
 herein. No patent license is granted separate from the Software, for                
 code that you delete from the Software, or for combinations of the                  
 Software with other software or hardware.                                           
*/

package org.openadaptor.auxil.connector.iostream.reader;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.exception.ComponentException;

/**
 * Simple StreamReader to listen on a socket, and provide a StreamReader based on the first connection that receives.
 * <p>
 * If the streamReader is read before any incoming connection is received, then it blocks until something connects.
 * 
 * Note: In it's current form it is very primitive.
 * 
 * @author OA3 Eddy Higgins
 */
// ToDo: Add cleanup code - currently does NO cleanup of resources whatsoever.
public class SocketReader extends AbstractStreamReader {
  static Log log = LogFactory.getLog(SocketReader.class);

  private int port;

  private ServerSocket serverSocket;

  // BEGIN Bean getters/setters

  public void setPort(int port) {
    this.port = port;
  }

  public int getPort() {
    return port;
  }

  // END Bean getters/setters

  /**
   * Begin Listening on a ServerSocket.
   * <p>
   * Simple singleThreaded first pass. connect() method name is slightly misleading as it's really the adaptor which is
   * connecting to it.
   * 
   * @throws org.openadaptor.control.ComponentException
   *           if an IOException occurs
   */
  public void connect() throws ComponentException {
    log.debug("Connect: Listening for connections on port " + port);
    try {
      serverSocket = new ServerSocket(port);
      DeferredInputStream dis = new DeferredInputStream(serverSocket);
      dis.setStreamReader(this);
      _inputStream = dis;
      super.connect();
      ;
    } catch (IOException ioe) {
      log.error("IOException - " + ioe.toString());
      throw new ComponentException(ioe.toString(), ioe, this);
    }
  }

  /**
   * Disconnect this StreamReader.
   * 
   * @throws org.openadaptor.control.ComponentException
   *           if an IOException occurs.
   */
  public void disconnect() {
    log.debug("Disconnect: Shutting down server socket on port " + port);
    super.disconnect();
    if (serverSocket != null) {
      try {
        serverSocket.close();
      } catch (IOException ioe) {
        log.warn("Failed to close socket - " + ioe.toString());
      }
    }
  }
}

/**
 * Sneaky little convenience class to let us initialise before we really have an <code>InputStream</code>.
 * <p>
 * Connections is only obtained on first call to read the fake <code>InputStream</code>. Note: It's really basic -
 * only allows a single connection.
 */
class DeferredInputStream extends InputStream {
  private InputStream realInputStream;

  private ServerSocket serverSocket;

  // Need this only if we want to tell it the current origin of records...
  private AbstractStreamReader streamReader;

  public DeferredInputStream(ServerSocket serverSocket) {
    this.serverSocket = serverSocket;
  }

  public void setStreamReader(AbstractStreamReader streamReader) {
    this.streamReader = streamReader;
  }

  public int read() throws IOException {
    if (realInputStream == null) {
      realInputStream = waitForRealConnection();
    }
    return realInputStream.read();
  }

  private InputStream waitForRealConnection() {
    InputStream is = null;
    try {
      SocketReader.log.info("Listening for connections on port " + serverSocket.getLocalPort());
      Socket socket = serverSocket.accept();
      String clientInfo = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
      SocketReader.log.warn("Got connection from " + clientInfo);
      /**
       * If we have a handle on an AbstractStreamReader, then tell it about the connection.
       */
      if (streamReader != null) {
        streamReader.setReaderContext(clientInfo);
      }
      is = socket.getInputStream();
    } catch (IOException ioe) {
      SocketReader.log.warn("Exception accepting socket Connection: " + ioe.toString());
    }
    return is;
  }
}
