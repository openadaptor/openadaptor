package com.tibco.tibrv;

import org.openadaptor.StubException;

public class TibrvMsg {

  public TibrvMsgField getField(String fieldName) throws TibrvException {
    throw new StubException(StubException.WARN_TIBCO_JAR);
  }

  public String getSendSubject() throws TibrvException {
    throw new StubException(StubException.WARN_TIBCO_JAR);
  }

  public void setSendSubject(String subject) throws TibrvException {
    throw new StubException(StubException.WARN_TIBCO_JAR);
  }

  public void update(String field_name, Object value) throws TibrvException {
    throw new StubException(StubException.WARN_TIBCO_JAR);
  }
  
  public int getNumFields() throws TibrvException {
    throw new StubException(StubException.WARN_TIBCO_JAR);
  }
  
  public TibrvMsgField getFieldByIndex(int index) throws TibrvException {
    throw new StubException(StubException.WARN_TIBCO_JAR);
  }

}
