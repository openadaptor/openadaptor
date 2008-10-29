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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.openadaptor.core.IComponent;
import org.openadaptor.core.Message;
import org.openadaptor.core.Response;
import org.openadaptor.core.exception.OAException;
import org.openadaptor.core.lifecycle.ILifecycleComponent;
import org.openadaptor.core.lifecycle.State;
import org.openadaptor.core.recordable.IDetailedComponentMetrics;
import org.openadaptor.core.recordable.IRecordableComponent;

/**
 * Class that maintains all metrics associated with one Node.
 * 
 * DRAFT. NOT READY FOR USE.
 * 
 * @author Kris Lachor
 */
public class ComponentMetrics implements IDetailedComponentMetrics{
  
  private static final Log log = LogFactory.getLog(ComponentMetrics.class.getName());
  
  public static final String ARRAY_OF ="array_of_";
  
  public static final String HETEROGENEOUS_TYPES = "heterogeneous_types";
  
  private static final String MILLISECONDS = "ms";
  
  private static final String UNKNOWN = "Unknown";
  
  protected static final String NONE = "None";
  
  protected static String MESSAGES_OF_TYPE = " message(s) of type ";
  
  private static final String LESS_THAN_ONE =  "less than 1 ";
  
  protected static final String METRICS_DISABLED = "Metrics recording DISABLED";
  
  private Map inputMsgCounter = new HashMap();
  
  private Map outputMsgCounter = new HashMap();
  
  private IRecordableComponent monitoredComponent;
  
  long minProcessTime = -1;
  
  long maxProcessTime = -1;
  
  long totalProcessTime = 0;
  
  long outputMsgs = 0;
  
  long discardedMsgs = 0;
  
  long exceptionMsgs = 0;
  
  private Date processStartTime;
  
  private Date processEndTime;
  
  private Date componentStartTime;
  
  private Date componentStopTime;
  
  long minIntervalTime = -1;
  
  long maxIntervalTime = -1;
  
  long totalIntervalTime = 0;
  
  Date lastStarted = null;
  
  boolean enabled = false;

  PeriodFormatter periodFormatter = new PeriodFormatterBuilder()
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
    .appendSuffix(" hour", " hours")
    .appendSeparator(" ")
    .appendMinutes()
    .appendSuffix(" min", " mins")
    .appendSeparator(" ")
    .appendSeconds()
    .appendSuffix(" sec", " secs")
    .appendSeparator(" ")
    .appendMillis()
    .appendSuffix(" millisec", " " + MILLISECONDS)
    .toFormatter();
  
//  /**
//   * Constructor.
//   */
//  protected ComponentMetrics() {
//    super();
//  }
//  
//  
//  
//  protected ComponentMetrics(boolean enabled) {
//    super();
//    this.enabled = enabled;
//  }



  protected ComponentMetrics(IRecordableComponent monitoredComponent) {
//    this();
    this.monitoredComponent = monitoredComponent;
  }

  public void recordComponentStart(){
    componentStartTime = new Date();
  }
  
  public void recordComponentStop(){
    componentStopTime = new Date();
  }

  /**
   * Records input message. The <code>inputMsgCounter</code> maps types of input 
   * messages to the number of occurences. A distintion is made of messages with
   * single elements and messages with arrays of elements. Furthermore, when
   * dealing with an array or elements a check is made of elements are of the
   * same type. Samples of <code>inputMsgCounter</code> are:
   * 
   * a)
   * java.lang.String -> 3434
   * ----------------------------------
   * b)
   * array_of_java.lang.String -> 123
   * ----------------------------------
   * c)
   * array_of_heterogeneous_types -> 3
   * java.lang.Short -> 15
   * 
   * Starts timers.
   * 
   * @param msg
   */
  public void recordMessageStart(Message msg){
    if(!enabled){
      return;
    }
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
    String id = monitoredComponent==null?"Unknown":monitoredComponent.getId();
    
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
    if(!enabled){
      return;
    }
   
    if(processStartTime!=null){
      processEndTime = new Date();
      long processTime = processEndTime.getTime() - processStartTime.getTime();
      totalProcessTime+=processTime;
      if(maxProcessTime<processTime){
        maxProcessTime=processTime;
      }
      if(minProcessTime>processTime || minProcessTime==-1){
        minProcessTime=processTime;
      }
    }
    else{
      throw new OAException("Could not match input and output messages.");
    }
     
    outputMsgs++;
    
    Object [] collatedOutput = response.getCollatedOutput();
    if(collatedOutput.length==0){
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
  }

  
  public void recordDiscardedMsgEnd(Message msg){
    if(!enabled){
      return;
    }
    discardedMsgs++;
  }
  
  
  public void recordExceptionMsgEnd(Message msg){
    if(!enabled){
      return;
    }
    exceptionMsgs++;
  }
  
  /**
   * How many input messages entered the component. 
   */
  public long [] getInputMsgCounts(){
    long count = 0;
    Iterator it = inputMsgCounter.keySet().iterator();
    while(it.hasNext()){
      Object dataType = it.next();
      Long countLong = (Long) inputMsgCounter.get(dataType);
      count+= countLong.longValue();
    }
    return new long[]{count};
  }
  
  //TODO was copy&paste of input
  public long [] getOutputMsgCounts(){
    long count = 0;
    Iterator it = outputMsgCounter.keySet().iterator();
    while(it.hasNext()){
      Object dataType = it.next();
      Long countLong = (Long) outputMsgCounter.get(dataType);
      count+= countLong.longValue();
    }
    return new long[]{count};
  }
  
  /**
   * Collates numeric data about messages that left the component, in human readable format.
   */
  public String getOutputMsgs() {
    if(!enabled){
      return METRICS_DISABLED;
    }
    StringBuffer outputMsgs = new StringBuffer();
    if(getOutputMsgCounts()[0]==0){
      outputMsgs.append(NONE);
    }
    Iterator it = outputMsgCounter.keySet().iterator();
    boolean first = true;
    while(it.hasNext()){
      if(first){
        first = false;
      }
      else{
        outputMsgs.append("/n");
      }
      Object dataType = it.next();
      Long countLong = (Long) outputMsgCounter.get(dataType);
      outputMsgs.append(countLong);
      outputMsgs.append(MESSAGES_OF_TYPE);
      outputMsgs.append(dataType);
    }
    return outputMsgs.toString();
  }


  /**
   * Collates numeric data about messages that entered the component, in human readable format.
   */
  public String getInputMsgs() {
    if(!enabled){
      return METRICS_DISABLED;
    }
    StringBuffer inputMsgs = new StringBuffer();
    if(getInputMsgCounts()[0]==0){
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
  
  public String getProcessTime() {
    if(!enabled){
      return METRICS_DISABLED;
    }
    long msgCount = getInputMsgCounts()[0];
    long timeAvgMs = -1;
    if(msgCount!=0){
      timeAvgMs = (long) (totalProcessTime/msgCount);
    }
    return formatDuration(timeAvgMs, minProcessTime, maxProcessTime);
  }

  private String formatDuration(long duration){
    StringBuffer sb = new StringBuffer();
    if(duration==0){
      sb.append(LESS_THAN_ONE);
      sb.append(MILLISECONDS);
    }
    else if(duration==-1){
      sb.append(UNKNOWN);
    }
    else{
      sb.append(periodFormatter.print(new Period(duration)));
    }
    return sb.toString();
  }
  
  private String formatDuration(long duration, long durationMin, long durationMax){
    StringBuffer sb = new StringBuffer();
    if(duration==0){
      sb.append("Less than 1 ");
      sb.append(MILLISECONDS);
    }
    else if(duration==-1){
      sb.append(UNKNOWN);
    }
    else{
      sb.append(periodFormatter.print(new Period(duration)));
      sb.append(" (min ");
      sb.append(formatDuration(durationMin));
      sb.append(", max ");
      sb.append(periodFormatter.print(new Period(durationMax)));
      sb.append(")");
    }
    return sb.toString();
  }
  
  public String getProcessTimeMin() {
    return formatDuration(minProcessTime);
  }
  
  public String getProcessTimeMax() {
    return formatDuration(maxProcessTime);
  }

  public String [] getInputMsgTypes() {
    Set inputMsgTypes = inputMsgCounter.keySet();
    return (String[]) inputMsgTypes.toArray(new String[]{});
  }

  public String getIntervalTime() {
    if(!enabled){
      return METRICS_DISABLED;
    }
    long msgCount = getInputMsgCounts()[0];
    long timeAvgMs = -1;
    if(msgCount!=0){
      timeAvgMs = (long) (totalIntervalTime/msgCount);
    }
    return formatDuration(timeAvgMs, minIntervalTime, maxIntervalTime);
  }

  public String getIntervalTimeMax() {
    return formatDuration(maxIntervalTime);
  }

  public String getIntervalTimeMin() {
    return formatDuration(minIntervalTime);
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
    return new String[]{UNKNOWN};
  }

  /**
   * Checks how long the component has been started. Need to be a listener on
   * component state changes. 
   */
  public String getUptime() {
    if(lastStarted==null){
      return UNKNOWN;
    }
    long uptime = new Date().getTime() - lastStarted.getTime();
    return  formatDuration(uptime);
  }

  public void stateChanged(ILifecycleComponent component, State newState) {
    if(newState == State.STARTED){
      lastStarted = new Date();
    }
    else{
      lastStarted = null;
    }
  }

  public void disable() {
    this.enabled = false;
  }

  public void enable() {
    this.enabled = true;
    
  }
  public boolean enabled() {
    return this.enabled;
  }

  protected Date getProcessStartTime() {
    return processStartTime;
  }

  protected Map getInputMsgCounter() {
    return inputMsgCounter;
  }

  protected Date getProcessEndTime() {
    return processEndTime;
  }

  protected Map getOutputMsgCounter() {
    return outputMsgCounter;
  }

  public void setMetricsEnabled(boolean metricsEnabled) {
    enabled = metricsEnabled;
  }

  public boolean isMetricsEnabled() {
    return enabled;
  }

  public IComponent getComponent() {
    return monitoredComponent;
  }
 
}
