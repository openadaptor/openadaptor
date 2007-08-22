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

/**
 * A polling read connector that repeatedly calls to underlying read connector. 
 * It allows for these parameters:
 * 
 * interval - interval between two subsequent calls (in milliseconds)
 * 
 * @author Kris Lachor
 */
public class LoopingPollingReadConnector extends AbstractPollingReadConnector {

  private static final Log log = LogFactory.getLog(LoopingPollingReadConnector.class);
  
  private long intervalMs = -1;
  
  public Object[] next(long timeoutMs) {
    Object [] result =  super.next(timeoutMs);
    return result;
  }

  public long getPollIntervalMs() {
    return intervalMs;
  }

 
  public void connect() {
    super.connect();
//  log.debug(getReadConnector().getId() + " poll count = " + count);
    calculateReconnectTime();
  }

  /**
   * Optional.
   * 
   * @param interval set the adaptor to poll every X milliseconds
   */
  public void setPollIntervalMs(long intervalMs) {
    this.intervalMs = intervalMs;
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

  
  private void calculateReconnectTime() {
    reconnectTime = new Date(reconnectTime.getTime() + intervalMs);
    log.info("next poll time = " + reconnectTime.toString());
  }
  
//  /**
//   * Checks that the mandatory properties have been set
//   * 
//   * @param exceptions
//   *          list of exceptions that any validation errors will be appended to
//   */
//  public void validate(List exceptions) {
//    if (delegate == null) {
//      exceptions.add(new ValidationException("[delegate] property not set. " 
//          + "Please supply an instance of " + IReadConnector.class.getName() + " for it", this));
//    }
//
//    if (cron != null) {
//      if (intervalMs > -1) {
//        log.warn("[cronExpression] takes precedence over [pollIntervalX] which will be ignored");
//      }
//
//      if (limit > 0) {
//        log.warn("[cronExpression] takes precedence over [pollLimit] which will be ignored");
//      }
//    }
//
//    if (cron == null && forceInitialPoll) {
//      log.warn("Property [forceInitialPoll] is only applicable when using the [cronExpression]. "
//          + "It will be ignored");
//    }
//  }

}
