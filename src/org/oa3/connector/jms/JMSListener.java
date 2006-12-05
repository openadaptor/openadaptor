/*
 * [[
 * Copyright (C) 2001 - 2006 The Software Conservancy as Trustee. All rights
 * reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Nothing in this notice shall be deemed to grant any rights to
 * trademarks, copyrights, patents, trade secrets or any other intellectual
 * property of the licensor or any contributor except as expressly stated
 * herein. No patent license is granted separate from the Software, for
 * code that you delete from the Software, or for combinations of the
 * Software with other software or hardware.
 * ]]
 */
package org.oa3.connector.jms;

/*
 * File: $Header: /cvs/oa3/src/org/oa3/connector/jms/JMSListener.java,v 1.7 2006/10/18 13:50:30 kscully Exp $ Rev:
 * $Revision: 1.7 $ Created Sep 28, 2005 by Kevin Scully
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.OAException;
import org.oa3.connector.AbstractReadConnector;
import org.oa3.transaction.ITransactional;

/**
 * Read Connector class that implements listening to JMS.
 * 
 * Uses an instance of <code>JMSConnector</code> to perform the actual interaction with JMS.
 * 
 * @author OA3 Core Team
 */
public class JMSListener extends AbstractReadConnector implements ITransactional {

  private static final Log log = LogFactory.getLog(JMSListener.class);

  /** JMS Connetion component that does all the real work. */
  private JMSConnection jmsConnection;

  /** Total number of polls before listener shuts down. Default = 0 which implies unlimited. */
  private int pollLimit = 0;

  /** Current poll count. */
  private int pollsCompleted = 0;

  // BEGIN bean getters/setters

  /**
   * Set default receive timeout.
   * 
   * @param timeout
   */
  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  /**
   * Underlying <code>JMSConnection</code> component.
   * 
   * @return The JMS Connection.
   */
  public JMSConnection getJmsConnection() {
    return jmsConnection;
  }

  /**
   * Set underlying <code>JMSConnection</code> component.
   * 
   * @param jmsConnection
   *          The JMSConenction implementation.
   */
  public void setJmsConnection(JMSConnection jmsConnection) {
    this.jmsConnection = jmsConnection;
  }

  /**
   * Total number of polls before listener shuts down. Default = 0 which implies unlimited.
   * 
   * @return int Maximum number of polls.
   */
  public int getPollLimit() {
    return pollLimit;
  }

  /**
   * Set tootal number of polls before listener shuts down. Default = 0 which implies unlimited.
   * 
   * @param pollLimit
   *          Maximum number of polls.
   */
  public void setPollLimit(int pollLimit) {
    this.pollLimit = pollLimit;
  }

  // END bean getters/setters

  /**
   * Establish a connection to external message transport without starting the externalconnector. If already connected
   * then do nothing.
   */
  public void connect() {
    if (!isConnected()) {
      jmsConnection.connect();
      jmsConnection.getSession();
    }
  }

  /**
   * Disconnect from the external message transport. If already disconnected then do nothing.
   * 
   * @throws org.oa3.control.OAException
   */
  public void disconnect() {
    if (isConnected()) {
      getJmsConnection().disconnect();
    }
  }

  /**
   * True if connected.
   * 
   * @return Whether connected or not.
   */
  public boolean isConnected() {
    return getJmsConnection().isConnected();
  }

  /**
   * Use jms connection to listen for JMS Messages.
   * 
   * @return Received external message.
   */
  private Object receiveMessages(long timeoutMs) {
    return getJmsConnection().receive(timeoutMs);
  }

  /**
   * Return the next array of received records.
   * 
   * @return Object[] Array of record objects.
   * @throws org.oa3.control.OAException
   */
  public Object[] nextRecord(long timeoutMs) throws OAException {
    if (!isConnected()) {
      throw new OAException("Calling next on a disconnected Connector");
    }
    Object[] records = null;
    if ((getPollLimit() <= 0) || (pollsCompleted < getPollLimit())) {
      pollsCompleted++;
      Object rawRecord = receiveMessages(timeoutMs);
      if (rawRecord != null)
        records = new Object[] { rawRecord };

    } else {
      log.info("Total of " + pollLimit + " polls completed.");
      disconnect(); // JMSListener always disconnects when it reaches the poll limit.
      return null;
    }

    return records;
  }

  public boolean isDry() {
    return false;
  }

  public Object getResource() {
    return jmsConnection.getTransactionalResource();
  }

  public Object getReaderContext() {
    return null;
  }

  // Property Access Stuff
}
