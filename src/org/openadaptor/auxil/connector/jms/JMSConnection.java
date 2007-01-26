/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved. 
                                                                                     
 Permission is hereby granted, free of charge, to any person obtaining a             
 copy of this software and associated documentation files (the                       
"Software"), to deal in the Software without restriction, including                
 without limitation the rights to use, copy, modify, merge, publish,                 
 distribute, sublicense, and/or sell copies of the Software, and to                  
 permit persons to whom the Software is furnished to do so, subject to               
 the following conditions:                                                           
                                                                                     
 The above copyright notice and this permission notice shall be included             
 in all copies or substantial portions of the Software.                              
                                                                                     
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS           
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                          
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                               
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE              
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION              
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION               
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                     
                                                                                     
 Nothing in this notice shall be deemed to grant any rights to                       
 trademarks, copyrights, patents, trade secrets or any other intellectual            
 property of the licensor or any contributor except as expressly stated              
 herein. No patent license is granted separate from the Software, for                
 code that you delete from the Software, or for combinations of the                  
 Software with other software or hardware.                                           
*/

package org.openadaptor.auxil.connector.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.jndi.JNDIConnection;
import org.openadaptor.core.Component;
import org.openadaptor.core.exception.ComponentException;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.jms.*;
import javax.jms.IllegalStateException;
import java.io.Serializable;
import java.util.List;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Jan 15, 2007 by oa3 Core Team
 */

/**
 * Utility class providing JMS support to the JMS Listener and Publisher classes. Uses JMS 1.1 compliant code.
 * <p/>
 * Provides following:
 * <p/>
 * Receive from and Publish to both Queues and Topics.
 * Provide access to Underlying Transactional resources.
 * <p/>
 * Main Properties
 * <ul>
 * <li><b>connectionFactoryName</b>     Name used to look up connectionfactory in JNDI
 * <li><b>connectionFactory</b>         Real JMS ConnectionFactory. When this is set <code>connectionFactoryName</code> is ignored.
 * <li><b>destinationName</b>           Name used to look up destination (queue/topic) in JNDI
 * <li><b>username</b>                  Username to authenticate the JMS Connection (optional)
 * <li><b>password</b>                  Password to authenticate the JMS Connection (optional)
 * <li><b>durable</b>                   Defaults to <i>false</i>. If <i>true</i> create a durable topic subscription.
 * <li><b>durableSubscriptionName</b>   Defaults to the <code>destinationName</code>. The name used to create the durable topic subscription.
 * <li><b>acknowledgeMode</b>           Defaults to <code>Session.AUTO_ACKNOWLEDGE</code>
 * <li><b>messageSelector</b>           Not set by default.
 * <li><b>noLocal</b>                   Default is <i>false</i>
 * <li><b>clientID</b>                  Default is not set. Use with caution. ConnectionFactory must be configured to allow the clientID to be set.
 * <li><b>jndiConnection</b>            JndiConnection used for lookups
 * <li><b>transacted</b>                Set true if a transacted session is required. Default is false and ignored if an XAConnection is made.
 * <li><b>deliveryMode<b>               Set the delivery mode for the message messageProducer (used for publishing). Defaults to <code>Message.DEFAULT_DELIVERY_MODE</code>.
 * <li><b>priority<b>                   Set the priority for the message messageProducer (used for publishing). Defaults to <code>Message.DEFAULT_PRIORITY</code>.
 * <li><b>timeToLive<b>                 Set the time to live for messages published by the message messageProducer. Defaults to <code>Message.DEFAULT_TIME_TO_LIVE</code>.
 * </ul>
 * <p/>
 * Notes:
 * <ol>
 * <li>
 * </ol>
 */
public class JMSConnection extends Component implements ExceptionListener {

  private static final Log log = LogFactory.getLog(JMSConnection.class);

  /**
   * Name used to look up connectionfactory in JNDI.
   */
  private String connectionFactoryName = null;

  /**
   * Name used to look up destination (queue/topic) in JNDI.
   */
  private String destinationName = null;

  /**
   * Password to authenticate the JMS Connection (optional).
   */
  private String password = null;

  /**
   * Username to authenticate the JMS Connection (optional).
   */
  private String username = null;

  /**
   * JndiConnection used for lookups.
   */
  private JNDIConnection jndiConnection;

  /**
   * True if expected to make a transacted connection to jms. NB this is used for local rather than xa transactions.
   */
  private boolean isTransacted = false;

  /**
   * True for setting up durable subscriptions to topics.
   */
  private boolean durable = false;

  /**
   * The name used to create the durable topic subscription. Defaults to the <code>destinationName</code>.
   */
  private String durableSubscriptionName = null;

  /**
   * JMS acknowledge mode to use. Defaults to <code>Session.AUTO_ACKNOWLEDGE</code>
   */
  private int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;

  /**
   * Message Selctor to use when receiving messages. Not set by default.
   */
  private String messageSelector = null;

  /**
   * Default is <i>false</i>
   */
  private boolean noLocal = false;

  /**
   * Default DeliveryMode value specified for this Connection's Message Producer.
   */
  private int deliveryMode = Message.DEFAULT_DELIVERY_MODE;

  /**
   * Default Priority value specified for this Connection's Message Producer.
   */
  private int priority = Message.DEFAULT_PRIORITY;

  /**
   * Default time to live value specified for this Connection's Message Producer.
   */
  private long timeToLive = Message.DEFAULT_TIME_TO_LIVE;

  /**
   * ID for client. Default is not set as can be set by ConnectionFactory. Use with caution. ConnectionFactory must be configured to allow the clientID to be set.
   */
  private String clientID = null;

  /**
   * Real JMS ConnectionFactory. When this is set <code>connectionFactoryName</code> is ignored.
   */
  private ConnectionFactory connectionFactory = null;

  private Context ctx;

  private Session session = null;

  private Connection connection = null;

  private MessageConsumer messageConsumer = null;

  private MessageProducer messageProducer = null;

  /**
   * transactional resource, can either be XAResource or ITransactionalResource
   */
  private Object transactionalResource = null;
  private JMSException listenerException = null;

  // Connection Support

  public void connectForReader() {
    if (connection == null) {
      createConnection();
      createSession();
      createMessageConsumer();
      startConnection();
    }
  }

  public void connectForWriter() {
    if (connection == null) {
      createConnection();
      createSession();
      createMessageProducer();
    }
  }

  /**
   * Close the connection.
   */
  public void disconnect() {
    if (connection != null) {
      close();
    }
  }

  /**
   * True if there is an existing session.
   *
   * @return boolean
   */
  public boolean isConnected() {
    return (connection != null);
  }

  // End Connection

  // Deliver/ Receive

  /**
   * Deliver the parameter to the confgured destination.
   *
   * @param message
   * @return String The JMS Message ID.
   */
  public String deliver(Object message) {
    String msgId;
    try {
      Message msg = createMessage(message);
      // send the record
      if (log.isDebugEnabled())
        log.debug("JmsPublisher sending [" + message + "]");
        messageProducer.send(msg, getDeliveryMode(), getPriority(), getTimeToLive());
      msgId = msg.getJMSMessageID();
    } catch (MessageFormatException e) {
      throw new ComponentException("MessageFormatException during publish.", e, this);
    } catch (InvalidDestinationException e) {
      throw new ComponentException("InvalidDestinationException during publish.", e, this);
    } catch (JMSException jmse) {
      throw new ComponentException("JMSException during publish.", jmse, this);
    }
    return msgId;
  }

  /**
   * Create required message objects.
   *
   * @param messageBody
   * @return Message created from the supplied messageBody
   * @throws javax.jms.JMSException
   */
  protected Message createMessage(Object messageBody) throws JMSException {
    Message msg;
    if (messageBody instanceof String) {
      TextMessage textMsg = getSession().createTextMessage();
      textMsg.setText((String) messageBody);
      msg = textMsg;
    } else if (messageBody instanceof Serializable) {
      ObjectMessage objectMsg = getSession().createObjectMessage();
      objectMsg.setObject((Serializable) messageBody);
      msg = objectMsg;
    } else {
      // We have not needed more message types in practice.
      // If we do need them in future this is where they go.
      throw new ComponentException("Undeliverable record type [" + messageBody.getClass().getName() + "]", this);
    }
    return msg;
  }

  // End Deliver

  // Receive

  /**
   * Receive a message from the configured destination. This is a blocking receive which will time out based
   * on the value of the <b>timout</b> property.
   *
   * @return Object  The contents of the received message.
   */
  public Object receive(long timeoutMs) {
    Message msg;
    try {
      if (timeoutMs < 0) {
        msg = getMessageConsumer().receive();
      } else {
        msg = getMessageConsumer().receive(timeoutMs);
      }
    } catch (JMSException jmse) {
      log.error("receiveMessages - could not receive message [JMSException: " + jmse + "]");
      throw new ComponentException("receiveMessages - could not receive message.", jmse, this);
    }
    if (msg != null) {
      return unpackJMSMessage(msg);
    } else {
      return null;
    }
  }

  protected Object unpackJMSMessage(Message msg) throws ComponentException {
    Object msgContents;
    try {
      if (msg instanceof TextMessage) {
        log.debug("Handling TextMessage");
        msgContents = ((TextMessage) msg).getText();
      } else {
        if (msg instanceof ObjectMessage) {
          log.debug("Handling ObjectMessage");
          msgContents = ((ObjectMessage) msg).getObject();
        } else {
          throw new ComponentException("Unsupported JMS Message Type.", this);
        }
      }
    } catch (JMSException e) {
      throw new ComponentException("Error processing JMS Message text.", e, this);
    }
    return msgContents;
  }

  // End Receive

  // Validate

  public void validate(List exceptions) {
    if ((getConnectionFactory() == null) && (connectionFactoryName == null)) {
      exceptions.add( new ComponentException("One of properties connectionFactory or connectionFactoryName must be set.", this));
    }
    if(jndiConnection == null) {
      exceptions.add(new ComponentException("Property jndiConnection is mandatory", this));
    }
    if(getDestinationName() == null) {
      exceptions.add( new ComponentException("Property destinationName is mandatory", this)); 
    }
  }

  // End passValidate

  // Session Stuff

  protected Session getSession() {
    return session;
  }

  /**
   * Optionally create and return a JMS Session. If a TransactionManager is referenced and the session is
   * transacted then register a TransactionSpec with that TransactionManager.
   *
   * @return Session A JMS Session
   */
  protected Session createSession() {
    if (!isConnected()) {
      throw new ComponentException("Attempt to get a session without calling connect() first", this);
    }
    if (session == null) {
      try {
        Session newSession;
        if (connection instanceof XAConnection) {
          newSession = ((XAConnection) connection).createXASession();
          transactionalResource = ((XASession) newSession).getXAResource();
        } else {
          newSession = (connection.createSession(isTransacted, acknowledgeMode));
          if (isTransacted) {
            transactionalResource =  new JMSTransactionalResource(this);
          }
        }
        session = newSession;
      } catch (JMSException jmse) {
        throw new ComponentException("Unable to create session from connection", jmse, this);
      }
    }
    return session;
  }


  public Object getTransactionalResource() {
    return transactionalResource;
  }

  protected void setTransactionalResource(Object transactionalResource) {
    this.transactionalResource = transactionalResource;
  }

  // End Session Stuff

  protected void setConnection(Connection connection) {
    this.connection = connection;
  }

  protected Connection getConnection() {
    return connection;
  }

  protected void createConnection() {
    if (log.isDebugEnabled())
      log.debug("connecting...");
    try {
      if (connectionFactory == null) {
        Object factoryObject = getCtx().lookup(connectionFactoryName);
        if (factoryObject instanceof ConnectionFactory) {
          connectionFactory = (ConnectionFactory) factoryObject;
        } else { //We don't have a valid connectionFactory.
          if (connectionFactory == null) {
            String reason = "Unable to get a connectionFactory. This may be because the required JMS implementation jars are not available on the classpath";
            log.error(reason);
            throw new ComponentException(reason, this);
          } else {
            String reason = "Factory object is not an instance of ConnectionFactory. This should not happen";
            log.error(reason);
            throw new ComponentException(reason, this);
          }
        }
      }

      setConnection(createConnection(connectionFactory));

      if (clientID != null) {
        // A clientID has been configured so attempt to set it.
        try {
          connection.setClientID(clientID);
        } catch (InvalidClientIDException ice) {
          throw new ComponentException(
              "Error setting clientID. ClientID either duplicate(most likely) or invalid. Check for other connected clients",
              ice, this);
        } catch (IllegalStateException ise) {
          throw new ComponentException(
              "Error setting clientID. ClientID most likely administratively set. Check ConnectionFactory settings",
              ise, this);
        }
      }

      // Set ExceptionListener

      connection.setExceptionListener(this);

    } catch (JMSException e) {
      log.error("JMSException during connect." + e);
      throw new ComponentException(" JMSException during connect.", e, this);
    } catch (NamingException e) {
      log.error("NamingException during connect." + e);
      throw new ComponentException("NamingException during connect.", e, this);
    }

  }

  protected Connection createConnection(ConnectionFactory factory) throws JMSException {
    Connection newConnection;
    boolean useUsername = true;
    if (username == null || username.compareTo("") == 0) {
      useUsername = false;
    }

    if (factory instanceof XAConnectionFactory) {

      if (useUsername) {
        newConnection = ((XAConnectionFactory) factory).createXAConnection(username, password);
      } else {
        newConnection = ((XAConnectionFactory) factory).createXAConnection();
      }
    } else {
      if (useUsername) {
        newConnection = factory.createConnection(username, password);
      } else {
        newConnection = factory.createConnection();
      }
    }
    return newConnection;
  }

  protected void startConnection() {
    try {
      connection.start();
    } catch (JMSException e) {
      log.error("JMSException during connection start." + e);
      throw new ComponentException(" JMSException during connection start.", e, this);
    }
  }

  // Consumer Support

  protected MessageConsumer getMessageConsumer() {
    return messageConsumer;
  }

  protected MessageConsumer createMessageConsumer() {
    if (messageConsumer == null) {
      messageConsumer = createMessageConsumer(destinationName);
    }
    return messageConsumer;
  }

  protected void closeConsumer() throws JMSException {
    if (messageConsumer != null) {
      try {
        messageConsumer.close();
      } catch (JMSException e) {
        throw new ComponentException("Unable close messageConsumer for  [" + destinationName + "]", e, this);
      } finally {
        messageConsumer = null;
      }
    }
  }

  protected MessageConsumer createMessageConsumer(String destinationName) {
    Destination destination;
    MessageConsumer newConsumer;

    try {
      destination = (Destination) getCtx().lookup(destinationName);
    } catch (NamingException e) {
      throw new ComponentException("Unable to resolve Destination for [" + destinationName + "]", e, this);
    }
    try {
      if (durable) {
        newConsumer = getSession().createDurableSubscriber((Topic) destination, destinationName, messageSelector, noLocal);
      } else {
        newConsumer = getSession().createConsumer(destination, messageSelector, noLocal);
      }
    } catch (JMSException e) {
      throw new ComponentException("Unable to subscribe to Destination: [" + destination + "]", e, this);
    }

    return newConsumer;

  }

  // End Consumer Support

  // Producer Support

  protected MessageProducer getMessageProducer() {
    return messageProducer;
  }

  protected MessageProducer createMessageProducer() {
    if (messageProducer == null) {
      messageProducer = createMessageProducer(destinationName);
    }
    return messageProducer;
  }

  protected void closeProducer() throws JMSException {
    if (messageProducer != null) {
      try {
        messageProducer.close();
      } catch (JMSException e) {
        throw new ComponentException("Unable close producer for  [" + destinationName + "]", e, this);
      } finally {
        messageProducer = null;
      }
    }
  }

  protected MessageProducer createMessageProducer(String subject) {
    MessageProducer newProducer;
    Destination destination;
    try {
      destination = (Destination) getCtx().lookup(subject);
    } catch (NamingException e) {
      throw new ComponentException("Unable to resolve Destination for [" + subject + "]", e, this);
    }
    try {
      newProducer = session.createProducer(destination);
    } catch (JMSException e) {
      throw new ComponentException("Exception creating JMS Producer ", e, this);
    }
    log.info(" Producer initialised for JMS Destination=" + newProducer);
    return newProducer;
  }

  // End Producer Support

  /** Close everything */
  protected void close() {
    try {
      closeConsumer();
      closeProducer();
      if (session != null) {
        session.close();
      }
      if (connection != null) {
        connection.close();
      }
      setConnection(null);
    } catch (JMSException e) {
      throw new ComponentException("Unable close connection for  [" + destinationName + "]", e, this);
    }
  }

  // ExceptionListener implementation

  public void onException(JMSException jmsException) {
    setListenerException(jmsException);
    log.error("Exception Callback triggered. Exception: " + jmsException);
    this.disconnect(); // ????
  }

  private void setListenerException(JMSException jmsException) {
    this.listenerException = jmsException;
  }

  public Exception getListenerException() {
    return listenerException;
  }

  // JNDI Support

  protected Context getCtx() throws NamingException {
    if (ctx == null) {
      ctx = jndiConnection.connect();
    }
    return ctx;
  }

  protected void setCtx(Context ctx) {
    this.ctx = ctx;
  }

  // End JNDI support

  // Bean Properties

  /**
   * Name used to look up connectionfactory in JNDI.
   */
  public void setConnectionFactoryName(String connectionFactoryName) {
    this.connectionFactoryName = connectionFactoryName;
  }

  protected String getConnectionFactoryName() { return connectionFactoryName; }

  /**
   * Real JMS ConnectionFactory. When this is set <code>connectionFactoryName</code> is ignored.
   */
  public void setConnectionFactory(ConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  protected ConnectionFactory getConnectionFactory() { return connectionFactory; }

  /**
   * Name used to look up destination (queue/topic) in JNDI.
   */
  public void setDestinationName(String destinationName) {
    this.destinationName = destinationName;
  }

  protected String getDestinationName() { return destinationName; }

  /**
   * Password to authenticate the JMS Connection (optional).
   */
  public void setPassword(String password) {
    this.password = password;
  }

  protected String getPassword() { return password; }

  /**
   * Username to authenticate the JMS Connection (optional).
   */
  public void setUsername(String username) {
    this.username = username;
  }

  protected String getUsername() { return username; }

  /**
   * Set the JNDIConnection used to look up jndi managed resources.
   */
  public void setJndiConnection(JNDIConnection jndiConnection) {
    this.jndiConnection = jndiConnection;
  }

  /**
   * true if a transacted connection is to be made.
   */
  public void setTransacted(boolean transacted) {
    this.isTransacted = transacted;
  }

  /**
   * true if a transacted connection is to be made.
   */
  protected boolean isTransacted() {
    return isTransacted;
  }

  /**
   * Defaults to <i>false</i>. If <i>true</i> create a durable topic subscription.
   */
  public void setDurable(boolean durable) {
    this.durable = durable;
  }

  protected boolean isDurable() {
    return durable;
  }

  /**
   * Defaults to the <code>destinationName</code>. The name used to create the durable topic subscription.
   */
  protected String getDurableSubscriptionName() {
    if (durableSubscriptionName == null)
      // Since we must have a name to set up a Durable Subscription we default to the destinationName.
      return destinationName;
    else
      return durableSubscriptionName;
  }

  /**
   * Defaults to the <code>destinationName</code>. The name used to create the durable topic subscription.
   */
  public void setDurableSubscriptionName(String durableSubscriptionName) {
    this.durableSubscriptionName = durableSubscriptionName;
  }

  /**
   * Defaults to <code>Session.AUTO_ACKNOWLEDGE</code>
   */
  public void setAcknowledgeMode(int acknowledgeMode) {
    this.acknowledgeMode = acknowledgeMode;
  }

  /** Defaults to <code>Session.AUTO_ACKNOWLEDGE</code> */
  protected int getAcknowledgeMode() {
    return acknowledgeMode;
  }

  /**
   * Not set by default.
   */
  public void setMessageSelector(String messageSelector) {
    this.messageSelector = messageSelector;
  }

  protected String getMessageSelector() {
    return messageSelector;
  }

  /**
   * Default is <i>false</i>.
   */
  public void setNoLocal(boolean noLocal) {
    this.noLocal = noLocal;
  }

  protected boolean isNoLocal() {
    return noLocal;
  }

  /**
   * Default DeliveryMode value specified for this Connection's Message Producer.
   */
  protected int getDeliveryMode() { return deliveryMode; }

  /**
   * Default DeliveryMode value specified for this Connection's Message Producer.
   */
  public void setDeliveryMode(int deliveryMode) { this.deliveryMode = deliveryMode; }

  /**
   * Default Priority value specified for this Connection's Message Producer.
   */
  protected int getPriority() { return priority; }

  /**
   * Default Priority value specified for this Connection's Message Producer.
   */
  public void setPriority(int priority) { this.priority = priority; }

  /**
   * Default time to live value specified for this Connection's Message Producer.
   */
  protected long getTimeToLive() { return timeToLive; }

  /**
   * Default time to live value specified for this Connection's Message Producer.
   */
  public void setTimeToLive(long timeToLive) { this.timeToLive = timeToLive; }

  /**
   * Default is not set. Use with caution. ConnectionFactory must be configured to allow the clientID to be set.
   */
  public void setClientID(String clientID) {
    this.clientID = clientID;
  }

  protected String getClientID() {
    return clientID;
  }
  // End Bean Properties


}
