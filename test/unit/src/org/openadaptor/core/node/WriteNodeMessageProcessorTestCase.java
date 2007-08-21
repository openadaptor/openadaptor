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
  /**
   * Instantiate a test object. Add a well behaved Mock WriteConnector.
   *
   * @return IMessageProcessor  The component being tested.
   */
  protected IMessageProcessor instantiateTestMessageProcessor() {
    WriteNode testInstance = new WriteNode("WriteNode as IMessageProcessor");
    // Simple well behaved ConnectorMock
    Mock connectorMock = mock(IWriteConnector.class);
    testInstance.setConnector((IWriteConnector)connectorMock.proxy());
    // Expectations of a well behaved connector
    connectorMock.stubs().method("deliver");
    return testInstance;
  }

  /**
   * Test with a Mock Processor configured to produce a specific result.
   * Overridden as Connector expectations need to be set as well.
   */
  public void testProcessWithProcessor() {
    String inputOne = "inputone";
    String returnOne = "returnone";

    Mock testProcessorMock = mock(IDataProcessor.class);
    IDataProcessor testProcessor = (IDataProcessor)testProcessorMock.proxy();

    Object[] processedValue = new Object[]{returnOne};
    testProcessorMock.expects(once()).method("process").with(eq(inputOne)).will(returnValue(processedValue));

    Mock connectorMock = mock(IWriteConnector.class);
    ((WriteNode)testMessageProcessor).setConnector((IWriteConnector)connectorMock.proxy());
    // Expectations of a well behaved connector
    connectorMock.expects(once()).method("deliver").will(returnValue(returnOne));

    ((Node)testMessageProcessor).setProcessor(testProcessor);

    Message message = new Message(new Object[] {inputOne}, null, null);
    Response response = testMessageProcessor.process(message);

    assertTrue("Expected a Response", response != null);
    assertTrue("Expected one batch in the response", response.getBatches().size() == 1);
    assertTrue("Response value wasn't as expected", response.getCollatedOutput()[0] == returnOne);
  }
}
