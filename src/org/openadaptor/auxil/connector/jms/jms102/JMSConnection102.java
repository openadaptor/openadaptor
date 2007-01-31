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

package org.openadaptor.auxil.connector.jms.jms102;

import java.io.Serializable;

import javax.jms.ConnectionFactory;
import javax.jms.IllegalStateException;
import javax.jms.InvalidClientIDException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
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
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.jms.XAQueueConnection;
import javax.jms.XAQueueConnectionFactory;
import javax.jms.XATopicConnection;
import javax.jms.XATopicConnectionFactory;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.jms.JMSConnection;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.ProcessingException;
import org.openadaptor.core.exception.ResourceException;

/**
 * Utility class providing JMS support to the JMS Listener and Publisher classes. Uses JMS 1.0.2 compliant code.
 * <p/>
 * Provides following:
 * <p/>
 * Receive from and Publish to both Queues and Topics.
 * Provide access to Underlying Transactional resources.
 * <p/>
 * Main Properties
 * <ul>
 * <li><b>topic</b>                    True if connecting to topic (see note 1 below)
 * <li><b>queue</b>                    True if connecting to queue (see note 1 below)
 * </ul>
 * <p/>
 * Notes:
 * <ol>
 * <li><b>topic</b> and <b>queue</b> are optional properties. If either is set to true then we assume we are connecting to the appropriate destination type.
 * If neither (or both!) are set to true then they are ignored and we try to determine whether we are talking to a queue or topic from the ConnectionFactory.
 * If we still can't work it out then we give up and throw a ResourceException. The advantage of this approach is for most reasonably
 * implemented JMS Servers all we need is the ConnectionFactory to work out what we are talking to and topic/queue configuration is unnecessary.
 * </ol>
 */
public class JMSConnection102 extends JMSConnection {

  private static final Log log = LogFactory.getLog(JMSConnection102.class);


  /** True if connecting to topic. */
  private boolean isTopic = false;

  /** True if connecting to queue. */
  private boolean isQueue = false;

  // Bean Properties



  /** True if connecting to topic (see note 1 in class comment) */
  public void setTopic(boolean topic) {
    this.isTopic = topic;
  }

  /** True if connecting to queue (see note 1 in class comment) */
  public void setQueue(boolean queue) {
    this.isQueue = queue;
  }


  // End Bean Properties


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
    String msgId;

    try {
      Message msg = createMessage(messageBody);
      // send the record
      if (log.isDebugEnabled())
        log.debug("JmsPublisher sending [" + messageBody + "]");
      //((TopicPublisher)getMessageProducer()).publish( msg, getDeliveryMode(), getPriority(), getTimeToLive() );
      msgId = msg.getJMSMessageID();
    } catch (JMSException jmse) {
      throw new ConnectionException("JMSException during publish.", jmse, this);
    } catch (ComponentException oae) {
      throw oae; // Make sure it isn't swallowed !
    } catch (Exception e) {
      throw new ConnectionException("Exception during publish.", e, this);
    }
    return msgId;
  }

  protected String deliverToQueue(Object messageBody) {
    String msgId;
    try {
      Message msg = createMessage(messageBody);
      // send the record
      if (log.isDebugEnabled())
        log.debug("JmsPublisher sending [" + messageBody + "]");
      //((QueueSender) getMessageProducer()).send( msg, getDeliveryMode(), getPriority(), getTimeToLive() );
      msgId = msg.getJMSMessageID();
    } catch (JMSException jmse) {
      throw new ConnectionException("JMSException during publish.", jmse, this);
    } catch (ComponentException oae) {
      throw oae; // Make sure it isn't swallowed !
    } catch (Exception e) {
      throw new ConnectionException("Exception during publish.", e, this);
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
    if (messageBody instanceof String) {
      //TextMessage textMsg = getSession().createTextMessage();
      //textMsg.setText((String) messageBody);
      //msg = textMsg;
    } else if (messageBody instanceof Serializable) {
      //ObjectMessage objectMsg = getSession().createObjectMessage();
      //objectMsg.setObject((Serializable) messageBody);
      //msg = objectMsg;
    } else {
      // We have never needed more message types in practice.
      // If we do need them in future this is where they go.
      throw new ProcessingException("Undeliverable record type [" + messageBody.getClass().getName() + "]", this);
    }
    //return msg;
    return null;
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
    /*
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
    */
    return null;
  }

  protected Object unpackJMSMessage(Message msg) throws ComponentException {

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
          throw new ProcessingException("Unsupported JMS Message Type.", this);
        }
      }
    } catch (JMSException e) {
      throw new ConnectionException("Error processing JMS Message text.", e, this);
    }
    return msgContents;
  }

  // End Receive

  // Session Stuff


  protected Session createSession() {
    Session newSession;
    try {
      if (isTopic) {
        newSession = createTopicSession();
      } else {
        newSession = createQueueSession();
      }
    } catch (JMSException jmse) {
      throw new ConnectionException("Unable to create session from connection", jmse, this);
    }
    return newSession;
  }

  protected TopicSession createTopicSession() throws JMSException {
    /*
    TopicSession newSession;
    if (getConnection() instanceof XATopicConnection) {
      newSession = (TopicSession) (((XATopicConnection) getConnection()).createXATopicSession());
      setTransactionalResource( ((XASession) newSession).getXAResource() );
    } else {
      newSession = (((TopicConnection) getConnection()).createTopicSession( isTransacted(), getAcknowledgeMode() ));
      if (isTransacted()) {
        setTransactionalResource( new JMSTransactionalResource(this) );
      }
    }
    return newSession;
    */
    return null;
  }

  protected QueueSession createQueueSession() throws JMSException {
    /*
    QueueSession newSession;
    if (getConnection() instanceof XAQueueConnection) {
      newSession = (QueueSession) (((XAQueueConnection)getConnection()).createXAQueueSession());
      setTransactionalResource (((XASession) newSession).getXAResource() );
    } else {
      newSession = (((QueueConnection) getConnection()).createQueueSession( isTransacted(), getAcknowledgeMode() ));
      if (isTransacted()) {
        setTransactionalResource( new JMSTransactionalResource(this) );
      }
    }
    return newSession;
    */
    return null;
  }

  // End Session Stuff

 protected void createConnection() {
    if (log.isDebugEnabled())
      log.debug("connecting...");
    try {
      if (getConnectionFactory() == null) {
        Object factoryObject = getCtx().lookup(getConnectionFactoryName());
        if (factoryObject instanceof ConnectionFactory) {
          setConnectionFactory((ConnectionFactory) factoryObject);
        } else { //We don't have a valid connectionFactory.
          if (getConnectionFactory() == null) {
            String reason = "Unable to get a connectionFactory. This may be because the required JMS implementation jars are not available on the classpath";
            log.error(reason);
            throw new ConnectionException(reason, this);
          } else {
            String reason = "Factory object is not an instance of ConnectionFactory. This should not happen";
            log.error(reason);
            throw new ConnectionException(reason, this);
          }
        }
      }

      boolean factoriesUnified = ((getConnectionFactory() instanceof TopicConnectionFactory) && (getConnectionFactory() instanceof QueueConnectionFactory));
      boolean topicQueueDefined = (isTopic != isQueue);

      if (!topicQueueDefined && factoriesUnified) {
        // We haven't a prayer of guessing so bail out
        throw new ResourceException(
            "Using JMS 1.0.2 and cannot determine whether using Queue or Topic. Please set one of Properties 'queue' or 'topic' on JMSConnection102 to true", this);
      }

      if (topicQueueDefined) { // Follow the properties
        if (isTopic) {
          setConnection(createTopicConnection((TopicConnectionFactory) getConnectionFactory()));
        } else {
          setConnection(createQueueConnection((QueueConnectionFactory) getConnectionFactory()));
        }
      } else { // Follow the ConnectionFactory classes
        if ((getConnectionFactory() instanceof TopicConnectionFactory)) {
          setConnection(createTopicConnection((TopicConnectionFactory) getConnectionFactory()));
          // Make sure the property settings reflect what we've done
          setTopic(true);
          setQueue(false);
        } else {
          if (getConnectionFactory() instanceof QueueConnectionFactory) {
            setConnection(createQueueConnection((QueueConnectionFactory) getConnectionFactory()));
            // Make sure the property settings reflect what we've done
            setTopic(false);
            setQueue(true);
          }
        }
      }
      if (getClientID() != null) {
        // A clientID has been configured so attempt to set it.
        try {
          getConnection().setClientID(getClientID());
        } catch (InvalidClientIDException ice) {
          throw new ConnectionException(
              "Error setting clientID. ClientID either duplicate(most likely) or invalid. Check for other connected clients",
              ice, this);
        } catch (IllegalStateException ise) {
          throw new ConnectionException(
              "Error setting clientID. ClientID most likely administratively set. Check ConnectionFactory settings",
              ise, this);
        }
      }

    } catch (JMSException e) {
      log.error("JMSException during connect." + e);
      throw new ConnectionException(" JMSException during connect.", e, this);
    } catch (NamingException e) {
      log.error("NamingException during connect." + e);
      throw new ConnectionException("NamingException during connect.", e, this);
    }

  }

  protected TopicConnection createTopicConnection(TopicConnectionFactory factory) throws JMSException, NamingException {
    TopicConnection newConnection;

    // Look up the TopicConnectionFactory
    TopicConnectionFactory tconFactory = (TopicConnectionFactory) factory;

    // Get a TopicConnection from the TopicConnectionFactory
    boolean useUsername = true;
    if (getUsername() == null || getUsername().compareTo("") == 0) {
      useUsername = false;
    }

    // Do things diferently based on whether or not the ConnectionFactory is XA Enabled.
    if (tconFactory instanceof XATopicConnectionFactory) {
      if (useUsername) {
        newConnection = ((XATopicConnectionFactory) tconFactory).createXATopicConnection( getUsername(), getPassword() );
      } else {
        newConnection = ((XATopicConnectionFactory) tconFactory).createXATopicConnection();
      }
    } else {
      if (useUsername) {
        newConnection = tconFactory.createTopicConnection( getUsername(), getPassword() );
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
    if (getUsername() == null || getUsername().compareTo("") == 0) {
      useUsername = false;
    }
    if (qconFactory instanceof XAQueueConnectionFactory) {
      if (useUsername) {
        newConnection = ((XAQueueConnectionFactory) qconFactory).createXAQueueConnection(getUsername(), getPassword());
      } else {
        newConnection = ((XAQueueConnectionFactory) qconFactory).createXAQueueConnection();
      }
    } else {
      if (useUsername) {
        newConnection = qconFactory.createQueueConnection( getUsername(), getPassword() );
      } else {
        newConnection = qconFactory.createQueueConnection();
      }
    }
    return newConnection;
  }

  // Consumer Support


  protected MessageConsumer createMessageConsumer(String destinationName) {
//    MessageConsumer newConsumer;
    if (isTopic) { // isTopic will have been set correctly when the original connection was made
        //newConsumer = createTopicSubscriber(getDestinationName());
      } else {
        //newConsumer = createQueueReceiver(getDestinationName());
      }
    //return newConsumer;
    return null;
  }

  protected TopicSubscriber createTopicSubscriber(String destination) {
    Topic topic = null;
    TopicSubscriber subscriber = null;
    try {
      topic = (Topic) getCtx().lookup(destination);
    } catch (NamingException e) {
      throw new ConnectionException("Unable to resolve Topic for [" + destination + "]", e, this);
    }
    /*
    try {

      if (isDurable()) {
        subscriber = (((TopicSession) getSession()).createDurableSubscriber(topic, getDurableSubscriptionName(),
            getMessageSelector(), isNoLocal()));
      } else {
        subscriber = (((TopicSession) getSession()).createSubscriber(topic, getMessageSelector(), isNoLocal()));
      }

    } catch (JMSException e) {
      throw new ComponentException("Unable to subscribe for Topic [" + destination + "]", e, this);
    }
  */
    log.info("Consumer initialized for JMSDestination=" + topic);

    return subscriber;
  }

  protected QueueReceiver createQueueReceiver(String aDestination) {
    /*
    Queue queue;
    QueueReceiver aReceiver;
    QueueSession queueSession = (QueueSession) getSession();
    try {
      queue = (Queue) getCtx().lookup(aDestination);
    } catch (NamingException e) {
      throw new ComponentException("Unable to resolve Queue for [" + aDestination + "]", e, this);
    }
    // Create a Queue Receiver
    try {
      aReceiver = queueSession.createReceiver(queue, getMessageSelector());
    } catch (JMSException e) {
      throw new ComponentException("Exception creating JMS QueueReceiver ", e, this);
    }
    return aReceiver;
    */
    return null;
  }

  // End Consumer Support

  // Producer Support

  protected MessageProducer createMessageProducer(String subject) {
    /*
    MessageProducer newProducer;
    if (isTopic) {
        newProducer = createTopicPublisher(getDestinationName());
      } else {
        newProducer = createQueueSender(getDestinationName());
      }
    return newProducer;
    */
    return null;
  }

  protected MessageProducer createTopicPublisher(String subject) {
    /*
    Topic topic = null;
    TopicPublisher aPublisher;
    TopicSession topicSession = (TopicSession) getSession();
    try {
      topic = (Topic) getCtx().lookup(subject);
    } catch (NamingException e) {
      throw new ComponentException("Unable to resolve Topic for [" + subject + "]", e, this);
    }
    // Create a message producer (TopicPublisher)
    try {
      aPublisher = topicSession.createPublisher((Topic) topic);
    } catch (JMSException e) {
      throw new ComponentException("Exception creating JMS Producer ", e, this);
    }

    log.info(" Producer initialised for JMSDestination=" + topic);
    return aPublisher;
    */
    return null;
  }

  protected QueueSender createQueueSender(String aDestination) {
    /*
    Queue queue;
    QueueSender aSender;
    QueueSession queueSession = (QueueSession) getSession();
    try {
      queue = (Queue) getCtx().lookup(aDestination);
    } catch (NamingException e) {
      throw new ComponentException("Unable to resolve Queue for [" + aDestination + "]", e, this);
    }
    // Create a Queue Sender
    try {
      aSender = queueSession.createSender(queue);
    } catch (JMSException e) {
      throw new ComponentException("Exception creating JMS QueueSender ", e, this);
    }
    return aSender;
    */
    return null;
  }

  // End Producer Support

}
