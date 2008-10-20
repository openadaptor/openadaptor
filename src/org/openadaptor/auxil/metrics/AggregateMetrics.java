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
package org.openadaptor.auxil.metrics;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.lifecycle.ILifecycleComponent;
import org.openadaptor.core.lifecycle.State;
import org.openadaptor.core.recordable.IComponentMetrics;
import org.openadaptor.core.recordable.IRecordableComponent;

/**
 * Class that agregates metrics from all components in an adaptor.
 * 
 * DRAFT. NOT READY FOR USE.
 * 
 * @author Kris Lachor
 */
public class AggregateMetrics implements IComponentMetrics {

  private static final Log log = LogFactory.getLog(AggregateMetrics.class);
  
  //TODO alternatively, this may hold IRecordableComponents
  Set componentMetrics = new HashSet();
  
  private boolean enabled = true;
  
  /**
   * Disables metrics recording across entire adaptor.
   * 
   * @see org.openadaptor.core.recordable.IComponentMetrics#disable()
   */
  public void disable() {
    log.info("Disabling metrics recording in all recordable components");
    Iterator it = componentMetrics.iterator();
    while(it.hasNext()){
      IComponentMetrics detailedMetrics = (IComponentMetrics) it.next();
      detailedMetrics.disable();
    }
    enabled = false;
  }

  /**
   * Enables metrics recording across entire adaptor.
   * 
   * @see org.openadaptor.core.recordable.IComponentMetrics#enable()
   */
  public void enable() {
    log.info("Enabling metrics recording in all recordable components");
    Iterator it = componentMetrics.iterator();
    while(it.hasNext()){
      IComponentMetrics detailedMetrics = (IComponentMetrics) it.next();
      detailedMetrics.enable();
    }
    enabled = true;
  }

  /**
   * TODO unclear what this should return. 
   * 
   * @see org.openadaptor.core.recordable.IComponentMetrics#enabled()
   */
  public boolean enabled() {
    return enabled;
  }

  /**
   * Returns a sum of discarded messages from all recordable components.
   * 
   * @see org.openadaptor.core.recordable.IComponentMetrics#getDiscardedMsgCount()
   */
  public long getDiscardedMsgCount() {
    Iterator it = componentMetrics.iterator();
    long discardedCount = 0;
    while(it.hasNext()){
      IComponentMetrics detailedMetrics = (IComponentMetrics) it.next();
      discardedCount+=detailedMetrics.getDiscardedMsgCount();
    }
    log.info("Sum of discarded messages in all recordable components: " + discardedCount);
    return discardedCount;
  }

  /**
   * Returns a sum of messages that caused an exception from all recordable components.
   * 
   * @see org.openadaptor.core.recordable.IComponentMetrics#getExceptionMsgCount()
   */
  public long getExceptionMsgCount() {
    Iterator it = componentMetrics.iterator();
    long exceptionMsgsCount = 0;
    while(it.hasNext()){
      IComponentMetrics detailedMetrics = (IComponentMetrics) it.next();
      exceptionMsgsCount+=detailedMetrics.getExceptionMsgCount();
    }
    log.info("Sum of messages that caused an exception in all recordable components: " + exceptionMsgsCount);
    return exceptionMsgsCount;
  }

  /**
   * @see org.openadaptor.core.recordable.IComponentMetrics#getInputMsgCounts()
   */
  public long[] getInputMsgCounts() {
    //Need to get hold of the nodes at the start of the pipeline
    return null;
  }

  /**
   * @see org.openadaptor.core.recordable.IComponentMetrics#getInputMsgTypes()
   */
  public String[] getInputMsgTypes() {
    //Need to get hold of the nodes at the start of the pipeline
    return null;
  }

  /**
   * @see org.openadaptor.core.recordable.IComponentMetrics#getIntervalTimeAvg()
   */
  public String getIntervalTimeAvg() {
    // Need to get hold of the first and last nodes in the pipeline
    return null;
  }

  /**
   * @see org.openadaptor.core.recordable.IComponentMetrics#getIntervalTimeMax()
   */
  public String getIntervalTimeMax() {
    // Need to get hold of the first and last nodes in the pipeline
    return null;
  }

  /**
   * @see org.openadaptor.core.recordable.IComponentMetrics#getIntervalTimeMin()
   */
  public String getIntervalTimeMin() {
    // Need to get hold of the first and last nodes in the pipeline
    return null;
  }

  /**
   * @see org.openadaptor.core.recordable.IComponentMetrics#getOutputMsgCount()
   */
  public long getOutputMsgCount() {
    // need to get hold of last nodes in the pipeline
    return 0;
  }

  /**
   * @see org.openadaptor.core.recordable.IComponentMetrics#getOutputMsgTypes()
   */
  public String[] getOutputMsgTypes() {
    // need to get hold of last nodes in the pipeline
    return null;
  }

  /* (non-Javadoc)
   * @see org.openadaptor.core.recordable.IComponentMetrics#getProcessTimeAvg()
   */
  public String getProcessTimeAvg() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.openadaptor.core.recordable.IComponentMetrics#getProcessTimeMax()
   */
  public String getProcessTimeMax() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.openadaptor.core.recordable.IComponentMetrics#getProcessTimeMin()
   */
  public String getProcessTimeMin() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.openadaptor.core.recordable.IComponentMetrics#getStartedSince()
   */
  public String getUptime() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.openadaptor.core.lifecycle.ILifecycleListener#stateChanged(org.openadaptor.core.lifecycle.ILifecycleComponent, org.openadaptor.core.lifecycle.State)
   */
  public void stateChanged(ILifecycleComponent component, State newState) {
    // TODO Auto-generated method stub

  }

  /**
   * Adds metrics that will be included as part of these agregate metrics.
   */
  public void addRecordableComponent(IRecordableComponent recordableComponent){
    log.info("Adding component metrics to agregate: " + recordableComponent);
    componentMetrics.add(recordableComponent.getMetrics());
  }

  public String getDuration() {
    // TODO Auto-generated method stub
    return null;
  }
}
