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
package org.oa3.auxillary.jms;

import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.IllegalStateException;
import javax.jms.InvalidClientIDException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.jms.XAQueueConnection;
import javax.jms.XAQueueConnectionFactory;
import javax.jms.XASession;
import javax.jms.XATopicConnection;
import javax.jms.XATopicConnectionFactory;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.auxillary.jndi.JNDIConnection;
import org.oa3.core.exception.OAException;
import org.oa3.core.exception.ResourceException;

/* 
 * File: $Header: /cvs/oa3/src/org/oa3/connector/jms/JMSConnection.java,v 1.17 2006/10/20 10:15:04 kscully Exp $
 * Rev:  $Revision: 1.17 $
 * Created Dec 20, 2005 by Kevin Scully
 */
/**
 * Utility class providing JMS support to the JMS Listener and Publisher classes. Uses JMS 1.0.2 compliant code.
 * <p/>
 * Provides following:
 * <p/>
 * Receive from and Publish to both Queues and Topics.
 * Provide access to Underlying Transactional resources.
 * Will register with JtaTransactionManagers if configured that way.
 * <p/>
 * Main Properties
 * <ul>
 * <li><b>connectionFactoryName</b>    Name used to look up connectionfactory in JNDI
 * <li><b>destinationName</b>          Name used to look up destination (queue/topic) in JNDI
 * <li><b>username</b>                 Username to authenticate the JMS Connection (optional)
 * <li><b>password</b>                 Password to authenticate the JMS Connection (optional)
 * <li><b>topic</b>                    True if connecting to topic (see note 1 below)
 * <li><b>queue</b>                    True if connecting to queue (see note 1 below)
 * <li><b>durable</b>                  Defaults to <i>false</i>. If <i>true</i> create a durable topic subscription.
 * <li><b>durableSubscriptionName</b>  Defaults to the <code>destinationName</code>. The name used to create the durable topic subscription.
 * <li><b>acknowledgeMode</b>          Defaults to <code>Session.AUTO_ACKNOWLEDGE</code>
 * <li><b>messageSelector</b>          Not set by default.
 * <li><b>noLocal</b>                  Default is <i>false</i>
 * <li><b>clientID</b>                 Default is not set. Use with caution. ConnectionFactory must be configured to allow the clientID to be set.
 * <li><b>jndiConnection</b>           JndiConnection used for lookups
 * See Note 2.
 * <li><b>connectionFactory</b>        Real JMS ConnectionFactory. When this is set <code>connectionFactoryName</code> is ignored.
 * </ul>
 * <p/>
 * Notes:
 * <ol>
 * <li><b>topic</b> and <b>queue</b> are optional properties. If either is set to true then we assume we are connecting to the appropriate destination type.
 * If neither (or both!) are set to true then they are ignored and we try to determine whether we are talking to a queue or topic from the ConnectionFactory.
 * If we still can't work it out then we give up and throw a ResourceException. The advantage of this approach is for most reasonably
 * implemented JMS Servers all we need is the ConnectionFactory to work out what we are talking to and topic/queue configuration is unnecessary.
 * <li>The whole transaction <b>timeout</b> area is unsatisfactory at present. At a minimum the transaction timeout must be longer than any receive
 * timeouts plus time taken to process any messages through the pipeline. If this isn't true then the transaction manager will countermand any commits and
 * force a rollback.
 * </ol>
 */
public class JMSConnection {

  private static final Log log = LogFactory.getLog(JMSConnection.class);

  /** Name used to look up connectionfactory in JNDI. */
  private String connectionFactoryName = null;

  /** Name used to look up destination (queue/topic) in JNDI. */
  private String destinationName = null;

  /** Password to authenticate the JMS Connection (optional). */
  private String password = null;

  /** Username to authenticate the JMS Connection (optional). */
  private String username = null;

  /** True if connecting to topic. */
  private boolean isTopic = false;

  /** True if connecting to queue. */
  private boolean isQueue = false;

  /** JndiConnection used for lookups. */
  private JNDIConnection jndiConnection;

  /** True if expected to make a transacted connection to jms. NB this is used for local rather than xa transactions. */
  private boolean isTransacted = false;

  /** True for setting up durable subscriptions to topics. */
  private boolean durable = false;

  /** The name used to create the durable topic subscription. Defaults to the <code>destinationName</code>. */
  private String durableSubscriptionName = null;

  /** JMS acknowledge mode to use. Defaults to <code>Session.AUTO_ACKNOWLEDGE</code> */
  private int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;

  /** Message Selctor to use when receiving messages. Not set by default. */
  private String messageSelector = null;

  /** Default is <i>false</i> */
  private boolean noLocal = false;

  /** ID for client. Default is not set as can be set by ConnectionFactory. Use with caution. ConnectionFactory must be configured to allow the clientID to be set. */
  private String clientID = null;

  /** Real JMS ConnectionFactory. When this is set <code>connectionFactoryName</code> is ignored. */
  protected ConnectionFactory connectionFactory = null;

  private Context ctx;

  protected Session session = null;

  private Connection connection = null;

  private MessageConsumer consumer = null;

  private MessageProducer producer = null;

  /** transactional resource, can either be XAResource or ITransactionalResource */
  private Object transactionalResource = null;

  // Bean Properties

  /** Name used to look up connectionfactory in JNDI. */
  public void setConnectionFactoryName(String connectionFactoryName) {
    this.connectionFactoryName = connectionFactoryName;
  }

  /** Real JMS ConnectionFactory. When this is set <code>connectionFactoryName</code> is ignored. */
  public void setConnectionFactory(ConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  /** Name used to look up destination (queue/topic) in JNDI. */
  public void setDestinationName(String destinationName) {
    this.destinationName = destinationName;
  }

  /** Password to authenticate the JMS Connection (optional). */
  public void setPassword(String password) {
    this.password = password;
  }

  /** Username to authenticate the JMS Connection (optional). */
  public void setUsername(String username) {
    this.username = username;
  }

  /** Set the JNDIConnection used to look up jndi managed resources. */
  public void setJndiConnection(JNDIConnection jndiConnection) {
    this.jndiConnection = jndiConnection;
  }

  /** true if a transacted connection is to be made. */
  public void setTransacted(boolean transacted) {
    this.isTransacted = transacted;
  }

  /** True if connecting to topic (see note 1 in class comment) */
  public void setTopic(boolean topic) {
    this.isTopic = topic;
  }

  /** True if connecting to queue (see note 1 in class comment) */
  public void setQueue(boolean queue) {
    this.isQueue = queue;
  }

  /** Defaults to <i>false</i>. If <i>true</i> create a durable topic subscription. */
  public void setDurable(boolean durable) {
    this.durable = durable;
  }

  /** Defaults to the <code>destinationName</code>. The name used to create the durable topic subscription. */
  protected String getDurableSubscriptionName() {
    if (durableSubscriptionName == null)
      // Since we must have a name to set up a Durable Subscription we default to the destinationName.
      return destinationName;
    else
      return durableSubscriptionName;
  }

  /** Defaults to the <code>destinationName</code>. The name used to create the durable topic subscription. */
  public void setDurableSubscriptionName(String durableSubscriptionName) {
    this.durableSubscriptionName = durableSubscriptionName;
  }

  /** Defaults to <code>Session.AUTO_ACKNOWLEDGE</code> */
  public void setAcknowledgeMode(int acknowledgeMode) {
    this.acknowledgeMode = acknowledgeMode;
  }

  /** Not set by default. */
  public void setMessageSelector(String messageSelector) {
    this.messageSelector = messageSelector;
  }

  /** Default is <i>false</i>. */
  public void setNoLocal(boolean noLocal) {
    this.noLocal = noLocal;
  }

  /** Default is not set. Use with caution. ConnectionFactory must be configured to allow the clientID to be set. */
  public void setClientID(String clientID) {
    this.clientID = clientID;
  }

  // End Bean Properties

  // Connection Support

  /**
   * Create a connection and session.
   */
  public void connect() {
    if (connection == null) {
      createConnection();
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
    if (isTopic) {
      return deliverToTopic(message);
    } else {
      return deliverToQueue(message);
    }
  }

  protected String deliverToTopic(Object messageBody) {
    String msgId = null;

    try {
      Message msg = createMessage(messageBody);
      // send the record
      if (log.isDebugEnabled())
        log.debug("JmsPublisher sending [" + messageBody + "]");
      ((TopicPublisher) getProducer()).publish(msg); // todo JMS102
      //todo more properties
      //((TopicPublisher)getProducer()).publish( textMessage, getDeliveryMode(), getJMSPriority(), getTimeToLive() );
      msgId = msg.getJMSMessageID();
    } catch (JMSException jmse) {
      throw new OAException("JMSException during publish.", jmse);
    } catch (OAException oae) {
      throw oae; // Make sure it isn't swallowed !
    } catch (Exception e) {
      throw new OAException("Exception during publish.", e);
    }
    return msgId;
  }

  protected String deliverToQueue(Object messageBody) {
    String msgId = null;
    try {
      Message msg = createMessage(messageBody);
      // send the record
      if (log.isDebugEnabled())
        log.debug("JmsPublisher sending [" + messageBody + "]");
      ((QueueSender) getProducer()).send(msg); // todo JMS102
      //todo more properties
      //((QueueSender) getProducer()).send( textMessage, getDeliveryMode(), getJMSPriority(), getTimeToLive() );
      msgId = msg.getJMSMessageID();
    } catch (JMSException jmse) {
      throw new OAException("JMSException during publish.", jmse);
    } catch (OAException oae) {
      throw oae; // Make sure it isn't swallowed !
    } catch (Exception e) {
      throw new OAException("Exception during publish.", e);
    }
    return msgId;
  }

  /**
   * Create required message objects.
   *
   * @param messageBody
   * @return Message created from the supplied messageBody
   * @throws JMSException
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
      // We have never needed more message types in practice.
      // If we do need them in future this is where they go.
      throw new OAException("Undeliverable record type [" + messageBody.getClass().getName() + "]");
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
        msg = getConsumer().receive();
      } else {
        msg = getConsumer().receive(timeoutMs);
      }
    } catch (JMSException jmse) {
      log.error("receiveMessages - could not receive message [JMSException: " + jmse + "]");
      throw new OAException("receiveMessages - could not receive message.", jmse);
    }
    if (msg != null) {
      return unpackJMSMessage(msg);
    } else {
      return null;
    }
  }

  protected Object unpackJMSMessage(Message msg) throws OAException {

    Object msgContents = null;
    try {
      if (msg instanceof TextMessage) {
        log.debug("Handling TextMessage");
        msgContents = ((TextMessage) msg).getText();
      } else {
        if (msg instanceof ObjectMessage) {
          log.debug("Handling ObjectMessage");
          msgContents = ((ObjectMessage) msg).getObject();
        } else {
          // Todo This needs to be a fatal error
          throw new OAException("Unsupported JMS Message Type.");
        }
      }
    } catch (JMSException e) {
      //log.error("Error processing JMS Message text [" + e + "]");
      throw new OAException("Error processing JMS Message text.", e);
    }
    return msgContents;
  }

  // End Receive

  // Session Stuff

  /**
   * Optionally create and return a JMS Session. If a TransactionManager is referenced and the session is
   * transacted then register a TransactionSpec with that TransactionManager.
   *
   * @return Session A JMS Session
   */
  public Session getSession() {
    if (!isConnected()) {
      throw new OAException("Attempt to get a session without calling connect() first");
    }
    //  TODO: review the assumption that we do nothing if the session is already set
    if (session == null) {
      try {
        if (isTopic) {
          session = createTopicSession();
        } else {
          session = createQueueSession();
        }
        // We have a session and oa3 takes responsibility for being ready for messages so we can start
        connection.start(); 
      } catch (JMSException jmse) {
        throw new OAException("Unable to create session from connection", jmse);
      }
    }
    return session;
  }

  public Object getTransactionalResource() {
    return transactionalResource;
  }
  
  protected TopicSession createTopicSession() throws JMSException {
    TopicSession newSession;
    if (connection instanceof XATopicConnection) {
      newSession = (TopicSession) (((XATopicConnection) connection).createXATopicSession());
      transactionalResource = ((XASession) newSession).getXAResource();
    } else {
      newSession = (((TopicConnection) connection).createTopicSession(isTransacted, acknowledgeMode));
      if (isTransacted) {
        transactionalResource =  new JMSTransactionalResource(this);
      }
    }
    return newSession;
  }

  protected QueueSession createQueueSession() throws JMSException {
    QueueSession newSession;
    if (connection instanceof XAQueueConnection) {
      newSession = (QueueSession) (((XAQueueConnection)connection).createXAQueueSession());
      transactionalResource = ((XASession) newSession).getXAResource();
    } else {
      newSession = (((QueueConnection) connection).createQueueSession(isTransacted, acknowledgeMode));
      if (isTransacted) {
        transactionalResource = new JMSTransactionalResource(this);
      }
    }
    return newSession;
  }

  // End Session Stuff

  protected void setConnection(Connection connection) {
    this.connection = connection;
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
            throw new OAException(reason);
          } else {
            String reason = "Factory object is not an instance of ConnectionFactory. This should not happen";
            log.error(reason);
            throw new OAException(reason);
          }
        }
      }

      boolean factoriesUnified = ((connectionFactory instanceof TopicConnectionFactory) && (connectionFactory instanceof QueueConnectionFactory));
      boolean topicQueueDefined = (isTopic != isQueue);

      if (!topicQueueDefined && factoriesUnified) {
        // We haven't a prayer of guessing so bail out
        throw new ResourceException(
            "Using JMS 1.0.2 and cannot determine whether using Queue or Topic. Please set one of Properties 'queue' or 'topic' on JMSConnection to true");
      }

      if (topicQueueDefined) { // Follow the properties
        if (isTopic) {
          setConnection(createTopicConnection((TopicConnectionFactory) connectionFactory));
        } else {
          setConnection(createQueueConnection((QueueConnectionFactory) connectionFactory));
        }
      } else { // Follow the ConnectionFactory classes
        if ((connectionFactory instanceof TopicConnectionFactory)) {
          setConnection(createTopicConnection((TopicConnectionFactory) connectionFactory));
          // Make sure the property settings reflect what we've done
          setTopic(true);
          setQueue(false);
        } else {
          if (connectionFactory instanceof QueueConnectionFactory) {
            setConnection(createQueueConnection((QueueConnectionFactory) connectionFactory));
            // Make sure the property settings reflect what we've done
            setTopic(false);
            setQueue(true);
          }
        }
      }
      if (clientID != null) {
        // A clientID has been configured so attempt to set it.
        try {
          connection.setClientID(clientID);
        } catch (InvalidClientIDException ice) {
          throw new OAException(
              "Error setting clientID. ClientID either duplicate(most likely) or invalid. Check for other connected clients",
              ice);
        } catch (IllegalStateException ise) {
          throw new OAException(
              "Error setting clientID. ClientID most likely administratively set. Check ConnectionFactory settings",
              ise);
        }
      }

    } catch (JMSException e) {
      log.error("JMSException during connect." + e);
      throw new OAException(" JMSException during connect.", e);
    } catch (NamingException e) {
      log.error("NamingException during connect." + e);
      throw new OAException("NamingException during connect.", e);
    }

  }

  protected TopicConnection createTopicConnection(TopicConnectionFactory factory) throws JMSException, NamingException {
    TopicConnection newConnection;

    // Look up the TopicConnectionFactory
    TopicConnectionFactory tconFactory = (TopicConnectionFactory) factory;

    // Get a TopicConnection from the TopicConnectionFactory
    boolean useUsername = true;
    if (username == null || username.compareTo("") == 0) {
      useUsername = false;
    }

    // Do things diferently based on whether or not the ConnectionFactory is XA Enabled.
    if (tconFactory instanceof XATopicConnectionFactory) {
      if (useUsername) {
        newConnection = ((XATopicConnectionFactory) tconFactory).createXATopicConnection(username, password);
      } else {
        newConnection = ((XATopicConnectionFactory) tconFactory).createXATopicConnection();
      }
    } else {
      if (useUsername) {
        newConnection = tconFactory.createTopicConnection(username, password);
      } else {
        newConnection = tconFactory.createTopicConnection();
      }
    }
    return newConnection;
  }

  protected QueueConnection createQueueConnection(QueueConnectionFactory qconFactory) throws JMSException,
      NamingException {
    QueueConnection newConnection;
    boolean useUsername = true;
    if (username == null || username.compareTo("") == 0) {
      useUsername = false;
    }
    if (qconFactory instanceof XAQueueConnectionFactory) {
      if (useUsername) {
        newConnection = ((XAQueueConnectionFactory) qconFactory).createXAQueueConnection(username, password);
      } else {
        newConnection = ((XAQueueConnectionFactory) qconFactory).createXAQueueConnection();
      }
    } else {
      if (useUsername) {
        newConnection = qconFactory.createQueueConnection(username, password);
      } else {
        newConnection = qconFactory.createQueueConnection();
      }
    }
    return newConnection;
  }

  // Consumer Support

  protected MessageConsumer getConsumer() {
    if (consumer == null) {
      if (isTopic) { // isTopic will have been set correctly when the original connection was made
        consumer = createTopicSubscriber(destinationName);
      } else {
        consumer = createQueueReceiver(destinationName);
      }
    }
    return consumer;
  }

  protected void closeConsumer() throws JMSException {
    if (consumer != null) {
      try {
        consumer.close();
      } catch (JMSException e) {
        throw new OAException("Unable close consumer for  [" + destinationName + "]", e);
      } finally {
        consumer = null;
      }
    }
  }

  protected TopicSubscriber createTopicSubscriber(String destination) {
    Topic topic = null;
    TopicSubscriber subscriber = null;
    try {
      topic = (Topic) getCtx().lookup(destination);
    } catch (NamingException e) {
      throw new OAException("Unable to resolve Topic for [" + destination + "]", e);
    }
    try {
      if (durable) {
        subscriber = (((TopicSession) getSession()).createDurableSubscriber(topic, durableSubscriptionName,
            messageSelector, noLocal));
      } else {
        subscriber = (((TopicSession) getSession()).createSubscriber(topic, messageSelector, noLocal));
      }
    } catch (JMSException e) {
      throw new OAException("Unable to subscriber for Topic [" + destination + "]", e);
    }
    log.info("Consumer initialized for JMSDestination=" + topic);

    return subscriber;
  }

  protected QueueReceiver createQueueReceiver(String aDestination) {
    Queue queue;
    QueueReceiver aReceiver;
    QueueSession queueSession = (QueueSession) getSession();
    try {
      queue = (Queue) getCtx().lookup(aDestination);
    } catch (NamingException e) {
      throw new OAException("Unable to resolve Queue for [" + aDestination + "]", e);
    }
    // Create a Queue Receiver
    try {
      aReceiver = queueSession.createReceiver(queue, messageSelector);
    } catch (JMSException e) {
      throw new OAException("Exception creating JMS QueueReceiver ", e);
    }
    return aReceiver;
  }

  // End Consumer Support

  // Producer Support

  protected MessageProducer getProducer() {
    if (producer == null) {
      if (isTopic) {
        producer = createTopicPublisher(destinationName);
      } else {
        producer = createQueueSender(destinationName);
      }
    }
    return producer;
  }

  protected void closeProducer() throws JMSException {
    if (producer != null) {
      try {
        producer.close();
      } catch (JMSException e) {
        throw new OAException("Unable close producer for  [" + destinationName + "]", e);
      } finally {
        producer = null;
      }
    }
  }

  protected MessageProducer createTopicPublisher(String subject) {
    Topic topic = null;
    TopicPublisher aPublisher;
    TopicSession topicSession = (TopicSession) getSession();
    try {
      topic = (Topic) getCtx().lookup(subject);
    } catch (NamingException e) {
      throw new OAException("Unable to resolve Topic for [" + subject + "]", e);
    }
    // Create a message producer (TopicPublisher)
    try {
      aPublisher = topicSession.createPublisher((Topic) topic);
    } catch (JMSException e) {
      throw new OAException("Exception creating JMS Producer ", e);
    }

    log.info(" Producer initialised for JMSDestination=" + topic);
    return aPublisher;
  }

  protected QueueSender createQueueSender(String aDestination) {
    Queue queue;
    QueueSender aSender;
    QueueSession queueSession = (QueueSession) getSession();
    try {
      queue = (Queue) getCtx().lookup(aDestination);
    } catch (NamingException e) {
      throw new OAException("Unable to resolve Queue for [" + aDestination + "]", e);
    }
    // Create a Queue Sender
    try {
      aSender = queueSession.createSender(queue);
    } catch (JMSException e) {
      throw new OAException("Exception creating JMS QueueSender ", e);
    }
    return aSender;
  }

  // End Producer Support

  protected void close() {
    try {
      if (consumer != null) {
        consumer.close();
      }
      if (connection != null) {
        connection.close();
      }
      setConnection(null);
    } catch (JMSException e) {
      throw new OAException("Unable close connection for  [" + destinationName + "]", e);
    }
  }

  protected Context getCtx() throws NamingException {
    if (ctx == null) {
      ctx = jndiConnection.connect();
    }
    return ctx;
  }

  protected void setCtx(Context ctx) {
    this.ctx = ctx;
  }

}
