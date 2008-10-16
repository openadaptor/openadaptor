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

import org.openadaptor.core.lifecycle.ILifecycleComponent;
import org.openadaptor.core.lifecycle.State;
import org.openadaptor.core.recordable.IComponentMetrics;

/**
 * Class that agregates metrics from all components in an adaptor.
 * 
 * DRAFT. NOT READY FOR USE.
 * 
 * @author Kris Lachor
 */
public class AgregateMetrics implements IComponentMetrics {

  /* (non-Javadoc)
   * @see org.openadaptor.core.recordable.IComponentMetrics#disable()
   */
  public void disable() {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.openadaptor.core.recordable.IComponentMetrics#enable()
   */
  public void enable() {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.openadaptor.core.recordable.IComponentMetrics#enabled()
   */
  public boolean enabled() {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see org.openadaptor.core.recordable.IComponentMetrics#getDiscardedMsgCount()
   */
  public long getDiscardedMsgCount() {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see org.openadaptor.core.recordable.IComponentMetrics#getExceptionMsgCount()
   */
  public long getExceptionMsgCount() {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see org.openadaptor.core.recordable.IComponentMetrics#getInputMsgCounts()
   */
  public long[] getInputMsgCounts() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.openadaptor.core.recordable.IComponentMetrics#getInputMsgTypes()
   */
  public String[] getInputMsgTypes() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.openadaptor.core.recordable.IComponentMetrics#getIntervalTimeAvg()
   */
  public long getIntervalTimeAvg() {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see org.openadaptor.core.recordable.IComponentMetrics#getIntervalTimeMax()
   */
  public long getIntervalTimeMax() {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see org.openadaptor.core.recordable.IComponentMetrics#getIntervalTimeMin()
   */
  public long getIntervalTimeMin() {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see org.openadaptor.core.recordable.IComponentMetrics#getOutputMsgCount()
   */
  public long getOutputMsgCount() {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see org.openadaptor.core.recordable.IComponentMetrics#getOutputMsgTypes()
   */
  public String[] getOutputMsgTypes() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.openadaptor.core.recordable.IComponentMetrics#getProcessTimeAvg()
   */
  public long getProcessTimeAvg() {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see org.openadaptor.core.recordable.IComponentMetrics#getProcessTimeMax()
   */
  public long getProcessTimeMax() {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see org.openadaptor.core.recordable.IComponentMetrics#getProcessTimeMin()
   */
  public long getProcessTimeMin() {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see org.openadaptor.core.recordable.IComponentMetrics#getStartedSince()
   */
  public Date getStartedSince() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.openadaptor.core.lifecycle.ILifecycleListener#stateChanged(org.openadaptor.core.lifecycle.ILifecycleComponent, org.openadaptor.core.lifecycle.State)
   */
  public void stateChanged(ILifecycleComponent component, State newState) {
    // TODO Auto-generated method stub

  }

}
