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

import org.openadaptor.core.*;
import org.jmock.Mock;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Aug 20, 2007 by oa3 Core Team
 */

public class WriteNodeMessageProcessorTestCase extends AbstractTestNodeMessageProcessor {

  protected Mock writeConnectorMock;

  /**
   * Instantiate a test object. Add a well behaved Mock WriteConnector.
   *
   * @return IMessageProcessor  The component being tested.
   */
  protected IMessageProcessor instantiateTestMessageProcessor() {
    return new WriteNode("WriteNode as IMessageProcessor");
  }

  /** Add a mock IWriteConnector */
  protected void instantiateMocksFor(IMessageProcessor messageProcessor) {
    super.instantiateMocksFor(messageProcessor);
    // Add a mock IWriteConnector
    writeConnectorMock = mock(IWriteConnector.class);
    ((WriteNode)testMessageProcessor).setConnector((IWriteConnector)writeConnectorMock.proxy());
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    writeConnectorMock = null;
  }

  /**
   * Test invoking 'process' on a correctly configured IMessageProcessor instance.
   */
  public void testProcess() {
    // Connector will expect the output of any processor to be its input
    // We set it up so that if it receives the expected input it simple echoes it.
    // Since a mock IDataProcessor is configured, the expected input is its output
    writeConnectorMock.expects(once()).method("deliver").with(eq(new Object[] {responsePayload})).will(returnValue(responsePayload));
    super.testProcess();
  }

  /**
   * Test behaviour when no DataProcessor is explicitly configured.
   * For a WriteNode this is the normal case.
   */
  public void testProcessWithNoProcessorSet() {
    // Connector will expect the output of any processor to be its input
    // We set it up so that if it receives the expected input it simply echoes the response payload.
    writeConnectorMock.expects(once()).method("deliver").with(eq(new Object[] {inputPayload})).will(returnValue(responsePayload));
    super.testProcessWithNoProcessorSet();
  }
}
