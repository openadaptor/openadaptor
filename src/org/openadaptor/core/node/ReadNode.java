/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in the
 Software without restriction, including without limitation the rights to use, copy,
 modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 and to permit persons to whom the Software is furnished to do so, subject to the
 following conditions:

 The above copyright notice and this permission notice shall be included in all 
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Nothing in this notice shall be deemed to grant any rights to trademarks, copyrights,
 patents, trade secrets or any other intellectual property of the licensor or any
 contributor except as expressly stated herein. No patent license is granted separate
 from the Software, for code that you delete from the Software, or for combinations
 of the Software with other software or hardware.
*/

package org.openadaptor.core.node;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IComponent;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.Message;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.lifecycle.IRunnable;
import org.openadaptor.core.lifecycle.State;
import org.openadaptor.core.transaction.ITransaction;
import org.openadaptor.core.transaction.ITransactionInitiator;
import org.openadaptor.core.transaction.ITransactionManager;
import org.openadaptor.core.transaction.ITransactional;

public class ReadNode extends Node implements IRunnable, ITransactionInitiator {

  private static final Log log = LogFactory.getLog(ReadNode.class);

  private IReadConnector connector;

  private long timeoutMs = 1000;

  private int exitCode = 0;

  private ITransactionManager transactionManager;

  private Object prevReaderContext;
  
  public ReadNode() {
    super();
  }

  public ReadNode(String id) {
    super(id);
  }

  public ReadNode(final String id, final IReadConnector connector) {
    this(id);
    this.connector = connector;
  }

  public void setConnector(final IReadConnector connector) {
    this.connector = connector;
  }

  protected IReadConnector getConnector() {
    return connector;
  }

  public void setTimeoutMs(long timeout) {
    this.timeoutMs = timeout;
  }

  /**
   * if set then this component will call stop on the adaptor
   * if it terminates unexpectedly
   */
//  public void setStopAdaptorOnError(boolean stopAdaptor) {
//    this.stopAdaptorOnError = stopAdaptor;
//  }
//  
  public void setTransactionManager(final ITransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  public void validate(List exceptions) {
    super.validate(exceptions);
    if (connector == null) {
      exceptions.add(new RuntimeException(toString() + " does not have a connector"));
    } else {
      connector.validate(exceptions);
    }
  }

  public void start() {
    try {
      connector.connect();
    } catch (RuntimeException ex) {
      log.error(getId() + " failed to connect", ex);
      disconnectNoThrow();
      throw new ConnectionException("failed to connect", ex, this);
    }
    super.start();
  }

  private void disconnectNoThrow() {
    try {
      connector.disconnect();
    } catch (Exception ex) {
    }
  }

  public void run() {
    if (!isState(State.STARTED)) {
      log.warn(getId() + " has not been started");
      exitCode = 0;
    }
    try {
      log.info(getId() + " running");
      ITransaction transaction = null;
      while (isState(State.STARTED) && !connector.isDry()) {
        try {
          if (transaction == null) {
            transaction = transactionManager.getTransaction();
            enlistConnector(transaction);
          }
          if (getNextAndProcess(transaction)) {
            if (transaction.getErrorOrException() == null) {
              log.debug(getId() + " committing transaction");
              transaction.commit();
            } else {
              log.info(getId() + " rolling back transaction");
              transaction.rollback();
            }
            transaction = null;
          }
        } catch (Throwable e) {
          transaction.setErrorOrException(e);
          exitCode = 1;
          log.error(getId() + " uncaught exception, rolling back transaction and stopping", e);
          transaction.rollback();
          transaction = null;
          stop();
//          if (stopAdaptorOnError && adaptor != null) {
//            adaptor.stopNoWait();
//          }
        }
      }
    } finally {
      log.info(getId() + " no longer running");
      connector.disconnect();
      super.stop();
    }
  }

  public void stop() {
    if (isState(State.STARTED)) {
      log.info(getId() + " is stopping");
      setState(State.STOPPING);
    }
  }

  private boolean getNextAndProcess(ITransaction transaction) {
    Object[] data = getNext();
    if (data != null) {
      if (connector.getReaderContext() == prevReaderContext) {
        resetProcessor(connector.getReaderContext());
        prevReaderContext = connector.getReaderContext();
      }
      Message msg = new Message(data, this, transaction);
      process(msg);
    }
    return data != null;
  }

  private Object[] getNext() {
    Object[] data = connector.next(timeoutMs);
    return data;
  }

  public int getExitCode() {
    return exitCode;
  }

  private void enlistConnector(ITransaction transaction) {
    if (connector instanceof ITransactional) {
      Object resource = ((ITransactional) connector).getResource();
      if (resource != null) {
        log.debug(getId() + " enlisting in transaction");
        transaction.enlist(resource);
      }
    }
  }

  public String getId() {
    String id = super.getId();
    if (id == null && connector instanceof IComponent) {
      return ((IComponent) connector).getId();
    }
    return id;
  }

  public String toString() {
    return getId();
  }

}
