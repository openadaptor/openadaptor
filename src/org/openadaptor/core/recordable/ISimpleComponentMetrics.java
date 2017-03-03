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

package org.openadaptor.core.recordable;

import org.openadaptor.auxil.metrics.ComponentMetrics;
import org.openadaptor.core.IMessageProcessor;

/**
 * An interface that represents simple metrics associated with an 
 * {@link IRecordableComponent}. An {@link IRecordableComponent} 
 * will typically be any of the {@link IMessageProcessor}s that 
 * form the adaptor's processing pipeline, or the adaptor itself.
 * 
 * These simple metrics are typically represented as text/Strings and 
 * are meant to be human-readable, for example via a JMX console,
 * rather than used for further computer processing.
 * 
 * @see IComponentMetrics
 * @see ComponentMetrics
 * @see IRecordableComponent
 * @author Kris Lachor
 */
public interface ISimpleComponentMetrics {

    /**
     * @return description of quantities and data types of messages
     *         that enter the component.
     */
    String getInputMsgs();
  
    /**
     * @return description of quantities and data types of messages
     *         that leave the component. Includes information on
     *         discards and exceptions.
     */  
    String getOutputMsgs();
    
    /**
     * @return information about discarded messages and messages that
     *        caused exceptions.
     */
    String getDiscardsAndExceptions();
  
   /**
    * @return descriptive information about the time the component
    *         took to process messages, for example minimal time,
    *         maximal time, average time.
    */
    String getProcessTime();
    
    /**
     * @return descriptive information about the idle time between
     *        processing messages. Includes min, max and average idle
     *        time.
     */
    String getIntervalTime();
    
    /**
     * @return the component uptime, in a human-readable format.
     */ 
    String getUptime();
    
    /**
     * Enables or disables capturing metrics for the component. Capturing
     * metrics involves a certain performance overhead, and therefore
     * in some adaptors in may be desirable to enable them only on ad-hoc
     * basis. 
     * 
     * @param metricsEnabled true if metrics are to be enabled, false if disabled.
     */
    void setMetricsEnabled(boolean metricsEnabled);
    
    /**
     * @return true if metrics capturing is currently enabled, false otherwise.
     */
    boolean isMetricsEnabled();
    
}
