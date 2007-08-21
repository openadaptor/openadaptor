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
import org.openadaptor.core.IMessageProcessor;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.Response;
import org.openadaptor.core.lifecycle.AbstractTestIRunnable;
import org.openadaptor.core.lifecycle.IRunnable;
import org.openadaptor.core.transaction.ITransactionInitiator;
import org.openadaptor.core.transaction.TransactionManager;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Aug 20, 2007 by oa3 Core Team
 */

/** Test ReadNode as an IRunnable implementation */
public class ReadNodeRunnableTestCase extends AbstractTestIRunnable {

  protected IRunnable instantiateTestRunnable() {
    return new ReadNode("test");
  }

  protected void setUp() throws Exception {
    super.setUp();
    testRunnable = instantiateTestRunnable();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    testRunnable = null;
  }

  /**
   * This test uses a real TransactionManager and real Message/Response objects
   * but mocks up everything else.
   *
   * Important to note that the IRunnable is actually a ReadNode hence the casting
   * to manage property setting.
   */
  public void testRun() {
    Mock readConnectorMock = mock(IReadConnector.class);
    IReadConnector readConnector = (IReadConnector) readConnectorMock.proxy();

    Mock messageRouterMock = mock(IMessageProcessor.class);
    IMessageProcessor messageRouter = (IMessageProcessor) messageRouterMock.proxy();

    Response testResponse = new Response();
    testResponse.addOutput("Test Output");

    messageRouterMock.stubs().method("process").will(returnValue(testResponse));

    testRunnable.setMessageProcessor(messageRouter);

    ((ReadNode)testRunnable).setConnector(readConnector);
    ((ITransactionInitiator)testRunnable).setTransactionManager(new TransactionManager()); // Using the default implementation for now 'cos I'm too lazy to stub.
    readConnectorMock.expects(once()).method("connect");
    readConnectorMock.expects(once()).method("disconnect");

    readConnectorMock.expects(atLeastOnce()).method("isDry").
      will(onConsecutiveCalls(returnValue(false), returnValue(false), returnValue(true)));

    readConnectorMock.stubs().method("next").will(returnValue(new Object[] {"test" }));

    Object connectorReaderContext = new Object(); // Bit of connector fluff
    readConnectorMock.stubs().method("getReaderContext").will(returnValue(connectorReaderContext));

    testRunnable.start();
    testRunnable.run();

    assertTrue("Expected an exitcode of 0", testRunnable.getExitCode() == 0 );
  }
}
