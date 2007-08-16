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

import java.util.ArrayList;
import java.util.List;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Aug 16, 2007 by oa3 Core Team
 */

public class ReadNodeTestCase extends NodeTestCase {

  protected IMessageProcessor instantiateTestMessageProcessor() {
    return new ReadNode("test");
  }

  public void testProcessWithReadConnector() {
    Mock readConnectorMock = mock(IReadConnector.class);
    IReadConnector readConnector = (IReadConnector) readConnectorMock.proxy();

    ((ReadNode)testMessageProcessor).setConnector(readConnector);

    Message message = new Message(new Object[] {}, null, null);
    Response response = testMessageProcessor.process(message);

    assertTrue("Expected a Response", response != null);
  }

  // Validation Tests. Not really IMessageProcessor's responsibility but I want some tests for now.

  public void testValidation() {
    Mock readConnectorMock = mock(IReadConnector.class);
    IReadConnector readConnector = (IReadConnector) readConnectorMock.proxy();

    List exceptions = new ArrayList();
    readConnectorMock.expects(once()).method("validate").with(eq(exceptions));

    ((ReadNode)testMessageProcessor).setConnector(readConnector);

    ((Node)testMessageProcessor).validate(exceptions);

    assertTrue("Unexpected exceptions", exceptions.size() == 0);
  }  

  public void testValidationWithProcessor() {
    Mock readConnectorMock = mock(IReadConnector.class);
    IReadConnector readConnector = (IReadConnector) readConnectorMock.proxy();
    Mock testProcessorMock = mock(IDataProcessor.class);
    IDataProcessor testProcessor = (IDataProcessor)testProcessorMock.proxy();

    List exceptions = new ArrayList();
    readConnectorMock.expects(once()).method("validate").with(eq(exceptions));
    testProcessorMock.expects(once()).method("validate").with(eq(exceptions));

    ((ReadNode)testMessageProcessor).setConnector(readConnector);
    ((Node)testMessageProcessor).setProcessor(testProcessor);

    ((Node)testMessageProcessor).validate(exceptions);

    assertTrue("Unexpected exceptions", exceptions.size() == 0);
  }

  public void testFailValidationWithNoConnector() {
    List exceptions = new ArrayList();
    ((Node)testMessageProcessor).validate(exceptions);

    assertTrue("Unexpected exceptions", exceptions.size() == 1);
  }
}
