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

import java.util.List;
import java.util.ArrayList;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Aug 16, 2007 by oa3 Core Team
 */

/**
 * Concrete test case for a Node as an IMessageProcessor.
 */
public class NodeTestCase extends AbstractTestIMessageProcessor {

  protected IMessageProcessor instantiateTestMessageProcessor() {
    return new Node("test");
  }

  /**
   * text with a Mock Processor configured to produce a specific result.
   */
  public void testProcessWithProcessor() {
    Mock testProcessorMock = mock(IDataProcessor.class);
    IDataProcessor testProcessor = (IDataProcessor)testProcessorMock.proxy();

    String inputOne = "inputone";
    String returnOne = "returnone";
    testProcessorMock.expects(once()).method("process").with(eq(inputOne)).will(returnValue(new Object[] {returnOne}));

    ((Node)testMessageProcessor).setProcessor(testProcessor);

    Message message = new Message(new Object[] {inputOne}, null, null);
    Response response = testMessageProcessor.process(message);

    assertTrue("Expected a Response", response != null);
    assertTrue("Expected one batch in the response", response.getBatches().size() == 1);
    assertTrue("Response value wasn't as expected", response.getCollatedOutput()[0] == returnOne);
  }

  // Validation Tests. Not really IMessageProcessor's responsibility but I want some tests somewhere.

  public void testValidation() {
    List exceptions = new ArrayList();
    ((Node)testMessageProcessor).validate(exceptions);
    assertTrue("Unexpected exceptions", exceptions.size() == 0);
  }

  public void testValidationWithProcessor() {
    Mock testProcessorMock = mock(IDataProcessor.class);
    IDataProcessor testProcessor = (IDataProcessor)testProcessorMock.proxy();

    List exceptions = new ArrayList();
    testProcessorMock.expects(once()).method("validate").with(eq(exceptions));

    ((Node)testMessageProcessor).setProcessor(testProcessor);
    ((Node)testMessageProcessor).validate(exceptions);

    assertTrue("Unexpected exceptions", exceptions.size() == 0);
  }
}
