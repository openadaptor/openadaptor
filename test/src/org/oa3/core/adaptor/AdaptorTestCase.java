/*
 * [[
 * Copyright (C) 2001 - 2006 The Software Conservancy as Trustee. All rights
 * reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Nothing in this notice shall be deemed to grant any rights to
 * trademarks, copyrights, patents, trade secrets or any other intellectual
 * property of the licensor or any contributor except as expressly stated
 * herein. No patent license is granted separate from the Software, for
 * code that you delete from the Software, or for combinations of the
 * Software with other software or hardware.
 * ]]
 */

package org.oa3.core.adaptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.oa3.core.adaptor.Adaptor;
import org.oa3.core.adaptor.AdaptorInpoint;
import org.oa3.core.adaptor.AdaptorOutpoint;
import org.oa3.core.connector.TestReadConnector;
import org.oa3.core.connector.TestWriteConnector;
import org.oa3.core.processor.TestProcessor;
import org.oa3.core.router.Router;
import org.oa3.core.router.RoutingMap;

public class AdaptorTestCase extends TestCase {

	/**
	 * AdaptorInPoint -> AdaptorOutPoint
	 * ReadConnector sends one message and then "closes", causing adaptor to stop and 
	 * WriteConnector to check it has received it's expected output.
	 */
	public void test1() {
		
		// create inpoint
		AdaptorInpoint inpoint = new AdaptorInpoint();
		TestReadConnector inconnector = new TestReadConnector();
		inconnector.setDataString("foobar");
		inpoint.setConnector(inconnector);
		
		// create outpoint
		AdaptorOutpoint outpoint = new AdaptorOutpoint();
		TestWriteConnector outconnector = new TestWriteConnector();
		outconnector.setExpectedOutput(inconnector.getDataString());
		outpoint.setConnector(outconnector);
		
		// create routing map
		RoutingMap routingMap = new RoutingMap();
		Map processMap = new HashMap();
		processMap.put(inpoint, outpoint);
		routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create router
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
		adaptor.setRunInpointsInCallingThread(true);
		
		// run adaptor
		adaptor.run();
		assertTrue(adaptor.getExitCode() == 0);
	}
	
	/**
	 * ReadConnector -> WriteConnector
	 * uses "autoboxing" so no need to construct AdaptorInPoint & AdaptorOutPoint
	 * sends ten messages in with batch size of 3, ReadConnector closes when when
	 * all messages have been sent, causing adaptor to stop and WriteConnector to
	 * check it has received it's expected output.
	 */
	public void test2() {
		
		// create inpoint
		TestReadConnector inpoint = new TestReadConnector("InPoint");
		inpoint.setDataString("foobar");
		inpoint.setBatchSize(3);
		inpoint.setMaxSend(10);
		
		// create outpoint
		TestWriteConnector outpoint = new TestWriteConnector("OutPoint");
		List output = new ArrayList();
		for (int i = 0; i < 10; i++) {
			output.add(inpoint.getDataString().replaceAll("%n", String.valueOf(i+1)));
		}
		outpoint.setExpectedOutput(output);
		
		// create router
		RoutingMap routingMap = new RoutingMap();
		Map processMap = new HashMap();
		processMap.put(inpoint, outpoint);
		routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
		adaptor.setRunInpointsInCallingThread(true);
		
		// run adaptor
		adaptor.run();
		assertTrue(adaptor.getExitCode() == 0);
	}
	
	/**
	 * IReadConnector -> IDataProcessor -> IWriteConnector
	 * uses "autoboxing" so no need to construct AdaptorInPoint, Node & AdaptorOutPoint
	 * ReadConnector sends one message and then "closes", causing adaptor to stop and 
	 * WriteConnector to check it has received it's expected output.
	 */
	public void test3() {
		
		// create inpoint
		TestReadConnector inpoint = new TestReadConnector("InPoint");
		inpoint.setDataString("foobar");
		
		// create processor
		TestProcessor processor = new TestProcessor("Processor1");
		
		// create outpoint
		TestWriteConnector outpoint = new TestWriteConnector("OutPoint");
		outpoint.setExpectedOutput(processor.getId() + "(" + inpoint.getDataString() + ")");
		
		// create router
		RoutingMap routingMap = new RoutingMap();
		Map processMap = new HashMap();
		processMap.put(inpoint, processor);
		processMap.put(processor, outpoint);
		routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
		adaptor.setRunInpointsInCallingThread(true);
		
		// run adaptor
		adaptor.run();
		assertTrue(adaptor.getExitCode() == 0);
	}
	
	/**
	 * Exception routing example
	 */
	public void test4() {
		
		// create inpoint
		TestReadConnector inpoint = new TestReadConnector("InPoint");
		inpoint.setDataString("foobar");
		inpoint.setBatchSize(3);
		inpoint.setMaxSend(10);
		
		// create processor
		TestProcessor processor = new TestProcessor("Processor1");
		
		// create processor which deliberately throws a RuntimeException
		TestProcessor exceptor = new TestProcessor("Exceptor");
		exceptor.setExceptionFrequency(3);
		
		// create processor which deliberately throws a NPE
		TestProcessor npeExceptor = new TestProcessor("NpeExceptor");
		npeExceptor.setExceptionClassName("java.lang.NullPointerException");
		npeExceptor.setExceptionFrequency(5);
		
		// create outpoint for processed data
		TestWriteConnector outpoint = new TestWriteConnector("OutPoint");
		outpoint.setExpectedOutput(createStringList(processor.getId() + "(" + inpoint.getDataString() + ")", 10));
		
		// create outpoint for MessageExceptions
		TestWriteConnector errOutpoint = new TestWriteConnector("Error1");
		errOutpoint.setExpectedOutput(createStringList("java.lang.RuntimeException:null:" + inpoint.getDataString(), 3));

		// create outpoint for MessageExceptions
		TestWriteConnector npeOutpoint = new TestWriteConnector("Error2");
		npeOutpoint.setExpectedOutput(createStringList("java.lang.NullPointerException:null:" + processor.getId() + "(" + inpoint.getDataString() + ")", 2));

		// create routing map
		RoutingMap routingMap = new RoutingMap();

		Map processMap = new HashMap();
		List inpointRecipients = new ArrayList();
		inpointRecipients.add(processor);
		inpointRecipients.add(exceptor);
		processMap.put(inpoint, inpointRecipients);
		List processorRecipients = new ArrayList();
		processorRecipients.add(outpoint);
		processorRecipients.add(npeExceptor);
		processMap.put(processor, processorRecipients);
		routingMap.setProcessMap(processMap);
		
		Map exceptionMap = new HashMap();
		exceptionMap.put("java.lang.NullPointerException", npeOutpoint);
		exceptionMap.put("java.lang.Exception", errOutpoint);
		routingMap.setExceptionMap(exceptionMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
		adaptor.setRunInpointsInCallingThread(true);
		
		// run adaptor
		adaptor.run();
		assertTrue(adaptor.getExitCode() == 0);
	}

	/**
	 * Discard routing example
	 */
	public void test5() {
		
		// create inpoint
		TestReadConnector inpoint = new TestReadConnector("InPoint");
		inpoint.setDataString("foobar");
		
		// create processor
		TestProcessor processor = new TestProcessor("Processor1");
		
		// create processor which deliberately discards data
		TestProcessor discarder = new TestProcessor("Discarder");
		discarder.setDiscardFrequency(1);
		
		// create processor which deliberately discards data
		TestProcessor discarder2 = new TestProcessor("Discarder2");
		discarder2.setDiscardFrequency(1);
		
		// create outpoint for processed data
		TestWriteConnector outpoint = new TestWriteConnector("OutPoint");
		outpoint.setExpectedOutput(processor.getId() + "(" + inpoint.getDataString() + ")");
		
		// create outpoint for first discarder
		TestWriteConnector discard = new TestWriteConnector("Discard1");
		discard.setExpectedOutput(inpoint.getDataString());

		// create outpoint for first discarder
		TestWriteConnector discard2 = new TestWriteConnector("Discard2");
		discard2.setExpectedOutput(processor.getId() + "(" + inpoint.getDataString() + ")");
		
		// create outpoint for first discarder
		TestWriteConnector discard3 = new TestWriteConnector("Discard3");
		discard3.setExpectedOutput(processor.getId() + "(" + inpoint.getDataString() + ")");

		// create routing map
		RoutingMap routingMap = new RoutingMap();

		Map processMap = new HashMap();
		List inpointRecipients = new ArrayList();
		inpointRecipients.add(processor);
		inpointRecipients.add(discarder);
		processMap.put(inpoint, inpointRecipients);
		List processorRecipients = new ArrayList();
		processorRecipients.add(outpoint);
		processorRecipients.add(discarder2);
		processMap.put(processor, processorRecipients);
		routingMap.setProcessMap(processMap);
		
		Map discardMap = new HashMap();
		discardMap.put(discarder, discard);
		List discarder2Recipients = new ArrayList();
		discarder2Recipients.add(discard2);
		discarder2Recipients.add(discard3);
		discardMap.put(discarder2, discarder2Recipients);
		routingMap.setDiscardMap(discardMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
		adaptor.setRunInpointsInCallingThread(true);
		
		// run adaptor
		adaptor.run();
		assertTrue(adaptor.getExitCode() == 0);
	}
	
  public void testParallelPipelines() {
    
    // pipeline 1
    TestReadConnector inpoint1 = new TestReadConnector("i1");
    inpoint1.setDataString("foobar");
    TestProcessor processor1 = new TestProcessor("p1");
    TestWriteConnector outpoint1 = new TestWriteConnector("o1");
    outpoint1.setExpectedOutput(processor1.getId() + "(" + inpoint1.getDataString() + ")");
    
    // pipeline 2
    TestReadConnector inpoint2 = new TestReadConnector("i2");
    inpoint2.setDataString("barfoo");
    TestProcessor processor2 = new TestProcessor("p2");
    TestWriteConnector outpoint2 = new TestWriteConnector("o2");
    outpoint2.setExpectedOutput(processor2.getId() + "(" + inpoint2.getDataString() + ")");
    
    // create routing map
    RoutingMap routingMap = new RoutingMap();
    Map processMap = new HashMap();
    processMap.put(inpoint1, processor1);
    processMap.put(processor1, outpoint1);
    processMap.put(inpoint2, processor2);
    processMap.put(processor2, outpoint2);
    routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
    adaptor.setRunInpointsInCallingThread(true);
    
    // run adaptor
    try {
      adaptor.run();
      fail("should not be allow to run multiple inpoint from calling thread");
    } catch (Exception e) {
    }
    
    adaptor.setRunInpointsInCallingThread(false);
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);
    
  }

  public void testFanIn() {
    
    // create inpoint1
    TestReadConnector inpoint1 = new TestReadConnector("i1");
    inpoint1.setDataString("foobar");
    inpoint1.setMaxSend(10);
    inpoint1.setIntervalMs(10);
    TestProcessor processor1 = new TestProcessor("p1");
    
    // create inpoint2
    TestReadConnector inpoint2 = new TestReadConnector("i2");
    inpoint2.setDataString("foobar");
    inpoint2.setMaxSend(5);
    inpoint2.setIntervalMs(5);

    TestWriteConnector outpoint1 = new TestWriteConnector("o1");
    outpoint1.setExpectedOutput(createStringList(processor1.getId() + "(" + inpoint1.getDataString() + ")", 15));

    // create routing map
    RoutingMap routingMap = new RoutingMap();
    Map processMap = new HashMap();
    processMap.put(inpoint1, processor1);
    processMap.put(processor1, outpoint1);
    processMap.put(inpoint2, processor1);
    routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
    adaptor.setRunInpointsInCallingThread(true);
    
    // run adaptor
    try {
      adaptor.run();
      fail("should not be allow to run multiple inpoint from calling thread");
    } catch (Exception e) {
    }
    
    adaptor.setRunInpointsInCallingThread(false);
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);
  }

	public static List createStringList(String s, int n) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < n; i++) {
			list.add(s);
		}
		return list;
	}
}
