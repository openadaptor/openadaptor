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
package org.openadaptor.spring;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class ConfigurableSpringAdaptorTestCase extends TestCase {

	/** testee */
	ConfigurableSpringAdaptor testee;

	/** Simple default test. */
	public void testRunSpringAdaptorAsDefault() {
		testee = new ConfigurableSpringAdaptor();
		List<String> configs = new ArrayList<String>();
		configs.add("example/tools/test/testAdaptor.xml");
		testee.setConfigUrls(configs);

		System.setProperty("inputfile", new File("").getAbsolutePath()
				+ "/example/tools/test/input/input.txt");
		System.setProperty("key", "key1");
		System.setProperty("value", "value1");
		assertEquals(0, testee.run());
	}

	/** Run the adaptor three times. */
	public void testRunAdaptor3Times() {
		testee = new ConfigurableSpringAdaptor();
		List<String> configs = new ArrayList<String>();
		configs.add("example/tools/test/testAdaptor.xml");
		testee.setConfigUrls(configs);

		System.setProperty("inputfile", new File("").getAbsolutePath()
				+ "/example/tools/test/input/input.txt");
		System.setProperty("key", "key1");
		System.setProperty("value", "value1");
		// 1
		assertEquals(0, testee.run());
		// 2
		assertEquals(0, testee.run());
		// 3
		assertEquals(0, testee.run());
	}

	/** Test given propUrls. */
	public void testPropUrls() {
		testee = new ConfigurableSpringAdaptor();
		List<String> configs = new ArrayList<String>();
		configs.add("example/tools/test/testAdaptor.xml");
		testee.setConfigUrls(configs);

		// Add additional propUrls
		List<String> props = new ArrayList<String>();
		props.add("example/tools/test/key.properties");
		props.add("example/tools/test/value.properties");
		testee.setPropsUrls(props);

		System.setProperty("inputfile", new File("").getAbsolutePath()
				+ "/example/tools/test/input/input.txt");
		assertEquals(0, testee.run());
	}

	/**
	 * Test given application context.
	 * 
	 * @throws IOException
	 * @throws BeansException
	 */
	public void testAppContext() throws BeansException, IOException {
		Resource res = new FileSystemResource(new File("").getAbsolutePath()
				+ "/example/tools/test/testAdaptor.xml");

		GenericApplicationContext ctx = new GenericApplicationContext(
				new XmlBeanFactory(res));
		testee = new ConfigurableSpringAdaptor(ctx);
		testee.setIgnoreConfigUrls(true);

		System.setProperty("inputfile", new File("").getAbsolutePath()
				+ "/example/tools/test/input/input.txt");
		System.setProperty("key", "key1");
		System.setProperty("value", "value1");
		assertEquals(0, testee.run());
	}

	/**
	 * Test no config urls.
	 * 
	 * @throws IOException
	 * @throws BeansException
	 */
	public void testAppContextConfigUrlsError() throws BeansException,
			IOException {
		Resource res = new FileSystemResource(new File("").getAbsolutePath()
				+ "/example/tools/test/testAdaptor.xml");

		testee = new ConfigurableSpringAdaptor(new GenericApplicationContext(
				new XmlBeanFactory(res)));

		System.setProperty("inputfile", new File("").getAbsolutePath()
				+ "/example/tools/test/input/input.txt");
		System.setProperty("key", "key1");
		System.setProperty("value", "value1");

		try {
			testee.run();
			fail("Fail!");
		} catch (RuntimeException expected) {
			assertNotNull(expected);
			assertEquals("no config urls specified", expected.getMessage());
		}
	}
}
