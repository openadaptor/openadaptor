package org.openadaptor.core.adaptor;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.openadaptor.core.Component;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.IWriteConnector;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.adaptor.AdaptorRunConfiguration;
import org.openadaptor.core.processor.TestProcessor;
import org.openadaptor.core.router.Pipeline;

public class AdaptorRunConfigurationTest extends TestCase {

  private static final Object DATA = "foobar";

  private Adaptor createTestAdaptor(Object[] pipeline,Object exceptionHandler) {
    Pipeline p=new Pipeline();
    p.setProcessors(Arrays.asList(pipeline));
    if (exceptionHandler!=null) {
      p.setExceptionProcessor(exceptionHandler);
    }
    Adaptor adaptor=new Adaptor();
    adaptor.setMessageProcessor(p);
    return adaptor;
  }
  private Adaptor createTestAdaptor(Object[] pipeline) {
    return createTestAdaptor(pipeline,null);
  }
  
  public void testStop() {

    MyTestReadConnector reader = new MyTestReadConnector();
    MyTestProcessor processor = new MyTestProcessor();
    MyTestWriteConnector writer = new MyTestWriteConnector();
    
    Adaptor adaptor = createTestAdaptor(new Object[] { reader, processor, writer});
    AdaptorRunConfiguration config = new AdaptorRunConfiguration();
    config.setStopCronExpression("0,10,20,30,40,50 * * * * ?");
    adaptor.setRunConfiguration(config);
    adaptor.run();
    
    assertTrue(adaptor.getExitCode() == 0);
    assertTrue(reader.connectedRanAndDisconnected());
    assertTrue(processor.connectedRanAndDisconnected());
    assertTrue(writer.connectedRanAndDisconnected());
    
  }

  public void testRestart() {

    MyTestReadConnector reader = new MyTestReadConnector();
    MyTestProcessor processor = new MyTestProcessor();
    MyTestWriteConnector writer = new MyTestWriteConnector();
    
    Adaptor adaptor = createTestAdaptor(new Object[] { reader, processor, writer});
    AdaptorRunConfiguration config = new AdaptorRunConfiguration();
    config.setRestartCronExpression("0,10,20,30,40,50 * * * * ?");
    adaptor.setRunConfiguration(config);
    reader.setRestartLimit(config, 3);
    adaptor.run();
    
    assertTrue(adaptor.getExitCode() == 0);
    assertTrue(reader.connectedRanAndDisconnected());
    assertTrue(processor.connectedRanAndDisconnected());
    assertTrue(writer.connectedRanAndDisconnected());
    assertTrue(reader.getRunCount() == 3);
    
  }
  
  public void testStartStop() {

    MyTestReadConnector reader = new MyTestReadConnector();
    MyTestProcessor processor = new MyTestProcessor();
    MyTestWriteConnector writer = new MyTestWriteConnector();
    
    Adaptor adaptor = createTestAdaptor(new Object[] { reader, processor, writer});
    AdaptorRunConfiguration config = new AdaptorRunConfiguration();
    config.setStartCronExpression("0,15,30,45 * * * * ?");
    config.setStopCronExpression("5,20,35,50 * * * * ?");
    adaptor.setRunConfiguration(config);
    reader.setRestartLimit(config, 2);
    adaptor.run();
    
    assertTrue(adaptor.getExitCode() == 0);
    assertTrue(reader.connectedRanAndDisconnected());
    assertTrue(processor.connectedRanAndDisconnected());
    assertTrue(writer.connectedRanAndDisconnected());
    assertTrue(reader.getRunCount() == 2);
    
  }

  public void testRestartOnFail() {

    MyTestReadConnector reader = new MyTestReadConnector();
    TestProcessor processor = new TestProcessor();
    processor.setExceptionFrequency(2);
    TestComponent writer = new MyTestWriteConnector();
    
    Adaptor adaptor = createTestAdaptor(new Object[] { reader, processor, writer});
    AdaptorRunConfiguration config = new AdaptorRunConfiguration();
    config.setRestartAfterFailLimit(3);
    config.setRestartAfterFailDelayMs(2 * 1000);
    adaptor.setRunConfiguration(config);
    adaptor.run();
    
    assertTrue(adaptor.getExitCode() != 0);
    assertTrue(reader.connectedRanAndDisconnected());
    assertTrue(writer.connectedRanAndDisconnected());
    assertTrue(reader.getRunCount() == 3);
    
  }

  public void testRestartOnFailWithCron() {

    MyTestReadConnector reader = new MyTestReadConnector();
    TestProcessor processor = new TestProcessor();
    processor.setExceptionFrequency(2);
    TestComponent writer = new MyTestWriteConnector();
    
    Adaptor adaptor = createTestAdaptor(new Object[] { reader, processor, writer});
    AdaptorRunConfiguration config = new AdaptorRunConfiguration();
    config.setRestartAfterFailLimit(2);
    config.setRestartAfterFailCronExpression("0,10,20,30,40,50 * * * * ?");
    adaptor.setRunConfiguration(config);
    adaptor.run();
    
    assertTrue(adaptor.getExitCode() != 0);
    assertTrue(reader.connectedRanAndDisconnected());
    assertTrue(writer.connectedRanAndDisconnected());
    assertTrue(reader.getRunCount() == 2);
    
  }

  public void testStopAndRestart() {
    MyTestReadConnector reader = new MyTestReadConnector();
    MyTestProcessor processor = new MyTestProcessor();
    MyTestWriteConnector writer = new MyTestWriteConnector();
    
    Adaptor adaptor = createTestAdaptor(new Object[] { reader, processor, writer});
    AdaptorRunConfiguration config = new AdaptorRunConfiguration();
    config.setStopCronExpression("15,45 * * * * ?");
    config.setRestartCronExpression("0,30 * * * * ?");
    adaptor.setRunConfiguration(config);
    reader.setRestartLimit(config, 3);
    adaptor.run();
    
    assertTrue(adaptor.getExitCode() == 0);
    assertTrue(reader.connectedRanAndDisconnected());
    assertTrue(processor.connectedRanAndDisconnected());
    assertTrue(writer.connectedRanAndDisconnected());
    assertTrue(reader.getRunCount() == 3);
    
  }
  
  class TestComponent extends Component {
    int runCount = 0;
    boolean connected = false;
    boolean disconnected = false;
    boolean hasRun = false;
    TestComponent(String id) {
      super(id);
    }
    boolean connectedRanAndDisconnected() {
      return connected && hasRun && disconnected;
    }
    int getRunCount() {
      return runCount;
    }
  }
  
  class MyTestProcessor extends TestComponent implements IDataProcessor {

    public MyTestProcessor() {
      super("processor");
    }
    
    public Object[] process(Object data) {
      hasRun = true;
      return new Object[] {data};
    }

    public void reset(Object context) {
    }

    public void validate(List exceptions) {
      connected = disconnected = true;
      runCount++;
    }
    
  }
  
  class MyTestWriteConnector extends TestComponent implements IWriteConnector {

    MyTestWriteConnector() {
      super("write");
    }

    public void connect() {
      connected = true;
    }

    public Object deliver(Object[] data) {
      if (data[0] != null) {
        hasRun = true;
      }
      return null;
    }

    public void disconnect() {
      disconnected = true;
      runCount++;
    }

    public void validate(List exceptions) {
      // TODO Auto-generated method stub
      
    }
    
  }
  
  class MyTestReadConnector extends TestComponent implements IReadConnector {

    AdaptorRunConfiguration runConfig;
    int restartLimit;
    
    MyTestReadConnector() {
      super("read");
    }

    void setRestartLimit(AdaptorRunConfiguration runConfig, int limit) {
      this.runConfig = runConfig;
      restartLimit = limit;
    }
    
    public void connect() {
      connected = true;
    }

    public void disconnect() {
      disconnected = true;
      runCount++;
      if (restartLimit > 0 && runCount >= restartLimit) {
        runConfig.setExitFlag();
      }
    }

    public Object getReaderContext() {
      return null;
    }

    public boolean isDry() {
      return false;
    }

    public Object[] next(long timeoutMs) {
      hasRun = true;
      try {
        Thread.sleep(timeoutMs);
      } catch (InterruptedException e) {
      }
      return new Object[] {DATA};
    }

    public void validate(List exceptions) {
      // TODO Auto-generated method stub
      
    }
  }
  
}
