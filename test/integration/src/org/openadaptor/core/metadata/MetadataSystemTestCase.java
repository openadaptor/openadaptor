package org.openadaptor.core.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openadaptor.core.Component;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.IMetadataAware;
import org.openadaptor.core.IWriteConnector;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.router.Router;
import org.openadaptor.util.TestComponent;

import junit.framework.TestCase;

/**
 * Integration/system tests for the metadata. 
 * 
 * @author Kris Lachor
 */
public class MetadataSystemTestCase extends TestCase {
  
  public static String TEST_METADATA_KEY = "Processor";
  public static String TEST_METADATA_VALUE = "Metadata";
  
  private Router router = new Router();
  
  private Map processMap = new HashMap();
  
  private Adaptor adaptor = new Adaptor();
 
  protected void setUp() throws Exception {
    adaptor.setMessageProcessor(router);
  }
  
  /**
   * Starts a simple adaptor that does not throw exceptions,
   * ensures there were no calls to the exceptionProcessor. Validates metadata set in 
   * the read connector.
   */
  public void testAccessMetadata(){
    Map expectedMetadata = new HashMap();
    expectedMetadata.put(TestComponent.TEST_METADATA_KEY, TestComponent.TEST_METADATA_VALUE);
    IWriteConnector writeConnector = new MetadataAccessingWriteConnector(expectedMetadata);
    processMap.put(new TestComponent.TestReadConnector(), writeConnector);
    router.setProcessMap(processMap);
    TestComponent.DummyExceptionHandler eHandler = new TestComponent.DummyExceptionHandler();
    assertTrue(eHandler.counter == 0);
    router.setExceptionProcessor(eHandler);
    adaptor.run();
    assertTrue(eHandler.counter == 0);
  }
  
  /**
   * Starts a simple adaptor that does not throw exceptions,
   * ensures there were no calls to the exceptionProcessor. 
   * 
   * Validates metadata set in the read connector and processor.
   */
  public void testModifyMetadataInProcessor(){
    Map expectedMetadata = new HashMap();
    expectedMetadata.put(TestComponent.TEST_METADATA_KEY, TestComponent.TEST_METADATA_VALUE);
    expectedMetadata.put(TEST_METADATA_KEY, TEST_METADATA_VALUE);
    IWriteConnector writeConnector = new MetadataAccessingWriteConnector(expectedMetadata);
    processMap.put(new TestComponent.TestReadConnector(), new MetadataDataProcessor());
    processMap.put(new MetadataDataProcessor(), writeConnector);
    router.setProcessMap(processMap);
    TestComponent.DummyExceptionHandler eHandler = new TestComponent.DummyExceptionHandler();
    assertTrue(eHandler.counter == 0);
    router.setExceptionProcessor(eHandler);
    adaptor.run();
    assertTrue(eHandler.counter == 0);
  }
  
  
  
  /**
   * A processor based on {@link TestComponent.DummyDataProcessor} that also
   * accesses and adds to the message metadata.
   */
  public static class MetadataDataProcessor extends Component implements IDataProcessor, IMetadataAware {
    
    public int counter = 0;
    
    protected Map metadata;
    
    public Object[] process(Object data) {
      counter++;
      metadata.put(TEST_METADATA_KEY, TEST_METADATA_VALUE);
      return new Object[]{data};
    }
    
    public void validate(List exceptions) {}
    public void reset(Object context) {}

    public void setMetadata(Map metadata) {
      this.metadata = metadata;
    }
  }
  
  public static final class MetadataDataProcessorSplitMessage extends MetadataDataProcessor {
    public Object[] process(Object data) {
      counter++;
      metadata.put(TEST_METADATA_KEY, TEST_METADATA_VALUE);
      return new Object[]{data, data};
    }
  }
    
  
  /**
   * A write connector based on {@link TestComponent.TestWriteConnector} that also
   * validates the metadata.
   */
  protected class MetadataAccessingWriteConnector extends Component 
       implements IWriteConnector, IMetadataAware {
    
    protected Map metadata;
    
    public int counter = 0;
    
    public List dataCollection = new ArrayList();
  
    private Map expectedMetadata;
    
    public MetadataAccessingWriteConnector(Map expectedMetadata) {
      this.expectedMetadata = expectedMetadata;
    }

    public void connect() {}
    
    public void disconnect() {}
    
    public Object deliver(Object[] data) {
       counter++;
       if(data == null || data.length == 0){
         throw new RuntimeException("No data to write");
       }
       dataCollection.add(data);
       
       /* Check the metadata */
       if(metadata.size()!= expectedMetadata.size()){
         throw new RuntimeException("Wrong metadata size");
       }
       Iterator it = expectedMetadata.keySet().iterator();
       while(it.hasNext()){
         Object key = it.next();
         if(!metadata.get(key).equals(expectedMetadata.get(key))){
           throw new RuntimeException("Wrong metadata content");
         }
       }       
       return null;
    }
    
    public void validate(List exceptions) {}

    public void setMetadata(Map metadata) {
      this.metadata = metadata;
    }
  }
  
  
}
