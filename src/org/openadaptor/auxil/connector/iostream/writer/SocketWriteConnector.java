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
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.iostream.writer.string.LineWriter;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.ValidationException;

/**
 * Basic implementation of a TCP/IP socket. Will connect to a remote server on
 * a specified port and write the incoming data to it. Currently, the adaptor
 * will stop after the data has been transferred.
 *
 * @author Fred Perry, Russ Fennell
 */
public class SocketWriteConnector extends AbstractStreamWriteConnector {

  private static final Log log = LogFactory.getLog(SocketWriteConnector.class);

  private String hostname;

  private int port = -1;

  private Socket socket;

  /**
   * Mandatory
   *
   * @param hostname name or ip address of the remote server to connect to
   */
  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  /**
   * Mandatory
   *
   * @param port the TCP/IP port number that the remote server is listening on
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
    if (hostname == null) {
      exceptions.add(new ValidationException("hostname property not set", this));
    }
    if (port == -1) {
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
   * Connects to the remote server and returns an OutputStream that can be used to
   * communicate with it via the specified port.
   *
   * @return a comms stream
   *
   * @throws IOException if there was a comms error
   */
  protected OutputStream getOutputStream() throws IOException {
    log.info(getId() + " connecting to " + hostname + ":" + port);
    try {
      socket = new Socket(hostname, port);
      return socket.getOutputStream();
    } catch (UnknownHostException e) {
      throw new ConnectionException("UnknownHostExceptiont, " + hostname + ", " + e.getMessage(), e, this);
    }
  }

  /**
   * Closes the connection to the remote server.
   */
  public void disconnect() {
    log.debug(getId() + " disconnecting from host:port " + hostname + ":" + port);
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
