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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.IWriteConnector;
import org.openadaptor.core.exception.*;
import org.openadaptor.core.transaction.ITransactional;

import javax.jms.*;

import java.util.List;

/**
 * Write Connector class that implements publishing to JMS.
 * <p>
 * Manages a single JMS Session and MessageProducer.
 * <p>
 * Delegates managing the actual jms connection to
 * an instance of <code>JMSConnector</code>
 *
 * <p/>
 * Main Properties
 * <ul>
 * <li><b>destinationName</b>           Name used to look up destination (queue/topic) in JNDI
 * <li><b>acknowledgeMode</b>           Defaults to <code>Session.AUTO_ACKNOWLEDGE</code>
 * <li><b>transacted</b>                False by default. If true then a TransactionalResource is acquired as long as either the JMS Session is transacted or XA resources are available.
 * <li><b>deliveryMode</b>              Set the delivery mode for the message messageProducer (used for publishing). Defaults to <code>Message.DEFAULT_DELIVERY_MODE</code>.
 * <li><b>priority</b>                  Set the priority for the message messageProducer (used for publishing). Defaults to <code>Message.DEFAULT_PRIORITY</code>.
 * <li><b>timeToLive</b>                Set the time to live for messages published by the message messageProducer. Defaults to <code>Message.DEFAULT_TIME_TO_LIVE</code>.
 * <li><b>messageGenerator</b>          The IMessageGenerator instance used to generate JMSMessage instances. Defaults to an instance of <code>DefaultMessageGenerator</code>.
 * </ul>
 * <p/>
 * @author OA3 Core Team
 */
public class JMSWriteConnector extends Component implements IWriteConnector, ITransactional {

  private static final Log log = LogFactory.getLog(JMSWriteConnector.class);

  /**
   * True if expected to make a transacted connection to jms. NB this is used for local rather than xa transactions.
   */
  private boolean isTransacted = false;
  /**
   * JMS acknowledge mode to use. Defaults to <code>Session.AUTO_ACKNOWLEDGE</code>
   */
  private int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
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
   * JMS Connection component that does all the real work.
   */
  private JMSConnection jmsConnection;
  private Session session;
  /**
   * transactional resource, can either be XAResource or ITransactionalResource
   */
  private Object transactionalResource;
  private MessageProducer messageProducer;
  /**
   * Name used to look up destination (queue/topic) in JNDI.
   */
  private String destinationName;

  /**
   * Default is true which means log JMS Message Id of any published messages.
   */
  private boolean logMessageId = true;

  /**
   * Use to generate instances of JMSMessage. Defaults to an instance of DefaultMessageGenerator.
   */
  private IMessageGenerator messageGenerator = new DefaultMessageGenerator();

  private Destination destination;

  // Constructors

  public JMSWriteConnector() {}

  public JMSWriteConnector(String id) {
    super(id);
  }

  // Connector Interface

  /**
   * Deliver a batch of records to the external message transport.
   *
   * @param records -
   *                an Array of records to be processed.
   * @return result information if any. May well be null.
   * @throws OAException
   */
  public Object deliver(Object[] records) throws OAException {
    if (!isConnected())
      throw new ConnectionException("Attempting to deliver via a disconnected Connector", this);
    if (isConnected()) {
      int size = records.length;
      // ToDo: Get OA team to review the way the unbatching is done.
      String[] msgIds = new String[size];

      for (int i = 0; i < size; i++) { // Un-batch the individual records.
        Object record = records[i];
        // log.info(getLoggingId() + " sending [" + record + "]");
        if (record != null) // todo Really necessary?
          msgIds[i] = deliverRecord(record);
        else
          throw new ProcessingException("Cannot deliver null record.", this);
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
      session = jmsConnection.createSessionFor(this);
      
      //destination may not be known until after a message is received for a request/reply
      if(destination!=null || destinationName!=null)
    	  messageProducer = createMessageProducer();
      transactionalResource = createTransactionalResource(session);
    }
  }

  /**
   * Disconnect from the external message transport. If already disconnected then do nothing.
   */
  public void disconnect() {
    if (isConnected()) {
      try {
        if(messageProducer!=null)
        	messageProducer.close();
        session.close();
      } catch (JMSException e) {
        throw new ConnectionException("Exception closing JMSReadConnector.", e, this);
      }
      finally {
        messageProducer = null;
        session = null;
        jmsConnection.disconnectFor(this);
      }
    }
  }

  public void validate(List exceptions) {
//    if ((getDestinationName() == null) && (getDestination() == null)) {
//      exceptions.add(new ValidationException("One of destinationName or destination must be set.", this));
//    }
    if (jmsConnection == null) {
      exceptions.add(new ValidationException("Property jmsConnection is mandatory", this));
    } else {
      jmsConnection.validate(exceptions);
    }
    if ((getDestinationName() != null) && (jmsConnection != null)) {  // Null jmsConnection is already a validation failure
      if(jmsConnection.getJndiConnection() == null) {
        exceptions.add(new ValidationException("I destinationName is set then jmsConnection MUST have jndiConnection set.", this));  
      }
    }
  }

  /**
   * True if connected.
   *
   * @return whether connected or not.
   */
  public boolean isConnected() {
//    return ((session != null) && (messageProducer != null));
    return (session != null);
  }

  // ITransactional implementation

  public Object getResource() {
    return transactionalResource;
  }

  // Support methods

  protected MessageProducer createMessageProducer() {
    MessageProducer newProducer;
    if (destination == null) {
      destination = jmsConnection.lookupDestination(destinationName);
    }
    try {
      newProducer = session.createProducer(destination);
    } catch (JMSException e) {
      throw new ConnectionException("Exception creating JMS Producer ", e, this);
    }
    log.info(" Producer initialised for JMS Destination=" + newProducer);
    return newProducer;
  }

  protected MessageProducer createMessageProducer(Message msg) {
	    MessageProducer newProducer;
	    Destination reply;
	    try {
			reply = msg.getJMSDestination();
		} catch (JMSException e) {
		      throw new ConnectionException("Exception getting destination from message ", e, this);
		}

		try {
	      newProducer = session.createProducer(reply);
	    } catch (JMSException e) {
	      throw new ConnectionException("Exception creating JMS Producer ", e, this);
	    }
	    log.info(" Producer initialised for JMS Destination=" + newProducer);
	    return newProducer;
	  }

  /**
   * Deliver the parameter to the confgured destination.
   *
   * @param message
   * @return String The JMS Message ID.
   */
  protected String deliverRecord(Object message) {
    String msgId;
    try {
      // Delegate message creation to an instance of IMessageGenerator.
      Message msg = messageGenerator.createMessage(message, session);
      // If a default destination has been supplied then set the message's
      // to match that. If a default has not been set then we rely on the
      // the message itself to supply one. Ideally this would be done in the
      // MessageGenerator.
      if (destination != null) {
        msg.setJMSDestination(destination);
      }
      // send the record
      if (log.isDebugEnabled())
        log.debug("JmsPublisher sending [" + message + "]");
      
      // May need to create producer from the destination specified in the msg
      if(messageProducer==null) {
    	  messageProducer = createMessageProducer(msg);
      } else if(messageProducer.getDestination()!=msg.getJMSDestination()) {
    	  messageProducer.close();
    	  messageProducer = createMessageProducer(msg);
      }
      messageProducer.send(msg, deliveryMode, priority, timeToLive);
      msgId = msg.getJMSMessageID();
      if (logMessageId) { // Optionally log the message id of the published message.
        log.info( "[" + getId() + "=" + getDestinationName() + "] sent message [ JMSMessageID=" + msgId + "] to data connection/service" );
      }
    } catch (RecordFormatException e) {
      throw new ProcessingException("RecordFormatException during publish.", e, this);
    } catch (InvalidDestinationException e) {
      throw new ConnectionException("InvalidDestinationException during publish.", e, this);
    } catch (JMSException jmse) {
      throw new ConnectionException("JMSException during publish.", jmse, this);
    }
    return msgId;
  }

  /**
   * Create a transactional resource for this connector.<br>
   * If transacted is true then a JMSTransactionalResource is returned if not using XA and the JMS Session is transacted. 
   * If transacted is true and using XA and a JMS XASession is available then return the XAResource.
   * If transacted is false then Null is returned. 
   * A Null resource is also returned in any other scenario.
   *
   * @param newSession
   * @return TransactionalResource, XAResource or null
   */
  private Object createTransactionalResource(Session newSession) {
    Object resource = null;
    if (isTransacted) {
      if (!jmsConnection.isUseXa() && isTransactedSession(newSession)) {
        log.info(this.getId()+" creating local transaction resource.");
        resource = new JMSTransactionalResource(newSession);
      } else if (jmsConnection.isUseXa() && newSession instanceof XASession) {
        log.info(this.getId()+" supplying XAResouce as transaction resource.");
        resource = ((XASession) newSession).getXAResource();
      }
    }
    else {
      log.info("JMSWriteConnector ["+getId()+"] not transacted. Not creating transaction resource.");
    }
    return resource;
  }


  /**
   * Test transacted status of the JMS Session object. Wrap any JMSExceptions in a ConnectionException.
   * @param aSession  Session to be tested.
   * @return          Transacted or not
   */
  private boolean isTransactedSession(Session aSession) {
    try {
      return aSession.getTransacted();
    } catch (JMSException e) {
      throw new ConnectionException("Error testing transacted state of JMS Session : [" + getDestinationName() + "]", e, this);
    }
  }

  // Bean Properties

  // bean implementation

  public boolean isTransacted() {
    return isTransacted;
  }

  public int getAcknowledgeMode() {
    return acknowledgeMode;
  }

  public Object getTransactionalResource() {
    return transactionalResource;
  }

  public String getDestinationName() {
    return destinationName;
  }

  /**
   * Set underlying <code>JMSConnection</code> component.
   *
   * @param connection The JMSConenction implementation.
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

  public void setTransacted(boolean transacted) {
    isTransacted = transacted;
  }

  public void setAcknowledgeMode(int acknowledgeMode) {
    this.acknowledgeMode = acknowledgeMode;
  }

  public void setDeliveryMode(int deliveryMode) {
    this.deliveryMode = deliveryMode;
  }

  protected int getDeliveryMode() {
    return deliveryMode;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  protected int getPriority() {
    return priority;
  }

  public void setTimeToLive(long timeToLive) {
    this.timeToLive = timeToLive;
  }

  protected long getTimeToLive() {
    return timeToLive;
  }

  public void setDestinationName(String destinationName) {
    this.destinationName = destinationName;
  }

  /**
   * If true log the JMS Message Id after publishing.
   * @return boolean Default is true
   */
  public boolean isLogMessageId() {
    return logMessageId;
  }

  /**
   * If true log the JMS Message Id after publishing. Defaults to true.
   * @param logMessageId
   */
  public void setLogMessageId(boolean logMessageId) {
    this.logMessageId = logMessageId;
  }
  // end bean implementation

  /** Return the IMessageGenerator instance used to generate JMSMessage instances. Defaults to an instance of DefaultMessageGenerator. */
  public IMessageGenerator getMessageGenerator() {
    return messageGenerator;
  }

  /** Set the IMessageGenerator instance used to generate JMSMessage instances. Defaults to an instance of DefaultMessageGenerator. */
  public void setMessageGenerator(IMessageGenerator messageGenerator) {
    this.messageGenerator = messageGenerator;
  }

  public Destination getDestination() {
    return destination;
  }

  public void setDestination(Destination destination) {
    this.destination = destination;
  }
}
