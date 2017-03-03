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
import org.jmock.core.stub.VoidStub;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.lifecycle.IRunnable;
import org.openadaptor.core.transaction.AbstractTestITransactionInitiator;
import org.openadaptor.core.transaction.ITransaction;
import org.openadaptor.core.transaction.ITransactionInitiator;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Aug 30, 2007 by oa3 Core Team
 */

public class ReadNodeTransactionInitiatorTestCase extends AbstractTestITransactionInitiator {
  private Mock readConnectorMock;
  private Mock transactionMock;

  protected ITransactionInitiator instantiateTestTransactionInitiator() {
    ReadNode readNode = new ReadNode();
    readNode.setId("TestReadNode");
    return readNode;
  }

  protected void instantiateMocksFor(ITransactionInitiator transactionInitiator) {
    super.instantiateMocksFor(transactionInitiator);
    readConnectorMock = mock(IReadConnector.class);
    IReadConnector mockReadConnector = (IReadConnector) readConnectorMock.proxy();
    ((ReadNode) transactionInitiator).setConnector(mockReadConnector);

    transactionMock = mock(ITransaction.class);
  }

  /**
   * run(..) is the method that initiates transactions etc from the ReadNode perspective.
   */

  public void testRun() {
    String responsePayload = "Response";

    readConnectorMock.stubs().method("connect");
    readConnectorMock.stubs().method("next").will(returnValue(new Object[]{responsePayload}));
    readConnectorMock.stubs().method("getReaderContext");
    readConnectorMock.stubs().method("disconnect");

    readConnectorMock.expects(atLeastOnce()).method("isDry").
      will(onConsecutiveCalls(returnValue(false), returnValue(false), returnValue(true)));

    transactionManagerMock.expects(atLeastOnce()).method("getTransaction").will(returnValue(transactionMock.proxy()));
    transactionMock.expects(atLeastOnce()).method("commit");
    transactionMock.expects(once()).method("rollback");
    transactionMock.expects(atLeastOnce()).method("getErrorOrException").
      will(onConsecutiveCalls(returnValue(new Throwable()), returnValue(null), returnValue(null)));

    ((IRunnable) testTransactionInitiator).start();
    ((IRunnable) testTransactionInitiator).run();
  }

  /**
   * run(..) is the method that initiates transactions etc from the ReadNode perspective.
   */

  public void testRunNoTransactionManager() {
    testTransactionInitiator.setTransactionManager(null);

    String responsePayload = "Response";

    readConnectorMock.stubs().method("connect");
    readConnectorMock.stubs().method("next").will(returnValue(new Object[]{responsePayload}));
    readConnectorMock.stubs().method("getReaderContext");
    readConnectorMock.stubs().method("disconnect");

    readConnectorMock.expects(atLeastOnce()).method("isDry").
      will(onConsecutiveCalls(returnValue(false), returnValue(false), returnValue(true)));

    transactionManagerMock.expects(never()).method("getTransaction");
    transactionMock.expects(never()).method("commit");
    transactionMock.expects(never()).method("rollback");
    transactionMock.expects(never()).method("getErrorOrException");

    ((IRunnable) testTransactionInitiator).start();
    ((IRunnable) testTransactionInitiator).run();
  }

  // throw new RuntimeException("transaction has been marked for rollback only")

  public void testRunWithCommitFailure() {
    String responsePayload = "Response";
    Exception testException = new RuntimeException("transaction has been marked for rollback only");

    readConnectorMock.stubs().method("connect");
    readConnectorMock.stubs().method("next").will(returnValue(new Object[]{responsePayload}));
    readConnectorMock.stubs().method("getReaderContext");
    readConnectorMock.stubs().method("disconnect");
    readConnectorMock.stubs().method("isDry").will(returnValue(false));

    transactionManagerMock.expects(once()).method("getTransaction").will(returnValue(transactionMock.proxy()));
    transactionMock.expects(once()).method("commit").will(throwException(testException));
    transactionMock.expects(once()).method("rollback");
    transactionMock.expects(once()).method("getErrorOrException").will(returnValue(null));
    transactionMock.expects(once()).method("setErrorOrException").with(eq(testException));

    ((IRunnable) testTransactionInitiator).start();
    ((IRunnable) testTransactionInitiator).run();

    assertTrue("Expected a non-zero exit code", ((IRunnable) testTransactionInitiator).getExitCode() != 0 );
  }

  public void testRunWithCommitFailureOnThirdMessage() {
    String responsePayload = "Response";
    Exception testException = new RuntimeException("transaction has been marked for rollback only");

    readConnectorMock.stubs().method("connect");
    readConnectorMock.stubs().method("next").will(returnValue(new Object[]{responsePayload}));
    readConnectorMock.stubs().method("getReaderContext");
    readConnectorMock.stubs().method("disconnect");
    readConnectorMock.stubs().method("isDry").will(returnValue(false));

    transactionManagerMock.stubs().method("getTransaction").will(returnValue(transactionMock.proxy()));
    transactionMock.expects(atLeastOnce()).method("commit").
      will( onConsecutiveCalls( new VoidStub(), new VoidStub(), throwException(testException)));
    transactionMock.expects(once()).method("rollback");
    transactionMock.expects(atLeastOnce()).method("getErrorOrException").will(returnValue(null));
    transactionMock.expects(once()).method("setErrorOrException").with(eq(testException));

    ((IRunnable) testTransactionInitiator).start();
    ((IRunnable) testTransactionInitiator).run();

    assertTrue("Expected a non-zero exit code", ((IRunnable) testTransactionInitiator).getExitCode() != 0 );
  }

}
