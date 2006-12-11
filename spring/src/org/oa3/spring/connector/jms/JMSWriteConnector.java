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
package org.oa3.spring.connector.jms;

import org.oa3.core.Component;
import org.oa3.core.IWriteConnector;
import org.oa3.core.exception.ComponentException;
import org.springframework.jms.core.JmsTemplate102;

/* 
 * File: $Header: /cvs/oa3/src/org/oa3/connector/jms/JMSSpringPublisher.java,v 1.4 2006/10/18 13:50:30 kscully Exp $
 * Rev:  $Revision: 1.4 $
 * Created Jan 25, 2006 by Kevin Scully
 */
/**
 * Example of a Publisher that encapsulates Spring's JMSTemplate to publish to JMS Destinations. So far just uses
 * JMS 1.0.2 compatible templates.
 */
public class JMSWriteConnector extends Component implements IWriteConnector {

  private JmsTemplate102 template;

  /**
   * Establish a connection to external message transport without starting the external connector. If already
   * connected then do nothing.
   */
  public void connect() {
    // Pattern as suggested by Sugath
    // TODO promote to abstract superclass
    if (!isConnected()) {
      doConnect();
    }
  }

  /** Do the actual work of connecting. All it is doing for now is verifying the existence of a Template. */
  protected void doConnect() {
    // Al I am doing for now is verifying the existence of a Template.
    if (getTemplate() == null) {
      throw new ComponentException("No Spring JMS Template set.", this);
    }
  }

  /** Disconnect from the external message transport. If already disconnected then do nothing. */
  public void disconnect() {
    // Pattern as suggested by Sugath
    // TODO promote to abstract superclass
    if (isConnected()) {
      doDisconnect();
    }
  }

  /** Do the actual work of disconnecting. */
  protected void doDisconnect() {
    // Nothing useful I can think of to do here
  }

  /**
   * True if connected.
   *
   * @return Whether or not connected.
   */
  public boolean isConnected() {
    return (getTemplate() != null); // Until I know better. If we have one we are connected.
  }

  // Bean stuff

  /**
   * get the underlying Spring JMSTemplate wrapped by this Listener.
   *
   * @return The underlying Spring JMSTemplate.
   */
  public JmsTemplate102 getTemplate() {
    return template;
  }

  /**
   * Set the underlying Spring JMSTemplate wrapped by this Listener.
   *
   * @param template The underlying Spring JMSTemplate.
   */
  public void setTemplate(JmsTemplate102 template) {
    this.template = template;
  }

  /**
   * Deliver a record.
   *
   * @param records - an Array of records to be processed.
   * @return result information if any. May well be null.
   * @throws org.oa3.control.ComponentException
   */
  public Object deliver(Object[] records) throws ComponentException {
    for (int i = 0; i < records.length; i++) {
      deliverRecord(records[i]);
    }
    return null;
  }

  protected Object deliverRecord(Object record) {
    getTemplate().convertAndSend(record);
    return null;
  }
}
