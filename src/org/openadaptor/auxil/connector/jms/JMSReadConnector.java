/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

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
import org.openadaptor.core.IMetadataAware;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.OAException;
import org.openadaptor.core.exception.ProcessingException;
import org.openadaptor.core.exception.RecordFormatException;
import org.openadaptor.core.transaction.ITransactional;

import javax.jms.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Read Connector class that implements listening to JMS.
 * <p>
 * Manages a single JMS Session and MessageConsumer.
 * <p>
 * Delegates to <code>JMSConnector</code> to get the actual Connection with JMS.
 * <p/>
 * Main Properties
 * <ul>
 * <li><b>destinationName</b>           Name used to look up destination (queue/topic) in JNDI
 * <li><b>destination</b>               Actual JMS Destination Object. If set overrides destinationName.
 * <li><b>acknowledgeMode</b>           Defaults to <code>Session.AUTO_ACKNOWLEDGE</code>
 * <li><b>transacted</b>                False by default. If true then a TransactionalResource is acquired as long as either the JMS Session is transacted or XA resources are available.
 * <li><b>durable</b>                   Defaults to <i>false</i>. If <i>true</i> create a durable topic subscription.
 * <li><b>durableSubscriptionName</b>   Defaults to the <code>destinationName</code>. The name used to create the durable topic subscription.
 * <li><b>messageSelector</b>           Not set by default.
 * <li><b>noLocal</b>                   Default is <i>false</i>
 * <li><b>messageConvertor</b>          The IMessageConvertor instance used to extracts the message data from JMSMessage instances. Defaults to an instance of <code>DefaultMessageConvertor</code>.
 * <li><b>populateMetadataFromProperties</b>    Default <i>false</i>. If true then copy any populated JMS Message Properties into the message metadata.

 * </ul>
 * <p/>
 * @see JMSConnection
 * @author OA3 Core Team
 */
public class JMSReadConnector extends Component implements ExceptionListener, IReadConnector, ITransactional, IMetadataAware {

  private static final Log log = LogFactory.getLog(JMSReadConnector.class);

  private JMSConnection jmsConnection;

  /**
   * True if expected to make a transacted connection to jms. NB this is used for local rather than xa transactions.
   */
  private boolean isTransacted = false;

  /**
   * JMS acknowledge mode to use. Defaults to <code>Session.AUTO_ACKNOWLEDGE</code>
   */
  private int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
  private Session session;
  /**
   * Name used to look up destination (queue/topic) in JNDI.
   */
  private String destinationName = null;
  /**
   * True for setting up durable subscriptions to topics.
   */
  private boolean durable = false;
  /**
   * The name used to create the durable topic subscription. Defaults to the <code>destinationName</code>.
   */
  private String durableSubscriptionName = null;
  /**
   * Message Selector to use when receiving messages. Not set by default.
   */
  private String messageSelector = null;
  /**
   * Default is <i>false</i>
   */
  private boolean noLocal = false;
  private MessageConsumer messageConsumer;
  /**
   * transactional resource, can either be XAResource or ITransactionalResource
   */
  private Object transactionalResource;

  private Exception listenerException;

  /**
   * Default is true which means log JMS Message Id of any published messages.
   */
  private boolean logMessageId = true;

  private IMessageConvertor messageConvertor = new DefaultMessageConvertor();

  private Destination destination;
  
  /**
   * If true then the metadata will be populated with any jms message properties.
   */
  private boolean populateMetadataFromProperties = false;

  private Map metadata;

  // Constructors

  public JMSReadConnector() {}

  public JMSReadConnector(String id) {
    super(id);
  }

  // IReadConnector implementation

  public void connect() {
    if (!isConnected()) {
      session = jmsConnection.createSessionFor(this);
      messageConsumer = createMessageConsumerFor(session);
      transactionalResource = createTransactionalResource(session);
      jmsConnection.installAsExceptionListener(this);
    }
  }

  public void disconnect() {
    if (isConnected()) {
      try {
        messageConsumer.close();
        session.close();
      } catch (JMSException e) {
        throw new ConnectionException("Exception closing JMSReadConnector.", e, this);
      }
      finally{
        messageConsumer = null;
        session = null;
        jmsConnection.disconnectFor(this);
      }
    }
  }

  public void validate(List exceptions) {
    if((getDestinationName() == null) && (getDestination() == null))  {
      exceptions.add( new ConnectionException("Either destinationName or destination must be set.", this));
    }
    if(jmsConnection == null) {
      exceptions.add(new ConnectionException("Property jmsConnection is mandatory", this));
    }
    else {
      jmsConnection.validate(exceptions);
      if((destinationName != null) && (jmsConnection.getJndiConnection() == null)) {
        exceptions.add(new ConnectionException("If destinationName is set then jmsConnection must have jndiConnection set.", this));  
      }
    }
  }

  public boolean isConnected() {
    return (session != null);
  }

  public Object[] next(long timeoutMs) throws OAException {
    if (!isConnected()) throw new ConnectionException("Attempt to read from disconnected JMSReadConnector", this);
    Object data = receive(timeoutMs);
    if (data != null) {
      log.debug(getId() + " got jms message");
    }
    return data != null ? new Object[] {data} : null;
  }

  public boolean isDry() {
    return false;
  }

  public Object getResource() {
    return transactionalResource;
  }

  public Object getReaderContext() {
    return null;
  }
  
  public void setReaderContext(Object context) {
  }

  // ExceptionListener implementation

  public void onException(JMSException jmsException) {
    listenerException = jmsException;
    log.error("Exception Callback triggered. Exception: " + jmsException);
  }

  // Support methods

  /**
   * Receive a message from the configured destination. This is a blocking receive which will time out based
   * on the value of the <b>timeout</b> property.
   *
   * @return Object  The contents of the received message.
   */
  protected Object receive(long timeoutMs) {
    Message msg;
    try {
      if (timeoutMs < 0) {
        msg = messageConsumer.receive();
      } else {
        msg = messageConsumer.receive(timeoutMs);
        //Populate Metadata if this is configured
        if (msg != null && isPopulateMetadataFromProperties()){
          readMetadataFromMessageProperties(msg);
        }
      }
      if ((msg != null) && (logMessageId)) {
        log.info("[" + getId() + "=" + getDestinationName() + "] received message [JMSMessageID=" + msg.getJMSMessageID() + "]");
      }
      // If we have been sent a JMSException via the ExceptionListener registration mechanism.
      // We treat the exception as if it had been thrown by the receive method by throwing it now.
      if (listenerException != null) {
        ConnectionException ce = new ConnectionException("onException called during receive.", listenerException, this);
        listenerException = null;
        throw ce;
      }
    } catch (JMSException jmse) {
      log.error("Exception during receive message [JMSException: " + jmse + "]");
      throw new ConnectionException("Exception during receive message.", jmse, this);
    }
    // Unpack the message contents
    return unpackJMSMessage(msg);
  }

  /** Unpack the message contents */
  protected Object unpackJMSMessage(Message msg) {
    Object msgContents = null;
    if (msg != null) {
      try {
        msgContents = messageConvertor.unpackMessage(msg);
      } catch (JMSException e) {
        throw new ConnectionException("Error processing JMS Message text.", e, this);
      }
      catch (RecordFormatException e) {
        throw new ProcessingException(e.getMessage(), this);
      }
    }
    return msgContents;
  }
  
  /**
   * Copy any JMS Message Properties into the metadata.
   * 
   * @param msg
   * @throws JMSException
   */
  protected void readMetadataFromMessageProperties(Message msg) throws JMSException {
    Map addToMetadata = new HashMap();
    
    Enumeration names = msg.getPropertyNames();
    while (names.hasMoreElements()) {
      String nextName = (String)names.nextElement();
      addToMetadata.put(nextName, msg.getObjectProperty(nextName));
    }
    metadata.putAll(addToMetadata);
  }

  protected MessageConsumer createMessageConsumerFor(Session connectorSession) {
    if (messageConsumer == null) {
      MessageConsumer newConsumer;
      if (destination == null) {
        destination = jmsConnection.lookupDestination(getDestinationName());
      }
      try {
        if (durable) {
          newConsumer = connectorSession.createDurableSubscriber((Topic) destination, getDurableSubscriptionName(), getMessageSelector(), isNoLocal());
        } else {
          if (isNoLocal()) {  // The value of noLocal only seems to matter if it's true.
            newConsumer = connectorSession.createConsumer(destination, getMessageSelector(), isNoLocal());
          } else {
            newConsumer = connectorSession.createConsumer(destination, getMessageSelector());
          }
        }
      } catch (JMSException e) {
        throw new ConnectionException("Unable to subscribe to Destination: [" + getDestinationName() + "]", e, this);
      }
      messageConsumer = newConsumer;
    }
    return messageConsumer;
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
      log.info("JMSReadConnector ["+getId()+"] not transacted. Not creating transaction resource.");
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

  // End  Support methods

  // Bean implementation


  public void setJmsConnection(JMSConnection jmsConnection) {
    this.jmsConnection = jmsConnection;
  }

  public boolean isTransacted() {
    return isTransacted;
  }

  public void setTransacted(boolean transacted) {
    isTransacted = transacted;
  }

  public int getAcknowledgeMode() {
    return acknowledgeMode;
  }

  public void setAcknowledgeMode(int acknowledgeMode) {
    this.acknowledgeMode = acknowledgeMode;
  }

  public String getDestinationName() {
    return destinationName;
  }

  public void setDestinationName(String destinationName) {
    this.destinationName = destinationName;
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

  public void setDurable(boolean durable) {
    this.durable = durable;
  }

  public void setDurableSubscriptionName(String durableSubscriptionName) {
    this.durableSubscriptionName = durableSubscriptionName;
  }

  protected String getMessageSelector() {
    return messageSelector;
  }

  public void setMessageSelector(String messageSelector) {
    this.messageSelector = messageSelector;
  }

  protected boolean isNoLocal() {
    return noLocal;
  }

  public void setNoLocal(boolean noLocal) {
    this.noLocal = noLocal;
  }

  /**
   * If true log the JMS Message Id of received messages.
   * @return boolean Default is true
   */
  public boolean isLogMessageId() {
    return logMessageId;
  }

  /**
   * If true log the JMS Message Id of received messages. Defaults to true.
   * @param logMessageId
   */
  public void setLogMessageId(boolean logMessageId) {
    this.logMessageId = logMessageId;
  }

  /** Get the IMessageConvertor used to convert the JMS Message to a record */
  public IMessageConvertor getMessageConvertor() {
    return messageConvertor;
  }

  /** Set the IMessageConvertor used to convert the JMS Message to a record */
  public void setMessageConvertor(IMessageConvertor messageConvertor) {
    this.messageConvertor = messageConvertor;
  }

  public Destination getDestination() {
    return destination;
  }

  public void setDestination(Destination destination) {
    this.destination = destination;
  }

  /**
   * If true then the metadata will be populated with any jms message properties.
   * 
   * @param populateMetadataFromProperties the populateMetadataFromProperties to set
   */
  public void setPopulateMetadataFromProperties(
      boolean populateMetadataFromProperties) {
    this.populateMetadataFromProperties = populateMetadataFromProperties;
  }

  /**
   * If true then the metadata will be populated with any jms message properties.
   * @return the populateMetadataFromProperties
   */
  public boolean isPopulateMetadataFromProperties() {
    return populateMetadataFromProperties;
  }

  public void setMetadata(Map metadata) {
    this.metadata = metadata;
    
  }

  // End Bean implementation

}


