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
import org.openadaptor.core.*;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Aug 16, 2007 by oa3 Core Team
 */

public class ReadNodeMessageProcessorTestCase extends AbstractTestNodeMessageProcessor {

  protected Mock readConnectorMock;

  protected IMessageProcessor instantiateTestMessageProcessor() {
    ReadNode testNode =  new ReadNode("Test ReadNode as MessageProcessor");
    return testNode;
  }

  protected void instantiateMocksFor(IMessageProcessor messageProcessor) {
    super.instantiateMocksFor(messageProcessor);
    readConnectorMock = mock(IReadConnector.class);
    IReadConnector mockReadConnector = (IReadConnector) readConnectorMock.proxy();
    ((ReadNode)messageProcessor).setConnector(mockReadConnector);
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    readConnectorMock = null;
  }

  /**
   * Test invoking 'process' on a correctly configured IMessageProcessor instance.
   */
  public void testProcess() {
    readConnectorMock.stubs().method("isDry").will(returnValue(false));
    readConnectorMock.expects(once()).method("next").will(returnValue(new Object[] {inputPayload}));
    readConnectorMock.stubs().method("getReaderContext");
    super.testProcess();
  }

  /**
   * Test with no explicitly configured IDataProcessor. This is the normal case
   * for a ReadNode and the Response should contain the output from the ReadConnector.
   */
  public void testProcessWithNoProcessorSet() {
    readConnectorMock.stubs().method("isDry").will(returnValue(false));
    readConnectorMock.expects(once()).method("next").will(returnValue(new Object[] {responsePayload}));
    readConnectorMock.stubs().method("getReaderContext");
    super.testProcessWithNoProcessorSet();
  }


  public void testNullFromConnector() {
    readConnectorMock.stubs().method("isDry").will(returnValue(false));
    readConnectorMock.expects(once()).method("next").will(returnValue(null));
    Message message = new Message(new Object[]{}, null, null);
    Response response = testMessageProcessor.process(message);
    assertTrue("Expected a Response", response != null);
    assertTrue("Expected no data", response.getCollatedOutput().length == 0);
  }

  public void testNoDataFromConnector() {
    readConnectorMock.stubs().method("isDry").will(returnValue(false));
    readConnectorMock.expects(once()).method("next").will(returnValue(new Object[] {}));
    Message message = new Message(new Object[]{}, null, null);
    Response response = testMessageProcessor.process(message);
    assertTrue("Expected a Response", response != null);
    assertTrue("Expected no data", response.getCollatedOutput().length == 0);
  }

}
