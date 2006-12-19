package com.ibm.mq;
public class MQException extends Exception {
  
  public static final int MQRC_CONNECTION_BROKEN = 2009;
  public static final int MQRC_NO_MSG_AVAILABLE = 2033;
  
  public int reasonCode;
  
  private static final long serialVersionUID = 1L;

  public MQException() {
    throw new RuntimeException( "this is stub code, you need the MQ jar in your classpath" );
  }

  public MQException(String message, Throwable cause) {
    throw new RuntimeException( "this is stub code, you need the MQ jar in your classpath" );
  }

  public MQException(String message) {
    throw new RuntimeException( "this is stub code, you need the MQ jar in your classpath" );
  }

  public MQException(Throwable cause) {
    throw new RuntimeException( "this is stub code, you need the MQ jar in your classpath" );
  }   
}
