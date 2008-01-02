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

package org.openadaptor.core.connector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.IPollingReadConnector;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.exception.OAException;
import org.openadaptor.core.exception.ValidationException;
import org.openadaptor.core.transaction.ITransactional;

import java.util.Date;
import java.util.List;

/**
 * An abstract implementation of {@link IPollingReadConnector}. 
 * <br>
 * Based on the legacy <code>PollingReadConnector</code>.
 * When this connector polls it calls the following on the IReadConnector it is
 * wrapping...
 * <ul>
 * <li>{@link IReadConnector#connect()}
 * <li>{@link IReadConnector#next(long)}, until {@link IReadConnector#isDry()}
 * returns true
 * <li>{@link IReadConnector#disconnect()}
 * </ul>
 * 
 * @see IPollingReadConnector
 * @see IReadConnector
 * @author Fred Perry, Kris Lachor, Eddy Higgins
 */
public abstract class AbstractPollingReadConnector extends Component implements IPollingReadConnector {

  private static final Log log = LogFactory.getLog(AbstractPollingReadConnector.class);
  
  private static final int DEFAULT_RECONNECT_INTERVAL = 1000;
  
  private IReadConnector delegate;
  
  protected Date reconnectTime;

  protected Date startTime;
  
  protected int limit = 1;
  
  private int count;

  /**
   * Constructor.
   */
  public AbstractPollingReadConnector() {
    super();
  }

  /**
   * Constructor.
   * 
   * @param id the id.
   */
  public AbstractPollingReadConnector(String id) {
    super(id);
  }

  /**
   * Initialises start and reconnect times. Calls {@link #connect()} on the delegate.
   * Keeps count of the number of calls.
   */
  public void connect() {
    initTimes();
    delegate.connect();
    count++;
    log.debug(getId() + " delegate #connect count = " + count);
    calculateReconnectTime();
  }

  /**
   * Fetch next data from delegate, subject to polling conditions.
   * <br>
   * It will call {@link #next(long)} on the <code>delegate</code>, but first makes
   * sure the time is right, i.e. we're past the <code>startTime</code> and 
   * the delegate isn't dry. 
   * If the delegate <em>is</em dry, and we're past the <code>reconnectTime</code>
   * the delegate is disconnected and (re)connected again.
   * If the delegate is still dry despite a reconnection, it sleeps for 
   * the length of the <code>timeout</code>.
   *
   * @throws OAException
   */
  public Object[] next(long timeoutMs) throws OAException {
    Date now = new Date();

    if (delegate.isDry() && now.after(reconnectTime)) {
      disconnect();
      connect();
    }

    if (!delegate.isDry() && now.after(startTime)) {
      return delegate.next(timeoutMs);
    } else {
      sleepNoThrow(timeoutMs);
      return null;
    }
  }
  
  /**
   * Initialises connect and reconnect times to current time. 
   * This method will typically be overridden to init times in a connector specific way.
   */
  protected void initTimes() {
    if (startTime == null) {
      startTime = new Date();
    }
    if (reconnectTime == null) {
      reconnectTime = new Date();
    }
  }
  
  /**
   * Recalculates reconnect time but adding <code>DEFAULT_RECONNECT_INTERVAL</code> to 
   * the current <code>reconnectTime<code>.
   * This method will typically be overridden to init times in a connector specific way.
   */
  protected void calculateReconnectTime() {
    reconnectTime = new Date(reconnectTime.getTime() + DEFAULT_RECONNECT_INTERVAL);
    log.info(getId() + " next poll time = " + reconnectTime.toString());
  }
  
  /**
   * @return true if either the <code>delegate</code> is dry or the number of calls to
   *         the <code>delegate</code> has exceeded the max limit.
   * @see IReadConnector#isDry()
   */
  public boolean isDry() {
    return delegate.isDry() && limit > 0 && count >= limit;
  }
  
  /**
   * Optional. Defaults to 1
   * 
   * @param limit the number of polls to perform before exiting the adaptor. A limit
   *          of less than 1 indicates an infinte loop and the adaptor will
   *          never exit!
   */
  public void setPollLimit(final int limit) {
    this.limit = limit;

    if (limit == 0){
      log.warn("A PollLimit of zero will result in an infinite polling loop");
    }
  }

  private void sleepNoThrow(long timeoutMs) {
    try {
      Thread.sleep(timeoutMs);
    } catch (InterruptedException e) {
      /* ignores errors */
    }
  }
  
  /**
   * Forwards to the <code>delegate<code>.
   * 
   * @see IReadConnector#disconnect()
   */
  public void disconnect() {
    this.delegate.disconnect();  
  }

  /**
   * Forwards to the <code>delegate<code>.
   * 
   * @see IReadConnector#getReaderContext()
   */
  public Object getReaderContext() {
    return this.delegate.getReaderContext();
  }
  
  /**
   * Forwards to the <code>delegate<code>.
   * 
   * @see IReadConnector#setReaderContext(Object)
   */
  public void setReaderContext(Object context) {
    this.delegate.setReaderContext(context);
  }

  /**
   * Makes sure that the <code>delegate</code> has been set.
   * 
   * @see IReadConnector#validate(List)
   */
  public void validate(List exceptions) {
    log.debug("Forwarding to the delegate.");
    if (delegate == null) {
      exceptions.add(new ValidationException("[delegate] property not set. " 
        + "Please supply an instance of " + IReadConnector.class.getName() + " for it", this));
    }
    this.delegate.validate(exceptions);
  }
  
  /**
   * @return the delegate#getResource() if the delegate is an ITransactional, null otherwise.
   * @see ITransactional#getResource()
   */
  public Object getResource() {
    if( this.delegate instanceof ITransactional){
      return ((ITransactional) this.delegate).getResource();
    }
    else{
      return null;
    }
  }
  
  /**
   * Sets the delegate IReadConnector.
   * 
   * @param delegate the delegate IReadConnector
   * @see IPollingReadConnector#getDelegate()
   */
  public void setDelegate(IReadConnector delegate) {
    this.delegate = delegate;
  }

  /**
   * @see IPollingReadConnector#getDelegate()
   */
  public IReadConnector getDelegate() {
    return delegate;
  }

}
