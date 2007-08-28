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
package org.openadaptor.core;

import org.jmock.MockObjectTestCase;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Aug 16, 2007 by oa3 Core Team
 */

/**
 * Abstract test class that should be extended by test classes for IMessageProcessor implementations.
 */
public abstract class AbstractTestIMessageProcessor extends MockObjectTestCase {

  protected IMessageProcessor testMessageProcessor;
  protected String inputPayload = "inputone";
  protected String responsePayload = "returnone";

  protected void setUp() throws Exception {
    super.setUp();
    inputPayload = "inputone";
    responsePayload = "returnone";
    testMessageProcessor = instantiateTestMessageProcessor();
    instantiateMocksFor(testMessageProcessor);
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    inputPayload = null;
    responsePayload = null;
    testMessageProcessor = null;
  }

  /**
   * Instantiate a test object. Basic assumption here is the component is well enough
   * configured that "process" can be  invoked on it.
   *
   * @return IMessageProcessor  The component being tested.
   */
  protected abstract IMessageProcessor instantiateTestMessageProcessor();

  protected abstract void instantiateMocksFor(IMessageProcessor messageProcessor);

  /**
   * Test invoking 'process' on a correctly configured IMessageProcessor instance.
   */
  public void testProcess() {
    Message message = new Message(new Object[] { inputPayload }, null, null);
    Response response = testMessageProcessor.process(message);
    assertTrue("Expected a real response object", response != null);
    assertTrue("Expected Batch size of one in the response", response.getBatches().size() == 1);
    assertTrue("Did not get expected data in the response", response.getCollatedOutput()[0] == responsePayload);

  }


}
