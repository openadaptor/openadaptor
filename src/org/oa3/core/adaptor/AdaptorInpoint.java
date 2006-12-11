/*
 * [[
 * Copyright (C) 2001 - 2006 The Software Conservancy as Trustee. All rights
 * reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Nothing in this notice shall be deemed to grant any rights to
 * trademarks, copyrights, patents, trade secrets or any other intellectual
 * property of the licensor or any contributor except as expressly stated
 * herein. No patent license is granted separate from the Software, for
 * code that you delete from the Software, or for combinations of the
 * Software with other software or hardware.
 * ]]
 */

package org.oa3.core.adaptor;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.core.IComponent;
import org.oa3.core.IReadConnector;
import org.oa3.core.Message;
import org.oa3.core.lifecycle.State;
import org.oa3.core.node.Node;
import org.oa3.core.transaction.ITransaction;
import org.oa3.core.transaction.ITransactionManager;
import org.oa3.core.transaction.ITransactional;

public final class AdaptorInpoint extends Node implements IAdaptorInpoint {

  private static final Log log = LogFactory.getLog(AdaptorInpoint.class);

  private IReadConnector connector;

  private boolean enabled = true;

  private long timeoutMs = 1000;

  private int exitCode = 0;

  private ITransactionManager transactionManager;

  private Object prevReaderContext;

  public AdaptorInpoint() {
    super();
  }

  public AdaptorInpoint(String id) {
    super(id);
  }

  public AdaptorInpoint(final String id, final IReadConnector connector) {
    this(id);
    this.connector = connector;
  }

  public void setConnector(final IReadConnector connector) {
    this.connector = connector;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public void setTimeoutMs(long timeout) {
    this.timeoutMs = timeout;
  }

  public void setTransactionManager(final ITransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  public void validate(List exceptions) {
    super.validate(exceptions);
    if (connector == null) {
      exceptions.add(new RuntimeException(toString() + " does not have a connector"));
    }
  }

  public void start() {
    if (enabled) {
      connector.connect();
      super.start();
    } else {
      log.info(getId() + " is not enabled");
    }
  }

  public void run() {
    if (!isState(State.RUNNING)) {
      log.warn(getId() + " has not been started");
      exitCode = 0;
    }
    try {
      log.info(getId() + " running");
      while (isState(State.RUNNING) && !connector.isDry()) {
        ITransaction transaction = null;
        try {
          transaction = transactionManager.getTransaction();
          enlistConnector(transaction);
          getDataAndProcess(transaction);
          log.debug(getId() + " committing transaction");
          transaction.commit();
        } catch (Throwable e) {
          log.error(getId() + " stopping, uncaught exception", e);
          exitCode = 1;
          stop();
          log.info(" rolling back transaction");
          transaction.rollback();
        }
      }
    } finally {
      log.info(getId() + " no longer running");
      stop();
      connector.disconnect();
    }
  }

  /**
   * extracted so that frameworks can plug in their own transaction management
   * @param transaction
   */
  public void getDataAndProcess(ITransaction transaction) {
    Object[] data = connector.next(timeoutMs);
    if (data != null) {
      if (connector.getReaderContext() == prevReaderContext) {
        resetProcessor(connector.getReaderContext());
        prevReaderContext = connector.getReaderContext();
      }
      Message msg = new Message(data, this, transaction);
      process(msg);
    }
  }

  public void setAdaptor(Adaptor adaptor) {
    setNext(adaptor);
    if (transactionManager == null) {
      transactionManager = adaptor.getTransactionManager();
    }
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
      return ((IComponent)connector).getId();
    }
    return id;
  }
  
  public String toString() {
    return getId();
  }
}
