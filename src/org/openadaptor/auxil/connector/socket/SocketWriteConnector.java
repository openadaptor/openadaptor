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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.auxil.connector.iostream.writer.AbstractStreamWriteConnector;
import org.openadaptor.auxil.connector.iostream.writer.string.LineWriter;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.ValidationException;
import org.openadaptor.core.transaction.ITransactionalResource;

/**
 * A Write Connector that writes data to a socket. Depending on how the properties are set
 * this connector will either connect to a remote socket or accept socket connection from
 * a remote client, use {@link #setRemoteHostname(String)} to control this behaviour. 
 * 
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

public class SocketWriteConnector extends AbstractStreamWriteConnector {

  private static final Log log = LogFactory.getLog(SocketWriteConnector.class);

  private String remoteHostname;

  private int port;

  private int retryPeriod = 0;

  private int maxNumRetries = 0;

  private String restrictedHost = null;

  private int handshakeTimeoutMs = 0;
  
  private boolean pseudoTransaction = false;

  private int pseudoTransactionTimeoutMs = 0;
  
  private Socket socket;
  
  private ISocketHandshake socketHandshake = null;
  
  private BufferedReader socketReader = null;
  
  private OutputStream socketWriter = null;
  
  private boolean initiatedConnection = true;

  /**
   * Calls the super constructor and sets the dataWriter to be an instance of a
   * LineWriter.
   */
  public SocketWriteConnector() {
    super();
    setDataWriter(new LineWriter());
  }

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

  public int getPseudoTransactionTimeoutMs() {
	return pseudoTransactionTimeoutMs;
  }
  
  public void setPseudoTransactionTimeoutMs(int pseudoTransactionTimeoutMs) {
	  this.pseudoTransactionTimeoutMs = (pseudoTransactionTimeoutMs > 0 ? pseudoTransactionTimeoutMs : 0);
  }

  public boolean isPseudoTransaction() {
	  return pseudoTransaction;
  }

  public void setPseudoTransaction(boolean pseudoTransaction) {
	  this.pseudoTransaction = pseudoTransaction;
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
    if (retryPeriod < 0) {
    	exceptions.add(new ValidationException("retryPeriod cannot be less than 0", this));
    	this.retryPeriod = 0;
    }
    if (maxNumRetries < 0) {
    	exceptions.add(new ValidationException("maxNumRetries cannot be less than 0", this));
    	this.maxNumRetries = 0;
    }
    if (maxNumRetries > 0 && retryPeriod == 0) { //Am I being a bit anal here, does it really matter?!
    	exceptions.add(new ValidationException("maxNumRetries cannot be more than 0 when retryPeriod is 0", this));
    }
    
    if (socketHandshake == null) { //The property was not set, so use default
    	socketHandshake = new SimpleSocketHandshake();
    }
    socketHandshake.validate();
  }
  
  public void connect() {
	super.connect();
	try {
		socketReader = getInputStream();
	} catch (IOException e) {
		throw new RuntimeException(e.getMessage());
	}
	doHandshake(socket);
  }

  /**
   * If the {@link #setRemoteHostname remoteHostname} property is set then connects to remote host
   * and returns the OutputStream from that socket. Otherwise it creates a ServerSocket
   * and waits for a connection from a client, when this happens it returns the OutputStream
   * from that socket;

   * @throws IOException if there was a comms error
   */
  protected OutputStream getOutputStream() throws IOException {
  /* This effectively acts as the 'real' connect as the connection attempt occurs when the Socket or ServerSocket class gets instantiated
   * 
   */
    if (remoteHostname != null) {
      log.info(getId() + " connecting to " + remoteHostname + ":" + port);
      setInitiatedConnection(true);
      try {
    	if ( retryPeriod > 0 ) {
    		repeatedAttempts();
    	} else {
    		socket = new Socket(remoteHostname, port);
    	}
        configureSocket(socket);
        socketWriter = socket.getOutputStream();
        return socketWriter;
      } catch (UnknownHostException e) {
        throw new ConnectionException("UnknownHostExceptiont, " + remoteHostname + ", " + e.getMessage(), e, this);
      }
    } else {
      log.info(getId() + " waiting for connection...");
      setInitiatedConnection(false);
      ServerSocket serverSocket = new ServerSocket(port);
      socket = serverSocket.accept();
      // check hostname if we only allow connections from specific hosts
      String client_host = socket.getInetAddress().getHostName();
      if ( restrictedHost != null && !restrictedHost.equals(client_host)) {
		throw new ConnectionException("This component is not configured to accept connections from [" + client_host + "]", this);
      }
      log.info(getId() + " accepted connection from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
      configureSocket(socket);
      socketWriter = socket.getOutputStream();
      return socketWriter;
    }
  }
  
  private void repeatedAttempts() {
		// retry mode - we attempt a connection (ignoring errors) until
		// either we succeed , we have tried the max set times or the timeout elapses
		int num_retries = 0;
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
  }
  
  protected LineNumberReader getInputStream() throws IOException {
	  return new LineNumberReader(new InputStreamReader(socket.getInputStream()));
  }

 private void configureSocket(Socket socket2) throws SocketException {
    socket.setTcpNoDelay(true);
    socket.setKeepAlive(true);
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
 
 
  public Object deliver(Object[] data) {
	 Object ret = super.deliver(data);
	 if (isPseudoTransaction()) {
		 getCOMMITResponse(socket);
	 }
	 return ret;
  }
 
  private void getCOMMITResponse(Socket socket) {
	  BufferedReader socketReader;
	  String reply = "";
	  int saveReadTimeOut = 0;
	  try {
		  if (getPseudoTransactionTimeoutMs() > 0) {
			  saveReadTimeOut = socket.getSoTimeout(); //Save current timeout
			  socket.setSoTimeout(getPseudoTransactionTimeoutMs());
		  }
		  socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
  		  while (reply.length() == 0)  {
  	  		  reply = socketReader.readLine();
  	  		 log.debug("reply length was: " + reply.length() + " in " +this.getId());
  		  }
  		  if (getPseudoTransactionTimeoutMs() > 0) {
			  socket.setSoTimeout(saveReadTimeOut); //Set saved timeout
		  }
	  } catch (IOException e) {
		  throw new ComponentException("In " +  this.getClass().getName() + " getCOMMITResponse(socket) " + e.getMessage(), this);
	  }
		
	  if (reply != null && ("COMMIT".equals(reply) || reply.length() == 0)) {
		  log.debug("Received commit response: " + reply + " in " +this.getId());
	  }
	  else {
		  log.debug("Received commit response: " + reply + " in " +this.getId());
		  throw new ComponentException("In " +  this.getClass().getName() + " getCOMMITResponse(socket) we did not get the expected response.", this);
	  }
  }

 /**
   * Closes the connection to the remote server.
   */
  public void disconnect() {
    log.debug(getId() + " disconnecting");
    if (socket != null) {
      try {
  		if (getPseudoTransactionTimeoutMs() > 0) {
  		  socket.setSoTimeout(getPseudoTransactionTimeoutMs());
  		}
    	getSocketHandshake().sayGoodbye(socketWriter, socketReader, isPseudoTransaction());
        socket.close();
      } catch (IOException ioe) {
        log.warn(getId() + " failed to close socket - " + ioe.toString());
      }
    }
    super.disconnect();
  }

  /**
   * This Inner Class implements the transactional resource for this connector as used by
   * Openadaptor's default Transaction Manager. The idea is that we use the default transaction
   * mechanism to support any require COMMIT or ROLLBACK actions
   * 
   * @author Wratislp
   */  
  protected class SocketReaderTransactionResource implements ITransactionalResource {      
	    public void begin() {
	      // Nothing specific to do when a transaction starts.      
	    }
	   public void commit() {
	      log.debug("Commit called on [" + getId() +"]");
	      if (isPseudoTransaction()) {
	    	  log.debug("Sending COMMIT to socket"); 
	      }
	    }
	    public void rollback(Throwable e) {
		      log.debug("Rollback called on [" + getId() +"]");
		      if (isPseudoTransaction()) {
		    	  log.debug("Sending ROLLBACK to socket"); 
		      }
		}
  }
}
