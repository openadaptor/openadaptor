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

import org.openadaptor.core.IMessageProcessor;
import org.openadaptor.core.Message;
import org.openadaptor.core.Response;
import org.openadaptor.core.exception.MessageException;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Aug 20, 2007 by oa3 Core Team
 */

public class ProcessorNodeMessageProcessorTestCase extends AbstractTestNodeMessageProcessor {
  
  protected Object[] data;
  protected Object[] exceptionData;

  protected IMessageProcessor instantiateTestMessageProcessor() {
    return new ProcessorNode("Test as IMessageProcessor");
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    data = null;
    exceptionData = null;
  }

  protected void setUp() throws Exception {
    super.setUp();
    data = new Object[]{"foo", "bar", "foobar"};
    exceptionData = new Object[]{
      new MessageException(data[0], new RuntimeException("test"), "test"),
      data[1],
      new MessageException(data[2], new RuntimeException("test"), "test")
    };
  }

  public void testProcessWithNoProcessorSet() {
    responsePayload = inputPayload;
    super.testProcessWithNoProcessorSet();
  }

  public void testProcessWithBatch() {
    Message message = new Message(data, null, null, null);
    for (int i=0; i < data.length; i++) {
      testProcessorMock.expects(once()).method("process").with(eq(data[i])).will(returnValue(new Object[] { data[i] }));
    }
    {
      Response response = testMessageProcessor.process(message);
      Object[] output = response.getCollatedOutput();
      assertTrue(equals(data, output));
    }
  }

  public void testProcessWithExceptions() {
    for (int i=0; i < data.length; i++) {
      testProcessorMock.expects(once()).method("process").with(eq(exceptionData[i])).will(returnValue(new Object[] { exceptionData[i] }));
    }
    Response response = testMessageProcessor.process(new Message(exceptionData, null, null, null));
    Object[] output = response.getCollatedOutput();
    assertFalse(equals(data, output));
  }

  public void testProcessWithStripOutExceptionsSet() {
    for (int i=0; i < data.length; i++) {
      testProcessorMock.expects(once()).method("process").with(eq(data[i])).will(returnValue(new Object[] { data[i] }));
    }
    ((ProcessorNode)testMessageProcessor).setStripOutExceptions(true);
    Response response = testMessageProcessor.process(new Message(exceptionData, null, null, null));
    Object[] output = response.getCollatedOutput();
    assertTrue(equals(data, output));
  }

  // Handy compare Arrays helper method
  private boolean equals(Object[] a, Object[] b) {
    if (a != null && a != null && a.length == b.length) {
      for (int i = 0; i < a.length; i++) {
        if (!a[i].equals(b[i])) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }
}
