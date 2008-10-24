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

import org.openadaptor.core.IMessageProcessor;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.recordable.IRecordableComponent;
import org.openadaptor.spring.SpringAdaptor;
import org.openadaptor.util.SystemTestUtil;

import junit.framework.TestCase;

/**
 * System tests for {@link ComponentMetrics}.
 * 
 * @author Kris Lachor
 */
public class ComponentMetricsSystemTestCase extends TestCase {

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
    assertEquals(adaptor.getMetrics().getInputMsgCounts()[0], 0);
    
    /* same checks for all Nodes */
    Iterator it = adaptor.getMessageProcessors().iterator();
    while(it.hasNext()){
      IMessageProcessor mProcessor = (IMessageProcessor) it.next();
      if(mProcessor instanceof IRecordableComponent){
        IRecordableComponent recComp = (IRecordableComponent) mProcessor;
        assertFalse(recComp.getMetrics().isMetricsEnabled());
        assertEquals(recComp.getMetrics().getInputMsgCounts()[0], 0);
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
    assertEquals(adaptor.getMetrics().getInputMsgCounts()[0], 1);
    
    /* same checks for all Nodes */
    Iterator it = adaptor.getMessageProcessors().iterator();
    while(it.hasNext()){
      IMessageProcessor mProcessor = (IMessageProcessor) it.next();
      if(mProcessor instanceof IRecordableComponent){
        IRecordableComponent recComp = (IRecordableComponent) mProcessor;
        assertTrue(recComp.getMetrics().isMetricsEnabled());
        assertEquals(recComp.getMetrics().getInputMsgCounts()[0], 1);
      }
    }
  }
  
}
