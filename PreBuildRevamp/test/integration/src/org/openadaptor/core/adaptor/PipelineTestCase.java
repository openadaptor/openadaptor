package org.openadaptor.core.adaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.connector.TestReadConnector;
import org.openadaptor.core.connector.TestWriteConnector;
import org.openadaptor.core.node.ReadNode;
import org.openadaptor.core.node.WriteNode;
import org.openadaptor.core.processor.TestProcessor;
import org.openadaptor.core.router.Pipeline;

public class PipelineTestCase extends TestCase {

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

  public void test() {
    // create readNode
    ReadNode readNode = new ReadNode();
    TestReadConnector inconnector = new TestReadConnector();
    inconnector.setDataString("foobar");
    readNode.setConnector(inconnector);

    // create writeNode
    WriteNode writeNode = new WriteNode();
    TestWriteConnector outconnector = new TestWriteConnector();
    outconnector.setExpectedOutput(inconnector.getDataString());
    writeNode.setConnector(outconnector);

    // create router
    Adaptor adaptor = createTestAdaptor(new Object[] {readNode, writeNode});
    adaptor.setRunInCallingThread(true);

    // run adaptor
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);
  }
  
  public void test2() {
    
    // create readNode
    TestReadConnector readNode = new TestReadConnector("ReadConnector");
    readNode.setDataString("foobar");
    readNode.setBatchSize(3);
    readNode.setMaxSend(10);
    
    // create writeNode
    TestWriteConnector writeNode = new TestWriteConnector("WriteNode");
    List output = new ArrayList();
    for (int i = 0; i < 10; i++) {
      output.add(readNode.getDataString().replaceAll("%n", String.valueOf(i+1)));
    }
    writeNode.setExpectedOutput(output);
    
    // create adaptor
    Adaptor adaptor = createTestAdaptor(new Object[] {readNode, writeNode});
    adaptor.setRunInCallingThread(true);
    
    // run adaptor
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);
  }
  
  /**
   * IReadConnector -> IDataProcessor -> IWriteConnector
   * uses "autoboxing" so no need to construct ReadNode, Node & WriteNode
   * ReadConnector sends one message and then "closes", causing adaptor to stop and 
   * WriteConnector to check it has received it's expected output.
   */
  public void test3() {
    
    // create readNode
    TestReadConnector readNode = new TestReadConnector("ReadConnector");
    readNode.setDataString("foobar");
    
    // create processor
    TestProcessor processor = new TestProcessor("Processor1");
    
    // create writeNode
    TestWriteConnector writeNode = new TestWriteConnector("WriteNode");
    writeNode.setExpectedOutput(processor.getId() + "(" + readNode.getDataString() + ")");
    
    // create adaptor
    Adaptor adaptor = createTestAdaptor(new Object[] {readNode, processor,writeNode});
    adaptor.setRunInCallingThread(true);
    
    // run adaptor
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);
  }
  
  /**
   * Exception example
   */
  public void test4() {
    
    // create readNode
    TestReadConnector readNode = new TestReadConnector("ReadConnector");
    readNode.setDataString("foobar");
    readNode.setBatchSize(3);
    readNode.setMaxSend(10);
    
    // create processor
    TestProcessor processor = new TestProcessor("Processor1");
    
    // create processor which deliberately throws a RuntimeException
    TestProcessor exceptor = new TestProcessor("Exceptor");
    exceptor.setExceptionFrequency(3);
    
    // create writeNode for processed data
    TestWriteConnector writeNode = new TestWriteConnector("WriteNode");
    writeNode.setExpectedOutput(createStringList(exceptor.getId() + "(" + processor.getId() + "(" + readNode.getDataString() + "))", 7));
    
    // create writeNode for MessageExceptions
    TestWriteConnector errWriteConnector = new TestWriteConnector("Error1");
    errWriteConnector.setExpectedOutput(createStringList("java.lang.RuntimeException:null:" + processor.getId() + "(" + readNode.getDataString() + ")", 3));

    // create adaptor
    Adaptor adaptor = createTestAdaptor(new Object[] {readNode, processor, exceptor, writeNode},errWriteConnector);
    adaptor.setRunInCallingThread(true);
    
    // run adaptor
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);
  }

  /*
  private void expectRuntimeException(Object[] pipeline) {
    try {
      createTestAdaptor(pipeline);
      fail("RuntimeException expected, but not generated");
    }
    catch (RuntimeException re) {}
  }
 
  public void testConstraints() {
    TestReadConnector readNode = new TestReadConnector("ReadConnector");
    readNode.setDataString("foobar");
    TestProcessor processor = new TestProcessor("Processor1");
    TestWriteConnector writeNode = new TestWriteConnector("WriteNode");
    writeNode.setExpectedOutput(processor.getId() + "(" + readNode.getDataString() + ")");
    
    expectRuntimeException(new Object[] {processor, readNode, writeNode});

    expectRuntimeException(new Object[] {readNode, processor});

      expectRuntimeException(new Object[] {writeNode, processor, readNode});
      expectRuntimeException(new Object[] {readNode});
    }
   */ 
  public static List createStringList(String s, int n) {
    ArrayList list = new ArrayList();
    for (int i = 0; i < n; i++) {
      list.add(s);
    }
    return list;
  }

}
