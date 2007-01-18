package org.openadaptor.core.adaptor;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.connector.TestReadConnector;
import org.openadaptor.core.connector.TestWriteConnector;
import org.openadaptor.core.node.ReadNode;
import org.openadaptor.core.node.WriteNode;
import org.openadaptor.core.processor.TestProcessor;

public class PipelineTestCase extends TestCase {

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

    // create pipeline
    Object[] pipeline = new Object[] {readNode, writeNode};

    // create router
    Adaptor adaptor = new Adaptor();
    adaptor.setPipeline(pipeline);
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
    
    // create pipeline
    Object[] pipeline = new Object[] {readNode, writeNode};
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setPipeline(pipeline);
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
    
    // create pipeline
    Object[] pipeline = new Object[] {readNode, processor, writeNode};
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setPipeline(pipeline);
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

    // create pipeline
    Object[] pipeline = new Object[] {readNode, processor, exceptor, writeNode};
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setExceptionWriteConnector(errWriteConnector);
    adaptor.setPipeline(pipeline);
    adaptor.setRunInCallingThread(true);
    
    // run adaptor
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);
  }

  public void testConstraints() {
    TestReadConnector readNode = new TestReadConnector("ReadConnector");
    readNode.setDataString("foobar");
    TestProcessor processor = new TestProcessor("Processor1");
    TestWriteConnector writeNode = new TestWriteConnector("WriteNode");
    writeNode.setExpectedOutput(processor.getId() + "(" + readNode.getDataString() + ")");
    
    try {
      Object[] pipeline = new Object[] {processor, readNode, writeNode};
      Adaptor adaptor =  new Adaptor();
      adaptor.setPipeline(pipeline);
      fail("expected an exception");
    } catch (RuntimeException ex) {
    }
    
    try {
      Object[] pipeline = new Object[] {readNode, processor};
      Adaptor adaptor =  new Adaptor();
      adaptor.setPipeline(pipeline);
      fail("expected an exception");
    } catch (RuntimeException ex) {
    }
    
    try {
      Object[] pipeline = new Object[] {writeNode, processor, readNode};
      Adaptor adaptor =  new Adaptor();
      adaptor.setPipeline(pipeline);
      fail("expected an exception");
    } catch (RuntimeException ex) {
    }
    
    try {
      Object[] pipeline = new Object[] {readNode};
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
