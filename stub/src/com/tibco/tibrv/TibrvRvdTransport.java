package com.tibco.tibrv;

import org.openadaptor.StubException;

public class TibrvRvdTransport extends TibrvTransport {

  public TibrvRvdTransport(String service, String network, String daemon) throws TibrvException {
    throw new StubException(StubException.WARN_TIBCO_JAR);
  }

  public void send(TibrvMsg msg) {
    throw new StubException(StubException.WARN_TIBCO_JAR);
  }
  
  public void destroy() {
    throw new StubException(StubException.WARN_TIBCO_JAR);
  }

}
