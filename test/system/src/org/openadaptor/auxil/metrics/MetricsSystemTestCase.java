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

import java.util.Iterator;
import java.util.Arrays;

import org.openadaptor.core.IMessageProcessor;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.node.ReadNode;
import org.openadaptor.core.node.WriteNode;
import org.openadaptor.core.recordable.IComponentMetrics;
import org.openadaptor.core.recordable.IRecordableComponent;
import org.openadaptor.spring.SpringAdaptor;
import org.openadaptor.util.SystemTestUtil;

import junit.framework.TestCase;

/**
 * System tests for {@link ComponentMetrics}, {@link AggregateMetrics} 
 * and recording metrics in general.
 * 
 * @author Kris Lachor
 */
public class MetricsSystemTestCase extends TestCase {

  private static final String RESOURCE_LOCATION = "test/system/src/";
  private static final String ADAPTOR_1 = "metrics-adaptor1.xml";
  private static final String ADAPTOR_2 = "metrics-adaptor2.xml";
  private static final String ADAPTOR_3 = "metrics-adaptor3.xml";
  private static final String ADAPTOR_4 = "metrics-adaptor4.xml";
  private static final String ADAPTOR_5 = "metrics-adaptor5.xml";
  
  /**
   * Adaptor with a reader and a writer. Sends one message (String). 
   * Metrics are disabled.
   */
  public void testDisabled() throws Exception{
    SpringAdaptor springAdaptor = SystemTestUtil.runAdaptor(this, RESOURCE_LOCATION, ADAPTOR_1);
    Adaptor adaptor = springAdaptor.getAdaptor();
    assertTrue(adaptor.getExitCode()==0);
    
    /* Check Adaptor's metrics are disabled */
    assertFalse(adaptor.getMetrics().isMetricsEnabled());
    assertEquals(adaptor.getMetrics().getInputMsgCounts().length, 0);
    assertEquals(adaptor.getMetrics().getOutputMsgCounts().length, 0);
    assertEquals(adaptor.getMetrics().getUptime(), ComponentMetrics.UNKNOWN);
     
    /* same checks for all Nodes */
    Iterator it = adaptor.getMessageProcessors().iterator();
    while(it.hasNext()){
      IMessageProcessor mProcessor = (IMessageProcessor) it.next();
      if(mProcessor instanceof IRecordableComponent){
        IRecordableComponent recComp = (IRecordableComponent) mProcessor;
        assertFalse(recComp.getMetrics().isMetricsEnabled());
        assertEquals(recComp.getMetrics().getInputMsgCounts().length, 0);
        assertEquals(recComp.getMetrics().getOutputMsgCounts().length, 0);
        assertEquals(recComp.getMetrics().getUptime(), ComponentMetrics.UNKNOWN);
      }
    }
  }
  
  /**
   * Adaptor with a reader and a writer. Sends one message (String). 
   * Metrics enabled.
   */
  public void testEnabled() throws Exception{
    SpringAdaptor springAdaptor = SystemTestUtil.runAdaptor(this, RESOURCE_LOCATION, ADAPTOR_2);
    Adaptor adaptor = springAdaptor.getAdaptor();
    assertTrue(adaptor.getExitCode()==0);
    
    /* Check Adaptor's metrics are enabled */
    assertTrue(adaptor.getMetrics().isMetricsEnabled());

    IComponentMetrics metrics = adaptor.getMetrics();
    
    assertTrue(Arrays.equals(metrics.getInputMsgCounts(), new long[]{1}));
    assertTrue(Arrays.equals(metrics.getInputMsgTypes(), new String[]{"java.lang.String"}));
    assertNotNull(metrics.getInputMsgs());

    assertTrue(Arrays.equals(metrics.getOutputMsgCounts(), new long[]{1}));
    assertNotNull(metrics.getOutputMsgs());

    assertTrue(metrics.getProcessTimeMin() <= metrics.getProcessTimeAvg());
    assertTrue(metrics.getProcessTimeAvg() <= metrics.getProcessTimeMax());
    
    assertTrue(metrics.getIntervalTimeMin() <= metrics.getIntervalTimeAvg());
    assertTrue(metrics.getIntervalTimeAvg() <= metrics.getIntervalTimeMax());
        
    assertTrue(Arrays.equals(metrics.getOutputMsgTypes(), new String[]{"java.lang.String"}));
    
    /* same checks for all Nodes */
    Iterator it = adaptor.getMessageProcessors().iterator();
    while(it.hasNext()){
      IMessageProcessor mProcessor = (IMessageProcessor) it.next();
      if(mProcessor instanceof IRecordableComponent){
        metrics = ((IRecordableComponent) mProcessor).getMetrics();
        assertTrue(metrics.isMetricsEnabled());
        
        assertTrue(Arrays.equals(metrics.getInputMsgCounts(), new long[]{1}));
        assertNotNull(metrics.getInputMsgs());
        
        /* Write node won't have output messages */
        if(mProcessor instanceof WriteNode){
          assertTrue(Arrays.equals(metrics.getOutputMsgCounts(), new long[0]));
        }
        else{
          assertTrue(Arrays.equals(metrics.getOutputMsgCounts(), new long[]{1}));
          assertTrue(Arrays.equals(metrics.getOutputMsgTypes(), new String[]{"java.lang.String"}));  
        }
        
        assertTrue(metrics.getProcessTimeMin() <= metrics.getProcessTimeAvg());
        assertTrue(metrics.getProcessTimeAvg() <= metrics.getProcessTimeMax());
        
        assertTrue(metrics.getIntervalTimeMin() <= metrics.getIntervalTimeAvg());
        assertTrue(metrics.getIntervalTimeAvg() <= metrics.getIntervalTimeMax());
   
        assertTrue(Arrays.equals(metrics.getInputMsgTypes(), new String[]{"java.lang.String"}));
      }
    }
  }
  
  /**
   * Adaptor with enabled metrics. Check components' uptime.
   */
  public void testUptime() throws Exception {
    SpringAdaptor springAdaptor = SystemTestUtil.runAdaptor(this, RESOURCE_LOCATION, ADAPTOR_2);
    Adaptor adaptor = springAdaptor.getAdaptor();
    
    IComponentMetrics adaptorMetrics = adaptor.getMetrics();
    assertTrue(! ComponentMetrics.UNKNOWN.equals(adaptorMetrics.getUptime()));
    
    /* same checks for all Nodes */
    for(Iterator it=adaptor.getMessageProcessors().iterator(); it.hasNext(); ){
      IMessageProcessor mProcessor = (IMessageProcessor) it.next();
      if(mProcessor instanceof IRecordableComponent){
        IRecordableComponent recComp = (IRecordableComponent) mProcessor;
        assertTrue(! ComponentMetrics.UNKNOWN.equals(recComp.getMetrics().getUptime()));
      }
    }
  }
  
  /**
   * Adaptor with enabled metrics. One message sent. Checks the min, max and avg process
   * times are the same.
   */
  public void testProcessTime() throws Exception {
    SpringAdaptor springAdaptor = SystemTestUtil.runAdaptor(this, RESOURCE_LOCATION, ADAPTOR_2);
    Adaptor adaptor = springAdaptor.getAdaptor();
    
    ComponentMetrics adaptorMetrics = (ComponentMetrics) adaptor.getMetrics();
   
    assertEquals(adaptorMetrics.getProcessTimeAvg(), adaptorMetrics.getProcessTimeMin());
    assertEquals(adaptorMetrics.getProcessTimeAvg(), adaptorMetrics.getProcessTimeMax());
  }
  
  /**
   * Runs adaptor with enabled metrics. One message sent. Processor throws excepion.
   */
  public void testRecordingExceptions() throws Exception {
    SpringAdaptor springAdaptor = SystemTestUtil.runAdaptor(this, RESOURCE_LOCATION, ADAPTOR_3);
    Adaptor adaptor = springAdaptor.getAdaptor();
    
    IComponentMetrics adaptorMetrics = (IComponentMetrics) adaptor.getMetrics();
   
    assertEquals(adaptorMetrics.getExceptionMsgCount(), 1);
    assertNotSame(adaptorMetrics.getDiscardsAndExceptions(), ComponentMetrics.NONE);
    
    for(Iterator it=adaptor.getMessageProcessors().iterator(); it.hasNext(); ){
      IMessageProcessor mProcessor = (IMessageProcessor) it.next();
      if(mProcessor instanceof IRecordableComponent){
        IRecordableComponent recComp = (IRecordableComponent) mProcessor;
        if(recComp instanceof WriteNode){
          assertEquals(recComp.getMetrics().getExceptionMsgCount(), 0);
          assertEquals(recComp.getMetrics().getDiscardsAndExceptions(), ComponentMetrics.NONE);
        }
        else if(recComp instanceof ReadNode){
          assertEquals(recComp.getMetrics().getExceptionMsgCount(), 0);
          assertEquals(recComp.getMetrics().getDiscardsAndExceptions(), ReaderMetrics.NOT_APPLICABLE_FOR_READERS);
        }
        else{
          assertEquals(recComp.getMetrics().getExceptionMsgCount(), 1);	
          assertNotSame(recComp.getMetrics().getDiscardsAndExceptions(), ComponentMetrics.NONE);
        }
      }
    }
  }
  
  /**
   * Tests AggregateMetrics (metrics used by Adaptor/Router). Two parallel write nodes.
   */
  public void testAggregateMetricsFanOut() throws Exception {
    SpringAdaptor springAdaptor = SystemTestUtil.runAdaptor(this, RESOURCE_LOCATION, ADAPTOR_4);
    IComponentMetrics metrics = (IComponentMetrics) springAdaptor.getAdaptor().getMetrics();
    assertTrue(Arrays.equals(metrics.getInputMsgCounts(), new long[]{1}));
    assertTrue(Arrays.equals(metrics.getInputMsgTypes(), new String[]{"java.lang.String"}));
    
    /* Messages going to fan-out are from then on counted as separate messages */
    assertTrue(Arrays.equals(metrics.getOutputMsgCounts(), new long[]{1,1}));
    assertTrue(Arrays.equals(metrics.getOutputMsgTypes(), new String[]{"java.lang.String", "java.lang.String"}));
  }
  
  /**
   * Recreates a problem found in testing, where an adaptor reads one line from a file,
   * processes it and the reader reports negative between message time interval (instead of unknown).
   */
  public void testTimeInterval() throws Exception {
    SpringAdaptor springAdaptor = SystemTestUtil.runAdaptor(this, RESOURCE_LOCATION, ADAPTOR_5);
    assertTrue(springAdaptor.getAdaptor().getExitCode()==0);
    IComponentMetrics metrics = (IComponentMetrics) springAdaptor.getAdaptor().getMetrics();
    assertEquals(metrics.getIntervalTimeAvg(), -1);
    assertEquals(metrics.getIntervalTimeMin(), -1);
    assertEquals(metrics.getIntervalTimeMax(), -1);
    for(Iterator it= springAdaptor.getAdaptor().getMessageProcessors().iterator(); it.hasNext(); ){
      IMessageProcessor mProcessor = (IMessageProcessor) it.next();
      if(mProcessor instanceof IRecordableComponent){
        metrics = ((IRecordableComponent) mProcessor).getMetrics();
        assertEquals(metrics.getIntervalTimeAvg(), -1);
        assertEquals(metrics.getIntervalTimeMin(), -1);
        assertEquals(metrics.getIntervalTimeMax(), -1);
      }
    }
  }
}
