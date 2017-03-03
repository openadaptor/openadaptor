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

package org.openadaptor.core.adaptor;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.openadaptor.core.connector.TestReadConnector;
import org.openadaptor.core.connector.TestWriteConnector;
import org.openadaptor.core.processor.TestProcessor;
import org.openadaptor.core.router.Router;
import org.openadaptor.core.router.RoutingMap;

public class ConnectorExceptionTestCase extends TestCase {

	/**
	 * test that write connector exceptions are trapped and routed correctly
	 *
	 */
	public void testWriteNodeException() {

		TestReadConnector readNode = new TestReadConnector("i");
		readNode.setDataString("x");
		readNode.setMaxSend(5);

		TestProcessor processor = new TestProcessor("p");

		TestWriteConnector writeNode = new TestWriteConnector("o");
		writeNode.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", 3));
		writeNode.setExceptionFrequency(2);

		TestWriteConnector errorWriteNode = new TestWriteConnector("e");
		errorWriteNode.setExpectedOutput(AdaptorTestCase.createStringList("java.lang.RuntimeException:test:p(x)", 2));

		// create router
		RoutingMap routingMap = new RoutingMap();
		
		Map processMap = new HashMap();
		processMap.put(readNode, processor);
		processMap.put(processor, writeNode);
		routingMap.setProcessMap(processMap);
		
		Map exceptionMap = new HashMap();
		exceptionMap.put("java.lang.Exception", errorWriteNode);
		routingMap.setExceptionMap(exceptionMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor = new Adaptor();
    adaptor.setMessageProcessor(router);
		adaptor.setRunInCallingThread(true);

		// run adaptor
		adaptor.run();
		
		assertTrue(adaptor.getExitCode() == 0);

	}
	
	/**
	 * test that write connector exception causes adaptor to fail if there
	 * is no exception mapping
	 *
	 */
	public void testWriteNodeException2() {

		TestReadConnector readNode = new TestReadConnector("i");
		readNode.setDataString("x");
		readNode.setMaxSend(5);

		TestProcessor processor = new TestProcessor("p");

		TestWriteConnector writeNode = new TestWriteConnector("o");
		writeNode.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", 1));
		writeNode.setExceptionFrequency(2);

		// create router
		RoutingMap routingMap = new RoutingMap();
		
		Map processMap = new HashMap();
		processMap.put(readNode, processor);
		processMap.put(processor, writeNode);
		routingMap.setProcessMap(processMap);
		
		Map exceptionMap = new HashMap();
		routingMap.setExceptionMap(exceptionMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor = new Adaptor();
    adaptor.setMessageProcessor(router);
		adaptor.setRunInCallingThread(true);

		// run adaptor
		adaptor.run();
		
		assertTrue(adaptor.getExitCode() != 0);
	}
	
//	/**
//	 * Disabled - old test was silly. Need to fix batch handling before re-enabling
//   * test that write connector exception within batch (of size >1) is catchable.
//   * Note that the granularaity of the exception data is that of a batch.
//	 *
//	 */
//	public void testWriteNodeException3() {
//
//		TestReadConnector readNode = new TestReadConnector("i");
//		readNode.setDataString("x");
//		readNode.setMaxSend(10);
//		readNode.setBatchSize(4);
//
//		TestProcessor processor = new TestProcessor("p");
//
//    java.util.List expectedOutput=AdaptorTestCase.createStringList("p(x)", 7);
//		TestWriteConnector writeNode = new TestWriteConnector("o");
//		writeNode.setExpectedOutput(expectedOutput);
//		writeNode.setExceptionFrequency(2);
//
// 		TestWriteConnector errorWriteNode = new TestWriteConnector("e");
//		//errorWriteNode.setExpectedOutput(AdaptorTestCase.createStringList("java.lang.RuntimeException:test:p(x)", 0));
// 		// create router
//		RoutingMap routingMap = new RoutingMap();
//		
//		Map processMap = new HashMap();
//		processMap.put(readNode, processor);
//		processMap.put(processor, writeNode);
//		routingMap.setProcessMap(processMap);
//		
//		Map exceptionMap = new HashMap();
//		exceptionMap.put("java.lang.Exception", errorWriteNode);
//		routingMap.setExceptionMap(exceptionMap);
//    Router router = new Router(routingMap);
//    
//    // create adaptor
//    Adaptor adaptor = new Adaptor();
//    adaptor.setMessageProcessor(router);
//    adaptor.setRunInCallingThread(true);
//
//    // run adaptor
//    adaptor.run();
//    int errorWrites=errorWriteNode.getOutput().size();
//    assertTrue("Output does not match expected output",writeNode.getOutput().equals(expectedOutput));
//    assertTrue("errorWriteNode expected 3 exceptions",errorWrites==3);
//    assertTrue(adaptor.getExitCode() == 0);
//	}

	/**
	 * test that read connector exceptions cause the adaptor to fail
	 *
	 */
	public void testReadNodeException() {

		TestReadConnector readNode = new TestReadConnector("i");
		readNode.setDataString("x");
		readNode.setMaxSend(5);
		readNode.setExceptionFrequency(5);

		TestProcessor processor = new TestProcessor("p");

		TestWriteConnector writeNode = new TestWriteConnector("o");
		writeNode.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", 4));

		// create router
		RoutingMap routingMap = new RoutingMap();
		
		Map processMap = new HashMap();
		processMap.put(readNode, processor);
		processMap.put(processor, writeNode);
		routingMap.setProcessMap(processMap);
		
		Map exceptionMap = new HashMap();
		routingMap.setExceptionMap(exceptionMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor = new Adaptor();
    adaptor.setMessageProcessor(router);
		adaptor.setRunInCallingThread(true);

		// run adaptor
		adaptor.run();
		
		assertTrue(adaptor.getExitCode() != 0);
	}
}
