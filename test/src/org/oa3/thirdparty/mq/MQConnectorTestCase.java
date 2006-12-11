/*
 * [[ Copyright (C) 2001 - 2006 The Software Conservancy as Trustee. All rights
 * reserved. Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions: The
 * above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS
 * IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. Nothing in
 * this notice shall be deemed to grant any rights to trademarks, copyrights,
 * patents, trade secrets or any other intellectual property of the licensor or
 * any contributor except as expressly stated herein. No patent license is
 * granted separate from the Software, for code that you delete from the
 * Software, or for combinations of the Software with other software or
 * hardware. ]]
 */
package org.oa3.thirdparty.mq;
/*
 * File: $Header$ 
 * Rev: $Revision$
 */
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import junit.framework.TestCase;

/**
 * Very simple read/write test for the MQ Connectors. Simply sends and receives one text
 * message.
 * <br><br>
 * As this test is completely dependant on having access to both mq.jar and a working 
 * MQ environment it is normally not run. If it is to be run then the appropriate 
 * properties must be supplied in a file called mq.properties and it must be available
 * as a resource at runtime.
 * 
 * @author scullyk
 *
 */
public class MQConnectorTestCase extends TestCase {

  private static final String MQ_PROPERTIES = "mq.properties";

  private static final String NEVER_SENT = "I didn't send this string";

  private static final String TEST_RECORD = "TEST";

  private String channelName = "NOT SET";

  private String hostName = "NOT SET";

  private String managerName = "NOT SET";

  private int port = 9999;

  private String queueName = "NOT SET";

  private String userName = "NOT SET";

  private String password = "NOT SET";

  private Properties props;



  protected void setUp() throws Exception {
    super.setUp();
    
    // Look for a file mq.prperties that can be loaded as a resource.
    // If its not there we can't run the test.
    
    InputStream in = null;
    try {
      in = getClass().getResourceAsStream(MQ_PROPERTIES);      
      props = new Properties();
      if (in != null) {
        props.load(in);
      }
      else {
        fail("Unable to find mq.properties.");
      }
    }
    catch (Exception e) {
      fail("Error loading mq.properties.");
    }
    finally {
      try {
        if (in != null) {
          in.close();
        }
      }
      catch (IOException e) {
        fail("Unable to close input stream on mq.properties");
      }
    }    

    channelName = props.getProperty("channelName");
    hostName = props.getProperty("hostName");
    managerName = props.getProperty("managerName");
    port = Integer.parseInt((String) props.get("port"));
    queueName = props.getProperty("queueName");
    userName = props.getProperty("userName");
    password = props.getProperty("password");
  }

  public void testMqWriteRead() {
    try {
      mqWrite(TEST_RECORD);
    }
    catch (Exception e) {
      fail("Exception raised during deliver() : " + e);
    }
    Object[] readData = new Object[] { NEVER_SENT };
    try {
      readData = mqRead();
    }
    catch (Exception e) {
      fail("Unexpected exception during read : " + e);
    }
    assertTrue("Expected one record only.", readData.length == 1);
    assertEquals("Read data doesn't match input data", readData[0], TEST_RECORD);
  }

  protected void mqWrite(String testRecord) {
    MqConnection mqConnection = new MqConnection();
    mqConnection.setChannelName(channelName);
    mqConnection.setHostName(hostName);
    mqConnection.setManagerName(managerName);
    mqConnection.setPort(port);
    mqConnection.setQueueName(queueName);
    mqConnection.setUserName(userName);
    mqConnection.setPassword(password);

    MqWriteConnector writeConnector = new MqWriteConnector();
    writeConnector.setId("TestWriter");
    writeConnector.setConnection(mqConnection);

    writeConnector.connect();
    writeConnector.deliver(new Object[] { testRecord });
  }

  protected Object[] mqRead() {
    Object[] readData = new Object[] {};
    try {
      MqConnection mqConnection = new MqConnection();
      mqConnection.setChannelName(channelName);
      mqConnection.setHostName(hostName);
      mqConnection.setManagerName(managerName);
      mqConnection.setPort(port);
      mqConnection.setQueueName(queueName);
      mqConnection.setUserName(userName);
      mqConnection.setPassword(password);

      MqReadConnector readConnector = new MqReadConnector();
      readConnector.setConnection(mqConnection);
      readConnector.setId("TestReader");

      readConnector.connect();

      readData = readConnector.next(1000);
    }
    catch (Exception e) {
      fail("Unexpected exception during read : " + e);
    }

    assertEquals("Read data doesn't match input data", readData[0], TEST_RECORD);

    return readData;
  }

}
