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
package org.openadaptor.auxil.connector.jms;

import org.openadaptor.core.Component;
import org.openadaptor.core.IWriteConnector;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.core.transaction.ITransactional;

/**
 * Write Connector class that implements publiching to JMS.
 * 
 * Uses an instance of <code>JMSConnector</code> to perform the actual interaction with JMS.
 * 
 * @author OA3 Core Team
 */
public class JMSWriteConnector extends Component implements IWriteConnector, ITransactional {

  //private static final Log log = LogFactory.getLog(JMSPublisher.class);

  /** JMS Connetion component that does all the real work. */
  private JMSConnection jmsConnection;

  public JMSWriteConnector() {
  }

  public JMSWriteConnector(String id) {
    super(id);
  }
  
  // Connector Interfaces

  /**
   * Deliver a batch of records to the external message transport.
   * 
   * @param records -
   *          an Array of records to be processed.
   * @return result information if any. May well be null.
   * @throws org.openadaptor.control.ComponentException
   */
  public Object deliver(Object[] records) throws ComponentException {
    if (!isConnected())
      throw new ComponentException("Attempting to deliver via a disconnected Connector", this);
    if (isConnected()) {
      int size = records.length;
      // ToDo: Get OA team to review the way the unbatching is done.
      String[] msgIds = new String[size];

      for (int i = 0; i < size; i++) { // Un-batch the individual records.
        Object record = records[i];
        // log.info(getLoggingId() + " sending [" + record + "]");
        if (record != null) // todo Really necessary?
          getJmsConnection().deliver(record);
        else
          throw new ComponentException("Cannot deliver null record.", this);
      }
      return msgIds;
    }
    return null;
  }

  /**
   * Establish a connection to external message transport without starting the externalconnector. If already connected
   * then do nothing.
   */
  public void connect() {
    if (!isConnected()) {
      jmsConnection.connect();
      jmsConnection.getSession();
      jmsConnection.getProducer();
    }
  }

  /**
   * Disconnect from the external message transport. If already disconnected then do nothing.
   */
  public void disconnect() {
    if (isConnected()) {
      getJmsConnection().disconnect();
    }
  }

  /**
   * True if connected.
   * 
   * @return whether connected or not.
   */
  public boolean isConnected() {
    return getJmsConnection().isConnected();
  }

  // End Connector Interfaces

  // Bean Properties

  /**
   * Set underlying <code>JMSConnection</code> component.
   * 
   * @param connection
   *          The JMSConenction implementation.
   */
  public void setJmsConnection(JMSConnection connection) {
    this.jmsConnection = connection;
  }

  /**
   * Underlying <code>JMSConnection</code> component.
   * 
   * @return The JMS Connection.
   */
  public JMSConnection getJmsConnection() {
    return jmsConnection;
  }

  public Object getResource() {
    return jmsConnection.getTransactionalResource();
  }

}
