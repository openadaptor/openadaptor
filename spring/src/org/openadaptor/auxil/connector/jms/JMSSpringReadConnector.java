/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.auxil.connector.jms;

import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import org.openadaptor.core.Component;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.OAException;
import org.openadaptor.core.exception.ValidationException;
import org.springframework.jms.core.JmsTemplate;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Apr 2, 2008, by oa3 Core Team
 */

/**
 * Very simple JMS Reader that uses a configured JMSTemplate. 
 * This component uses the template as is and makes no attempt to manage it. A Destination
 * can be optionally supplied to the template by the reader otherwise we rely on a default 
 * destination being configured.
 *  
 * @author oa3 Core Team
 */
public class JMSSpringReadConnector extends Component implements IReadConnector {
  
  protected JmsTemplate template; 
  protected Destination destination = null;
  protected IMessageConvertor messageConvertor = new DefaultMessageConvertor();

  /**
   * Does a connect to test the JMS resources exist. This is probably more than we 
   * really need to do.
   * @see org.openadaptor.core.IReadConnector#connect()
   */
  public void connect() {
    try {
      ConnectionFactory connectionFactory = template.getConnectionFactory();
      Connection conn = connectionFactory.createConnection();
      conn.close();
    } catch (JMSException e) {
      throw new ConnectionException("Unable to create Connection from JmsTemplate: ", e);
    }
  }

  /**
   * Doesn't do anything, we leave everything to the Container.
   * @see org.openadaptor.core.IReadConnector#disconnect()
   */
  public void disconnect() {
    // We leave everything to the Container.
  }

  /**
   * Do nothing.
   * 
   * @see org.openadaptor.core.IReadConnector#getReaderContext()
   */
  public Object getReaderContext() {
    return null;
  }

  /**
   * Can never run dry.
   * @see org.openadaptor.core.IReadConnector#isDry()
   */
  public boolean isDry() {
    return false;
  }

  /**
   * Listen for the next message. Delegates to the JMSTemplate.
   * 
   * @see org.openadaptor.core.IReadConnector#next(long)
   */
  public Object[] next(long timeoutMs) {    
    Message message = null;
    if (destination == null) {
      message = template.receive();
    } else {
      message = template.receive(destination);
    }    
    Object[] receivedObject = null;
    if (message != null) {
      try {
        receivedObject = new Object[] { messageConvertor.unpackMessage(message) };
      } catch (JMSException e) {       
        throw new OAException("Unable to translate received Message: ", e, this);
      }

    } else {
      receivedObject = null;
    }
    return receivedObject;
  }

  /**
   * @see org.openadaptor.core.IReadConnector#setReaderContext(java.lang.Object)
   */
  public void setReaderContext(Object context) {
    // Nothing to do here
  }

  /**
   * The only validation we do is to ensure that the Spring JMSTemplate is set.
   * 
   * @see org.openadaptor.core.IReadConnector#validate(java.util.List)
   */
  public void validate(List exceptions) {
    if (template == null) {
      exceptions.add(new ValidationException("No Spring JMSTemplate supplied. You must supply a template.", this));
    }
  }
  
  // Property getters/setters

  /** 
   * Return the JMSTemplate used by the reader. This is a mandatory property.
   * @return JmsTemplate The mandatory Spring JmsTemplate
   */
  public JmsTemplate getTemplate() {
    return template;
  }

  /**
   * Set the JMSTemplate used by the reader. This is a mandatory property.
   * @param template The mandatory Spring JmsTemplate.
   */
  public void setTemplate(JmsTemplate template) {
    this.template = template;
  }

  /**
   * The IMessageConvertor used to extract the message data from JMSMessage instances. 
   * Defaults to an instance of <code>DefaultMessageConvertor</code>.
   * @return IMessageConvertor
   */
  public IMessageConvertor getMessageConvertor() {
    return messageConvertor;
  }

  /**
   * The IMessageConvertor used to extract the message data from JMSMessage instances. 
   * Defaults to an instance of <code>DefaultMessageConvertor</code>.   
   * @param messageConvertor the IMessageConvertor.
   */
  public void setMessageConvertor(IMessageConvertor messageConvertor) {
    this.messageConvertor = messageConvertor;
  }

  /**
   * Optional Destination to listen to for messages.
   * @return Destination
   */
  public Destination getDestination() {
    return destination;
  }

  /**
   * Optional Destination to listen to for messages.
   * @param destination
   */
  public void setDestination(Destination destination) {
    this.destination = destination;
  }

}
