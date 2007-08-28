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

import org.openadaptor.core.lifecycle.ILifecycleComponent;
import org.openadaptor.core.IWriteConnector;
import org.jmock.Mock;

import java.util.List;
import java.util.ArrayList;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Aug 23, 2007 by oa3 Core Team
 */

public class WriteNodeLifecycleComponentTestCase extends AbstractTestNodeLifecycleComponent {
  protected Mock writeConnectorMock;

  /**
   * Override to instantiate object to be tested
   */
  protected ILifecycleComponent instantiateTestLifecycleComponent() {
    return new WriteNode("WriteNode as ILifecyleComponent Test");
  }

  protected void instantiateMocksFor(ILifecycleComponent lifecycleComponent) {
    super.instantiateMocksFor(lifecycleComponent);
    writeConnectorMock = mock(IWriteConnector.class);
    ((WriteNode)lifecycleComponent).setConnector((IWriteConnector)writeConnectorMock.proxy());
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    writeConnectorMock = null;
  }

  public void testValidation() {
    writeConnectorMock.expects(once()).method("validate");
    super.testValidation();
  }

  public void testValidationWithNullProcessor() {
    writeConnectorMock.expects(once()).method("validate");
    super.testValidationWithNullProcessor();
  }

  /** Should fail validation if there is no connector set */
  public void testFailValidationWithNoConnector() {
    List exceptions = new ArrayList();
    testProcessorMock.stubs().method("validate").with(eq(exceptions));
    ((WriteNode) testLifecycleComponent).setConnector(null);
    testLifecycleComponent.validate(exceptions);

    assertTrue("Unexpected exceptions", exceptions.size() == 1);
  }

  public void testStart() {
    writeConnectorMock.expects(once()).method("connect");
    super.testStart();
  }

  public void testStartStop() {
    writeConnectorMock.expects(once()).method("connect");
    writeConnectorMock.expects(once()).method("disconnect");
    super.testStartStop();
  }
}
