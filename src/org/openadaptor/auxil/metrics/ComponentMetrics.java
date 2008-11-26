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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.openadaptor.core.Message;
import org.openadaptor.core.Response;
import org.openadaptor.core.lifecycle.ILifecycleComponent;
import org.openadaptor.core.lifecycle.ILifecycleListener;
import org.openadaptor.core.lifecycle.State;
import org.openadaptor.core.recordable.IComponentMetrics;
import org.openadaptor.core.recordable.IRecordableComponent;
import org.openadaptor.core.recordable.ISimpleComponentMetrics;

/**
 * Class that records and computes message metrics for a single 
 * {@link IRecordableComponent}.
 * 
 * Not thread-safe. 
 * 
 * @see IComponentMetrics
 * @see IRecordableComponent
 * @author Kris Lachor
 */
public class ComponentMetrics implements IComponentMetrics, ILifecycleListener{
   
  /* Constants */
  private   static final Log log = LogFactory.getLog(ComponentMetrics.class.getName());
  public    static final String ARRAY_OF ="array_of_";
  public    static final String HETEROGENEOUS_TYPES = "heterogeneous_types";
  private   static final String MILLISECONDS = "ms";
  protected static final String UNKNOWN = "Unknown";
  protected static final long   UNKNOWN_LONG = -1;
  protected static final String NONE = "None";
  protected static final long   NONE_LONG = 0;
  protected static final String NOT_APPLICABLE = "N/A";
  protected static final String MESSAGES_OF_TYPE = " message(s) of type ";
  private   static final String LESS_THAN_ONE =  "less than 1";
  protected static final String METRICS_DISABLED = "Metrics recording DISABLED";
  private   static final String SEPARATOR = ", ";

  private IRecordableComponent monitoredComponent;
  
  protected Map inputMsgCounter = new HashMap();
  
  protected Map outputMsgCounter = new HashMap();
  
  long minProcessTime = UNKNOWN_LONG;
  
  long maxProcessTime = UNKNOWN_LONG;
  
  long lastProcessTime = UNKNOWN_LONG;
  
  long totalProcessTime = NONE_LONG;
  
  long outputMsgs = NONE_LONG;
  
  long discardedMsgs = NONE_LONG;
  
  long exceptionMsgs = NONE_LONG;
  
  protected Date processStartTime;
  
  protected Date processEndTime;
  
  long minIntervalTime = UNKNOWN_LONG;
  
  long maxIntervalTime = UNKNOWN_LONG;
  
  long totalIntervalTime = NONE_LONG;
  
  /** When the component last chagned state to STARTED */
  Date lastStarted = null;

  /** When the component last chagned state to STOPPED */
  Date lastStopped = null;
  
  /** Are metrics enabled */
  boolean enabled = false;
  
  /** Flag that indicates if the component is currently processing a message. */
  boolean currentlyProcessing = false;

  private PeriodFormatter periodFormatter = new PeriodFormatterBuilder()
              .printZeroRarelyLast()
              .appendYears()
              .appendSuffix(" year", " years")
              .appendSeparator(" ")
              .appendMonths()
              .appendSuffix(" month", " months")
              .appendSeparator(" ")
              .appendDays()
              .appendSuffix(" day", " days")
              .appendSeparator(" ")
              .appendHours()
              .appendSuffix("h", "h")
              .appendSeparator(" ")
              .appendMinutes()
              .appendSuffix(" min", " mins")
              .appendSeparator(" ")
              .appendSeconds()
              .appendSuffix("s", "s")
              .appendSeparator(" ")
              .appendMillis()
              .appendSuffix(MILLISECONDS, MILLISECONDS)
              .toFormatter();


  /**
   * Constructor.
   * 
   * @param monitoredComponent the associated recordable component.
   */
  protected ComponentMetrics(IRecordableComponent monitoredComponent) {
    this.monitoredComponent = monitoredComponent;
  }

  /**
   * Constructor.
   * 
   * @param monitoredComponent the associated recordable component.
   * @param enabled wheather or not the metrics are to be enabled.
   */
  protected ComponentMetrics(IRecordableComponent monitoredComponent, boolean enabled) {
    this.monitoredComponent = monitoredComponent;
    this.enabled = enabled;
  }

  /**
   * Records input message. The <code>inputMsgCounter</code> maps types of input 
   * messages to the number of occurences. A distintion is made between messages 
   * with single elements and arrays. When dealing with an array or elements a 
   * check is made of elements are of the same type. 
   * Examples of <code>inputMsgCounter</code> are:
   * 
   * <ul>
   * <li>java.lang.String          -> 3434</li>
   * <li>array_of_java.lang.String -> 123</li>
   * <li>
   * array_of_heterogeneous_types  -> 3
   * java.lang.Short               -> 15
   * </li>
   * </ul>
   * 
   * Starts timers.
   * 
   * @param msg the recorded message.
   */
  public void recordMessageStart(Message msg){
    if(!enabled || msg==null){
      return;
    }
    processStartTime = new Date();
    currentlyProcessing = true;
    
    /* calculate intervals */
    if(! (processEndTime==null)){
      long intervalTime = processStartTime.getTime() - processEndTime.getTime();
      totalIntervalTime+=intervalTime;
      if(maxIntervalTime<intervalTime){
        maxIntervalTime=intervalTime;
      }
      if(minIntervalTime>intervalTime || minIntervalTime==UNKNOWN_LONG){
        minIntervalTime=intervalTime;
      }
    }
    
    Object [] data = msg.getData();
    if(data.length==0){
      return;
    }
    StringBuffer msgPayloadType = new StringBuffer();
    
    /* If data made up of more than one element it'll be described as an array */
    if(data.length>1){
      msgPayloadType.append(ARRAY_OF);
      
      /* Check if array holds homogeneous or heterogeneous types */
      String firstClass = data[0].getClass().getName();
      boolean sameTypes = true;
      for(int i=1; i<data.length; i++){
        if(! data[i].getClass().getName().equals(firstClass)){
          sameTypes = false;
          break;
        }
      }
      if(sameTypes){
        msgPayloadType.append(data[0].getClass().getName());
      }
      else{
        msgPayloadType.append(HETEROGENEOUS_TYPES);
      }
    }
    
    /* Data holds one element*/
    else{
      msgPayloadType.append(data[0].getClass().getName());
    }
    String msgPayloadTypeStr = msgPayloadType.toString();
  
    /* Increase the counter */
    Object count = inputMsgCounter.get(msgPayloadTypeStr);
    if(count==null){
      inputMsgCounter.put(msgPayloadTypeStr, new Long(1));
    }
    else{
      Long countLong = (Long) count;
      inputMsgCounter.put(msgPayloadTypeStr, new Long(countLong.longValue()+1));
    }
  }
  
  /**
   * Records end of message processing. 
   * 
   * @param msg holds the original message that entered the component.
   * @param response holds the response to the original message. 
   */
  public void recordMessageEnd(Message msg, Response response){
    if(!enabled || msg==null || response==null){
      return;
    }
    if(processStartTime!=null){
      processEndTime = new Date();
      long processTime = processEndTime.getTime() - processStartTime.getTime();
      totalProcessTime+=processTime;
      lastProcessTime=processTime;
      if(maxProcessTime<processTime){
        maxProcessTime=processTime;
      }
      if(minProcessTime>processTime || minProcessTime==UNKNOWN_LONG){
        minProcessTime=processTime;
      }
    }
    else{
      log.warn("Could not match input and output messages.");
    }
     
    outputMsgs++;
    
    Object [] collatedOutput = response.getCollatedOutput();
    if(collatedOutput.length==0){
      currentlyProcessing=false;
      return;
    }
    
    StringBuffer msgPayloadType = new StringBuffer();
    
    /* If the node did not split the message into multiple messages */
    if(collatedOutput.length==1){

      /* Dealing with a batch */
      if(collatedOutput[0].getClass().isArray()){
      
        Object [] output = (Object[]) collatedOutput[0]; 
        if(output.length>1){
          msgPayloadType.append(ARRAY_OF);
          
          /* Check if array holds homogeneous or heterogeneous types */
          String firstClass = output[0].getClass().getName();
          boolean sameTypes = true;
          for(int i=1; i<output.length; i++){
            if(! output[i].getClass().getName().equals(firstClass)){
              sameTypes = false;
              break;
            }
          }
          if(sameTypes){
            msgPayloadType.append(output[0].getClass().getName());
          }
          else{
            msgPayloadType.append(HETEROGENEOUS_TYPES);
          }
        }
        /* batch holds one element */
        else{
          msgPayloadType.append(output[0].getClass().getName());
        }
        
      }
      /* Dealing with a single element */
      else{
        msgPayloadType.append(collatedOutput[0].getClass().getName()); 
      }
    }
   
    String msgPayloadTypeStr = msgPayloadType.toString();
  
    /* Increase the counter */
    Object count = outputMsgCounter.get(msgPayloadTypeStr);
    if(count==null){
      outputMsgCounter.put(msgPayloadTypeStr, new Long(1));
    }
    else{
      Long countLong = (Long) count;
      outputMsgCounter.put(msgPayloadTypeStr, new Long(countLong.longValue()+1));
    }
    currentlyProcessing = false;
  }

  /**
   * @see IComponentMetrics#recordDiscardedMsgEnd(Message)
   */
  public void recordDiscardedMsgEnd(Message msg){
    if(!enabled || msg==null){
      return;
    }
    discardedMsgs++;
    currentlyProcessing = false;
  }
  
  /**
   * @see IComponentMetrics#recordExceptionMsgEnd(Message) 
   */
  public void recordExceptionMsgEnd(Message msg){
    if(!enabled || msg==null){
      return;
    }
    exceptionMsgs++;
    currentlyProcessing = false;
  }
  
  /**
   * How many input messages entered the component. Separate counters
   * per message types.
   * 
   * @see IComponentMetrics#getInputMsgCounts()
   */
  public long [] getInputMsgCounts(){
    return getMsgCounts(inputMsgCounter);
  }
  
  /**
   * How many input messages left the component. Separate counters
   * per message types.
   * 
   * @see IComponentMetrics#getOutputMsgCounts()
   */
  public long [] getOutputMsgCounts(){
    return getMsgCounts(outputMsgCounter);
  }
  
  private long [] getMsgCounts(Map msgCounter){
    long [] result = new long[msgCounter.keySet().size()];
    int i=0;
    for(Iterator it=msgCounter.keySet().iterator(); it.hasNext();){     
      Long countLong = (Long) msgCounter.get(it.next());
      result[i++] = countLong.longValue();
    }
    return result;
  }
  
  /**
   * @see IComponentMetrics#getInputMsgTypes()
   */
  public String [] getInputMsgTypes() {
    return (String[]) inputMsgCounter.keySet().toArray(new String[]{});
  }
  
  /**
   * @see IComponentMetrics#getOutputMsgTypes()
   */
  public String[] getOutputMsgTypes() {
    return (String[]) outputMsgCounter.keySet().toArray(new String[]{});
  }

  /**
   * Collates numeric data about messages that entered the component, in human readable format.
   */
  public String getInputMsgs() {
    if(!enabled){
      return METRICS_DISABLED;
    }
    StringBuffer inputMsgs = new StringBuffer();
    if(getInputMsgCounts().length==0){
      inputMsgs.append(NONE);
    }
    Iterator it = inputMsgCounter.keySet().iterator();
    boolean first = true;
    while(it.hasNext()){
      if(first){
        first = false;
      }
      else{
        inputMsgs.append("/n");
      }
      Object dataType = it.next();
      Long countLong = (Long) inputMsgCounter.get(dataType);
      inputMsgs.append(countLong);
      inputMsgs.append(MESSAGES_OF_TYPE);
      inputMsgs.append(dataType);
    }
    return inputMsgs.toString();
  }
  
  /**
   * Collates numeric data about messages that left the component, in human readable format.
   */
  public String getOutputMsgs() {
    if(!enabled){
      return METRICS_DISABLED;
    }
    StringBuffer outputMsgs = new StringBuffer();
    long [] outMsgCounts = getOutputMsgCounts();
    if(outMsgCounts.length == 0){
      outputMsgs.append(NONE);
    }
    String [] outMsgTypes = getOutputMsgTypes();
    for(int i=0;i<outMsgCounts.length;i++){
      if(i>0){
        outputMsgs.append(SEPARATOR);
      }
      outputMsgs.append(outMsgCounts[i]);
      outputMsgs.append(MESSAGES_OF_TYPE);
      outputMsgs.append(outMsgTypes[i]);
    }
    return outputMsgs.toString();
  }
  
  /**
   * @see ISimpleComponentMetrics#getDiscardsAndExceptions()
   */
  public String getDiscardsAndExceptions() {
    if(!enabled){
      return METRICS_DISABLED;
    }
	StringBuffer exDisMsgs = new StringBuffer();  
	if(getExceptionMsgCount()==0 && getDiscardedMsgCount()==0){
	  exDisMsgs.append(NONE);	
	}
	else{
	  exDisMsgs.append(getDiscardedMsgCount());
	  exDisMsgs.append(" discarded message(s), ");
	  exDisMsgs.append(getExceptionMsgCount());
	  exDisMsgs.append(" exception(s).");
	}
	return exDisMsgs.toString();
  }

  private String formatDuration(long duration){
    StringBuffer sb = new StringBuffer();
    if(duration==0){
      sb.append(LESS_THAN_ONE);
      sb.append(MILLISECONDS);
    }
    else if(duration==UNKNOWN_LONG){
      sb.append(NOT_APPLICABLE);
    }
    else{
      sb.append(periodFormatter.print(new Period(duration)));
    }
    return sb.toString();
  }

  private String formatDuration(long duration, long durationMin, long durationMax, long durationLast){
    StringBuffer sb = new StringBuffer();
    if(duration==0){
      sb.append(LESS_THAN_ONE);
      sb.append(MILLISECONDS);
    }
    else if(duration==UNKNOWN_LONG){
      sb.append(NOT_APPLICABLE);
    }
    else if(duration == durationMin){
      sb.append(formatDuration(duration));
    }
    else{
      sb.append(periodFormatter.print(new Period(duration)));
      sb.append(" (min: ");
      sb.append(formatDuration(durationMin));
      sb.append(", max: ");
      sb.append(periodFormatter.print(new Period(durationMax)));
      if(durationLast!=UNKNOWN_LONG){
        sb.append(", last: ");
        sb.append(formatDuration(durationLast));
      }
      sb.append(")");
    }
    return sb.toString();
  }

  /**
   * @see IComponentMetrics#getProcessTimeMin()
   */
  public long getProcessTimeMin() {
    return minProcessTime;
  }
  
  /**
   * @see IComponentMetrics#getProcessTimeMax()
   */
  public long getProcessTimeMax() {
    return maxProcessTime;
  }
  
  /**
   * @see IComponentMetrics#getProcessTimeLast()
   */
  public long getProcessTimeLast() {
    return lastProcessTime;
  }

  /**
   * Gets total of input messages (irrespective of message types).
   */
  private long getInputMsgsCount(){
    long [] intpuMsgCounts = getInputMsgCounts();
    long msgCount = 0;
    for(int i=0; i<intpuMsgCounts.length; i++){
      msgCount+=intpuMsgCounts[i];
    }
    return msgCount;
  }
  
  /**
   * @see IComponentMetrics#getProcessTimeAvg()
   */
  public long getProcessTimeAvg(){
    if(maxProcessTime==UNKNOWN_LONG){
      /* Hasn't processed anything yet */
      return UNKNOWN_LONG;
    }
    long msgCount = getInputMsgsCount();
    if(currentlyProcessing){
      msgCount--;
    }
    long timeAvgMs = UNKNOWN_LONG;
    if(msgCount!=0){
      timeAvgMs = (long) (totalProcessTime/msgCount);
    }  
    return timeAvgMs;
  }
  
  /**
   * @see IComponentMetrics#getIntervalTimeAvg()
   */
  public long getIntervalTimeAvg(){
    if(maxIntervalTime==UNKNOWN_LONG){
      /* Hasn't processed at least 2 messages yet */
      return UNKNOWN_LONG;
    }
    long intervalsCount = getInputMsgsCount() - 1;
    if(currentlyProcessing){
      intervalsCount--;
    }
    long timeAvgMs = UNKNOWN_LONG;
    if(intervalsCount!=0){
      timeAvgMs = (long) (totalIntervalTime/intervalsCount);
    }  
    return timeAvgMs;
  }
  
  /**
   * @see ISimpleComponentMetrics#getProcessTime()
   */
  public String getProcessTime() {
    if(!enabled){
      return METRICS_DISABLED;
    }
    if(maxProcessTime==UNKNOWN_LONG){
      /* Hasn't processed anything yet */
      return UNKNOWN;
    }
    long timeAvgMs = getProcessTimeAvg();
    return formatDuration(timeAvgMs, minProcessTime, maxProcessTime, lastProcessTime);
  }
  
  /**
   * @see ISimpleComponentMetrics#getIntervalTime()
   */
  public String getIntervalTime() {
    if(!enabled){
      return METRICS_DISABLED;
    }
    if(maxIntervalTime==UNKNOWN_LONG){
      return UNKNOWN;
    }
    long timeAvgMs = getIntervalTimeAvg();
    return formatDuration(timeAvgMs, minIntervalTime, maxIntervalTime, UNKNOWN_LONG);
  }

  /**
   * @see IComponentMetrics#getIntervalTimeMin()
   */
  public long getIntervalTimeMin() {
    return minIntervalTime;
  }
  
  /**
   * @see IComponentMetrics#getIntervalTimeMax()
   */
  public long getIntervalTimeMax() {
    return maxIntervalTime;
  }

  /**
   * @see IComponentMetrics#getDiscardedMsgCount()
   */
  public long getDiscardedMsgCount() {
    return discardedMsgs;
  }

  /**
   * @see IComponentMetrics#getExceptionMsgCount()
   */
  public long getExceptionMsgCount() {
    return exceptionMsgs;
  }

  /**
   * Checks how long the component has been started. Need to be a listener on
   * component state changes. 
   */
  public String getUptime() {
    long uptime = 0;
    if(!enabled || lastStarted==null){
      return UNKNOWN;
    }
    else if(lastStopped!=null && lastStopped.after(lastStarted)){
      uptime = lastStopped.getTime() - lastStarted.getTime(); 
    }
    else{
      uptime = new Date().getTime() - lastStarted.getTime();
    }
    return formatDuration(uptime);
  }
 
  /**
   * Monitors component start and stop times.
   * 
   * @see ILifecycleListener#stateChanged(ILifecycleComponent, State)
   */
  public void stateChanged(ILifecycleComponent component, State newState) {
    if(newState == State.STARTED){
      lastStarted = new Date();
    }
    else if(newState == State.STOPPED){
      lastStopped = new Date();
    }
  }

  /**
   * @see IComponentMetrics#recordComponentStart()
   */
  public void recordComponentStart() {
	lastStarted = new Date();
  }

  /**
   * @see IComponentMetrics#recordComponentStop()
   */
  public void recordComponentStop() {
	lastStopped = new Date();
  }
  
  /**
   * @see ISimpleComponentMetrics#setMetricsEnabled(boolean)
   */
  public void setMetricsEnabled(boolean metricsEnabled) {
    enabled = metricsEnabled;
  }

  /**
   * @see ISimpleComponentMetrics#isMetricsEnabled()
   */
  public boolean isMetricsEnabled() {
    return enabled;
  }

  /**
   * @see IComponentMetrics#getComponent()
   */
  public IRecordableComponent getComponent() {
    return monitoredComponent;
  }
}
