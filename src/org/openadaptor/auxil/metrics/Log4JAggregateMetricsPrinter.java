/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

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

import java.util.Collection;
import java.util.Iterator;

import org.openadaptor.core.recordable.IComponentMetrics;
import org.openadaptor.core.recordable.IMetricsPrinter;

/**
 * An {@link IMetricsPrinter} that sends a selection of metrics to
 * a logger. If the printed metrics of are {@link AggregateMetrics}
 * it also prints all the metrics contained in it.
 * 
 * @see Log4JSingleMetricsPrinter
 * @see IMetricsPrinter
 * @author Kris Lachor
 */
public class Log4JAggregateMetricsPrinter extends Log4JSingleMetricsPrinter {
  
  /**
   * @see org.openadaptor.core.recordable.IMetricsPrinter#print(org.openadaptor.core.recordable.IComponentMetrics)
   */
  public void print(IComponentMetrics metrics, String description) {
    printMetrics(metrics, description);
    if(metrics instanceof AggregateMetrics){
      Collection componentMetrics = ((AggregateMetrics)metrics).getComponentMetrics();
      if(componentMetrics==null){
        return;
      }
      for(Iterator it=componentMetrics.iterator();it.hasNext();){
        IComponentMetrics singleMetrics = (IComponentMetrics) it.next();
        printMetrics(singleMetrics, singleMetrics.getComponent().getId());
      }
    }
  }

  /**
   * @see org.openadaptor.core.recordable.IMetricsPrinter#print(org.openadaptor.core.recordable.IComponentMetrics)
   */
  public void print(IComponentMetrics metrics) {
    print(metrics, metrics.getComponent().getId());
  }
}
