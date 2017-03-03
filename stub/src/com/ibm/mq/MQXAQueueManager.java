package com.ibm.mq;
import javax.transaction.xa.XAResource;

import org.openadaptor.StubException;

public class MQXAQueueManager {

  public MQXAQueueManager(String queueName) throws com.ibm.mq.MQException {
    throw new StubException(StubException.WARN_MQ_JAR);
  }

  public XAResource getXAResource() throws javax.transaction.xa.XAException {
    throw new StubException(StubException.WARN_MQ_JAR);
  }

  public com.ibm.mq.MQQueueManager getQueueManager() {
    throw new StubException(StubException.WARN_MQ_JAR);
  }
}