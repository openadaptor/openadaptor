package org.openadaptor.core.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openadaptor.auxil.processor.GenericEnrichmentProcessor;
import org.openadaptor.core.Component;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.IMetadataAware;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.IWriteConnector;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.router.Router;
import org.openadaptor.util.TestComponent;

import junit.framework.TestCase;

/**
 * Integration/system tests for handling of the metadata. 
 * 
 * @author Kris Lachor
 */
public class MetadataSystemTestCase extends TestCase {
  
  public static String TEST_METADATA_KEY    = "Processor";
  public static String TEST_METADATA_VALUE  = "Metadata";
  
  private Router router = new Router();
  
  private Map processMap = new HashMap();
  
  private Adaptor adaptor = new Adaptor();
 
  protected void setUp() throws Exception {
    adaptor.setMessageProcessor(router);
  }
  
  /**
   * Starts a simple adaptor that does not throw exceptions,
   * ensures there were no calls to the exceptionProcessor. 
   * 
   * Read connector -> Metadata validating write connector
   * 
   * The write connector validates metadata set in the read connector.
   */
  public void testAccessMetadata(){
    Map expectedMetadata = new HashMap();
    expectedMetadata.put(TestComponent.TEST_METADATA_KEY, TestComponent.TEST_METADATA_VALUE);
    
    IWriteConnector writeConnector = new TestComponent.MetadataValidatingWriteConnector(expectedMetadata);
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
   * Read connector -> Metadata enriching processor -> Metadata validating write connector
   * 
   * Validates metadata set in the read connector and processor.
   */
  public void testModifyMetadataInProcessor(){
    Map expectedMetadata = new HashMap();
    expectedMetadata.put(TestComponent.TEST_METADATA_KEY, TestComponent.TEST_METADATA_VALUE);
    expectedMetadata.put(TEST_METADATA_KEY, TEST_METADATA_VALUE);
    
    IWriteConnector writeConnector = new TestComponent.MetadataValidatingWriteConnector(expectedMetadata);
    Map addToMetadata = new HashMap();
    addToMetadata.put(TEST_METADATA_KEY, TEST_METADATA_VALUE);
    IDataProcessor processor = new MetadataDataProcessor(addToMetadata);
    processMap.put(new TestComponent.TestReadConnector(), processor);
    processMap.put(processor, writeConnector);
    router.setProcessMap(processMap);
    TestComponent.DummyExceptionHandler eHandler = new TestComponent.DummyExceptionHandler();
    assertTrue(eHandler.counter == 0);
    router.setExceptionProcessor(eHandler);
    
    adaptor.run();
    assertTrue(eHandler.counter == 0);
  }
  
  /**
   * Tests the Router#cloneMetadataOnFanout flag set to true;
   * 
   * Starts a simple adaptor (that does not throw exceptions),
   * ensures there were no calls to the exceptionProcessor. 
   * 
   * Tests a fan-out. The read connector fans out to two different processors.
   * First processor adds something else than the second processor to the metadata.
   * 
   * Read connector -> Metadata enriching processor 1 -> Metadata validating write connector 1
   *                -> Metadata enriching processor 2 -> Metadata validating write connector 2
   *                
   * Two different write connectors verify the metadata for both has been different. 
   */
  public void testModifyMetadataFanout_CloneMetadataOnFanout_ON(){
    Map expectedMetadata1 = new HashMap();
    expectedMetadata1.put(TestComponent.TEST_METADATA_KEY, TestComponent.TEST_METADATA_VALUE);
    expectedMetadata1.put(TEST_METADATA_KEY, TEST_METADATA_VALUE);
    IWriteConnector writeConnector1 = new TestComponent.MetadataValidatingWriteConnector(expectedMetadata1);
    
    Map expectedMetadata2 = new HashMap();
    expectedMetadata2.put(TestComponent.TEST_METADATA_KEY, TestComponent.TEST_METADATA_VALUE);
    expectedMetadata2.put("foo", "bar");
    IWriteConnector writeConnector2 = new TestComponent.MetadataValidatingWriteConnector(expectedMetadata2);
    
    Map addToMetadata1 = new HashMap();
    addToMetadata1.put(TEST_METADATA_KEY, TEST_METADATA_VALUE);
    IDataProcessor processor1 = new MetadataDataProcessor(addToMetadata1);
    Map addToMetadata2 = new HashMap();
    addToMetadata2.put("foo", "bar");
    IDataProcessor processor2 = new MetadataDataProcessor(addToMetadata2);
    
    List fanOutList = new ArrayList();
    fanOutList.add(processor1);
    fanOutList.add(processor2);
    
    IReadConnector readConnector = new TestComponent.TestReadConnector();
    processMap.put(readConnector, fanOutList);
    processMap.put(processor1, writeConnector1);
    processMap.put(processor2, writeConnector2);
    router.setProcessMap(processMap);
    TestComponent.DummyExceptionHandler eHandler = new TestComponent.DummyExceptionHandler();
    assertTrue(eHandler.counter == 0);
    router.setExceptionProcessor(eHandler);
    router.setCloneMetadataOnFanout(true);
    
    adaptor.run();
    assertTrue(eHandler.counter == 0);
  }
  
  /**
   * Tests the Router#cloneMetadataOnFanout flag set to false (default). Based on
   * testModifyMetadataFanout_CloneMetadataOnFanout_ON.
   */
  public void testModifyMetadataFanout_CloneMetadataOnFanout_OFF(){
    Map expectedMetadata1 = new HashMap();
    expectedMetadata1.put(TestComponent.TEST_METADATA_KEY, TestComponent.TEST_METADATA_VALUE);
    expectedMetadata1.put(TEST_METADATA_KEY, TEST_METADATA_VALUE);
    IWriteConnector writeConnector1 = new TestComponent.MetadataValidatingWriteConnector(expectedMetadata1);
    
    Map expectedMetadata2 = new HashMap();
    expectedMetadata2.put(TestComponent.TEST_METADATA_KEY, TestComponent.TEST_METADATA_VALUE);
    expectedMetadata2.put("foo", "bar");
    
    /* Extra element expected in Metadata (added in branch 1) */
    expectedMetadata2.put(TEST_METADATA_KEY, TEST_METADATA_VALUE);
  
    IWriteConnector writeConnector2 = new TestComponent.MetadataValidatingWriteConnector(expectedMetadata2);
    
    Map addToMetadata1 = new HashMap();
    addToMetadata1.put(TEST_METADATA_KEY, TEST_METADATA_VALUE);
    IDataProcessor processor1 = new MetadataDataProcessor(addToMetadata1);
    Map addToMetadata2 = new HashMap();
    addToMetadata2.put("foo", "bar");
    
    IDataProcessor processor2 = new MetadataDataProcessor(addToMetadata2);
    
    List fanOutList = new ArrayList();
    fanOutList.add(processor1);
    fanOutList.add(processor2);
    
    IReadConnector readConnector = new TestComponent.TestReadConnector();
    processMap.put(readConnector, fanOutList);
    processMap.put(processor1, writeConnector1);
    processMap.put(processor2, writeConnector2);
    router.setProcessMap(processMap);
    TestComponent.DummyExceptionHandler eHandler = new TestComponent.DummyExceptionHandler();
    assertTrue(eHandler.counter == 0);
    router.setExceptionProcessor(eHandler);
    
    adaptor.run();
    assertTrue(eHandler.counter == 0);
  }
  
  /**
   *  Read connector -> Metadata enriching processor -> Metadata validating write connector
   */
  public void testEnrichmentProcessor(){
    Map expectedMetadata = new HashMap();
    /* From read connector */
    expectedMetadata.put(TestComponent.TEST_METADATA_KEY, TestComponent.TEST_METADATA_VALUE);
    
    /* From enrichment processor's read connector */
    expectedMetadata.put("Enrichment Read Connector", "Hello");
    
    /* From enrichment processor itself */
    expectedMetadata.put("EnrichmentProcessor", "Hello");
    
    IWriteConnector writeConnector = new TestComponent.MetadataValidatingWriteConnector(expectedMetadata);
    
    TestComponent.TestEnrichmentReadConnector enrichReadConnector = new TestComponent.TestEnrichmentReadConnector();
    Map addToMetadata = new HashMap();
    addToMetadata.put("Enrichment Read Connector", "Hello");
    enrichReadConnector.setAddToMetadata(addToMetadata);
    MetadataEnrichmentProcessor enrichmentProcessor = new MetadataEnrichmentProcessor();
    enrichmentProcessor.setReadConnector(enrichReadConnector);
    
    IReadConnector readConnector = new TestComponent.TestReadConnector();
    processMap.put(readConnector, enrichmentProcessor);
    processMap.put(enrichmentProcessor, writeConnector);
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
    
    protected Map addToMetadata;
    
    public MetadataDataProcessor(Map addToMetadata) {
      this.addToMetadata = addToMetadata;
    }

    public Object[] process(Object data) {
      counter++;
      metadata.putAll(addToMetadata);
      return new Object[]{data};
    }
    
    public void validate(List exceptions) {}
    public void reset(Object context) {}

    public void setMetadata(Map metadata) {
      this.metadata = metadata;
    }
  }
  
  public static final class MetadataDataProcessorSplitMessage extends MetadataDataProcessor {
    
    public MetadataDataProcessorSplitMessage(Map addToMetadata) {
      super(addToMetadata);
    }

    public Object[] process(Object data) {
      super.process(data);
      return new Object[]{data, data};
    }
  }
    
  
  protected class MetadataEnrichmentProcessor extends GenericEnrichmentProcessor implements IMetadataAware{
    protected Map metadata;
    
    public Object[] enrich(Object input, Object[] enrichmentData) {
      metadata.put("EnrichmentProcessor", "Hello");      
      return super.enrich(input, enrichmentData);
    }

    public void setMetadata(Map metadata) {
      this.metadata = metadata;
    }
  }
  
}
