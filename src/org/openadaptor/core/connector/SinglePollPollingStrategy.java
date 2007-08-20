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
import org.openadaptor.core.IPollingReadConnector;

/**
 * Performs a single poll.
 * 
 * @author Kris Lachor
 */
public class SinglePollPollingStrategy extends AbstractPollingReadConnector {

  private static final Log log = LogFactory.getLog(SinglePollPollingStrategy.class);

  boolean executed = false;
  
  public Object[] next(long timeoutMs) {
    if(executed) return null;
    executed = true;
    return getReadConnector().next(timeoutMs);
  }
  
  /**
   * @todo this isn't necessarily the best method to let the caller 
   *   know.. this could perhaps be renamed to hasStrategyFinished() or something,
   *   for the meaning of isDry() here and in underlying connector could be different 
   *   and hence confusing.
   */
  public boolean isDry(){
    return executed && super.isDry();
  }


  //one shot needs to get everything with one call
  public int getConvertMode() {
    return IPollingReadConnector.CONVERT_ALL;
  }
  
  // need to have a validate method there that'd check if the delegate has been set up.

  
}
