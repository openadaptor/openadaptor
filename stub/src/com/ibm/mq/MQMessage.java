package com.ibm.mq;

import org.openadaptor.StubException;

public class MQMessage extends MQMD {
  
  public java.lang.String replyToQueueName;
  public java.lang.String replyToQueueManagerName;

  public java.lang.String readString(int i) throws java.io.IOException, java.io.EOFException {
    throw new StubException(StubException.WARN_MQ_JAR);
  }
  
  public void writeString(java.lang.String string) throws java.io.IOException {
    throw new StubException(StubException.WARN_MQ_JAR);
  }
  
  public int getDataLength() throws java.io.IOException {
    throw new StubException(StubException.WARN_MQ_JAR);
  }
  
  public void clearMessage() throws java.io.IOException {
    throw new StubException(StubException.WARN_MQ_JAR);
  }
  
  public int getTotalMessageLength() {
    throw new StubException(StubException.WARN_MQ_JAR);
 }
  
  public int getMessageLength() throws java.io.IOException {
    throw new StubException(StubException.WARN_MQ_JAR);
  }

  public String readStringOfByteLength(int messageLength) {
    throw new StubException(StubException.WARN_MQ_JAR);
  }
}