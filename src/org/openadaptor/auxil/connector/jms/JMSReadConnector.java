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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.core.transaction.ITransactional;

/**
 * Read Connector class that implements listening to JMS. Delegates to <code>JMSConnector</code> 
 * to perform the actual interaction with JMS.
 * 
 * @see JMSConnection
 * @author OA3 Core Team
 */
public class JMSReadConnector extends Component implements IReadConnector, ITransactional {

  private static final Log log = LogFactory.getLog(JMSReadConnector.class);

  private JMSConnection jmsConnection;

  public JMSReadConnector() {
  }
  
  public JMSReadConnector(String id) {
    super(id);
  }
  
  public void setJmsConnection(JMSConnection jmsConnection) {
    this.jmsConnection = jmsConnection;
  }

  public void connect() {
    if (!isConnected()) {
      jmsConnection.connectForReader();
    }
  }

  public void disconnect() {
    if (isConnected()) {
      jmsConnection.disconnect();
    }
  }

  public void validate(List exceptions) {
    if(jmsConnection == null) {
      exceptions.add(new ComponentException("Property jmsConnection is mandatory", this));
    }
    else {
      jmsConnection.validate(exceptions);
    }
  }
  
  public boolean isConnected() {
    return jmsConnection.isConnected();
  }

  public Object[] next(long timeoutMs) throws ComponentException {
    if (!isConnected()) throw new ComponentException("Attempt to read from disconnected JMSReadConnector", this);
    Object data = jmsConnection.receive(timeoutMs);
    if (data != null) {
      log.debug(getId() + " got jms message");
    }
    return data != null ? new Object[] {data} : null;
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
}
