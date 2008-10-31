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
  
  /**
   * Runs adaptor with a reader and a writer. Reader sends one message (String). 
   * Metrics are disabled.
   */
  public void testMetricsDisabled() throws Exception{
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
   * Runs adaptor with a reader and a writer. Reader sends one message (String). 
   * Metrics are enabled.
   */
  public void testMetricsEnabled() throws Exception{
    SpringAdaptor springAdaptor = SystemTestUtil.runAdaptor(this, RESOURCE_LOCATION, ADAPTOR_2);
    Adaptor adaptor = springAdaptor.getAdaptor();
    assertTrue(adaptor.getExitCode()==0);
    
    /* Check Adaptor's metrics are enabled */
    assertTrue(adaptor.getMetrics().isMetricsEnabled());

    assertTrue(Arrays.equals(adaptor.getMetrics().getInputMsgCounts(), new long[]{1}));
    assertTrue(Arrays.equals(adaptor.getMetrics().getInputMsgTypes(), new String[]{"java.lang.String"}));
    assertNotNull(adaptor.getMetrics().getInputMsgs());

    assertTrue(Arrays.equals(adaptor.getMetrics().getOutputMsgCounts(), new long[]{1}));
    assertNotNull(adaptor.getMetrics().getOutputMsgs());

    //TODO
//    assertTrue(Arrays.equals(adaptor.getMetrics().getOutputMsgTypes(), new String[]{"java.lang.String"}));
    
    /* same checks for all Nodes */
    Iterator it = adaptor.getMessageProcessors().iterator();
    while(it.hasNext()){
      IMessageProcessor mProcessor = (IMessageProcessor) it.next();
      if(mProcessor instanceof IRecordableComponent){
        IRecordableComponent recComp = (IRecordableComponent) mProcessor;
        assertTrue(recComp.getMetrics().isMetricsEnabled());
        
        assertTrue(Arrays.equals(recComp.getMetrics().getInputMsgCounts(), new long[]{1}));
        assertNotNull(recComp.getMetrics().getInputMsgs());
        assertTrue(Arrays.equals(recComp.getMetrics().getOutputMsgCounts(), new long[]{1}));
        
        //TODO check types?
      }
    }
  }
  
  /**
   * Runs adaptor with enabled metrics. Check components' uptime.
   */
  public void testUptime() throws Exception {
    SpringAdaptor springAdaptor = SystemTestUtil.runAdaptor(this, RESOURCE_LOCATION, ADAPTOR_2);
    Adaptor adaptor = springAdaptor.getAdaptor();
    
    IComponentMetrics adaptorMetrics = adaptor.getMetrics();
    
    //TODO Router/Adaptor doesn't do uptime atm
//    assertTrue(! ComponentMetrics.UNKNOWN.equals(adaptorMetrics.getUptime()));
    
    for(Iterator it=adaptor.getMessageProcessors().iterator(); it.hasNext(); ){
      IMessageProcessor mProcessor = (IMessageProcessor) it.next();
      if(mProcessor instanceof IRecordableComponent){
        IRecordableComponent recComp = (IRecordableComponent) mProcessor;
        assertTrue(! ComponentMetrics.UNKNOWN.equals(recComp.getMetrics().getUptime()));
      }
    }
  }
}
