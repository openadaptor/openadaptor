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
package org.oa3.core.connector;

/*
 * File: $Header: /cvs/oa3/src/org/oa3/connector/AbstractReadConnector.java,v 1.24 2006/10/18 14:32:51 kscully Exp $
 * Rev: $Revision: 1.24 $ Created Jul 26, 2005 by Kevin Scully
 */
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.core.Component;
import org.oa3.core.IReadConnector;
import org.oa3.core.exception.ComponentException;
import org.oa3.core.transaction.ITransactional;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;

/**
 * This is a IReadConnector that wraps another IReadConnector, it can be configured to poll at fixed intervals or using
 * a cron format.
 * 
 * Allows adaptors to poll files, directories, databases rss feeds etc...
 * 
 * @author OA3 Core Team
 */
public class PollingReadConnector extends Component implements IReadConnector, ITransactional {

  private static final Log log = LogFactory.getLog(PollingReadConnector.class);

  private Date reconnectTime;
  
  private Date startTime;

  private int count;

  private int limit = 1;

  private long intervalMs;

  private IReadConnector delegate;

  private boolean forceInitialPoll;

  private CronTrigger cron;

  public PollingReadConnector() {
  }

  public PollingReadConnector(String id) {
    super(id);
  }

  public void setDelegate(final IReadConnector delegate) {
    this.delegate = delegate;
  }

  public void setPollIntervalMs(final long interval) {
    this.intervalMs = interval;
  }

  public void setPollIntervalSecs(final int interval) {
    this.intervalMs = interval * 1000;
  }

  public void setPollIntervalMins(final int interval) {
    this.intervalMs = interval * 60 * 1000;
  }

  public void setPollIntervalHours(final int interval) {
    this.intervalMs = interval * 60 * 60 * 1000;
  }

  public void setPollLimit(final int limit) {
    this.limit = limit;
  }

  public void setCronExpression(String s) {
    cron = new CronTrigger();
    try {
      cron.setCronExpression(new CronExpression(s));
    } catch (ParseException e) {
      throw new RuntimeException("cron parse exception", e);
    }
    limit = 0;
  }

  public void setForceInitialPoll(final boolean force) {
    this.forceInitialPoll = force;
  }
  
  public final void connect() {
    initTimes();
    delegate.connect();
    count++;
    log.debug(getId() + " poll count = " + count);
    calculateReconnectTime();
  }

  public final void disconnect() {
    delegate.disconnect();
  }

  public final Object[] next(long timeoutMs) throws ComponentException {

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
      if (cron != null && !forceInitialPoll) {
        startTime = cron.getFireTimeAfter(startTime);
      }
    }
    if (reconnectTime == null) {
      reconnectTime = new Date();
    }
  }

  private void sleepNoThrow(long timeoutMs) {
    try {
      Thread.sleep(timeoutMs);
    } catch (InterruptedException e) {
    }
  }

  private void calculateReconnectTime() {
    if (cron != null) {
      reconnectTime = cron.getFireTimeAfter(reconnectTime);
    } else {
      reconnectTime = new Date(reconnectTime.getTime() + intervalMs);
    }
    log.info(getId() + " next poll time = " + reconnectTime.toString());
  }

  public final boolean isDry() {
    return delegate.isDry() && limit > 0 && count >= limit;
  }

  public final Object getReaderContext() {
    return delegate.getReaderContext();
  }

  public final Object getResource() {
    if (delegate instanceof ITransactional) {
      return ((ITransactional) delegate).getResource();
    } else {
      return null;
    }
  }
}