/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.auxil.connector.socket;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.iostream.writer.AbstractStreamWriteConnector;
import org.openadaptor.auxil.connector.iostream.writer.string.LineWriter;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.ValidationException;

public class SocketWriteConnector extends AbstractStreamWriteConnector {

  private static final Log log = LogFactory.getLog(SocketWriteConnector.class);

  private String remoteHostname;

  private int port;

  private Socket socket;

  public SocketWriteConnector(String id) {
    super(id);
    setDataWriter(new LineWriter());
  }

  public void setRemoteHostname(String hostname) {
    this.remoteHostname = hostname;
  }

  /**
   * Mandatory
   * 
   * @param port
   *          the TCP/IP port number to accept connections on or to connect to
   *          on the remote server
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * Checks that the mandatory properties have been set
   *
   * @param exceptions list of exceptions that any validation errors will be appended to
   */
  public void validate(List exceptions) {
    super.validate(exceptions);
    if (port == 0) {
      exceptions.add(new ValidationException("port property not set", this));
    }
  }

  /**
   * Calls the super constructor and sets the dataWriter to be an instance of a
   * LineWriter.
   */
  public SocketWriteConnector() {
    super();
    setDataWriter(new LineWriter());
  }

  /**
   * If the {@link #setRemoteHostname remoteHostname} property is set then connects to remote host
   * and returns the OutputStream from that socket. Otherwise it creates a ServerSocket
   * and waits for a connection from a client, when this happens it returns the OutputStream
   * from that socket;

   * @throws IOException if there was a comms error
   */
  protected OutputStream getOutputStream() throws IOException {
    if (remoteHostname != null) {
      log.info(getId() + " connecting to " + remoteHostname + ":" + port);
      try {
        socket = new Socket(remoteHostname, port);
        configureSocket(socket);
        return socket.getOutputStream();
      } catch (UnknownHostException e) {
        throw new ConnectionException("UnknownHostExceptiont, " + remoteHostname + ", " + e.getMessage(), e, this);
      }
    } else {
      log.info(getId() + " waiting for connection...");
      ServerSocket serverSocket = new ServerSocket(port);
      socket = serverSocket.accept();
      log.info(getId() + " accepted connection from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
      configureSocket(socket);
      return socket.getOutputStream();
    }
  }

 private void configureSocket(Socket socket2) throws SocketException {
    socket.setTcpNoDelay(true);
    socket.setKeepAlive(true);
  }

 /**
   * Closes the connection to the remote server.
   */
  public void disconnect() {
    log.debug(getId() + " disconnecting");
    super.disconnect();
    if (socket != null) {
      try {
        socket.close();
      } catch (IOException ioe) {
        log.warn(getId() + " failed to close socket - " + ioe.toString());
      }
    }
  }

}
