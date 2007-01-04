package org.oa3.core.adaptor;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.oa3.core.connector.TestReadConnector;
import org.oa3.core.connector.TestWriteConnector;
import org.oa3.core.processor.TestProcessor;

public class PipelineTestCase extends TestCase {

  public void test() {
    // create inpoint
    AdaptorInpoint inpoint = new AdaptorInpoint();
    TestReadConnector inconnector = new TestReadConnector();
    inconnector.setDataString("foobar");
    inpoint.setConnector(inconnector);

    // create outpoint
    AdaptorOutpoint outpoint = new AdaptorOutpoint();
    TestWriteConnector outconnector = new TestWriteConnector();
    outconnector.setExpectedOutput(inconnector.getDataString());
    outpoint.setConnector(outconnector);

    // create pipeline
    Object[] pipeline = new Object[] {inpoint, outpoint};

    // create router
    Adaptor adaptor = new Adaptor();
    adaptor.setPipeline(pipeline);
    adaptor.setRunInpointsInCallingThread(true);

    // run adaptor
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);
  }
  
  public void test2() {
    
    // create inpoint
    TestReadConnector inpoint = new TestReadConnector("InPoint");
    inpoint.setDataString("foobar");
    inpoint.setBatchSize(3);
    inpoint.setMaxSend(10);
    
    // create outpoint
    TestWriteConnector outpoint = new TestWriteConnector("OutPoint");
    List output = new ArrayList();
    for (int i = 0; i < 10; i++) {
      output.add(inpoint.getDataString().replaceAll("%n", String.valueOf(i+1)));
    }
    outpoint.setExpectedOutput(output);
    
    // create pipeline
    Object[] pipeline = new Object[] {inpoint, outpoint};
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setPipeline(pipeline);
    adaptor.setRunInpointsInCallingThread(true);
    
    // run adaptor
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);
  }
  
  /**
   * IReadConnector -> IDataProcessor -> IWriteConnector
   * uses "autoboxing" so no need to construct AdaptorInPoint, Node & AdaptorOutPoint
   * ReadConnector sends one message and then "closes", causing adaptor to stop and 
   * WriteConnector to check it has received it's expected output.
   */
  public void test3() {
    
    // create inpoint
    TestReadConnector inpoint = new TestReadConnector("InPoint");
    inpoint.setDataString("foobar");
    
    // create processor
    TestProcessor processor = new TestProcessor("Processor1");
    
    // create outpoint
    TestWriteConnector outpoint = new TestWriteConnector("OutPoint");
    outpoint.setExpectedOutput(processor.getId() + "(" + inpoint.getDataString() + ")");
    
    // create pipeline
    Object[] pipeline = new Object[] {inpoint, processor, outpoint};
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setPipeline(pipeline);
    adaptor.setRunInpointsInCallingThread(true);
    
    // run adaptor
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);
  }
  
  /**
   * Exception example
   */
  public void test4() {
    
    // create inpoint
    TestReadConnector inpoint = new TestReadConnector("InPoint");
    inpoint.setDataString("foobar");
    inpoint.setBatchSize(3);
    inpoint.setMaxSend(10);
    
    // create processor
    TestProcessor processor = new TestProcessor("Processor1");
    
    // create processor which deliberately throws a RuntimeException
    TestProcessor exceptor = new TestProcessor("Exceptor");
    exceptor.setExceptionFrequency(3);
    
    // create outpoint for processed data
    TestWriteConnector outpoint = new TestWriteConnector("OutPoint");
    outpoint.setExpectedOutput(createStringList(exceptor.getId() + "(" + processor.getId() + "(" + inpoint.getDataString() + "))", 7));
    
    // create outpoint for MessageExceptions
    TestWriteConnector errOutpoint = new TestWriteConnector("Error1");
    errOutpoint.setExpectedOutput(createStringList("java.lang.RuntimeException:null:" + processor.getId() + "(" + inpoint.getDataString() + ")", 3));

    // create pipeline
    Object[] pipeline = new Object[] {inpoint, processor, exceptor, outpoint};
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setExceptionProcessor(errOutpoint);
    adaptor.setPipeline(pipeline);
    adaptor.setRunInpointsInCallingThread(true);
    
    // run adaptor
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);
  }

  public void testConstraints() {
    TestReadConnector inpoint = new TestReadConnector("InPoint");
    inpoint.setDataString("foobar");
    TestProcessor processor = new TestProcessor("Processor1");
    TestWriteConnector outpoint = new TestWriteConnector("OutPoint");
    outpoint.setExpectedOutput(processor.getId() + "(" + inpoint.getDataString() + ")");
    
    try {
      Object[] pipeline = new Object[] {processor, inpoint, outpoint};
      Adaptor adaptor =  new Adaptor();
      adaptor.setPipeline(pipeline);
      fail("expected an exception");
    } catch (RuntimeException ex) {
    }
    
    try {
      Object[] pipeline = new Object[] {inpoint, processor};
      Adaptor adaptor =  new Adaptor();
      adaptor.setPipeline(pipeline);
      fail("expected an exception");
    } catch (RuntimeException ex) {
    }
    
    try {
      Object[] pipeline = new Object[] {outpoint, processor, inpoint};
      Adaptor adaptor =  new Adaptor();
      adaptor.setPipeline(pipeline);
      fail("expected an exception");
    } catch (RuntimeException ex) {
    }
    
    try {
      Object[] pipeline = new Object[] {inpoint};
      Adaptor adaptor =  new Adaptor();
      adaptor.setPipeline(pipeline);
      fail("expected an exception");
    } catch (RuntimeException ex) {
    }
  }
  
  public static List createStringList(String s, int n) {
    ArrayList list = new ArrayList();
    for (int i = 0; i < n; i++) {
      list.add(s);
    }
    return list;
  }

}
