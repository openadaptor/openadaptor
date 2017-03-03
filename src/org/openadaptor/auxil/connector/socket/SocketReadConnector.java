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

package org.openadaptor.auxil.connector.socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.iostream.reader.AbstractStreamReadConnector;
import org.openadaptor.auxil.connector.iostream.reader.IDataReader;
import org.openadaptor.auxil.connector.iostream.reader.string.LineReader;
import org.openadaptor.core.Constants;
import org.openadaptor.core.connector.QueuingReadConnector;
import org.openadaptor.core.connector.SocketQueuingReadConnector;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.ValidationException;
import org.openadaptor.core.transaction.ITransactional;
import org.openadaptor.core.transaction.ITransactionalResource;

/**
 * A Read Connector that reads data from a socket. Depending on how the properties are set
 * this connector will either connect to a remote socket or accept socket connection from
 * a remote client, use {@link #setRemoteHostname(String)} to control this behaviour. The
 * {@link #setDataReader(IDataReader)} method allows you to configure how data is read from
 * the socket input stream, this defaults to a @link {@link LineReader}.
 * @author perryj, extended by Dealbus Development
 * <p>
 * <b>Properties</b>
 * <ul>
 * <li><b>Port</b>				The port for the socket (Mandatory)
 * <li><b>RemoteHostname</b>		The server host name
 * 
 * <li><b>RetryPeriod</b>		If the remote server is not present when the sink tries to
 * 								connect to it then we back off and try again after the retry
 * 								period Defined in seconds 
 * <li><b>MaxRetries</b>		If we are in retry mode then the adaptor will terminate if it
 * 								can't connect after MaxRetries have been attempted
 *								Defaults to 0 which means retry for ever
 * <li><b>HandshakeDelegate</b>	Name of class to which handshaking is delegated to
 *                      		defaults SimpleSocketHandshakeProtocol
 * <li><b>RestrictedHost</b>	Used to limit connection to the socket server from specific clients
 * 
 * <li><b>HandshakeTimeoutMs</b> Time allowed for each handshake greet/reply in milliseconds (once connected), defaults to socket default (indefinite wait)
 * 
 * <li><b>PseudoTransaction</b> If true then the writer will expect a 'COMMIT' back from the socket (reader)
 * 								If it does not get it the writer will throw an exception
 * 
 * <li><b>PseudoTransactionTimeoutMs</b> 
 * 								The amount of time that the writer will wait for a psedoTransaction response from the socket before an exception is thrown
 * 								Default 0 means unlimited  
 * </ul>        
 * </p>            
 * <b>Not yet implemented</b>
 * <ul>
 * <li><b>ClientHostName</b>	Restricts connections to/from this machine only
 * 
 * <li><b>LocalBindAddress</b>	Restricts the server to only accept connect requests TO the
 * 								address defined
 * 								Typically used on multi-honed machines
 * 								If set to 127.0.0.1 then only connection from the localhost will
 * 								be accepted (in this case the ClientHostName property it
 * 								ignored)
 * <li><b>LocalBindPort</b>		Restricts the client to only communicate via the supplied local
 * 								port. Must define LocalBindAddress if this is defined 
 * 								Ignored if InitiateConnect is false (ie it is only used when acting as
 *                      		a client)messages to socket client, defaults to true
 * </ul>
 */
public class SocketReadConnector extends SocketQueuingReadConnector implements ITransactional {

  private static final Log log = LogFactory.getLog(SocketReadConnector.class);
  
  private int port;
  private String remoteHostname;
  private Socket socket = null;
  
  /** RetryPeriod - amount of time (in sec) between connection retries */
  private int retryPeriod = 0;

  /** ConenctTimeoutPeriod - amount of time (in sec) before connection retries give up */
  private int maxNumRetries = 0;
  
  /** 
   * Name of the client that is allowed to connect 
   * Only relevant if the remoteHostName is not set 
   */
  private String restrictedHost = null;

  /** 
   * Name of the client that is allowed to connect 
   * Only relevant if the remoteHostName is not set 
   */
  private int handshakeTimeoutMs = 0;
  
  private boolean initiatedConnection = true;

  
  private SocketStreamReadConnector streamReadConnector;
  private boolean connected;
  
  private ISocketHandshake socketHandshake = null;
  
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

  public void setRetryPeriod(int retryPeriod) {
	this.retryPeriod = retryPeriod;
  }

  public void setMaxNumRetries(int maxNumRetries) {
	this.maxNumRetries = maxNumRetries;
  }

  public void setRestrictedHost(String restrictedHost) {
	this.restrictedHost = restrictedHost;
  }

  public void setSocketHandshake(Object o) {
	if (o instanceof ISocketHandshake)
		this.socketHandshake = (ISocketHandshake) o;
	else
		throw new ComponentException(o.getClass().getName() + " is not an instance of ISocketHandshake", this);
  }

  private ISocketHandshake getSocketHandshake() {
	return socketHandshake;
  }
  

  public boolean isInitiatedConnection() {
	return initiatedConnection;
  }

  public void setInitiatedConnection(boolean initiatedConnection) {
	this.initiatedConnection = initiatedConnection;
  }

  public int gethandshakeTimeoutMs() {
	return handshakeTimeoutMs;
  }

  public void sethandshakeTimeoutMs(int handshakeTimeoutMs) {
  	if (handshakeTimeoutMs > 0) {
		this.handshakeTimeoutMs = handshakeTimeoutMs;
	}
	else {
		this.handshakeTimeoutMs = 0;
	}
  }

  public void setDataReader(final IDataReader dataReader) {
     streamReadConnector.setDataReader(dataReader);
  }

  public void validate(List exceptions) {
    if (port == 0) {
      exceptions.add(new ValidationException("port property not set", this));
    }
    
    if (socketHandshake == null) { //The property was not set, so use default
    	socketHandshake = new SimpleSocketHandshake();
    }
    socketHandshake.validate();
  }

  public void connect() {
    connected = true;

    // if remote hostname is defined then connect to remote server

    if (remoteHostname != null) {
      try {
    	//Socket socket = null;
      	if ( retryPeriod > 0 ) {
    		socket = repeatedAttempts();
    	} else {
    		socket = new Socket(remoteHostname, port);
    	}
        setInitiatedConnection(true);
        doHandshake(socket);
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
        t = new SocketServerThread(port, socket);
      } catch (IOException e) {
        throw new ConnectionException("failed to connect, " + e.getMessage(), e, this);
      }
      t.start();
    }
  }
  
  private Socket repeatedAttempts() {
	// retry mode - we attempt a connection (ignoring errors) until
	// either we succeed , we have tried the max set times or the timeout elapses
	int num_retries = 0;
	Socket socket;
	while (true) {
		try {
		  num_retries++;
		  log.info(getId() + " attempting to connect, attempt " + num_retries);
		  socket = new Socket(remoteHostname, port);
        break;
		}
		catch(IOException ioe) {
			log.info(getId() + " " + ioe.getMessage());

			// no connection so sleep and retry
			log.info(getId() + " No connection: will retry in " + retryPeriod + " sec(s)");
			try { Thread.sleep(retryPeriod*1000); } catch(InterruptedException e) {}

			// have we passed the number of retries? 0 means unlimited.
			if ( maxNumRetries > 0 && num_retries > maxNumRetries )
				throw new ConnectionException("Connection timeout elapsed: no connection after " + maxNumRetries + " retries", this);
		}
	}
	return socket;
 }
  
  private void doHandshake(Socket socket) {
	/**
	 *
	 * Do handshaking 
	 * The handshaking passes the writer and reader to the delegated handshaker, 
	 * This allows the handshaker to control what it sends and gets back as part of the handshake
	 * Default handshaker does nothing if unless the handshakeSay / handshakeProgress attributes are set
	*/
	OutputStream socketWriter;
	BufferedReader socketReader;
	try {
		socketWriter = socket.getOutputStream();
		socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	} catch (IOException e) {
		throw new ConnectionException("In " +  this.getClass().getName() + " doHandShake(socket) " + e.getMessage(), this);
	}
	
	
	try {
		int saveReadTimeOut = 0;
		if (gethandshakeTimeoutMs() > 0) { 
			saveReadTimeOut = socket.getSoTimeout(); //Save current timeout
			socket.setSoTimeout(gethandshakeTimeoutMs()); //Set specified timeout
		}
		if (isInitiatedConnection()) {
			getSocketHandshake().offerHandshake(socketWriter, socketReader);
		} 
		else {
			getSocketHandshake().acceptHandshake(socketWriter, socketReader);
		}
		if (gethandshakeTimeoutMs() > 0) {
			socket.setSoTimeout(saveReadTimeOut); //Set saved timeout
		}
	} catch (IOException e) {
		throw new ConnectionException(e.getMessage(), this);
	} catch (RuntimeException e) {
		throw new ConnectionException(e.getMessage(), this);
	}
  }

  public void disconnect() {
    connected = false;
    streamReadConnector.disconnect();
  }
  
  private static String getConnectionName(Socket s) {
    return s.getInetAddress().getHostAddress() + ":" + s.getPort();
  }
  
  protected void sendCommitRollback(String type) {
	  OutputStream socketWriter;
	  int saveReadTimeOut = 0;
	  log.debug("About to send pseudo commit message of: " + type + " from " + this.getClass().getName());
	  type += Constants.NEW_LINE;
	  try {
		  if (getPseudoTransactionTimeoutMs() > 0) {
			  saveReadTimeOut = socket.getSoTimeout(); //Save current timeout
			  socket.setSoTimeout(getPseudoTransactionTimeoutMs());
		  }
		  socketWriter = socket.getOutputStream();
		  for (int i = 0; i < type.length(); i++) {
			  socketWriter.write(type.charAt(i));
	      }
		  socketWriter.flush();
  		  if (getPseudoTransactionTimeoutMs() > 0) {
			  socket.setSoTimeout(saveReadTimeOut); //Set saved timeout
		  }
	  } catch (IOException e) {
		  throw new ComponentException("In " +  this.getClass().getName() + " getCOMMITResponse(socket) " + e.getMessage(), this);
	  }	  
  }

  
  /**
   * implementation of {@link ITransactionalResource}, which if the component is
   * transacted will be returned when {@link #getResource()} is called
   */
  
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

    SocketServerThread(int port, Socket socket) throws IOException {
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
          //Socket s = serverSocket.accept();
          socket = serverSocket.accept();
          log.info(getId() + " accepted connection from " + getConnectionName(socket) + ", changing thread name");
          currentThread().setName(getId() + "." + getConnectionName(socket));
          setInitiatedConnection(false);
          doHandshake(socket);
          streamReadConnector.setSocket(socket);
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
 
  /**
   * This Inner Class implements the transactional resource for this connector as used by
   * Openadaptor's default Transaction Manager. The idea is that we use the default transaction
   * mechanism to support any require COMMIT or ROLLBACK actions
   * 
   * @author Wratislp
   */  
}
