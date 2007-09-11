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
import org.openadaptor.core.IMessageProcessor;
import org.openadaptor.core.Response;
import org.openadaptor.core.lifecycle.ILifecycleComponent;
import org.openadaptor.core.lifecycle.IRunnable;
import org.openadaptor.core.lifecycle.State;
import org.openadaptor.core.transaction.AbstractTestITransactionInitiator;
import org.openadaptor.core.transaction.ITransaction;
import org.openadaptor.core.transaction.ITransactionInitiator;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Sep 5, 2007 by oa3 Core Team
 */

public class TransactedNodeRunnerTransactionInitiatorTestCase extends AbstractTestITransactionInitiator {

  private Mock transactionMock;
  private Mock mockMessageProcessor;
  private Mock mockManagedComponent;

  protected ITransactionInitiator instantiateTestTransactionInitiator() {
    TransactedNodeRunner nodeRunner = new TransactedNodeRunner();
    nodeRunner.setId("TestTransactedNodeRunner");
    return nodeRunner;
  }

  protected void instantiateMocksFor(ITransactionInitiator transactionInitiator) {
    super.instantiateMocksFor(transactionInitiator);
    transactionMock = mock(ITransaction.class);
    mockManagedComponent = mock(ILifecycleComponent.class);
    mockMessageProcessor = mock(IMessageProcessor.class);
    ((AbstractNodeRunner)transactionInitiator).setManagedComponent((ILifecycleComponent)mockManagedComponent.proxy());
    ((AbstractNodeRunner)transactionInitiator).setMessageProcessorDelegate((IMessageProcessor)mockMessageProcessor.proxy());
  }

  /**
   * run(..) is the method that initiates transactions etc from the ReadNode perspective.
   */

  public void testRun() {

  mockMessageProcessor.stubs().method("process").will(returnValue(new Response()));

  mockManagedComponent.stubs().method("start");
  mockManagedComponent.stubs().method("stop");

   mockManagedComponent.stubs().method("isState").with(eq(State.STARTED)).will(
      onConsecutiveCalls(returnValue(true), returnValue(true), returnValue(true), returnValue(false)));

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

    mockMessageProcessor.stubs().method("process").will(returnValue(new Response()));

    mockManagedComponent.stubs().method("start");
    mockManagedComponent.stubs().method("stop");
    mockManagedComponent.stubs().method("isState").with(eq(State.STARTED)).will(
      onConsecutiveCalls(returnValue(true), returnValue(true), returnValue(true), returnValue(false)));

    transactionManagerMock.expects(never()).method("getTransaction");
    transactionMock.expects(never()).method("commit");
    transactionMock.expects(never()).method("rollback");
    transactionMock.expects(never()).method("getErrorOrException");

    ((IRunnable) testTransactionInitiator).start();
    ((IRunnable) testTransactionInitiator).run();
  }

  // throw new RuntimeException("transaction has been marked for rollback only")

  public void testRunWithCommitFailure() {
    Exception testException = new RuntimeException("transaction has been marked for rollback only");
    mockMessageProcessor.stubs().method("process").will(returnValue(new Response()));
    mockManagedComponent.stubs().method("start");
    mockManagedComponent.stubs().method("stop");
    mockManagedComponent.stubs().method("isState").with(eq(State.STARTED)).will(
      onConsecutiveCalls(returnValue(true), returnValue(true), returnValue(true), returnValue(false)));

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

    Exception testException = new RuntimeException("transaction has been marked for rollback only");
    mockMessageProcessor.stubs().method("process").will(returnValue(new Response()));
    mockManagedComponent.stubs().method("start");
    mockManagedComponent.stubs().method("stop");
    mockManagedComponent.stubs().method("isState").with(eq(State.STARTED)).will(returnValue(true));

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
