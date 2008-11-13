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

import org.openadaptor.core.node.ReadNode;
import org.openadaptor.core.recordable.IRecordableComponent;

/**
 * Metrics class dedicated for {@link ReadNode}s. Several metrics
 * do not make sense in the context of read nodes.
 * 
 * @author Kris Lachor
 * @see ComponentMetrics
 */
public class ReaderMetrics extends ComponentMetrics{

  protected static final String NOT_APPLICABLE_FOR_READERS = "N/A for readers";
  
  /**
   * Constructor.
   */
  protected ReaderMetrics(IRecordableComponent monitoredComponent) {
    super(monitoredComponent);
  }

  /**
   * @return info that this isn't applicable to readers
   */
  public String getDiscardsAndExceptions() {
    return NOT_APPLICABLE_FOR_READERS;
  }

  /**
   * @return info that this isn't applicable to readers
   */
  public String getInputMsgs() {
    return NOT_APPLICABLE_FOR_READERS;
  }

  /**
   * @return info that this isn't applicable to readers
   */
  public String getIntervalTime() {
    return NOT_APPLICABLE_FOR_READERS;
  }

  /**
   * @return info that this isn't applicable to readers
   */
  public String getProcessTime() {
    return NOT_APPLICABLE_FOR_READERS;
  }

  /**
   * @return info that this isn't applicable to readers
   */
  public long getIntervalTimeAvg() {
    return UNKNOWN_LONG;
  }

  /**
   * @return info that this isn't applicable to readers
   */
  public long getIntervalTimeMax() {
    return UNKNOWN_LONG;
  }

  /**
   * @return info that this isn't applicable to readers
   */
  public long getIntervalTimeMin() {
    return UNKNOWN_LONG;
  }

  /**
   * @return info that this isn't applicable to readers
   */
  public long getProcessTimeAvg() {
    return UNKNOWN_LONG;
  }

  /**
   * @return info that this isn't applicable to readers
   */
  public long getProcessTimeLast() {
    return UNKNOWN_LONG;
  }

  /**
   * @return info that this isn't applicable to readers
   */
  public long getProcessTimeMax() {
    return UNKNOWN_LONG;
  }

  public long getProcessTimeMin() {
    return UNKNOWN_LONG;
  }
}
