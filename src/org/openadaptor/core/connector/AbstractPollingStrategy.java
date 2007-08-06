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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IPollingReadConnector;
import org.openadaptor.core.IPollingStrategy;
import org.openadaptor.core.exception.ComponentException;

/**
 * An abstract implementation of {@link IPollingStrategy}.
 * 
 * @author Kris Lachor
 */
public abstract class AbstractPollingStrategy implements IPollingStrategy {


  private static final Log log = LogFactory.getLog(AbstractPollingStrategy.class);
  
  IPollingReadConnector delegate;
  
  private Date reconnectTime;

  private Date startTime;
  
  protected int limit = 1;
  
  private int count;
  
  
  
  public IPollingReadConnector getReadConnector() {
    return this.delegate;
  }
  
  public void setPollingReadConnector(IPollingReadConnector delegate) {
    this.delegate = delegate;
  }
  
  public Object[] next(long timeoutMs) throws ComponentException {

    Date now = new Date();

    if (delegate.isDry() && now.after(reconnectTime)) {
      getReadConnector().disconnect();
      getReadConnector().connect();
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
      //todo move this part to cron strategy
//      if (cron != null && !forceInitialPoll) {
//        startTime = cron.getFireTimeAfter(startTime);
//      }
    }
    if (reconnectTime == null) {
      reconnectTime = new Date();
    }
  }
  
  private void sleepNoThrow(long timeoutMs) {
    try {
      Thread.sleep(timeoutMs);
    } catch (InterruptedException e) {
      // ignore errors
    }
  }
  
  public final void connect() {
    initTimes();
    delegate.connect();
    count++;
//    log.debug(getReadConnector().getId() + " poll count = " + count);
//    calculateReconnectTime();
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

    if (limit == 0)
      log.warn("A PollLimit of zero will result in an infinite polling loop");
  }

  public int getConvertMode() {
    return CONVERT_NEXT_ONLY;
  }

  

}
