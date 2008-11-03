/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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
import org.openadaptor.core.Message;
import org.openadaptor.core.Response;
import org.openadaptor.core.lifecycle.ILifecycleComponent;
import org.openadaptor.core.lifecycle.ILifecycleListener;

/**
 * Represents a class that records runtime metrics for an 
 * {@link IRecordableComponent} and the messages it processes. 
 * An {@link IRecordableComponent} will typically be any of the 
 * {@link IMessageProcessor}s that form the adaptor's processing 
 * pipeline, or the adaptor itself.
 * 
 * This interface extends {@link ISimpleComponentMetrics} with more detailed 
 * methods that return mostly numeric data that can be further processed 
 * and computed to achieve things like generating adaptor's reports,
 * storing audit data, generating charts and graphs from an adaptor's
 * run.
 * 
 * Extends {@link ILifecycleListener} to be able to automatically 
 * record starts and stops of those monitored components that are
 * {@link ILifecycleComponent}s.
 * 
 * @see ISimpleComponentMetrics
 * @see ComponentMetrics
 * @see IRecordableComponent
 * @author Kris Lachor
 */
public interface IComponentMetrics extends ISimpleComponentMetrics, ILifecycleListener{

  /**
   * Records the start of a message processing in the monitored component.
   * It should be called by the corresponding {@link IRecordableComponent}
   * when only the message enters the component.
   * 
   * @param msg a message traversing the adaptor's pipeline, as 
   *        it enters the monitored component.
   */
  void recordMessageStart(Message msg);
  
  /**
   * Records the end of a message processing in the monitored component.
   * It should be called by the corresponding {@link IRecordableComponent}
   * when right before the response leaves the component to be processed
   * by subsequent nodes in the pipeline.
   * 
   * @param msg a message traversing the adaptor's pipeline, as 
   *        it enters the monitored component.
   * @param response a response to the input message; the output from 
   *        the monitored component, a result of a successful 
   *        message processing.
   */
  void recordMessageEnd(Message msg, Response response);
  
  /**
   * Records the end of processing of a massage that is discarded.
   * 
   * @param msg the discarded message.
   */
  void recordDiscardedMsgEnd(Message msg);
  
  /**
   * Records the end of processing of a message that results in an
   * exception. 
   * 
   * @param msg a message that resulted in a processing error/exception.
   */
  public void recordExceptionMsgEnd(Message msg);
  
  /**
   * Records the start of the monitored component. Calling this method
   * is required only by those IRecordableComponents that are not
   * ILifecycleComponents, in which case their start and stop will be
   * recorded automatically.
   */
  public void recordComponentStart();
  
  /**
   * Records the stop of the monitored component. Calling this method
   * is required only by those IRecordableComponents that are not
   * ILifecycleComponents, in which case their start and stop will be
   * recorded automatically.
   */
  public void recordComponentStop();

  /**
   * Returns the count of input messages. Messages of different data type 
   * have separate counters. Names of data types can be checked with
   * {@link #getInputMsgTypes()}.
   * 
   * @return an array of input message counts.
   */
  long [] getInputMsgCounts();
 
  /**
   * Returns types of input messages. Indexes of the return array
   * will correspond to an array of counts returned by {@link #getInputMsgCounts()}.
   * 
   * @return an array of input message types.
   */
  String [] getInputMsgTypes();
  
  /**
   * Returns the count of output messages. Messages of different data type 
   * have separate counters. Names of data types can be checked with
   * {@link #getOutputMsgTypes()}.
   * 
   * @return an array of input message counts.
   */
  long [] getOutputMsgCounts();
  
  /**
   * Returns types of output messages. Indexes of the return array
   * will correspond to an array of counts returned by {@link #getOutputMsgCounts()}.
   * 
   * @return an array of input message types.
   */
  String [] getOutputMsgTypes();
 
  /**
   * @return number of messages discarded by the component.
   */
  long getDiscardedMsgCount();
  
  /**
   * @return number of messages that resulted in an exception.
   */
  long getExceptionMsgCount();
 
  
  long getProcessTimeMax();
	 
  long getProcessTimeMin();
    
  long getIntervalTimeMax();
    
  long getIntervalTimeMin();
 
  /**
   * @return the component for which these metrics are recorded.
   */
  IRecordableComponent getComponent();
  
}
