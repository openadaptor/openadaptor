package org.openadaptor.core.node;

import java.util.HashMap;
import java.util.Map;

import org.jmock.Mock;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.IMessageProcessor;
import org.openadaptor.core.Message;
import org.openadaptor.core.Response;

public class RunnableNodeWrapperMessageProcessorTestCase extends
    AbstractTestNodeMessageProcessor {

  protected Mock messageProcessorDelegateMock;
  protected IMessageProcessor messageProcessorDelegate;

  protected IMessageProcessor instantiateTestMessageProcessor() {
    // TODO Auto-generated method stub       
    return (IMessageProcessor)new RunnableNodeWrapper();
  }
  
  protected void instantiateMocksFor(IMessageProcessor messageProcessor) {
    // Add the ProcessorMock
    messageProcessorDelegateMock = mock(IMessageProcessor.class);
    messageProcessorDelegate = (IMessageProcessor)messageProcessorDelegateMock.proxy();
    //testProcessorMock = mock(IDataProcessor.class);
    //IDataProcessor testProcessor = (IDataProcessor)testProcessorMock.proxy();
        
    ((RunnableNodeWrapper)messageProcessor).setTarget(messageProcessorDelegate);
  }  

  protected void tearDown() throws Exception {
    // TODO Auto-generated method stub
    super.tearDown();
  }

  protected void setUp() throws Exception {
    // TODO Auto-generated method stub
    super.setUp();
  }

  public void testProcess() {
    // TODO Auto-generated method stub
    
    Response expectedResponse = new Response();
    expectedResponse.addOutput(responsePayload);
    messageProcessorDelegateMock.expects(atLeastOnce()).method("process").will(returnValue(expectedResponse));
    
    Message message = new Message(new Object[] { inputPayload }, null, null, null);
    
    Response response = testMessageProcessor.process(message);
    assertTrue("Expected a real response object", response != null);
    assertTrue("Expected Batch size of one in the response", response.getBatches().size() == 1);
    assertTrue("Did not get expected data in the response", response.getCollatedOutput()[0] == responsePayload);
  }
  
  
  public void testProcessWithNoProcessorSet() {
    // This test doesn't really make sense here
  } 
  
  

}
