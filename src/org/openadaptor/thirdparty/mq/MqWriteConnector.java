/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.thirdparty.mq;
/*
 * File: $Header$ 
 * Rev: $Revision$
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.connector.AbstractWriteConnector;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.transaction.ITransactional;

/**
 * WriteConnector that use MqConnection to write messages to an IBM MQ Queue.
 */
public class MqWriteConnector extends AbstractWriteConnector implements ITransactional {

  private static final Log log = LogFactory.getLog(MqWriteConnector.class);

  /**
   * Connection used by the Connector to interact with MQ.
   */
  private MqConnection connection;

  public MqWriteConnector() {
  }

  public MqWriteConnector(String id) {
    super(id);
  }

  // Bean Properties

  /**
   * Returns MQ Connection
   * 
   * @return MqConnection
   */
  public MqConnection getConnection() {
    return connection;
  }

  /**
   * Set MqConnection
   */
  public void setConnection(MqConnection connection) {
    this.connection = connection;
  }

  // End Bean Properties

  /**
   * Deliver a record.
   * 
   * @param records -
   *        an Array of records to be processed.
   * @return result information if any. May well be null.
   */
  public Object deliver(Object[] records)  {
    // WARNING This connector can only deal with Strings. Anything else will cause a problem.
    for (int i = 0; i < records.length; i++) {
      getConnection().deliverMessage((String) records[i]);
    }
    return null;
  }

  /**
   * Establish a connection to external message transport without starting the
   * externalconnector. If already connected then do nothing.
   * 
   * @throws org.openadaptor.core.exception.ConnectionException
   */
  public void connect() {
    if (!connected) {
      if (getConnection() == null) throw new ConnectionException("No MqConnection configured", this);
      getConnection().setId(getId()+"_Connection");
      getConnection().connectToMQ(false);
      connected = true;
      log.debug("MqWriteConnector successfully connected");
    }
  }

  /**
   * Disconnect from the external message transport. If already disconnected
   * then do nothing.
   */
  public void disconnect() {
    if (connected) {
      try {
        getConnection().close();
      }
      finally {
        connected = false;
        log.debug("MqWriteConnector disconnected");
      }
    }
  }

  /**
   * All connectors must be able to return a Transaction Resource.
   * 
   * @return Object The transactional Resource or null.
   */
  public Object getResource() {
    return getConnection().getResource();
  }
}
