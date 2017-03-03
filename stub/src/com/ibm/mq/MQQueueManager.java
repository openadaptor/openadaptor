package com.ibm.mq;

import org.openadaptor.StubException;

/*
 * File: $Header$
 * Rev:  $Revision$
 */
public class MQQueueManager {

  public MQQueueManager(String name) {
    throw new StubException(StubException.WARN_MQ_JAR);
  }

  public synchronized void commit() throws com.ibm.mq.MQException {
    throw new StubException(StubException.WARN_MQ_JAR);
  }

  public synchronized void backout() throws com.ibm.mq.MQException {
    throw new StubException(StubException.WARN_MQ_JAR);
  }

  public synchronized com.ibm.mq.MQQueue accessQueue(java.lang.String string, int i) throws com.ibm.mq.MQException {
    throw new StubException(StubException.WARN_MQ_JAR);
  }

  public synchronized com.ibm.mq.MQQueue accessQueue(java.lang.String string, int i, java.lang.String string1,
      java.lang.String string2, java.lang.String string3) throws com.ibm.mq.MQException {
    throw new StubException(StubException.WARN_MQ_JAR);
  }
}