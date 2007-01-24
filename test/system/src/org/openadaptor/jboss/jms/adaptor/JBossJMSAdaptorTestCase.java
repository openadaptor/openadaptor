package org.openadaptor.jboss.jms.adaptor;

import junit.framework.TestCase;
import org.openadaptor.auxil.connector.iostream.reader.FileReader;
import org.openadaptor.auxil.connector.iostream.reader.StreamReadConnector;
import org.openadaptor.auxil.connector.iostream.reader.StringRecordReader;
import org.openadaptor.auxil.connector.iostream.reader.FileReaderTestCase;
import org.openadaptor.auxil.connector.iostream.writer.FileWriter;
import org.openadaptor.auxil.connector.iostream.writer.StreamWriteConnector;
import org.openadaptor.auxil.connector.jms.JMSConnection;
import org.openadaptor.auxil.connector.jms.JMSReadConnector;
import org.openadaptor.auxil.connector.jms.JMSWriteConnector;
import org.openadaptor.core.IMessageProcessor;
import org.openadaptor.core.Message;
import org.openadaptor.core.Response;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.connector.TestWriteConnector;
import org.openadaptor.core.jmx.MBeanServer;
import org.openadaptor.core.lifecycle.LifecycleComponent;
import org.openadaptor.core.router.Router;
import org.openadaptor.core.router.RoutingMap;
import org.openadaptor.jboss.jms.connector.JBossJMSTestCase;

import javax.management.ObjectName;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JBossJMSAdaptorTestCase extends TestCase {

  String stopRecord = "<stop test=\"testFile2JMS\"/>";

  public void testFile2JMS() {

    // populate file
    List inputs = new ArrayList();
    String template = "<trade><trade_id>%ID%</trade_id><side>%SIDE%</side><stock ref=\"RIC\">BT.L</stock><size>1000000</size><price ccy=\"GBP\">%PRICE%</price></trade>";
    for (int i = 0; i < 2; i++) {
      String s = template;
      s = s.replaceAll("%ID%", String.valueOf(12345 + i));
      s = s.replaceAll("%SIDE%", i % 2 == 0 ? "BUY" : "SELL");
      s = s.replaceAll("%PRICE%", String.valueOf(Math.PI * (i +1)));
      inputs.add(s);
    }
    inputs.add(stopRecord);
    String filename = FileReaderTestCase.createTempFile(inputs, null, "\n");

    
    StreamReadConnector readNode = new StreamReadConnector();
    readNode.setId("FileIn");

    FileReader fileReader = new FileReader();
    fileReader.setPath(filename);
    readNode.setStreamReader(fileReader);
    readNode.setRecordReader(new StringRecordReader());
    
    JMSConnection connection = JBossJMSTestCase.getConnection();
    connection.setDestinationName("queue/testQueue");
    connection.setDurable(true);
    connection.setClientID("push");
    connection.setTransacted(true);

    JMSWriteConnector writeNode = new JMSWriteConnector();
    writeNode.setId("JmsOut");
    writeNode.setJmsConnection(connection);

    // create router
    RoutingMap routingMap = new RoutingMap();
    Map processMap = new HashMap();
    processMap.put(readNode, writeNode);
    routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
    adaptor.setRunInCallingThread(true);
    
    // run adaptor
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);

  }

  public void testJMS2File() {
    JMSConnection connection = JBossJMSTestCase.getConnection();
    connection.setDestinationName("queue/testQueue");
    connection.setClientID("pop");
    connection.setTransacted(true);

    JMSReadConnector readNode = new JMSReadConnector();
    readNode.setJmsConnection(connection);
    readNode.setId("JmsIn");

    StreamWriteConnector writeNode = new StreamWriteConnector();
    writeNode.setId("FileOut");
    FileWriter fileWriter = new FileWriter();
    writeNode.setStreamWriter(fileWriter);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();

    // create router
    RoutingMap routingMap = new RoutingMap();
    Map processMap = new HashMap();
    List recipients = new ArrayList();
    recipients.add(writeNode);
    recipients.add(new Stopper(adaptor, stopRecord, 2));
    processMap.put(readNode, recipients);
    routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    adaptor.setMessageProcessor(router);
    adaptor.setRunInCallingThread(true);
    
    // run adaptor
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);
  }
  
  public void testJmxStop() {
    JMSConnection connection = JBossJMSTestCase.getConnection();
    connection.setDestinationName("queue/testQueue");
    connection.setClientID("pop");
    connection.setTransacted(true);

    JMSReadConnector readNode = new JMSReadConnector();
    readNode.setJmsConnection(connection);
    readNode.setId("JmsIn");
    
    // create writeNode
    TestWriteConnector writeNode = new TestWriteConnector("writeNode");
    
    // create router
    RoutingMap routingMap = new RoutingMap();
    Map processMap = new HashMap();
    processMap.put(readNode, writeNode);
    routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
    adaptor.setRunInCallingThread(true);

    // create mbean server and register adaptor
    try {
      MBeanServer mbeanServer = new MBeanServer(8082);
      mbeanServer.registerMBean(adaptor, new ObjectName("beans:id=adaptor"));
    } catch (Exception e) {
      fail(e.getMessage());
    }
    
    // create ansd start thread to call stop from jmx
    Thread jmxStopThread = new JMXStopThread();
    jmxStopThread.start();
    
    // run adaptor
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);
  }
  
  public class Stopper extends LifecycleComponent implements IMessageProcessor {

    private int expectedRecordCount;
    private int count;
    private String stopRecord; 
    private Adaptor adaptor;
    
    public Stopper(Adaptor adaptor, String stopRecord, int expectedRecordCount) {
      super("Stopper");
      this.stopRecord = stopRecord;
      this.adaptor = adaptor;
      this.expectedRecordCount = expectedRecordCount;
    }
    
    public Response process(Message msg) {
      Object[] data = msg.getData();
      for (int i = 0; i < data.length; i++) {
        if (data[i].equals(stopRecord)) {
          adaptor.stopNoWait();
        } else {
          count++;
        }
      }
      return Response.EMPTY;
    }
    
    public void stop() {
      super.stop();
      assertTrue("expectected record count = " + expectedRecordCount 
          + ", actual = " + count, expectedRecordCount == count);
    }
  }
  
  /**
   * hack to sleep and then open a  url to the http interface to the jmx interface
   */
  public static class JMXStopThread extends Thread {
    public void run() {
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
      }
      try {
        URL url = new URL("http://localhost:8082/InvokeAction//beans%3Aid%3Dadaptor/action=exit?action=exit");
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        while (in.readLine() != null);
        in.close();
      } catch (MalformedURLException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
