package org.openadaptor.core.adaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.openadaptor.core.connector.TestReadConnector;
import org.openadaptor.core.connector.TestWriteConnector;
import org.openadaptor.core.node.ReadNode;
import org.openadaptor.core.node.WriteNode;
import org.openadaptor.core.processor.TestProcessor;
import org.openadaptor.core.router.Router;

public class AdaptorRouterTestCase extends TestCase {
  private static final int DEFAULT_BATCH_SIZE=TestReadConnector.DEFAULT_BATCH_SIZE;
  private static final int DEFAULT_MAX_SEND=TestReadConnector.DEFAULT_MAX_SEND;
  private static final String DEFAULT_DATA_STRING=TestReadConnector.DEFAULT_DATA_STRING;
  
  public void testWithoutAutoboxing() {
    ReadNode readNode = new ReadNode();
    TestReadConnector inconnector=createTestReadConnector("ReadConnector","foobar",DEFAULT_BATCH_SIZE,DEFAULT_MAX_SEND);
    readNode.setConnector(inconnector);

    // create writeNode
    WriteNode writeNode = new WriteNode();
    TestWriteConnector outconnector = new TestWriteConnector();
    outconnector.setExpectedOutput(inconnector.getDataString());
    writeNode.setConnector(outconnector);

    // create router
    Adaptor adaptor = createTestAdaptor(new Object[] {readNode, writeNode});

    checkCleanExecution(adaptor,true);
  }
  
  public void test2() {
    TestReadConnector readNode=createTestReadConnector("ReadConnector",DEFAULT_DATA_STRING,3,10);
    
    // create writeNode
    TestWriteConnector writeNode = new TestWriteConnector("WriteNode");
    List output = new ArrayList();
    for (int i = 0; i < 10; i++) {
      output.add(readNode.getDataString().replaceAll("%n", String.valueOf(i+1)));
    }
    writeNode.setExpectedOutput(output);
    
    // create adaptor
    Adaptor adaptor = createTestAdaptor(new Object[] {readNode, writeNode});
 
    checkCleanExecution(adaptor,true);
  }
  
  /**
   * IReadConnector -> IDataProcessor -> IWriteConnector
   * uses "autoboxing" so no need to construct ReadNode, Node & WriteNode
   * ReadConnector sends one message and then "closes", causing adaptor to stop and 
   * WriteConnector to check it has received it's expected output.
   */
  public void test3() {
    TestReadConnector readNode=createTestReadConnector("ReadConnector","foobar",DEFAULT_BATCH_SIZE,DEFAULT_MAX_SEND);

    // create processor
    TestProcessor processor = new TestProcessor("Processor1");
    
    // create writeNode
    TestWriteConnector writeNode = new TestWriteConnector("WriteNode");
    writeNode.setExpectedOutput(processor.getId() + "(" + readNode.getDataString() + ")");
    
    // create adaptor
    Adaptor adaptor = createTestAdaptor(new Object[] {readNode, processor,writeNode});

    checkCleanExecution(adaptor,true);
  }
  
  /**
   * Exception example
   */
  public void test4() {
    TestReadConnector readNode=createTestReadConnector("ReadConnector","foobar",3,10);
    
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
    
    checkCleanExecution(adaptor,true);
  }

   //Utility methods for test
  /**
   * Ensure that adaptor runs and exits cleanly
   * @param adaptor - a configured Adaptor instance.
   * @param useCallingThread - adaptor should run in callingThread
   */
  private static void checkCleanExecution(Adaptor adaptor,boolean useCallingThread) {
    adaptor.setRunInCallingThread(useCallingThread);
    adaptor.run();
    assertTrue(adaptor.getExitCode()==0);
  }
  
  private static TestReadConnector createTestReadConnector(String name,String dataString,int batchSize,int maxSend) {
    TestReadConnector testReadConnector = new TestReadConnector(name);
    testReadConnector.setDataString(dataString);
    testReadConnector.setBatchSize(batchSize);
    testReadConnector.setMaxSend(maxSend);
    return testReadConnector;
  }
   /**
    * Create an adaptor with the supplied pipeline, and exception Handler.
    * @param pipeline list of components to assemble
    * @param exceptionHandler Handler for exeptions
    * @return An Adaptor instance.
    */
   private static Adaptor createTestAdaptor(Object[] pipeline,Object exceptionHandler) {
     Router router=new Router();
     router.setProcessors(Arrays.asList(pipeline));
     if (exceptionHandler!=null) {
       router.setExceptionProcessor(exceptionHandler);
     }
     Adaptor adaptor=new Adaptor();
     adaptor.setMessageProcessor(router);
     return adaptor;
   }
   
   /**
    * Create an adaptor with the supplied pipeline (and no exception handler)
    * @param pipeline List of components to assemble.
    * @return An Adaptor instance.
    */
   private static Adaptor createTestAdaptor(Object[] pipeline) {
     return createTestAdaptor(pipeline,null);
   }

   /**
    * Utility method to create a list of n instances of String s.
    * @param s String to use as list entry
    * @param n Number of instances of s to create
    * @return List containing n instances of String s
    */
   private static List createStringList(String s, int n) {
     ArrayList list = new ArrayList();
     for (int i = 0; i < n; i++) {
       list.add(s);
     }
     return list;
   }
}
