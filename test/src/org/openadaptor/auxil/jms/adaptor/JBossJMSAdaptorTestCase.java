package org.oa3.auxil.jms.adaptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.ObjectName;

import junit.framework.TestCase;

import org.oa3.auxil.connector.iostream.FileReaderTestCase;
import org.oa3.auxil.connector.iostream.reader.FileReader;
import org.oa3.auxil.connector.iostream.reader.StreamReadConnector;
import org.oa3.auxil.connector.iostream.reader.StringRecordReader;
import org.oa3.auxil.connector.iostream.writer.FileWriter;
import org.oa3.auxil.connector.iostream.writer.StreamWriteConnector;
import org.oa3.auxil.connector.jms.JMSConnection;
import org.oa3.auxil.connector.jms.JMSReadConnector;
import org.oa3.auxil.connector.jms.JMSWriteConnector;
import org.oa3.auxil.jms.connector.JBossJMSTestCase;
import org.oa3.core.IMessageProcessor;
import org.oa3.core.Message;
import org.oa3.core.Response;
import org.oa3.core.adaptor.Adaptor;
import org.oa3.core.connector.TestWriteConnector;
import org.oa3.core.jmx.MBeanServer;
import org.oa3.core.lifecycle.LifecycleComponent;
import org.oa3.core.router.Router;
import org.oa3.core.router.RoutingMap;

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

    
    StreamReadConnector inpoint = new StreamReadConnector();
    inpoint.setId("FileIn");

    FileReader fileReader = new FileReader();
    fileReader.setPath(filename);
    inpoint.setStreamReader(fileReader);
    inpoint.setRecordReader(new StringRecordReader());
    
    JMSConnection connection = JBossJMSTestCase.getConnection();
    connection.setDestinationName("queue/testQueue");
    connection.setQueue(true);
    connection.setDurable(true);
    connection.setClientID("push");
    connection.setTransacted(true);

    JMSWriteConnector outpoint = new JMSWriteConnector();
    outpoint.setId("JmsOut");
    outpoint.setJmsConnection(connection);

    // create router
    RoutingMap routingMap = new RoutingMap();
    Map processMap = new HashMap();
    processMap.put(inpoint, outpoint);
    routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
    adaptor.setRunInpointsInCallingThread(true);
    
    // run adaptor
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);

  }

  public void testJMS2File() {
    JMSConnection connection = JBossJMSTestCase.getConnection();
    connection.setDestinationName("queue/testQueue");
    connection.setQueue(true);
    connection.setDurable(true);
    connection.setClientID("pop");
    connection.setTransacted(true);

    JMSReadConnector inpoint = new JMSReadConnector();
    inpoint.setJmsConnection(connection);
    inpoint.setId("JmsIn");

    StreamWriteConnector outpoint = new StreamWriteConnector();
    outpoint.setId("FileOut");
    FileWriter fileWriter = new FileWriter();
    outpoint.setStreamWriter(fileWriter);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();

    // create router
    RoutingMap routingMap = new RoutingMap();
    Map processMap = new HashMap();
    List recipients = new ArrayList();
    recipients.add(outpoint);
    recipients.add(new Stopper(adaptor, stopRecord, 2));
    processMap.put(inpoint, recipients);
    routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    adaptor.setMessageProcessor(router);
    adaptor.setRunInpointsInCallingThread(true);
    
    // run adaptor
    adaptor.run();
    assertTrue(adaptor.getExitCode() == 0);
  }
  
  public void testJmxStop() {
    JMSConnection connection = JBossJMSTestCase.getConnection();
    connection.setDestinationName("queue/testQueue");
    connection.setQueue(true);
    connection.setDurable(true);
    connection.setClientID("pop");
    connection.setTransacted(true);

    JMSReadConnector inpoint = new JMSReadConnector();
    inpoint.setJmsConnection(connection);
    inpoint.setId("JmsIn");
    
    // create outpoint
    TestWriteConnector outpoint = new TestWriteConnector("OutPoint");
    
    // create router
    RoutingMap routingMap = new RoutingMap();
    Map processMap = new HashMap();
    processMap.put(inpoint, outpoint);
    routingMap.setProcessMap(processMap);
    Router router = new Router(routingMap);
    
    // create adaptor
    Adaptor adaptor =  new Adaptor();
    adaptor.setMessageProcessor(router);
    adaptor.setRunInpointsInCallingThread(true);

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
          adaptor.stop();
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
        URL url = new URL("http://localhost:8082/InvokeAction//beans%3Aid%3Dadaptor/action=stop?action=stop");
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
