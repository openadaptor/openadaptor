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

import org.openadaptor.core.Message;
import org.openadaptor.core.Response;

import junit.framework.TestCase;

/**
 * Unit tests for {@link AggregateMetrics}.
 * 
 * @author Kris Lachor
 */
public class AggregateMetricsUnitTestCase extends TestCase {

  AggregateMetrics aggMetrics = new AggregateMetrics(null, true);
  
  ComponentMetrics simpleMetrics1 = new ComponentMetrics(null, true);
  
  ComponentMetrics simpleMetrics2 = new ComponentMetrics(null, true);
  
  {
    aggMetrics.addComponentMetrics(simpleMetrics1);
    aggMetrics.addComponentMetrics(simpleMetrics2);
  }
  
  Object [] testData1 = new Object[]{"foo"};
  
  Object [] testData2 = new Object[]{"foo", "bar"};
  
  Object [] testData3 = new Object[]{"foo", new Integer(4)};

  Object testSender = new Object();
  
  Message testMsg1 = new Message(testData1, testSender, null, null);
  
  Message testMsg2 = new Message(testData2, testSender, null, null);
  
  Message testMsg3 = new Message(testData3, testSender, null, null);
  
  /**
   * Test method for {@link org.openadaptor.auxil.aggMetrics.ComponentMetrics
   * #recordMessageStart(org.openadaptor.core.Message)}.
   */
  public void testRecordMessageStart() {
    /* 1st message consists of a single String */
    aggMetrics.recordMessageStart(testMsg1);
    assertNotNull(aggMetrics.processStartTime);
    assertNotNull(aggMetrics.inputMsgCounter);
    assertEquals(aggMetrics.inputMsgCounter.size(),1);
    assertTrue(aggMetrics.inputMsgCounter.keySet().contains("java.lang.String"));
    assertEquals(aggMetrics.inputMsgCounter.get("java.lang.String"), new Long(1));
    
    /* 2nd is an array of Strings */
    aggMetrics.recordMessageStart(testMsg2);
    assertEquals(aggMetrics.inputMsgCounter.size(),2);
    assertTrue(aggMetrics.inputMsgCounter.keySet().contains(ComponentMetrics.ARRAY_OF + "java.lang.String"));
    assertEquals(aggMetrics.inputMsgCounter.get("java.lang.String"), new Long(1));
    assertEquals(aggMetrics.inputMsgCounter.get(ComponentMetrics.ARRAY_OF + "java.lang.String"), new Long(1));
    
    /* 3rd is an array with data of different types */
    aggMetrics.recordMessageStart(testMsg3);
    assertEquals(aggMetrics.inputMsgCounter.size(),3);
    assertTrue(aggMetrics.inputMsgCounter.keySet().contains(ComponentMetrics.ARRAY_OF + ComponentMetrics.HETEROGENEOUS_TYPES));
    assertEquals(aggMetrics.inputMsgCounter.get(ComponentMetrics.ARRAY_OF + ComponentMetrics.HETEROGENEOUS_TYPES), new Long(1));

    /* pass one more msg with heterogeneous types, check counters */
    aggMetrics.recordMessageStart(testMsg3);
    assertEquals(aggMetrics.inputMsgCounter.size(),3);
    assertTrue(aggMetrics.inputMsgCounter.keySet().contains(ComponentMetrics.ARRAY_OF + ComponentMetrics.HETEROGENEOUS_TYPES));
    assertEquals(aggMetrics.inputMsgCounter.get(ComponentMetrics.ARRAY_OF + ComponentMetrics.HETEROGENEOUS_TYPES), new Long(2));
  }
  
//  /**
//   * Test method for {@link org.openadaptor.auxil.metrics.ComponentMetrics#recordMessageEnd(org.openadaptor.core.Message, org.openadaptor.core.Response)}.
//   * Sends various types of messages sequentially.
//   */
//  public void testRecordMessageEnd() {
//    
//    /* 1st response consists of a single String */
//    Response response = new Response(); 
//    response.addOutput(testData1);
//    try {
//      /* Should throw exception due to unmatch output message */
//      metrics.recordMessageEnd(testMsg1, response);
//      assertTrue(false);
//    } catch (Exception e) {
//    }
//    metrics.recordMessageStart(testMsg1);
//    metrics.recordMessageEnd(testMsg1, response);
//    assertNotNull(metrics.getProcessEndTime()); 
//    assertNotNull(metrics.outputMsgCounter);
//    assertEquals(metrics.outputMsgCounter.size(),1);
//    
//    assertTrue(metrics.outputMsgCounter.keySet().contains("java.lang.String"));
//    assertEquals(metrics.outputMsgCounter.get("java.lang.String"), new Long(1));
//    
//    /* 2nd response is an array of Strings */
//    metrics.recordMessageStart(testMsg2);
//    response = new Response(); 
//    response.addOutput(testData2);
//    metrics.recordMessageEnd(testMsg2, response);
//    assertEquals(metrics.outputMsgCounter.size(),2);
//    assertTrue(metrics.outputMsgCounter.keySet().contains(ComponentMetrics.ARRAY_OF + "java.lang.String"));
//    assertEquals(metrics.outputMsgCounter.get("java.lang.String"), new Long(1));
//    assertEquals(metrics.outputMsgCounter.get(ComponentMetrics.ARRAY_OF + "java.lang.String"), new Long(1));
//  }
}
