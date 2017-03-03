package org.openadaptor.core.history;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.IWriteConnector;
import org.openadaptor.core.Message;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.router.Router;
import org.openadaptor.util.TestComponent;

import junit.framework.TestCase;

/**
 * Integration tests for recording of message histories. 
 * 
 * @author Kris Lachor
 */
public class MessageHistoryTestCase extends TestCase {
 
  private Router router = new Router();
	   
  private Map processMap = new HashMap();
	  
  private Adaptor adaptor = new Adaptor();
	 
  protected void setUp() throws Exception {
    adaptor.setMessageProcessor(router);
  }

  /**
   * Starts an adaptor that does not record message history.
   * Checks no history was recorded.
   * 
   * Read connector -> dummy processor -> history validating processor write connector.
   * 
   * The write connector validates no message history is available in messge metadata.
   */
  public void testHistoryDisabled(){
	Map expectedMetadata = new HashMap();
	expectedMetadata.put(TestComponent.TEST_METADATA_KEY, TestComponent.TEST_METADATA_VALUE);  
	
	IDataProcessor processor = new TestComponent.DummyDataProcessor();
    processMap.put(new TestComponent.TestReadConnector(), processor);
    IWriteConnector writeConnector = new TestComponent.MetadataValidatingWriteConnector(expectedMetadata);
    processMap.put(processor, writeConnector);
    
    assertFalse(router.isHistoryEnabled());
    
    router.setProcessMap(processMap);
    TestComponent.DummyExceptionHandler eHandler = new TestComponent.DummyExceptionHandler();
    assertTrue(eHandler.counter == 0);
    router.setExceptionProcessor(eHandler);
    adaptor.run();
    assertTrue(eHandler.counter == 0);
  }  
	

  /**
   * Starts an adaptor that does records message history.
   * Checks correct history was recorded.
   * 
   * Read connector -> dummy processor -> history validating processor write connector.
   */
  public void testHistoryEnabled(){
	Map expectedMetadata = new HashMap();
	expectedMetadata.put(TestComponent.TEST_METADATA_KEY, TestComponent.TEST_METADATA_VALUE);  
	
	List expectedHistory = new ArrayList();
	expectedHistory.add("UNNAMED_MESSAGE_PROCESSOR");
	expectedHistory.add(new TestComponent.DummyDataProcessor().getClass().getName());
	expectedHistory.add(new TestComponent.MetadataValidatingWriteConnector(expectedMetadata).getClass().getName());
	expectedMetadata.put(Message.MESSAGE_HISTORY_KEY, expectedHistory);  
	
	router.setHistoryEnabled(true);
	IDataProcessor processor = new TestComponent.DummyDataProcessor();
    processMap.put(new TestComponent.TestReadConnector(), processor);
    IWriteConnector writeConnector = new TestComponent.MetadataValidatingWriteConnector(expectedMetadata);
    processMap.put(processor, writeConnector);
    
    assertTrue(router.isHistoryEnabled());
    
    router.setProcessMap(processMap);
    TestComponent.DummyExceptionHandler eHandler = new TestComponent.DummyExceptionHandler();
    assertTrue(eHandler.counter == 0);
    router.setExceptionProcessor(eHandler);
    adaptor.run();
    assertTrue(eHandler.counter == 0);
  }  
  
  
  /**
   * Checks recording of message history in a fan-out scenario.
   * 
   * Read connector -> dummy processor 1 -> history validating processor write connector 1.
   *                -> dummy processor 2 -> history validating processor write connector 2.
   */
  public void testHistoryFanout(){
    Map expectedMetadata1 = new HashMap();
    expectedMetadata1.put(TestComponent.TEST_METADATA_KEY, TestComponent.TEST_METADATA_VALUE);  
    
    List expectedHistory1 = new ArrayList();
    expectedHistory1.add("UNNAMED_MESSAGE_PROCESSOR");
    expectedHistory1.add(new TestComponent.DummyDataProcessor().getClass().getName());
    expectedHistory1.add(new TestComponent.MetadataValidatingWriteConnector(expectedMetadata1).getClass().getName());
    expectedMetadata1.put(Message.MESSAGE_HISTORY_KEY, expectedHistory1);  
    
    Map expectedMetadata2 = new HashMap();
    expectedMetadata2.put(TestComponent.TEST_METADATA_KEY, TestComponent.TEST_METADATA_VALUE);  
    
    List expectedHistory2 = new ArrayList();
    expectedHistory2.add("UNNAMED_MESSAGE_PROCESSOR");
    expectedHistory2.add(new TestComponent.DummyDataProcessor().getClass().getName());
    expectedHistory2.add(new TestComponent.MetadataValidatingWriteConnector(expectedMetadata1).getClass().getName());
    expectedHistory2.add(new TestComponent.DummyDataProcessor().getClass().getName());
    expectedHistory2.add(new TestComponent.MetadataValidatingWriteConnector(expectedMetadata1).getClass().getName());
    expectedMetadata2.put(Message.MESSAGE_HISTORY_KEY, expectedHistory1);  
    
    router.setHistoryEnabled(true);
    IDataProcessor processor1 = new TestComponent.DummyDataProcessor();
    processMap.put(new TestComponent.TestReadConnector(), processor1);
    IWriteConnector writeConnector1 = new TestComponent.MetadataValidatingWriteConnector(expectedMetadata1);
    processMap.put(processor1, writeConnector1);
    IDataProcessor processor2 = new TestComponent.DummyDataProcessor();
    processMap.put(new TestComponent.TestReadConnector(), processor2);
    IWriteConnector writeConnector2 = new TestComponent.MetadataValidatingWriteConnector(expectedMetadata2);
    processMap.put(processor2, writeConnector2);
    
    assertTrue(router.isHistoryEnabled());
    
    router.setProcessMap(processMap);
    TestComponent.DummyExceptionHandler eHandler = new TestComponent.DummyExceptionHandler();
    assertTrue(eHandler.counter == 0);
    router.setExceptionProcessor(eHandler);
    adaptor.run();
    assertTrue(eHandler.counter == 0);
  }  
  
}
