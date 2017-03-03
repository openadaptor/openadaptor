package com.ibm.mq;

import org.openadaptor.StubException;

public class MQQueue {

  public synchronized void close() throws com.ibm.mq.MQException {
    throw new StubException(StubException.WARN_MQ_JAR);
  }

  public synchronized void put(com.ibm.mq.MQMessage mqMessage, com.ibm.mq.MQPutMessageOptions mqPutMessageOptions)
      throws com.ibm.mq.MQException {
    throw new StubException(StubException.WARN_MQ_JAR);
  }

  public synchronized void get(com.ibm.mq.MQMessage mqMessage, com.ibm.mq.MQGetMessageOptions mqGetMessageOptions)
      throws com.ibm.mq.MQException {
    throw new StubException(StubException.WARN_MQ_JAR);
  }
}