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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.connector.TestReadConnector;
import org.openadaptor.core.connector.TestWriteConnector;
import org.openadaptor.core.node.ReadNode;
import org.openadaptor.core.node.WriteNode;
import org.openadaptor.core.processor.TestProcessor;
import org.openadaptor.core.router.Router;
import org.openadaptor.core.router.RoutingMap;

public class AdaptorTestCase extends TestCase {

	/**
	 * ReadNode -> WriteNode
	 * ReadConnector sends one message and then "closes", causing adaptor to stop and 
	 * WriteConnector to check it has received it's expected output.
	 */
	public void test1() {
		
		// create readNode
		ReadNode readNode = new ReadNode();
		TestReadConnector inconnector = new TestReadConnector();
		inconnector.setDataString("foobar");
		readNode.setConnector(inconnector);
		
		// create writeNode
		WriteNode writeNode = new WriteNode();
		TestWriteConnector outconnector = new TestWriteConnector();
		outconnector.setExpectedOutput(inconnector.getDataString());
		writeNode.setConnector(outconnector);
		
		// create routing map
		RoutingMap routingMap = new RoutingMap();
		Map processMap = new HashMap();
		processMap.put(readNode, writeNode);
		routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create router
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
		adaptor.setRunInCallingThread(true);
		
		// run adaptor
		adaptor.run();
		assertTrue(adaptor.getExitCode() == 0);
	}
	
	/**
	 * ReadConnector -> WriteConnector
	 * uses "autoboxing" so no need to construct ReadNode & WriteNode
	 * sends ten messages in with batch size of 3, ReadConnector closes when when
	 * all messages have been sent, causing adaptor to stop and WriteConnector to
	 * check it has received it's expected output.
	 */
	public void test2() {
		
		// create readNode
		TestReadConnector readNode = new TestReadConnector("ReadConnector");
		readNode.setDataString("foobar");
		readNode.setBatchSize(3);
		readNode.setMaxSend(10);
		
		// create writeNode
		TestWriteConnector writeNode = new TestWriteConnector("writeNode");
		List output = new ArrayList();
		for (int i = 0; i < 10; i++) {
			output.add(readNode.getDataString().replaceAll("%n", String.valueOf(i+1)));
		}
		writeNode.setExpectedOutput(output);
		
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
	
	/**
	 * IReadConnector -> IDataProcessor -> IWriteConnector
	 * uses "autoboxing" so no need to construct ReadNode, Node & WriteNode
	 * ReadConnector sends one message and then "closes", causing adaptor to stop and 
	 * WriteConnector to check it has received it's expected output.
	 */
	public void test3() {
		
		// create readNode
		TestReadConnector readNode = new TestReadConnector("ReadConnector");
		readNode.setDataString("foobar");
		
		// create processor
		TestProcessor processor = new TestProcessor("Processor1");
		
		// create writeNode
		TestWriteConnector writeNode = new TestWriteConnector("writeNode");
		writeNode.setExpectedOutput(processor.getId() + "(" + readNode.getDataString() + ")");
		
		// create router
		RoutingMap routingMap = new RoutingMap();
		Map processMap = new HashMap();
		processMap.put(readNode, processor);
		processMap.put(processor, writeNode);
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
	
	/**
	 * Exception routing example
	 */
	public void test4() {
		
		// create readNode
		TestReadConnector readNode = new TestReadConnector("ReadConnector");
		readNode.setDataString("foobar");
		readNode.setBatchSize(3);
		readNode.setMaxSend(10);
		
		// create processor
		TestProcessor processor = new TestProcessor("Processor1");
		
		// create processor which deliberately throws a RuntimeException
		TestProcessor exceptor = new TestProcessor("Exceptor");
		exceptor.setExceptionFrequency(3);
		
		// create processor which deliberately throws a NPE
		TestProcessor npeExceptor = new TestProcessor("NpeExceptor");
		npeExceptor.setExceptionClassName("java.lang.NullPointerException");
		npeExceptor.setExceptionFrequency(5);
		
		// create writeNode for processed data
		TestWriteConnector writeNode = new TestWriteConnector("writeNode");
		writeNode.setExpectedOutput(createStringList(processor.getId() + "(" + readNode.getDataString() + ")", 10));
		
		// create writeNode for MessageExceptions
		TestWriteConnector errWriteConnector = new TestWriteConnector("Error1");
		errWriteConnector.setExpectedOutput(createStringList("java.lang.RuntimeException:null:" + readNode.getDataString(), 3));

		// create writeNode for MessageExceptions
		TestWriteConnector npeWriteConnector = new TestWriteConnector("Error2");
		npeWriteConnector.setExpectedOutput(createStringList("java.lang.NullPointerException:null:" + processor.getId() + "(" + readNode.getDataString() + ")", 2));

		// create routing map
		RoutingMap routingMap = new RoutingMap();

		Map processMap = new HashMap();
		List readNodeRecipients = new ArrayList();
		readNodeRecipients.add(processor);
		readNodeRecipients.add(exceptor);
		processMap.put(readNode, readNodeRecipients);
		List processorRecipients = new ArrayList();
		processorRecipients.add(writeNode);
		processorRecipients.add(npeExceptor);
		processMap.put(processor, processorRecipients);
		routingMap.setProcessMap(processMap);
		
		Map exceptionMap = new HashMap();
		exceptionMap.put("java.lang.NullPointerException", npeWriteConnector);
		exceptionMap.put("java.lang.Exception", errWriteConnector);
		routingMap.setExceptionMap(exceptionMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
		adaptor.setRunInCallingThread(true);
		
		// run adaptor
		adaptor.run();
		assertTrue(adaptor.getExitCode() == 0);
	}

	/**
	 * Discard routing example
	 */
	public void test5() {
		
		// create readNode
		TestReadConnector readNode = new TestReadConnector("ReadConnector");
		readNode.setDataString("foobar");
		
		// create processor
		TestProcessor processor = new TestProcessor("Processor1");
		
		// create processor which deliberately discards data
		TestProcessor discarder = new TestProcessor("Discarder");
		discarder.setDiscardFrequency(1);
		
		// create processor which deliberately discards data
		TestProcessor discarder2 = new TestProcessor("Discarder2");
		discarder2.setDiscardFrequency(1);
		
		// create writeNode for processed data
		TestWriteConnector writeNode = new TestWriteConnector("writeNode");
		writeNode.setExpectedOutput(processor.getId() + "(" + readNode.getDataString() + ")");
		
		// create writeNode for first discarder
		TestWriteConnector discard = new TestWriteConnector("Discard1");
		discard.setExpectedOutput(readNode.getDataString());

		// create writeNode for first discarder
		TestWriteConnector discard2 = new TestWriteConnector("Discard2");
		discard2.setExpectedOutput(processor.getId() + "(" + readNode.getDataString() + ")");
		
		// create writeNode for first discarder
		TestWriteConnector discard3 = new TestWriteConnector("Discard3");
		discard3.setExpectedOutput(processor.getId() + "(" + readNode.getDataString() + ")");

		// create routing map
		RoutingMap routingMap = new RoutingMap();

		Map processMap = new HashMap();
		List readNodeRecipients = new ArrayList();
		readNodeRecipients.add(processor);
		readNodeRecipients.add(discarder);
		processMap.put(readNode, readNodeRecipients);
		List processorRecipients = new ArrayList();
		processorRecipients.add(writeNode);
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
		adaptor.setRunInCallingThread(true);
		
		// run adaptor
		adaptor.run();
		assertTrue(adaptor.getExitCode() == 0);
	}
	
  public void testParallelPipelines() {
    
    // pipeline 1
    TestReadConnector readNode1 = new TestReadConnector("i1");
    readNode1.setDataString("foobar");
    TestProcessor processor1 = new TestProcessor("p1");
    TestWriteConnector writeNode1 = new TestWriteConnector("o1");
    writeNode1.setExpectedOutput(processor1.getId() + "(" + readNode1.getDataString() + ")");
    
    // pipeline 2
    TestReadConnector readNode2 = new TestReadConnector("i2");
    readNode2.setDataString("barfoo");
    TestProcessor processor2 = new TestProcessor("p2");
    TestWriteConnector writeNode2 = new TestWriteConnector("o2");
    writeNode2.setExpectedOutput(processor2.getId() + "(" + readNode2.getDataString() + ")");
    
    // create routing map
    RoutingMap routingMap = new RoutingMap();
    Map processMap = new HashMap();
    processMap.put(readNode1, processor1);
    processMap.put(processor1, writeNode1);
    processMap.put(readNode2, processor2);
    processMap.put(processor2, writeNode2);
    routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
    adaptor.setRunInCallingThread(true);
    
    // run adaptor
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 1);
    
    adaptor.setRunInCallingThread(false);
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);
    
  }

  public void testFanIn() {
    
    // create readNode1
    TestReadConnector readNode1 = new TestReadConnector("i1");
    readNode1.setDataString("foobar");
    readNode1.setMaxSend(10);
    readNode1.setIntervalMs(10);
    TestProcessor processor1 = new TestProcessor("p1");
    
    // create readNode2
    TestReadConnector readNode2 = new TestReadConnector("i2");
    readNode2.setDataString("foobar");
    readNode2.setMaxSend(5);
    readNode2.setIntervalMs(5);

    TestWriteConnector writeNode1 = new TestWriteConnector("o1");
    writeNode1.setExpectedOutput(createStringList(processor1.getId() + "(" + readNode1.getDataString() + ")", 15));

    // create routing map
    RoutingMap routingMap = new RoutingMap();
    Map processMap = new HashMap();
    processMap.put(readNode1, processor1);
    processMap.put(processor1, writeNode1);
    processMap.put(readNode2, processor1);
    routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
    adaptor.setRunInCallingThread(true);
    
    // run adaptor
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 1);
    
    adaptor.setRunInCallingThread(false);
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
