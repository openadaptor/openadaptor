package com.tibco.tibrv;

import org.openadaptor.StubException;

public class Tibrv {
  public static final int IMPL_NATIVE = 0;

  public static void open(int i) throws TibrvException {
    throw new StubException(StubException.WARN_TIBCO_JAR);
  }

  public static TibrvQueue defaultQueue() {
    throw new StubException(StubException.WARN_TIBCO_JAR);
  }
}
