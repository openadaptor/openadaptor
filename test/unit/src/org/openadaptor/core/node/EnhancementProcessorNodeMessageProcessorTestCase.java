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
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.IEnrichmentProcessor;
import org.openadaptor.core.IMessageProcessor;
import org.openadaptor.core.Message;
import org.openadaptor.core.Response;

/**
 * 
 * TODO replace processor node tests with those from enhancement processor.
 * 
 * TODO implement.
 */
public class EnhancementProcessorNodeMessageProcessorTestCase extends AbstractTestNodeMessageProcessor {
 
  protected Mock enhancementProcessorMock;
 
//  protected Object[] data;
//  protected Object[] exceptionData;

  protected IMessageProcessor instantiateTestMessageProcessor() {
    return new EnhancementProcessorNode("Test EnhancementProcessorNode as IMessageProcessor");
  }

  protected void instantiateMocksFor(IMessageProcessor messageProcessor) {
    super.instantiateMocksFor(messageProcessor);
//    enhancementProcessorMock = mock(IEnrichmentProcessor.class);   
//    IEnrichmentProcessor mockEnhancementProcessor = (IEnrichmentProcessor) enhancementProcessorMock.proxy();
//    ((EnhancementProcessorNode)messageProcessor).setEnhancementProcessor(mockEnhancementProcessor);
  }
  
  protected void tearDown() throws Exception {
    super.tearDown();
    enhancementProcessorMock = null;
  }
  
  /**
   * Test invoking 'process' on a correctly configured IMessageProcessor instance.
   */
  public void testProcess() {
    assertTrue(true);
//    // Set processor expectations
//    testProcessorMock.stubs().method("reset");
//    testProcessorMock.expects(once()).method("process").with(eq(inputPayload)).will(returnValue(new Object[] {responsePayload}));
//    super.testProcess();
  }
  
  public void testProcessWithNoProcessorSet() {
    assertTrue(true);
//    ((Node)testMessageProcessor).setProcessor(IDataProcessor.NULL_PROCESSOR); // this seems to be the default unset value
//    Message message = new Message(new Object[] {inputPayload}, null, null);
//    Response response = testMessageProcessor.process(message);
//    assertTrue("Expected a real response object", response != null);
//    assertTrue("Expected Batch size of one in the response", response.getBatches().size() == 1);
//    assertTrue("Did not get expected data in the response", response.getCollatedOutput()[0] == responsePayload);
  }

  
}
