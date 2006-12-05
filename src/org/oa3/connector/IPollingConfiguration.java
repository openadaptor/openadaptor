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
package org.oa3.connector;

/*
 * File: $Header: /cvs/oa3/src/org/oa3/connector/IPollingConfiguration.java,v 1.7 2006/11/02 09:59:40 higginse Exp $
 * Rev: $Revision: 1.7 $ Created Apr 11, 2006 by Eddy Higgins
 */
/**
 * Polling Configuration Interface
 * 
 * This is a utility interface to define parameters for polling connectors.
 * <p>
 * Polling is expected to operate as follows: Polling will continue at <code>pollInterval</code> intervals until
 * <code>maxPollLimit</code> is reached. Each time a poll is successful, the poll counter is reset.
 * 
 * <p>
 * Note that this interface is still subject to review (Oct 2006), and is likely to change somewhat before being
 * hardened.
 * 
 * @author Eddy Higgins
 */
public interface IPollingConfiguration {

  /**
   * Default poll interval (in milliseconds) is ten seconds.
   */
  public static final long DEFAULT_POLL_INTERVAL = 10 * 1000;

  /**
   * Flag to indicate continuous polling.
   */
  public static final long POLL_FOREVER = -1;

  /**
   * Default number of polls is <code>POLL_FOREVER</code>.
   */
  public static final long DEFAULT_POLL_COUNT = POLL_FOREVER;

  /**
   * Get current poll interval (in milliseconds).
   * 
   * @return Current poll interval in milliseconds
   */
  public long getPollInterval();

  /**
   * Sets current poll interval using the supplied interval.
   * <p>
   * Note negative values should be ignored by implementations.
   * 
   * @param intervalInMillis
   *          Positive poll interval in milliseconds.
   */
  public void setPollInterval(long intervalInMillis);

  /**
   * Return the maximum number of poll attempts permitted between successful polls.
   * <p>
   * A successful poll is one which results in some data being retrieved.
   * 
   * @return maximum number of consecutive unsuccessful polls permitted
   */
  public long getMaxPollLimit();

  /**
   * Set the maximum number of consecutive unsuccessful polls permitted. Note that negative values should be coerced to
   * POLL_FOREVER by implementations.
   */
  public void setMaxPollLimit(long maxPollCount);

  /**
   * Flag to indicate if polling is currently active.
   * 
   * @return <tt>true</tt> if active, <tt>false</tt> otherwise (e.g. when maxPollLimit has been reached)
   */
  public boolean isActive();

  /**
   * This method will normally just wait for <code>pollInterval</code> and return. It returns <tt>true</tt> if
   * polling should continue, or <tt>false</tt> if maxpollLimit > 0 and has been reached.
   * 
   * @return <tt>true</tt> if polling should continue, <tt>false</tt> if polling has finished
   */
  public boolean pollWait();

  /**
   * This resets the poll counter.
   * <p>
   * This would typically be called when a poll has successfully resulted in data being retrieved.
   */
  public void resetPollCount();
}
