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

import java.util.HashMap;
import java.util.Map;

import org.oa3.core.adaptor.Adaptor;
import org.oa3.core.connector.TestReadConnector;
import org.oa3.core.connector.TestWriteConnector;
import org.oa3.core.processor.TestProcessor;
import org.oa3.core.router.RoutingMap;

import junit.framework.TestCase;

public class ConnectorExceptionTestCase extends TestCase {

	/**
	 * test that write connector exceptions are trapped and routed correctly
	 *
	 */
	public void testOutpointException() {

		TestReadConnector inpoint = new TestReadConnector("i");
		inpoint.setDataString("x");
		inpoint.setMaxSend(5);

		TestProcessor processor = new TestProcessor("p");

		TestWriteConnector outpoint = new TestWriteConnector("o");
		outpoint.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", 3));
		outpoint.setExceptionFrequency(2);

		TestWriteConnector errorOutpoint = new TestWriteConnector("e");
		errorOutpoint.setExpectedOutput(AdaptorTestCase.createStringList("java.lang.RuntimeException:test:p(x)", 2));

		// create routing map
		RoutingMap routingMap = new RoutingMap();
		
		Map processMap = new HashMap();
		processMap.put(inpoint, processor);
		processMap.put(processor, outpoint);
		routingMap.setProcessMap(processMap);
		
		Map exceptionMap = new HashMap();
		exceptionMap.put("java.lang.Exception", errorOutpoint);
		routingMap.setExceptionMap(exceptionMap);

		// create adaptor
		Adaptor adaptor = new Adaptor();
		adaptor.setRoutingMap(routingMap);
		adaptor.setRunInpointsInCallingThread(true);

		// run adaptor
		adaptor.run();
		
		assertTrue(adaptor.getExitCode() == 0);

	}
	
	/**
	 * test that write connector exception causes adaptor to fail if there
	 * is no exception mapping
	 *
	 */
	public void testOutpointException2() {

		TestReadConnector inpoint = new TestReadConnector("i");
		inpoint.setDataString("x");
		inpoint.setMaxSend(5);

		TestProcessor processor = new TestProcessor("p");

		TestWriteConnector outpoint = new TestWriteConnector("o");
		outpoint.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", 1));
		outpoint.setExceptionFrequency(2);

		// create routing map
		RoutingMap routingMap = new RoutingMap();
		
		Map processMap = new HashMap();
		processMap.put(inpoint, processor);
		processMap.put(processor, outpoint);
		routingMap.setProcessMap(processMap);
		
		Map exceptionMap = new HashMap();
		routingMap.setExceptionMap(exceptionMap);

		// create adaptor
		Adaptor adaptor = new Adaptor();
		adaptor.setRoutingMap(routingMap);
		adaptor.setRunInpointsInCallingThread(true);

		// run adaptor
		adaptor.run();
		
		assertTrue(adaptor.getExitCode() != 0);
	}
	
	/**
	 * test that write connector exception causes adaptor to fail if batch size
	 * is > 1.
	 *
	 */
	public void testOutpointException3() {

		TestReadConnector inpoint = new TestReadConnector("i");
		inpoint.setDataString("x");
		inpoint.setMaxSend(5);
		inpoint.setBatchSize(2);

		TestProcessor processor = new TestProcessor("p");

		TestWriteConnector outpoint = new TestWriteConnector("o");
		outpoint.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", 1));
		outpoint.setExceptionFrequency(2);

		TestWriteConnector errorOutpoint = new TestWriteConnector("e");
		errorOutpoint.setExpectedOutput(AdaptorTestCase.createStringList("java.lang.RuntimeException:test:p(x)", 0));

		// create routing map
		RoutingMap routingMap = new RoutingMap();
		
		Map processMap = new HashMap();
		processMap.put(inpoint, processor);
		processMap.put(processor, outpoint);
		routingMap.setProcessMap(processMap);
		
		Map exceptionMap = new HashMap();
		exceptionMap.put("java.lang.Exception", errorOutpoint);
		routingMap.setExceptionMap(exceptionMap);

		// create adaptor
		Adaptor adaptor = new Adaptor();
		adaptor.setRoutingMap(routingMap);
		adaptor.setRunInpointsInCallingThread(true);

		// run adaptor
		adaptor.run();
		
		assertTrue(adaptor.getExitCode() != 0);
	}
	
	/**
	 * test that read connector exceptions cause the adaptor to fail
	 *
	 */
	public void testInpointException() {

		TestReadConnector inpoint = new TestReadConnector("i");
		inpoint.setDataString("x");
		inpoint.setMaxSend(5);
		inpoint.setExceptionFrequency(5);

		TestProcessor processor = new TestProcessor("p");

		TestWriteConnector outpoint = new TestWriteConnector("o");
		outpoint.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", 4));

		// create routing map
		RoutingMap routingMap = new RoutingMap();
		
		Map processMap = new HashMap();
		processMap.put(inpoint, processor);
		processMap.put(processor, outpoint);
		routingMap.setProcessMap(processMap);
		
		Map exceptionMap = new HashMap();
		routingMap.setExceptionMap(exceptionMap);

		// create adaptor
		Adaptor adaptor = new Adaptor();
		adaptor.setRoutingMap(routingMap);
		adaptor.setRunInpointsInCallingThread(true);

		// run adaptor
		adaptor.run();
		
		assertTrue(adaptor.getExitCode() != 0);
	}
}
