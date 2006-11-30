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

package org.oa3.adaptor;

import java.util.HashMap;
import java.util.Map;

import org.oa3.connector.TestReadConnector;
import org.oa3.connector.TestWriteConnector;
import org.oa3.processor.TestProcessor;
import org.oa3.router.RoutingMap;

import junit.framework.TestCase;

public class ConnectorAndProcessorTestCase extends TestCase {

	/**
	 * test that processors can be configured with a inpoint
	 *
	 */
	public void testInpointProcessor() {
		runInpointProcessor(1, 1);
		runInpointProcessor(3, 10);
	}
	
	/**
	 * test that processors can be configured with a outpoint
	 *
	 */
	public void testOutpointProcessor() {
		runOutpointProcessor(1, 1);
		runOutpointProcessor(3, 10);
	}
	
	/**
	 * test that discards and exception routing works with outpoint
	 * processors
	 *
	 */
	public void testOutpointProcessorExceptionAndDiscards() {
		// create inpoint
		AdaptorInpoint inpoint = new AdaptorInpoint("i");
		TestReadConnector inconnector = new TestReadConnector();
		inconnector.setDataString("x");
		inconnector.setMaxSend(5);
		inpoint.setConnector(inconnector);
		
		// create outpoint
		AdaptorOutpoint outpoint = new AdaptorOutpoint("o");
		TestWriteConnector outconnector = new TestWriteConnector();
		outconnector.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", 3));
		outpoint.setConnector(outconnector);
		TestProcessor processor = new TestProcessor("p");
		processor.setExceptionClassName("java.lang.NullPointerException");
		processor.setExceptionFrequency(3);
		processor.setDiscardFrequency(4);
		outpoint.setProcessor(processor);

		// error outpoint
		TestWriteConnector errorOutpoint = new TestWriteConnector("e");
		errorOutpoint.setExpectedOutput(AdaptorTestCase.createStringList("java.lang.NullPointerException:null:x", 1));

		// discard outpoint
		TestWriteConnector discardOutpoint = new TestWriteConnector("d");
		discardOutpoint.setExpectedOutput(AdaptorTestCase.createStringList("x", 1));

		// create routing map
		RoutingMap routingMap = new RoutingMap();
		Map processMap = new HashMap();
		processMap.put(inpoint, outpoint);
		routingMap.setProcessMap(processMap);
		
		Map exceptionMap = new HashMap();
		exceptionMap.put("java.lang.Exception", errorOutpoint);
		routingMap.setExceptionMap(exceptionMap);

		Map discardMap = new HashMap();
		discardMap.put(outpoint, discardOutpoint);
		
		// create adaptor
		Adaptor adaptor =  new Adaptor();
		adaptor.setRoutingMap(routingMap);
		adaptor.setRunInpointsInCallingThread(true);
		
		// run adaptor
		adaptor.run();
		assertTrue(adaptor.getExitCode() == 0);
	}
	
	public void runInpointProcessor(int batchSize, int maxSend) {
		
		// create inpoint
		AdaptorInpoint inpoint = new AdaptorInpoint("i");
		TestReadConnector inconnector = new TestReadConnector();
		inconnector.setDataString("x");
		inconnector.setBatchSize(batchSize);
		inconnector.setMaxSend(maxSend);
		TestProcessor processor = new TestProcessor("p");
		inpoint.setConnector(inconnector);
		inpoint.setProcessor(processor);
		
		// create outpoint
		AdaptorOutpoint outpoint = new AdaptorOutpoint("o");
		TestWriteConnector outconnector = new TestWriteConnector();
		outconnector.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", maxSend));
		outpoint.setConnector(outconnector);
		
		// create routing map
		RoutingMap routingMap = new RoutingMap();
		Map processMap = new HashMap();
		processMap.put(inpoint, outpoint);
		routingMap.setProcessMap(processMap);
		
		// create adaptor
		Adaptor adaptor =  new Adaptor();
		adaptor.setRoutingMap(routingMap);
		adaptor.setRunInpointsInCallingThread(true);
		
		// run adaptor
		adaptor.run();
		assertTrue(adaptor.getExitCode() == 0);
	}
	
	public void runOutpointProcessor(int batchSize, int maxSend) {
		
		// create inpoint
		AdaptorInpoint inpoint = new AdaptorInpoint("i");
		TestReadConnector inconnector = new TestReadConnector();
		inconnector.setDataString("x");
		inconnector.setBatchSize(batchSize);
		inconnector.setMaxSend(maxSend);
		inpoint.setConnector(inconnector);
		
		// create outpoint
		AdaptorOutpoint outpoint = new AdaptorOutpoint("o");
		TestWriteConnector outconnector = new TestWriteConnector();
		outconnector.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", maxSend));
		outpoint.setConnector(outconnector);
		TestProcessor processor = new TestProcessor("p");
		outpoint.setProcessor(processor);
		
		// create routing map
		RoutingMap routingMap = new RoutingMap();
		Map processMap = new HashMap();
		processMap.put(inpoint, outpoint);
		routingMap.setProcessMap(processMap);
		
		// create adaptor
		Adaptor adaptor =  new Adaptor();
		adaptor.setRoutingMap(routingMap);
		adaptor.setRunInpointsInCallingThread(true);
		
		// run adaptor
		adaptor.run();
		assertTrue(adaptor.getExitCode() == 0);
	}

}
