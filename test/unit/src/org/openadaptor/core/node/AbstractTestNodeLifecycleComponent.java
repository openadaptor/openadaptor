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
import org.openadaptor.core.lifecycle.AbstractTestILifecycleComponent;
import org.openadaptor.core.lifecycle.State;
import org.openadaptor.core.lifecycle.ILifecycleComponent;

import java.util.ArrayList;
import java.util.List;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Aug 20, 2007 by oa3 Core Team
 */

abstract public class AbstractTestNodeLifecycleComponent extends AbstractTestILifecycleComponent {
  protected Mock testProcessorMock;

  protected void instantiateMocksFor(ILifecycleComponent lifecycleComponent) {
    testProcessorMock = mock(IDataProcessor.class);
    IDataProcessor testProcessor = (IDataProcessor) testProcessorMock.proxy();
    ((Node) lifecycleComponent).setProcessor(testProcessor);
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    testProcessorMock = null;
  }

  public void testValidation() {
    List exceptions = new ArrayList();
    testProcessorMock.expects(once()).method("validate").with(eq(exceptions));
    testLifecycleComponent.validate(exceptions);
    assertTrue("Unexpected exceptions", exceptions.size() == 0);
  }

  public void testValidationWithNullProcessor() {
    List exceptions = new ArrayList();
    ((Node)testLifecycleComponent).setProcessor(IDataProcessor.NULL_PROCESSOR);

    testLifecycleComponent.validate(exceptions);
    assertTrue("Unexpected exceptions", exceptions.size() == 0);
  }

  public void testStartStop() {
    assertTrue("Component should be in a stopped state initially.", testLifecycleComponent.isState(State.STOPPED));
    try {
      testLifecycleComponent.start();
    } catch (Exception e) {
      fail("Unexpected Exception while starting [" + e + "]");
    }
    assertTrue("Component should now be started.", testLifecycleComponent.isState(State.STARTED));
    try {
      testLifecycleComponent.stop();     
    } catch (Exception e) {
      fail("Unexpected Exception while stopping [" + e + "]");
    }
    assertTrue("Component should now be stopped.", testLifecycleComponent.isState(State.STOPPED));
  }
}
