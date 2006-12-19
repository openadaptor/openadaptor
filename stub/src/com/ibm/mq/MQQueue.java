package com.ibm.mq;
public class MQQueue {

  public synchronized void close() throws com.ibm.mq.MQException {
    throw new RuntimeException("this is stub code, you need the MQ jar in your classpath");
  }

  public synchronized void put(com.ibm.mq.MQMessage mqMessage, com.ibm.mq.MQPutMessageOptions mqPutMessageOptions)
      throws com.ibm.mq.MQException {
    throw new RuntimeException("this is stub code, you need the MQ jar in your classpath");
  }

  public synchronized void get(com.ibm.mq.MQMessage mqMessage, com.ibm.mq.MQGetMessageOptions mqGetMessageOptions)
      throws com.ibm.mq.MQException {
    throw new RuntimeException("this is stub code, you need the MQ jar in your classpath");
  }
}