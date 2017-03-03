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

import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.connector.TestReadConnector;
import org.openadaptor.core.connector.TestWriteConnector;
import org.openadaptor.core.node.ReadNode;
import org.openadaptor.core.node.WriteNode;
import org.openadaptor.core.processor.TestProcessor;
import org.openadaptor.core.router.Router;
import org.openadaptor.core.router.RoutingMap;

import junit.framework.TestCase;

public class ConnectorAndProcessorTestCase extends TestCase {

	/**
	 * test that processors can be configured with a readNode
	 *
	 */
	public void testReadNodeProcessor() {
		runReadNodeProcessor(1, 1);
		runReadNodeProcessor(3, 10);
	}
	
	/**
	 * test that processors can be configured with a writeNode
	 *
	 */
	public void testWriteNodeProcessor() {
		runWriteNodeProcessor(1, 1);
		runWriteNodeProcessor(3, 10);
	}
	
	/**
	 * test that discards and exception routing works with writeNode
	 * processors
	 *
	 */
	public void testWriteNodeProcessorExceptionAndDiscards() {
		// create readNode
		ReadNode readNode = new ReadNode("i");
		TestReadConnector inconnector = new TestReadConnector();
		inconnector.setDataString("x");
		inconnector.setMaxSend(5);
		readNode.setConnector(inconnector);
		
		// create writeNode
		WriteNode writeNode = new WriteNode("o");
		TestWriteConnector outconnector = new TestWriteConnector();
		outconnector.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", 3));
		writeNode.setConnector(outconnector);
		TestProcessor processor = new TestProcessor("p");
		processor.setExceptionFrequency(3);
		processor.setDiscardFrequency(4);
		writeNode.setProcessor(processor);

		// error writeNode
		TestWriteConnector errorWriteNode = new TestWriteConnector("e");
		errorWriteNode.setExpectedOutput(AdaptorTestCase.createStringList("java.lang.NullPointerException:null:x", 1));

		// discard writeNode
		TestWriteConnector discardWriteNode = new TestWriteConnector("d");
		discardWriteNode.setExpectedOutput(AdaptorTestCase.createStringList("x", 1));

		// create router
		RoutingMap routingMap = new RoutingMap();
		Map processMap = new HashMap();
		processMap.put(readNode, writeNode);
		routingMap.setProcessMap(processMap);
		
		Map exceptionMap = new HashMap();
		exceptionMap.put("java.lang.Exception", errorWriteNode);
		routingMap.setExceptionMap(exceptionMap);

		Map discardMap = new HashMap();
		discardMap.put(writeNode, discardWriteNode);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
		adaptor.setRunInCallingThread(true);
		
		// run adaptor
		adaptor.run();
		assertTrue(adaptor.getExitCode() == 0);
	}
	
	public void runReadNodeProcessor(int batchSize, int maxSend) {
		
		// create readNode
		ReadNode readNode = new ReadNode("i");
		TestReadConnector inconnector = new TestReadConnector();
		inconnector.setDataString("x");
		inconnector.setBatchSize(batchSize);
		inconnector.setMaxSend(maxSend);
		TestProcessor processor = new TestProcessor("p");
		readNode.setConnector(inconnector);
		readNode.setProcessor(processor);
		
		// create writeNode
		WriteNode writeNode = new WriteNode("o");
		TestWriteConnector outconnector = new TestWriteConnector();
		outconnector.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", maxSend));
		writeNode.setConnector(outconnector);
		
		// create router
		RoutingMap routingMap = new RoutingMap();
		Map processMap = new HashMap();
		processMap.put(readNode, writeNode);
		routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
		adaptor.setRunInCallingThread(true);
		
		// run adaptor
		adaptor.run();
		assertTrue(adaptor.getExitCode() == 0);
	}
	
	public void runWriteNodeProcessor(int batchSize, int maxSend) {
		
		// create readNode
		ReadNode readNode = new ReadNode("i");
		TestReadConnector inconnector = new TestReadConnector();
		inconnector.setDataString("x");
		inconnector.setBatchSize(batchSize);
		inconnector.setMaxSend(maxSend);
		readNode.setConnector(inconnector);
		
		// create writeNode
		WriteNode writeNode = new WriteNode("o");
		TestWriteConnector outconnector = new TestWriteConnector();
		outconnector.setExpectedOutput(AdaptorTestCase.createStringList("p(x)", maxSend));
		writeNode.setConnector(outconnector);
		TestProcessor processor = new TestProcessor("p");
		writeNode.setProcessor(processor);
		
		// create router
		RoutingMap routingMap = new RoutingMap();
		Map processMap = new HashMap();
		processMap.put(readNode, writeNode);
		routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor = new Adaptor();
    adaptor.setMessageProcessor(router);
		adaptor.setRunInCallingThread(true);
		
		// run adaptor
		adaptor.run();
		assertTrue(adaptor.getExitCode() == 0);
	}

}
