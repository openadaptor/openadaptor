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

package org.openadaptor.auxil.metrics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.recordable.IComponentMetrics;
import org.openadaptor.core.recordable.IMetricsPrinter;

/**
 * An {@link IMetricsPrinter} that sends a selection of metrics to
 * a logger.
 * 
 * @see IMetricsPrinter
 * @see Log4JAggregateMetricsPrinter
 * @author Kris Lachor
 */
public class Log4JSingleMetricsPrinter implements IMetricsPrinter {

  private static final Log log = LogFactory.getLog(Log4JSingleMetricsPrinter.class.getName());
  
  protected void printMetrics(IComponentMetrics metrics, String name){
    if(!metrics.isMetricsEnabled() && !(metrics instanceof AggregateMetrics)){
      return;
    }
    StringBuffer sb = new StringBuffer();
    sb.append("----- Metrics for " + name+ " -----\n");
    sb.append("Input messages :            " + metrics.getInputMsgs() + "\n");
    sb.append("Output messages:            " + metrics.getOutputMsgs() + "\n");
    sb.append("Discards and exceptions:    " + metrics.getDiscardsAndExceptions() + "\n");
    sb.append("Avg msg processing time:    " + metrics.getProcessTime() + "\n");
    sb.append("Avg between msgs idle time: " + metrics.getIntervalTime() + "\n");
    sb.append("Component uptime:           " + metrics.getUptime());
    log.info(sb.toString());  
  }

  /**
   * @see org.openadaptor.core.recordable.IMetricsPrinter#print(org.openadaptor.core.recordable.IComponentMetrics)
   */
  public void print(IComponentMetrics metrics, String description) {
    printMetrics(metrics, description);
  }

  /**
   * @see org.openadaptor.core.recordable.IMetricsPrinter#print(org.openadaptor.core.recordable.IComponentMetrics)
   */
  public void print(IComponentMetrics metrics) {
    printMetrics(metrics, metrics.getComponent().getId());
  }
}
