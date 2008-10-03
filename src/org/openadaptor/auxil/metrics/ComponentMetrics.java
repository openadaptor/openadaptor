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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.openadaptor.core.Message;
import org.openadaptor.core.lifecycle.ILifecycleComponent;
import org.openadaptor.core.lifecycle.State;
import org.openadaptor.core.recordable.IComponentMetrics;

/**
 * Class that maintains all metrics associated with one Node.
 * 
 * DRAFT. NOT READY FOR USE.
 * 
 * @author Kris Lachor
 */
public class ComponentMetrics implements IComponentMetrics{

  Map inputMsgCounter = new HashMap();
  
  long minProcessTime = -1;
  
  long maxProcessTime = -1;
  
  long totalProcessTime = -1;
  
  long outputMsgs = 0;
  
  long discardedMsgs = 0;
  
  long exceptionMsgs = 0;
  
  private Date processStartTime;
  
  private Date processEndTime;
  
  long minIntervalTime = -1;
  
  long maxIntervalTime = -1;
  
  long totalIntervalTime = -1;
  
  Date lastStarted = null;
  
  
  /**
   * Indicates start of message processing. 
   * Starts timers.
   * 
   * @param msg
   */
  public void recordMessageStart(Message msg){
    processStartTime = new Date();
    
    /* calculate intervals */
    if(! (processEndTime==null)){
      long intervalTime = processStartTime.getTime() - processEndTime.getTime();
      totalIntervalTime+=intervalTime;
      if(maxIntervalTime<intervalTime){
        maxIntervalTime=intervalTime;
      }
      if(minIntervalTime>intervalTime || minIntervalTime==-1){
        minIntervalTime=intervalTime;
      }
    }
    
    if(msg.getData().length==0){
      return;
    }
    String msgPayloadType = msg.getData()[0].getClass().getName();
    Object count = inputMsgCounter.get(msgPayloadType);
    if(count==null){
      inputMsgCounter.put(msgPayloadType, new Integer(1));
    }
    else{
      Integer countInt = (Integer) count;
      inputMsgCounter.put(msgPayloadType, new Integer(countInt.intValue()+1));
    }
   
  }
  
  /**
   * 
   * @param msg
   * @param processTime
   */
  public void recordMessageEnd(Message msg){
    processEndTime = new Date();
    long processTime = processEndTime.getTime() - processStartTime.getTime();
    totalProcessTime+=processTime;
    if(maxProcessTime<processTime){
      maxProcessTime=processTime;
    }
    if(minProcessTime>processTime || minProcessTime==-1){
      minProcessTime=processTime;
    }
    outputMsgs++;
  }

  public void recordDiscardedMsgEnd(Message msg){
    discardedMsgs++;
  }
  
  
  public void recordExceptionMsgEnd(Message msg){
    exceptionMsgs++;
  }
  
  public long [] getInputMsgCounts(){
    long count = 0;
    Iterator it = inputMsgCounter.keySet().iterator();
    while(it.hasNext()){
      Object dataType = it.next();
      Integer countInt = (Integer) inputMsgCounter.get(dataType);
      count+= countInt.longValue();
    }
    return new long[]{count};
  }
  
  public long getProcessTimeAvg() {
    long msgCount = getInputMsgCounts()[0];
    if(msgCount==0){
      return -1;
    }
    else{
      return (long) (totalProcessTime/msgCount);
    }
  }

  public long getProcessTimeMin() {
    return minProcessTime;
  }
  
  public long getProcessTimeMax() {
    return maxProcessTime;
  }

  public String [] getInputMsgTypes() {
    Set inputMsgTypes = inputMsgCounter.keySet();
    return (String[]) inputMsgTypes.toArray(new String[]{});
  }

  public long getIntervalTimeAvg() {
    long msgCount = getInputMsgCounts()[0];
    if(msgCount==0){
      return -1;
    }
    else{
      return (long) (totalIntervalTime/msgCount);
    }
  }

  public long getIntervalTimeMax() {
    return maxIntervalTime;
  }

  public long getIntervalTimeMin() {
    return minIntervalTime;
  }

  public long getDiscardedMsgCount() {
    return discardedMsgs;
  }

  public long getExceptionMsgCount() {
    return exceptionMsgs;
  }

  public long getOutputMsgCount() {
    return outputMsgs;
  }

  public String[] getOutputMsgTypes() {
    return new String[]{"UNKNOWN"};
  }

  /**
   * Checks how long the component has been started. Need to be a listener on
   * component state changes. 
   */
  public Date getStartedSince() {
    return lastStarted;
  }

  public void stateChanged(ILifecycleComponent component, State newState) {
    if(newState == State.STARTED){
      lastStarted = new Date();
    }
    else{
      lastStarted = null;
    }
  }
}
