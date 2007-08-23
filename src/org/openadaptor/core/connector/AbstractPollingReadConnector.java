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

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.IPollingReadConnector;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.core.exception.ValidationException;
import org.openadaptor.core.transaction.ITransactional;

/**
 * An abstract implementation of {@link IPollingReadConnector}.
 * 
 * @author Kris Lachor
 */
public abstract class AbstractPollingReadConnector extends Component implements IPollingReadConnector {

  private static final Log log = LogFactory.getLog(AbstractPollingReadConnector.class);
  
  IReadConnector delegate;
  
  protected Date reconnectTime;

  private Date startTime;
  
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

  public IReadConnector getReadConnector() {
    return this.delegate;
  }
  
  public void setDelegate(IReadConnector delegate) {
    this.delegate = delegate;
  }
  
  public Object[] next(long timeoutMs) throws ComponentException {

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
  
  private void initTimes() {
    if (startTime == null) {
      startTime = new Date();
    }
    if (reconnectTime == null) {
      reconnectTime = new Date();
    }
  }
  
  private void sleepNoThrow(long timeoutMs) {
    try {
      Thread.sleep(timeoutMs);
    } catch (InterruptedException e) {
      /* ignores errors */
    }
  }
  
  public void connect() {
    initTimes();
    delegate.connect();
    count++;
  }

  
  public boolean isDry() {
    return delegate.isDry() && limit > 0 && count >= limit;
  }
  
  /**
   * Optional. Defaults to 1
   * 
   * @param limit
   *          the number of polls to perform before exiting the adaptor. A limit
   *          of less than 1 indicates an infinte loop and the adaptor will
   *          never exit!
   */
  public void setPollLimit(final int limit) {
    this.limit = limit;

    if (limit == 0){
      log.warn("A PollLimit of zero will result in an infinite polling loop");
    }
  }

  public void disconnect() {
    log.debug("Forwarding to the delegate.");
    this.delegate.disconnect();  
  }

  public Object getReaderContext() {
    log.debug("Forwarding to the delegate.");
    return this.delegate.getReaderContext();
  }
  
  public void setReaderConext(Object context) {
    log.debug("Forwarding to the delegate.");
    this.delegate.setReaderConext(context);
  }

  public void validate(List exceptions) {
    log.debug("Forwarding to the delegate.");
    if (delegate == null) {
      exceptions.add(new ValidationException("[delegate] property not set. " 
        + "Please supply an instance of " + IReadConnector.class.getName() + " for it", this));
    }
    this.delegate.validate(exceptions);
  }

  public Object getResource() {
    if( this.delegate instanceof ITransactional){
      return ((ITransactional) this.delegate).getResource();
    }
    else{
      return null;
    }
  }

  public IReadConnector getDelegate() {
    return delegate;
  }

}
