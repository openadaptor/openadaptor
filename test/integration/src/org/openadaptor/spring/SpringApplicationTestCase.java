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

import junit.framework.TestCase;
import org.openadaptor.util.ResourceUtil;

import java.util.ArrayList;

public class SpringApplicationTestCase extends TestCase {

  public static String result;
  protected static final String RESOURCE_LOCATION = "test/integration/src/";

  public void setUp() {
    result = "";
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    //System.clearProperty("message"); // clear after each test to ensure only set if a test requires it.
  }

  public void testNoProps() {
    SpringAdaptor app = new SpringAdaptor();
    app.addConfigUrl(ResourceUtil.getResourcePath(this, RESOURCE_LOCATION, "test.xml"));
    app.setBeanId("Test");
    app.run();
    assertTrue(result.equals("foo"));
  }

  public void testSystemProps() {
    System.setProperty("message", "foobar");
    SpringAdaptor app = new SpringAdaptor();
    app.addConfigUrl(ResourceUtil.getResourcePath(this, RESOURCE_LOCATION, "test.xml"));
    app.setBeanId("Test");
    app.run();
    assertTrue(result.equals("foobar"));
  }

  /**
   * Using a props file with no configurer we allow one to be auto generated but still get an error
   * as no system properties have been set.
   */
  public void testNoSystemPropsAuto() {
    SpringAdaptor app = new SpringAdaptor();
    app.addConfigUrl(ResourceUtil.getResourcePath(this, RESOURCE_LOCATION, "test_no_configurer.xml"));
    app.setBeanId("Test");
    try {
      app.run();
      fail("Should have thrown an exception.");
    } catch (Exception e) {
      // This is cool. We are expecting an exception as there should be a property ref that is unresolvable.
    }
  }

  /**
   * Using a props file with no configurer we allow one to be auto generated. This time we set
   * the system property that we want to resolve the value to.
   */
  public void testSystemPropsAuto() {
    System.setProperty("noconfigurer_message", "foobar");
    SpringAdaptor app = new SpringAdaptor();
    app.addConfigUrl(ResourceUtil.getResourcePath(this, RESOURCE_LOCATION, "test_no_configurer.xml"));
    app.setBeanId("Test");
    app.run();
    assertTrue(result.equals("foobar"));
  }

  /**
   * Using a props file with no configurer we suppress auto generating one. This time we get
   * no error as no attempt to resolve a property is made. We just get the ${message} string.
   */
  public void testSystemPropsAutoSuppressed() {
    System.setProperty("noconfigurer_message", "foobar");
    SpringAdaptor app = new SpringAdaptor();
    app.addConfigUrl(ResourceUtil.getResourcePath(this, RESOURCE_LOCATION, "test_no_configurer.xml"));
    app.setBeanId("Test");
    app.setSuppressAutomaticPropsConfig(true);
    assertFalse(result.equals("foobar"));
    // We are not expecting to see ${message} resolved to the value of the message system property.
    // This is because we suppressed PropertyPlaceholderCongigurer auto generate and didn't supply one in the
    // spring configuration file.
  }

  /**
   * Using a props file with no configurer we allow one to be auto generated. This time we supply a URL to a
   * java properties file and test that the properties are resolved correctly.
   */
  public void testPropsURLAuto() {
    SpringAdaptor app = new SpringAdaptor();
    app.addConfigUrl(ResourceUtil.getResourcePath(this, RESOURCE_LOCATION, "test_no_configurer.xml"));
    app.setBeanId("Test");
    ArrayList props = new ArrayList();
    props.add("file:test/integration/src/org/openadaptor/spring/test.properties");
    app.setPropsUrls(props);
    app.run();
    assertTrue("Expected nofoo", result.equals("nofoo"));
  }

  public void testRunner() {
    SpringAdaptor app = new SpringAdaptor();
    app.addConfigUrl(ResourceUtil.getResourcePath(this, RESOURCE_LOCATION, "test.xml"));
    app.setBeanId("Runnables");
    app.run();
    assertTrue(result.equals("run1run2") || result.equals("run2run1"));
  }

  public static final class Test implements Runnable {

    private String mMessage;

    public Test() {}

    public Test(String message) {
      mMessage = message;
    }

    public void run() {
      System.err.println(mMessage);
      result += mMessage;
    }
  }
}
