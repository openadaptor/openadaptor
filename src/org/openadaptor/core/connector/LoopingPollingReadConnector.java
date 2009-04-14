/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

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
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.exception.OAException;
import org.openadaptor.core.transaction.ITransactional;

/**
 * A polling read connector that calls to underlying read connector at fixed time 
 * intervals. When this connector polls it calls the following on the IReadConnector it is
 * wrapping...
 * <ul>
 * <li>{@link IReadConnector#connect()}
 * <li>{@link IReadConnector#next(long)}, until {@link IReadConnector#isDry()}
 * returns true
 * <li>{@link IReadConnector#disconnect()}
 * </ul>
 * 
 * Out of the box, without setting any polling intervals the adaptor will perform 
 * a single poll and then exit</li>.
 *
 * By setting the <code>pollInterval</code> you will cause it to repeatedly poll, with the
 * specified poll interval between poll attempts.
 *
 * By setting the <code>pollInterval</code> and the <code>pollBackOff</code> you will cause it
 * to repeatedly poll (with the specified poll interval between attempts) until an empty set of
 * results is received when it will back off (for the specified poll back off) before continuing
 * the normal polling.  This is useful if you are polling an event source and wish to pick up data
 * when it is present, and then go to sleep for a bit when no data is present.
 * 
 * @author Fred Perry, Kris Lachor, Andrew Shire
 * @see CronnablePollingReadConnector
 * @see ThrottlingReadConnector
 */
public class LoopingPollingReadConnector extends AbstractPollingReadConnector implements ITransactional {

  private static final Log log = LogFactory.getLog(LoopingPollingReadConnector.class);
  
  private long intervalMs = -1;
  
  private long backoffMs = -1;
  
  private boolean dataReceived = true;
  private Date backedOffUntil = null;
  
  /**
   * Constructor.
   */
  public LoopingPollingReadConnector() {
    super();
  }

  /**
   * Constructor.
   * 
   * @param id a descriptive identifier for this connector.
   */
  public LoopingPollingReadConnector(String id) {
    super(id);
  }


  public Object[] next(long timeoutMs) throws OAException {
    Object[] result = super.next(timeoutMs);
    if (backoffMs > -1) {
      boolean oldDataReceived = dataReceived;
      dataReceived = (result != null) && (result.length > 0);
      if (!dataReceived && !oldDataReceived && backedOffUntil==null) {
        // We finished the previous batch of results and this batch is empty and we are not currently backed-off. 
        reconnectTime = new Date(new Date().getTime() + backoffMs);  // back-off relative to now
        backedOffUntil = reconnectTime;
        log.debug("backing off: next poll time = " + reconnectTime.toString());
      }
    }
    return result;
  }
  
  /**
   * Recalculates <code>reconnectTime</code> as the current <code>reconnectTime</code> 
   * plus <code>intervalMs</code> and removes it from being in backed off state.
   */
  protected void calculateReconnectTime() {
    reconnectTime = new Date(reconnectTime.getTime() + intervalMs);
    backedOffUntil = null;  // disable back-off mode so that our reconnectTime is respected
    log.debug("next poll time = " + reconnectTime.toString());
  }
  
  /**
   * Optional.
   * 
   * @param intervalMs set the adaptor to poll every X milliseconds
   */
  public void setPollIntervalMs(long intervalMs) {
    if (this.intervalMs > -1)
      log.warn("Multiple poll interval property settings detected. " + "[pollIntervalMS] will take precedence");

    this.intervalMs = intervalMs;
  }

  /**
   * Optional
   * 
   * @param interval set the adaptor to poll ever X seconds
   */
  public void setPollIntervalSecs(final int interval) {
    if (intervalMs > -1)
      log.warn("Multiple poll interval property settings detected. " + "[pollIntervalSecs] will take precedence");

    this.intervalMs = interval * 1000;
  }
  
  /**
   * Optional
   * 
   * @param interval
   *          set the adaptor to poll ever X minutes
   */
  public void setPollIntervalMins(final int interval) {
    if (intervalMs > -1)
      log.warn("Multiple poll interval property settings detected. " + "[pollIntervalMins] will take precedence");

    this.intervalMs = interval * 60 * 1000;
  }

  /**
   * Optional
   * 
   * @param interval
   *          set the adaptor to poll ever X hpurs
   */
  public void setPollIntervalHours(final int interval) {
    if (intervalMs > -1)
      log.warn("Multiple poll interval property settings detected. " + "[pollIntervalHours] will take precedence");

    this.intervalMs = interval * 60 * 60 * 1000;
  }
  
  /**
   * @return time interval between two subsequent calls to the underlying connector 
   *         #next() (in milliseconds)
   */
  public long getPollIntervalMs() {
    return intervalMs;
  }
  
  
  
  /**
   * Optional
   * 
   * @param backoffMS set the backoff time for adaptor polling in seconds (when last call to underlying connector returned no data)
   */
  public void setPollBackOffMS(long backoffMS) {
    if (this.backoffMs > -1)
      log.warn("Multiple poll backoff property settings detected. " + "[pollBackOffMS] will take precedence");

    this.backoffMs = backoffMs;
  }

  /**
   * Optional
   * 
   * @param backoff set the backoff time for adaptor polling in seconds (when last call to underlying connector returned no data)
   */
  public void setPollBackOffSecs(final int backoff) {
    if (backoffMs > -1)
      log.warn("Multiple poll backoff property settings detected. " + "[pollBackOffSecs] will take precedence");

    this.backoffMs = backoff * 1000;
  }

  /**
   * Optional
   * 
   * @param backoff set the backoff time for adaptor polling in minutes (when last call to underlying connector returned no data)
   */
  public void setPollBackOffMins(final int backoff) {
    if (backoffMs > -1)
      log.warn("Multiple poll backoff property settings detected. " + "[pollBackOffMins] will take precedence");

    this.backoffMs = backoff * 60 * 1000;
  }

  /**
   * Optional
   * 
   * @param backoff set the backoff time for adaptor polling in hours (when last call to underlying connector returned no data)
   */
  public void setPollBackOffHours(final int backoff) {
    if (backoffMs > -1)
      log.warn("Multiple poll backoff property settings detected. " + "[pollBackOffHours] will take precedence");

    this.backoffMs = backoff * 60 * 60 * 1000;
  }
  
  /**
   * @return backoff time interval between two subsequent calls to the underlying connector when last call returned no data
   *         #next() (in milliseconds)
   */
  public long getPollBackOffMs() {
    return backoffMs;
  }

}
