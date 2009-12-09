package com.tibco.tibrv;

import org.openadaptor.StubException;

public abstract class TibrvTransport {
  public void send(TibrvMsg msg) throws TibrvException {
    throw new StubException(StubException.WARN_TIBCO_JAR);
  }
  public abstract void destroy();
}
