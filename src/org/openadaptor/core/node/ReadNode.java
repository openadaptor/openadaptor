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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.*;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.lifecycle.IRunnable;
import org.openadaptor.core.lifecycle.State;
import org.openadaptor.core.router.Router;
import org.openadaptor.core.transaction.ITransaction;
import org.openadaptor.core.transaction.ITransactionInitiator;
import org.openadaptor.core.transaction.ITransactionManager;
import org.openadaptor.core.transaction.ITransactional;

import java.util.List;

/**
 * This class should be used to "wrap" an {@link IReadConnector}. It handles
 * the following
 * <li>propogates lifecycle management to {@link IReadConnector}
 * <li>polls {@link IReadConnector} for data, creates new {@link Message} and
 * delegates to configured {@link IMessageProcessor}
 * <li>Transaction creation, commit / exception
 * 
 * <br/><br/>This represents the "inpoint" of an Adaptor. Typically an
 * {@link Adaptor} is managing the lifecycle of a ReadNode, it sets the
 * TransactionManager and sets itself as the MessageProcessor.
 * 
 * <br/>By virtue of it's subclass, this class can also have an
 * {@link IDataProcessor} configured, if this the case then data received from
 * the {@link IReadConnector} is processed by the {@link IDataProcessor} before
 * being delegated. This by-passes any exception / discard management that can
 * be configured in delegates such as {@link Router}.
 * 
 * @author perryj
 * @see IReadConnector
 * @see Adaptor
 */
public class ReadNode extends Node implements IRunnable, ITransactionInitiator {

  private static final Log log = LogFactory.getLog(ReadNode.class);

  private IReadConnector connector;

  private long timeoutMs = 1000;

  private int exitCode = 0;
  
  private Throwable exitThrowable = null;

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

  public void setTransactionManager(final ITransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  public ITransactionManager getTransactionManager() {
    return transactionManager;
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
    } catch (Exception e) {
      log.debug("disconnect failed, " + e.getMessage());
    } 
  }

  public void run() {
    if (!isState(State.STARTED)) {
      log.warn(getId() + " has not been started");
      exitCode = 0;
    }
    ITransaction transaction = null;
    try {
      log.info(getId() + " running");
      while (isState(State.STARTED)) {
        if ((transaction == null) && (getTransactionManager() != null)) {
          transaction = getTransactionManager().getTransaction();
        }
        Response response = process(new Message(new Object[]{}, null, transaction));
        log.debug("Response is: " + response);
        if (transaction != null) {
          if (transaction.getErrorOrException() == null) {
            log.debug(getId() + " committing transaction");
            transaction.commit();
          } else {
            log.info(getId() + " rolling back transaction");
            transaction.rollback();
          }
          transaction = null;
        }
      }
    }
    catch (Throwable e) {
      exitCode = 1;
      exitThrowable = e;
      log.error(getId() + " uncaught exception, rolling back transaction and stopping", e);
      if (transaction != null) {
        transaction.setErrorOrException(e);
        transaction.rollback();
        transaction = null;
      }
      stop();
    }
    finally {
      log.info(getId() + " no longer running");
      disconnectNoThrow();
      super.stop();
    }
  }

  public void stop() {
    if (isState(State.STARTED)) {
      log.info(getId() + " is stopping");
      setState(State.STOPPING);
    }
  }

  public Response process(Message msg) {
    Response response = new Response();
    if (connector.isDry()) {
      stop();
      return response;
    }
    if (msg.getTransaction() != null) {
      enlistConnector(msg.getTransaction());
    }
    Object[] data = getNext();
    if (data != null && data.length != 0) {
      if (connector.getReaderContext() == prevReaderContext) {
        resetProcessor(connector.getReaderContext());
        prevReaderContext = connector.getReaderContext();
      }
      Message newMessage = new Message(data, this, msg.getTransaction());
      response = super.process(newMessage);
    } else {
      // Ideally we should still process through the IDataProcessor.
      // Unfortunately this does not match previously existing behaviour.
      return super.process(msg);
    }
    return response;
  }

  private Object[] getNext() {
    return connector.next(timeoutMs);
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

  public Throwable getExitError() {
    return exitThrowable;
  }

}
