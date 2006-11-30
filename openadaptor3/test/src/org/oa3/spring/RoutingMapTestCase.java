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

package org.oa3.spring;

import java.util.List;

import junit.framework.TestCase;

import org.oa3.ILifecycleListener;
import org.oa3.IMessageProcessor;
import org.oa3.Message;
import org.oa3.Response;
import org.oa3.State;
import org.oa3.router.IRoutingMap;
import org.springframework.beans.factory.ListableBeanFactory;


public class RoutingMapTestCase extends TestCase {

	ListableBeanFactory factory = SpringApplication.getBeanFactory("file:test/src/org/oa3/spring/routing.xml", null);

	private IRoutingMap getRoutingMap(String testName) {
		return (IRoutingMap) factory.getBean(testName);
	}
	
	private IMessageProcessor getNode(String beanName) {
		return (IMessageProcessor) factory.getBean(beanName);
	}
	
	public void test1() {
		IRoutingMap map = getRoutingMap("test1");
		
		List destinations = map.getProcessDestinations(getNode("InPoint"));
		assertTrue(destinations.size() == 1);
		assertTrue(destinations.get(0).toString().equals("OutPoint"));
		
		destinations = map.getProcessDestinations(getNode("OutPoint"));
		assertTrue(destinations.size() == 0);
		
		destinations = map.getDiscardDestinations(getNode("InPoint"));
		assertTrue(destinations.size() == 1);
		assertTrue(destinations.get(0).toString().equals("Discard"));
		
		destinations = map.getExceptionDestinations(getNode("InPoint"), new Exception("foo"));
		assertTrue(destinations.size() == 1);
		assertTrue(destinations.get(0).toString().equals("Error"));
		
		destinations = map.getExceptionDestinations(getNode("OutPoint"), new Exception("foo"));
		assertTrue(destinations.size() == 1);
		assertTrue(destinations.get(0).toString().equals("Error"));
		
		destinations = map.getExceptionDestinations(getNode("OutPoint"), new NullPointerException("foo"));
		assertTrue(destinations.size() == 1);
		assertTrue(destinations.get(0).toString().equals("Error"));
	}

	public void test2() {
		IRoutingMap map = getRoutingMap("test2");
		
		List destinations = map.getProcessDestinations(getNode("InPoint"));
		assertTrue(destinations.size() == 1);
		assertTrue(destinations.get(0).toString().equals("Processor1"));
		
		destinations = map.getProcessDestinations(getNode("Processor1"));
		assertTrue(destinations.size() == 2);
		assertTrue(destinations.get(0).toString().equals("Processor2"));
		assertTrue(destinations.get(1).toString().equals("Processor3"));
		
		destinations = map.getProcessDestinations(getNode("Processor2"));
		assertTrue(destinations.size() == 1);
		assertTrue(destinations.get(0).toString().equals("OutPoint"));
		
		destinations = map.getProcessDestinations(getNode("Processor3"));
		assertTrue(destinations.size() == 0);
		
		destinations = map.getDiscardDestinations(getNode("InPoint"));
		assertTrue(destinations.size() == 1);
		assertTrue(destinations.get(0).toString().equals("Discard"));

		destinations = map.getDiscardDestinations(getNode("Processor1"));
		assertTrue(destinations.size() == 2);
		assertTrue(destinations.get(0).toString().equals("Discard"));
		assertTrue(destinations.get(1).toString().equals("Error"));

		destinations = map.getExceptionDestinations(getNode("Processor3"), new Exception("foo"));
		assertTrue(destinations.size() == 1);
		assertTrue(destinations.get(0).toString().equals("Error"));

		destinations = map.getExceptionDestinations(getNode("InPoint"), new Exception("foo"));
		assertTrue(destinations.size() == 1);
		assertTrue(destinations.get(0).toString().equals("Error"));
		
		destinations = map.getExceptionDestinations(getNode("InPoint"), new NullPointerException("foo"));
		assertTrue(destinations.size() == 2);
		assertTrue(destinations.get(0).toString().equals("Discard"));
		assertTrue(destinations.get(1).toString().equals("Error"));
	}
	
	public static final class DummyNode implements IMessageProcessor {

		private String id;
		
		public DummyNode(String id) {
			this.id = id;
		}
		
		public Response process(Message msg) {
			throw new RuntimeException("not implemented");
		}

		public void addListener(ILifecycleListener listener) {
			throw new RuntimeException("not implemented");
		}

		public boolean isState(State s) {
			throw new RuntimeException("not implemented");
		}

		public void removeListener(ILifecycleListener listener) {
			throw new RuntimeException("not implemented");
		}

		public void start() {
			throw new RuntimeException("not implemented");
		}

		public void stop() {
			throw new RuntimeException("not implemented");
		}

		public void validate(List exceptions) {
			throw new RuntimeException("not implemented");
		}

		public void waitForState(State state) {
			throw new RuntimeException("not implemented");
		}
		
		public String toString() {
			return id;
		}
	}
}
