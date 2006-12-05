package org.oa3.jms;

import javax.jms.JMSException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.core.transaction.ITransactionalResource;

public class JMSTransactionalResource implements ITransactionalResource {

  private static final Log log = LogFactory.getLog(JMSTransactionalResource.class);

  private JMSConnection connection;

  public JMSTransactionalResource(final JMSConnection connection) {
    this.connection = connection;
  }

  public void begin() {
    try {
      if (!connection.getSession().getTransacted()) {
        throw new RuntimeException("Attempt to start transaction using an untransacted JMS Session");
      }
    } catch (JMSException e) {
      throw new RuntimeException("JMS Exception on attempt to start transaction using a JMS Session", e);
    }
    log.debug("JMSTransaction begun");
  }

  public void commit() {
    try {
      if (!connection.getSession().getTransacted()) {
        throw new RuntimeException("Attempt to commit a transaction using an untransacted JMS Session");
      } else {
        connection.getSession().commit();
        log.debug("JMSTransaction committed");
      }
    } catch (JMSException e) {
      throw new RuntimeException("JMS Exception on attempt to commit a transaction using a JMS Session", e);
    }
  }

  public void rollback() {
    try {
      if (!connection.getSession().getTransacted()) {
        throw new RuntimeException("Attempt to rollback a transaction using an untransacted JMS Session");
      } else {
        log.debug("JMSTransaction rolled back");
        connection.getSession().rollback();
      }
    } catch (JMSException e) {
      throw new RuntimeException("JMS Exception on attempt to rollback a transaction using a JMS Session", e);
    }
  }

}
