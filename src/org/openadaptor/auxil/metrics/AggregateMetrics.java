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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IComponent;
import org.openadaptor.core.node.WriteNode;
import org.openadaptor.core.recordable.IComponentMetrics;
import org.openadaptor.core.recordable.IRecordableComponent;

/**
 * Class that agregates metrics from all components in an adaptor.
 * 
 * DRAFT. NOT READY FOR USE.
 * 
 * @author Kris Lachor
 */
public class AggregateMetrics extends ComponentMetrics{

  private static final Log log = LogFactory.getLog(AggregateMetrics.class);
  
  Set componentMetrics = new HashSet();
  
  protected AggregateMetrics(IRecordableComponent recordableComponent) {
    super(recordableComponent);
  }
 
  public AggregateMetrics(IRecordableComponent monitoredComponent, boolean enabled) {
    super(monitoredComponent, enabled);
  }

  /**
   * Returns a sum of discarded messages from all recordable components.
   * 
   * @see org.openadaptor.core.recordable.IComponentMetrics#getDiscardedMsgCount()
   */
  public long getDiscardedMsgCount() {
    if(!enabled){
      return -1;
    }
    Iterator it = componentMetrics.iterator();
    long discardedCount = 0;
    while(it.hasNext()){
      IComponentMetrics detailedMetrics = (IComponentMetrics) it.next();
      discardedCount+=detailedMetrics.getDiscardedMsgCount();
    }
    log.debug("Sum of discarded messages in all recordable components: " + discardedCount);
    return discardedCount;
  }

  /**
   * Returns a sum of messages that caused an exception from all recordable components.
   * 
   * @see org.openadaptor.core.recordable.IComponentMetrics#getExceptionMsgCount()
   */
  public long getExceptionMsgCount() {
    if(!enabled){
      return -1;
    }
    Iterator it = componentMetrics.iterator();
    long exceptionMsgsCount = 0;
    while(it.hasNext()){
      IComponentMetrics detailedMetrics = (IComponentMetrics) it.next();
      exceptionMsgsCount+=detailedMetrics.getExceptionMsgCount();
    }
    log.debug("Sum of messages that caused an exception in all recordable components: " + exceptionMsgsCount);
    return exceptionMsgsCount;
  }

  /**
   * TODO
   */
  public void addComponentMetrics(IComponentMetrics componentMetrics){
    this.componentMetrics.add(componentMetrics);
  }

  /**
   * TODO
   */
  public long[] getOutputMsgCounts() {
    long [] msgCounts = new long[0];
    for(Iterator it = componentMetrics.iterator(); it.hasNext();){
      IComponentMetrics compMetrics = (IComponentMetrics) it.next();
      if(isLastInPipeline(compMetrics.getComponent())){
        msgCounts = ArrayUtils.addAll(msgCounts, compMetrics.getInputMsgCounts());
      }
    }
    return msgCounts;
  }

  /**
   * TODO
   */
  public String[] getOutputMsgTypes() {
    Collection msgTypes = new HashSet();
    for(Iterator it = componentMetrics.iterator(); it.hasNext();){
      IComponentMetrics compMetrics = (IComponentMetrics) it.next();
      if(isLastInPipeline(compMetrics.getComponent())){
        msgTypes.addAll(Arrays.asList(compMetrics.getInputMsgTypes()));
      }
    }
    return (String[]) msgTypes.toArray(new String[0]);
  }
  
  /**
   * TODO comments
   */
  private boolean isLastInPipeline(IComponent component){
    if(component instanceof WriteNode){
      return true;
    }
    return false;
  }

  public Collection getComponentMetrics() {
    return componentMetrics;
  }
}
