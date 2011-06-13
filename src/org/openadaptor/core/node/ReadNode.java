/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.metrics.ComponentMetricsFactory;
import org.openadaptor.core.*;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.lifecycle.IRunnable;
import org.openadaptor.core.lifecycle.State;
import org.openadaptor.core.recordable.IComponentMetrics;
import org.openadaptor.core.router.Router;
import org.openadaptor.core.transaction.ITransaction;
import org.openadaptor.core.transaction.ITransactionInitiator;
import org.openadaptor.core.transaction.ITransactionManager;
import org.openadaptor.core.transaction.ITransactional;


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

  public static long DEFAULT_TIMEOUT_MS = 1000;
  
  private static final Log log = LogFactory.getLog(ReadNode.class);

  private IReadConnector connector;

  private long timeoutMs = DEFAULT_TIMEOUT_MS;

  private int exitCode = 0;
  
  private Throwable exitThrowable = null;

  private ITransactionManager transactionManager;

  private Object prevReaderContext;
  
  /** Metrics associated with this node. */
  private IComponentMetrics metrics = (IComponentMetrics) ComponentMetricsFactory.newReaderMetrics(this);
  
  public ReadNode() {
    this(null);
  }

  public ReadNode(String id) {
    super(id);
    /* Substitute Node's metrics with own */
    super.setMetricsEnabled(false);
    super.setMetrics(metrics);
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
      /* First reset the exitCode (see SC94 for details) */
      exitCode = 0;
      connector.connect();
    } catch (RuntimeException ex) {
      setExitError(ex);
      log.error(getId() + " failed to connect", ex);
      disconnectNoThrow();
      throw new ConnectionException("failed to connect", ex, this);
    }
    super.start();
  }

  /**
   * Hints to this ReadNode that it should start stopping. It is up to the ReadNode
   * itself to gracefully finish processing current messages and then do all the clean-up
   * necessary and stop.
   * See SC57.
   */
  public void stop() {
    log.info(getId() + " stopping invoked");
    if (isState(State.STARTED)) {
      log.info(getId() + " is stopping");
      setState(State.STOPPING);
    }
    super.stop();
  }

  private void stopAndDisconnect() {
    log.info(getId() + " stop invoked");
    if (isState(State.STARTED)) {
      stop();
    }
    log.info(getId() + " no longer running");
    disconnectNoThrow();
  }

  private void disconnectNoThrow() {
    try {
      connector.disconnect();
    } catch (Exception e) {
      setExitError(e);
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
      //while ((isState(State.STARTED)) && !connector.isDry() ) {
      while (isState(State.STARTED)) {
        if (getTransactionManager() != null) {
          transaction = getTransactionManager().getTransaction();
        }
        Response response = process(new Message(new Object[]{}, null, transaction, null));
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
      setExitError(e);
      log.error(getId() + " uncaught exception, rolling back transaction and stopping", e);
      if (transaction != null) {
        transaction.setErrorOrException(e);
        transaction.rollback();
      }
      stop();
    }
    finally {
      stopAndDisconnect();
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
    if(connector instanceof IMetadataAware){
      ((IMetadataAware) connector).setMetadata(msg.getMetadata());
    }
    Object[] data = getNext();
    if (data != null && data.length != 0) {
      if (connector.getReaderContext() == prevReaderContext) {
        resetProcessor(connector.getReaderContext());
        prevReaderContext = connector.getReaderContext();
      }
	  Map metadata = msg.getMetadata();
	  if (data[0] instanceof Map && ((Map) data[0]).containsKey("readNodeMetadata")) {
		metadata.put("metadata", (((Map) data[0]).get("readNodeMetadata")));
		if (((Map) data[0]).containsKey("EOF")) {
			((Map)data[0]).remove("readNodeMetadata");
		}
		else {
			data[0] = ((Map)data[0]).get("data");
		}
	  }      
      Message newMessage = new Message(data, this, msg.getTransaction(), msg.getMetadata());
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

  /**
   * Returns exit code of this Runnable.
   */
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
  
  private void setExitError(Throwable throwable){
    exitCode++;
    exitThrowable = throwable;
  }

  /**
   * @return an unhandled exception, if any occured, from one of the nodes linked
   *         to this read node. Null if no exceptions in downstream nodes occured
   *         or if all those that did occur were handled by the exception handler.
   */
  public Throwable getExitError() {
    return exitThrowable;
  }
}
