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
 * File: $Header: /cvs/oa3/src/org/oa3/connector/PollingConfiguration.java,v 1.4 2006/10/17 16:43:00 higginse Exp $ Rev:
 * $Revision: 1.4 $ Created Apr 13, 2006 by Eddy Higgins
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Reference Implementation of the <code>IPollingConfiguration</code> interface.
 * 
 * @see org.oa3.core.connector.IPollingConfiguration for full details.
 * 
 * @author Eddy Higgins
 */
public class PollingConfiguration implements IPollingConfiguration {
  
  private static final Log log = LogFactory.getLog(PollingConfiguration.class);

  private long pollInterval = DEFAULT_POLL_INTERVAL;

  private long maxPollLimit = DEFAULT_POLL_COUNT;

  private long currentPoll = 1;

  private boolean polling = true;

  // BEGIN Bean getters/setters

  /**
   * Get current poll interval (in milliseconds).
   * 
   * @return Current poll interval in milliseconds
   */
  public long getPollInterval() {
    return pollInterval;
  }

  /**
   * Sets current poll interval using the supplied interval.
   * <p>
   * Note negative values should be ignored by implementations.
   * 
   * @param intervalInMillis
   *          Positive poll interval in milliseconds.
   */
  public void setPollInterval(long intervalInMillis) {
    if (intervalInMillis < 0) {
      log.warn("Ignoring invalid pollInterval of " + intervalInMillis);
    } else {
      this.pollInterval = intervalInMillis;
    }
  }

  /**
   * Return the maximum number of poll attempts permitted between successful polls.
   * <p>
   * A successful poll is one which results in some data being retrieved.
   * 
   * @return maximum number of consecutive unsuccessful polls permitted
   */
  public long getMaxPollLimit() {
    return maxPollLimit;
  }

  /**
   * Set the maximum number of consecutive unsuccessful polls permitted. Note that negative values should be coerced to
   * POLL_FOREVER by implementations.
   */
  public void setMaxPollLimit(long maxPollLimit) {
    this.maxPollLimit = maxPollLimit < 0 ? POLL_FOREVER : maxPollLimit;
  }

  // END Bean getters/setters

  /**
   * Flag to indicate if polling is currently active.
   * 
   * @return <tt>true</tt> if active, <tt>false</tt> otherwise (e.g. when maxPollLimit has been reached)
   */
  public boolean isActive() {
    return polling;
  }

  /**
   * This method will normally just wait for <code>pollInterval</code> and return. It returns <tt>true</tt> if
   * polling should continue, or <tt>false</tt> if maxpollLimit > 0 and has been reached.
   * 
   * @return <tt>true</tt> if polling should continue, <tt>false</tt> if polling has finished
   */
  public boolean pollWait() {
    polling = (maxPollLimit == 0) || (currentPoll < maxPollLimit);

    if (polling) {
      currentPoll++;
      long now = System.currentTimeMillis();
      long due = now + pollInterval;
      long napTime = due - now;
      log.debug("Sleeping until: " + due + " (interval=" + napTime + ")");
      while (napTime > 0) {
        try {
          log.debug("Poll #" + currentPoll + " sleeping for poll interval (" + pollInterval + ")");
          Thread.sleep(napTime);
        } catch (InterruptedException ie) {
          log.debug("Poll wait interrupted");
        }
        napTime = due - System.currentTimeMillis();
      }
    }
    return (polling);
  }

  /**
   * This resets the poll counter.
   * <p>
   * This would typically be called when a poll has successfully resulted in data being retrieved.
   */
  public void resetPollCount() {
    currentPoll = 1;
  }
}
