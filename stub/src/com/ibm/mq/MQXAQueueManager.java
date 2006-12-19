package com.ibm.mq;
import javax.transaction.xa.XAResource;

public class MQXAQueueManager {

  public MQXAQueueManager(String queueName) throws com.ibm.mq.MQException {
    throw new RuntimeException("this is stub code, you need the MQ jar in your classpath");
  }

  public XAResource getXAResource() throws javax.transaction.xa.XAException {
    throw new RuntimeException("this is stub code, you need the MQ jar in your classpath");
  }

  public com.ibm.mq.MQQueueManager getQueueManager() {
    throw new RuntimeException("this is stub code, you need the MQ jar in your classpath");
  }
}