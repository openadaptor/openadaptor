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

import org.oa3.core.connector.AbstractReadConnector;
import org.oa3.core.exception.ComponentException;
import org.springframework.jms.core.JmsTemplate;

/* 
 * File: $Header: /cvs/oa3/src/org/oa3/connector/jms/JMSSpringListener.java,v 1.6 2006/10/18 13:50:30 kscully Exp $
 * Rev:  $Revision: 1.6 $
 * Created Jan 25, 2006 by Kevin Scully
 */
/**
 * Example of a Listener that encapsulates Springs JMSTemplate to subscribe to JMS Destinations. So far just uses
 * JMS 1.0.2 compatible templates.
 */
public class JMSReadConnector extends AbstractReadConnector {

  private JmsTemplate template;

  /**
   * Receive next Message - needs to be reviewed as part of analysing
   * the Connector-to-InPoint interfaces.
   *
   * @return an array of records to be processed
   */
  public Object[] nextRecord(long timeoutMs) throws ComponentException {
    Object received = getTemplate().receiveAndConvert();
    if (received == null) {
      return null;
    } else {
      return new Object[] { received };
    }
  }

  public Object getReaderContext() {
    return null;
  }

  /**
   * Establish a connection to external message transport without starting the externalconnector.
   * <p>
   * If already connected then do nothing.
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
    // All I am doing for now is verifying the existence of a Template.
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
  public JmsTemplate getTemplate() {
    return template;
  }

  /**
   * Set the underlying Spring JMSTemplate wrapped by this Listener.
   *
   * @param template The underlying Spring JMSTemplate.
   */
  public void setTemplate(JmsTemplate template) {
    this.template = template;
  }

  public boolean isDry() {
    return false;
  }
}
