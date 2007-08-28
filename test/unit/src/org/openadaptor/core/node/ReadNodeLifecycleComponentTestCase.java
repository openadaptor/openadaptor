/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved.

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
package org.openadaptor.core.node;

import org.jmock.Mock;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.lifecycle.ILifecycleComponent;
import org.openadaptor.core.lifecycle.State;

import java.util.ArrayList;
import java.util.List;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Aug 20, 2007 by oa3 Core Team
 */

/**
 *
 * Concrete test of ReadNode as an ILifecycleComponent implementation.
 *
 * Most tests are overridden as configuration of ReadNode is different to that of Node.
 * Probably means that Node should really be an abstract superclass for the other Node
 * classes.
 */
public class ReadNodeLifecycleComponentTestCase extends AbstractTestNodeLifecycleComponent {

  protected Mock readConnectorMock;

  protected ILifecycleComponent instantiateTestLifecycleComponent() {
    ReadNode testInstance = new ReadNode("ReadNode LifeCycleComponentTest");
    // Add a mock Connector. Expectations should be set by the individual tests.
    readConnectorMock = mock(IReadConnector.class);
    testInstance.setConnector((IReadConnector) readConnectorMock.proxy());
    return testInstance;
  }

  protected void instantiateMocksFor(ILifecycleComponent lifecycleComponent) {
    super.instantiateMocksFor(lifecycleComponent);
    readConnectorMock = mock(IReadConnector.class);
    ((ReadNode)lifecycleComponent).setConnector((IReadConnector) readConnectorMock.proxy());
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    readConnectorMock = null;
  }

  public void testStart() {
    readConnectorMock.expects(once()).method("connect");
    super.testStart();
  }

  /**
   * Because ReadNode is a Runnable and stopping requires it to be "Running"
   * rather than merely started before it can be stopped there is too much
   * crossover between roles to test simply. Therefore for now for this component
   * is tested for aa switch to "STOPPING" instead.
   */
  public void testStartStop() {
    // Set Connector expectations
    readConnectorMock.stubs().method("connect");
    assertTrue("Component should be in a stopped state initially.", testLifecycleComponent.isState(State.STOPPED));
    try {
      testLifecycleComponent.start();
    } catch (Exception e) {
      fail("Unexpected Exception while starting [" + e + "]");
    }
    assertTrue("Component should now be started.", testLifecycleComponent.isState(State.STARTED));
    try {
      testLifecycleComponent.stop();
      testLifecycleComponent.waitForState(State.STOPPING);
    } catch (Exception e) {
      fail("Unexpected Exception while stopping [" + e + "]");
    }
    assertTrue("Component should now be stopped.", testLifecycleComponent.isState(State.STOPPING));

  }

  public void testValidation() {
    List exceptions = new ArrayList();
    // Configure the mock connector to expect a validate method invocation.
    readConnectorMock.expects(once()).method("validate").with(eq(exceptions));
    super.testValidation();
  }

  public void testValidationWithNullProcessor() {
    List exceptions = new ArrayList();
    readConnectorMock.expects(once()).method("validate").with(eq(exceptions));    
    ((Node) testLifecycleComponent).setProcessor(IDataProcessor.NULL_PROCESSOR);

    testLifecycleComponent.validate(exceptions);

    assertTrue("Unexpected exceptions", exceptions.size() == 0);
  }

  /** Should fail validation if there is no connector set */
  public void testFailValidationWithNoConnector() {
    List exceptions = new ArrayList();
    testProcessorMock.stubs().method("validate").with(eq(exceptions));
    ((ReadNode) testLifecycleComponent).setConnector(null);
    testLifecycleComponent.validate(exceptions);

    assertTrue("Unexpected exceptions", exceptions.size() == 1);
  }
}
