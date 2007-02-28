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

package org.openadaptor.auxil.connector.socket;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.iostream.reader.AbstractStreamReadConnector;
import org.openadaptor.auxil.connector.iostream.reader.IDataReader;
import org.openadaptor.auxil.connector.iostream.reader.string.LineReader;
import org.openadaptor.core.connector.QueuingReadConnector;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.ValidationException;

/**
 * A Read Connector that reads data from a socket. Depending on how the properties are set
 * this connector will either connect to a remote socket or accept socket connection from
 * a remote client, use {@link #setRemoteHostname(String)} to control this behaviour. The
 * {@link #setDataReader(IDataReader)} method allows you to configure how data is read from
 * the socket input stream, this defaults to a @link {@link LineReader}.
 * @author perryj
 *
 */
public class SocketReadConnector extends QueuingReadConnector {

  private static final Log log = LogFactory.getLog(SocketReadConnector.class);
  
  private int port;
  private String remoteHostname;
  private SocketStreamReadConnector streamReadConnector;
  private boolean connected;
  
  public SocketReadConnector() {
    streamReadConnector = new SocketStreamReadConnector();
    streamReadConnector.setDataReader(new LineReader());
  }
  
  public SocketReadConnector(String id) {
    super(id);
    streamReadConnector = new SocketStreamReadConnector();
    streamReadConnector.setId(id);
    streamReadConnector.setDataReader(new LineReader());
  }
  
  public void setId(String id) {
    super.setId(id);
    streamReadConnector.setId(id);
  }
  
  public void setPort(final int port) {
    this.port = port;
  }

  public void setRemoteHostname(final String remoteHostname) {
    this.remoteHostname = remoteHostname;
  }

  public void setDataReader(final IDataReader dataReader) {
     streamReadConnector.setDataReader(dataReader);
  }

  public void validate(List exceptions) {
    if (port == 0) {
      exceptions.add(new ValidationException("port property not set", this));
    }
  }

  public void connect() {
    connected = true;

    // if remote hostname is defined then connect to remote server

    if (remoteHostname != null) {
      try {
        Socket socket = new Socket(remoteHostname, port);
        streamReadConnector.setSocket(socket);
        Runnable runnable = new SocketReader(streamReadConnector);
        (new Thread(runnable, getConnectionName(socket))).start();
      } catch (Exception e) {
        throw new ConnectionException("failed to connect, " + e.getMessage(), e, this);
      }
    } 
    
    // otherwise create a new socket server thread and start it
    
    else {
      SocketServerThread t;
      try {
        log.info(getId() + " bound to " + port);
        t = new SocketServerThread(port);
      } catch (IOException e) {
        throw new ConnectionException("failed to connect, " + e.getMessage(), e, this);
      }
      t.start();
    }
  }

  public void disconnect() {
    connected = false;
    streamReadConnector.disconnect();
  }
  
  private static String getConnectionName(Socket s) {
    return s.getInetAddress().getHostAddress() + ":" + s.getPort();
  }

  public class SocketStreamReadConnector extends AbstractStreamReadConnector {

    private Socket socket;
    
    public void setSocket(Socket socket) throws IOException {
      this.socket = socket;
    }

    protected InputStream getInputStream() throws IOException {
      return socket.getInputStream();
    }
  }
  
  class SocketServerThread extends Thread {

    private ServerSocket serverSocket;

    SocketServerThread(int port) throws IOException {
      serverSocket = new ServerSocket(port);
    }
    
    /**
     * accepts a connection, creates a SocketReader and runs it, loops while connected flag is set
     */
    public void run() {
      while (connected) {
        try {
          currentThread().setName(getId() + "." + "accept");
          log.info(getId() + " waiting for connection...");
          Socket s = serverSocket.accept();
          log.info(getId() + " accepted connection from " + getConnectionName(s) + ", changing thread name");
          currentThread().setName(getId() + "." + getConnectionName(s));
          streamReadConnector.setSocket(s);
        } catch (Throwable e) {
          log.error(getId() + e.getMessage());
        }
        try {
          Runnable runnable = new SocketReader(streamReadConnector);
          runnable.run();
        } catch (Throwable t) {
          log.info(t);
        } finally {
          log.info("SocketReader exiting");
        }
      }
      log.info("SocketServerThread exiting");
    }
  }
  
  class SocketReader implements Runnable {

    private SocketStreamReadConnector connector;
    
    public SocketReader(SocketStreamReadConnector connector) {
      this.connector = connector;
    }
    
    public void run() {
      connector.connect();
      while (!connector.isDry()) {
        Object[] data = connector.next(0);
        for (int i = 0; data != null && i < data.length; i++) {
          enqueue(data[i]);
        }
      }
    }
  }
}
