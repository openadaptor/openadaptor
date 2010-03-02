/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a
 copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be included
 in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Nothing in this notice shall be deemed to grant any rights to
 trademarks, copyrights, patents, trade secrets or any other intellectual
 property of the licensor or any contributor except as expressly stated
 herein. No patent license is granted separate from the Software, for
 code that you delete from the Software, or for combinations of the
 Software with other software or hardware.
*/
package org.openadaptor.auxil.connector.jms;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.openadaptor.auxil.connector.jndi.JNDIConnection;
import org.openadaptor.core.exception.ConnectionException;

import javax.jms.*;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Jan 19, 2007 by oa3 Core Team
 */

public class JMSWriteConnectorTestCase extends MockObjectTestCase {
  protected static String CONNECTION_FACTORY_LOOKUP_NAME = "TestTopicConnectionFactory";
  protected static String DESTINATION_NAME = "testTopic";

  private JMSWriteConnector testWriteConnector;
  private MockJMSConnection mockJMSConnection;
  private MockJNDIConnection jndiConnection;
  private Mock dirContextMock;
  private Mock sessionMock;
  private Mock messageProducerMock;
  private Mock destinationMock;

  protected void setUp() throws Exception {
    super.setUp();

    dirContextMock = new Mock(DirContext.class);
    sessionMock = new Mock(Session.class);
    messageProducerMock = new Mock(MessageProducer.class);
    destinationMock = new Mock(Topic.class);

    // Mock of openadaptor3's JNDIConnection
    jndiConnection = new MockJNDIConnection();
    jndiConnection.setContext((DirContext)dirContextMock.proxy());

    mockJMSConnection = new MockJMSConnection();
    mockJMSConnection.setJndiConnection(jndiConnection);

    testWriteConnector = new JMSWriteConnector();
    testWriteConnector.setJmsConnection(mockJMSConnection);
    testWriteConnector.setJmsConnection(mockJMSConnection);
    testWriteConnector.setDestinationName(DESTINATION_NAME);
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    testWriteConnector = null;
    mockJMSConnection = null;
  }

   public void testValidate() {
    // Test instance should be set up to validate correctly
    List validateExceptions = new ArrayList();
    testWriteConnector.validate(validateExceptions);
    assertTrue("Didn't validate when it should have.", validateExceptions.size() == 0);
  }

  public void testValidateDestinationNameSet() {
    // Test instance should be set up to validate correctly
    List validateExceptions = new ArrayList();
    testWriteConnector.setDestination(null);
    testWriteConnector.setDestinationName("Test");
    testWriteConnector.setDestinationFromMetadata(null);
    testWriteConnector.validate(validateExceptions);
    assertTrue("Didn't validate when it should have.", validateExceptions.size() == 0);
  }

  public void testValidateDestinationSet() {
    // Test instance should be set up to validate correctly
    Destination mockDestination = (Destination)((new Mock(Destination.class)).proxy());
    List validateExceptions = new ArrayList();
    testWriteConnector.setDestination(mockDestination);
    testWriteConnector.setDestinationName(null);
    testWriteConnector.setDestinationFromMetadata(null);
    testWriteConnector.validate(validateExceptions);
    assertTrue("Didn't validate when it should have.", validateExceptions.size() == 0);
  }
  
  public void testValidateDestinationFromMetadataSet() {
    List validateExceptions = new ArrayList();
    testWriteConnector.setDestination(null);
    testWriteConnector.setDestinationName(null);
    testWriteConnector.setDestinationFromMetadata("MeaninglessName");
    testWriteConnector.validate(validateExceptions);
    assertTrue("Didn't validate when it should have.", validateExceptions.size() == 0);
  }  
  
  public void testValidateMoreThanOneDestSet() {
    List validateExceptions = new ArrayList();
    testWriteConnector.setDestination(null);
    testWriteConnector.setDestinationName("MeaninglessName");
    testWriteConnector.setDestinationFromMetadata("MeaninglessName");
    testWriteConnector.validate(validateExceptions);
    assertFalse("Should not have passed validate.", validateExceptions.size() == 0);
  }    
  
  public void testValidateMoreAllDestSet() {
    // Test instance should be set up to validate correctly
    Destination mockDestination = (Destination)((new Mock(Destination.class)).proxy());
    List validateExceptions = new ArrayList();
    testWriteConnector.setDestination(mockDestination);
    testWriteConnector.setDestinationName("MeaninglessName");
    testWriteConnector.setDestinationFromMetadata("MeaninglessName");
    testWriteConnector.validate(validateExceptions);
    assertFalse("Should not have passed validate.", validateExceptions.size() == 0);
  } 


  public void testFailValidate() {
    mockJMSConnection.setPassValidate(false);
    List validateExceptions = new ArrayList();
    testWriteConnector.validate(validateExceptions);
    assertTrue("Should have failed validate.", validateExceptions.size() > 0);
  }

   public void testFailValidateNoDestinations() {
    // Test instance should be set up to validate correctly
    List validateExceptions = new ArrayList();
    testWriteConnector.setDestination(null);
    testWriteConnector.setDestinationName(null);
    testWriteConnector.setDestinationFromMetadata(null);
    testWriteConnector.validate(validateExceptions);
    assertTrue("Should have failed validate.", validateExceptions.size() > 0);
  }

  public void testFailValidateNoConnection() {
    testWriteConnector.setJmsConnection(null);
    List validateExceptions = new ArrayList();
    testWriteConnector.validate(validateExceptions);
    assertTrue("Should have failed validate.", validateExceptions.size() > 0);
  }


  public void testFailValidateDestinationNameSetNoJNDI() {
    // Test instance should be set up to validate correctly
    List validateExceptions = new ArrayList();
    testWriteConnector.setDestination(null);
    testWriteConnector.setDestinationName("Test");
    mockJMSConnection.setJndiConnection(null);
    testWriteConnector.validate(validateExceptions);
    assertTrue("Should have failed validate.", validateExceptions.size() > 0);
  }

  public void testConnect() {
    setupConnectExpectations();
    try {
      testWriteConnector.connect();
      assertTrue("Should be connected.", testWriteConnector.isConnected());
    }
    catch (Exception e) {
      fail("Unexpected exception." + e);
    }
  }

  public void testConnectUsingDestination() {
    mockJMSConnection.setMockSession((Session)sessionMock.proxy());
    Mock localDestinationMock = new Mock(Destination.class);
    dirContextMock.expects(never()).method("lookup").with(eq(DESTINATION_NAME));
    sessionMock.expects(once()).method("createProducer")
      .with(eq(localDestinationMock.proxy()))
      .will(returnValue(messageProducerMock.proxy()));
    testWriteConnector.setDestination((Destination) localDestinationMock.proxy());
    try {
      testWriteConnector.connect();
      assertTrue("Should be connected.", testWriteConnector.isConnected());
    }
    catch (Exception e) {
      fail("Unexpected exception." + e);
    }
  }

  public void testConnectFailureConnectionException() {
    mockJMSConnection.setThrowConnectionExceptionOnConnect(true);
    try {
      testWriteConnector.connect();
      fail("Expected a ConnectionException to be thrown.");
    }
    catch (ConnectionException ce) { /* This is expected */ }
    catch (Exception e) {
      fail("Unexpected exception: " + e);
    }
  }

  public void testConnectFailureNPE() {
    mockJMSConnection.setThrowNPEOnConnect(true);
    try {
      testWriteConnector.connect();
      fail("Expected a ComponentException to be thrown.");
    }
    catch (NullPointerException npe) { /* This is expected */ }
    catch (Exception e) {
      fail("Unexpected exception: " + e);
    }
  }

  public void testDisconnect() {
    setupConnectExpectations();
    sessionMock.expects(once()).method("close");
    messageProducerMock.expects(once()).method("close");
    try {
      testWriteConnector.connect();
      testWriteConnector.disconnect();
      assertFalse("Should be disconnected. ", testWriteConnector.isConnected());
    } catch (Exception e) {
      fail("Unexpected Exception: " + e);
    }
  }

  public void testDisconnectFailureNPE() {
    setupConnectExpectations();
    mockJMSConnection.setThrowNPEOnDisconnect(true);
    sessionMock.expects(once()).method("close");
    messageProducerMock.expects(once()).method("close");
    try {
      testWriteConnector.connect();
      testWriteConnector.disconnect();
      fail("Expected a NullPointerException to be thrown.");
    } catch (NullPointerException ce) {
    }
    catch (Exception e) {
      fail("Unexpected Exception: " + e);
    }
  }

  public void testDisconnectFailureCE() {
    setupConnectExpectations();
    mockJMSConnection.setThrowConnectionExceptionOnDisconnect(true);
    sessionMock.expects(once()).method("close");
    messageProducerMock.expects(once()).method("close");
    try {
      testWriteConnector.connect();
      testWriteConnector.disconnect();
      fail("Expected a ConnectionException to be thrown.");
    } catch (ConnectionException ce) {
    }
    catch (Exception e) {
      fail("Unexpected Exception: " + e);
    }
  }

  public void testDeliver() {
    setupConnectExpectations();

    testWriteConnector.connect();

    Object testMessage = new ArrayList();
    Mock objectMessageMock = new Mock(ObjectMessage.class);
    String testMessageID = "Test ID";
    sessionMock.expects(once()).method("createObjectMessage").will(returnValue(objectMessageMock.proxy()));
    objectMessageMock.expects(once()).method("setObject").with(eq(testMessage));
    //messageProducerMock.expects(atLeastOnce()).method("getDestination");
    objectMessageMock.expects(atMostOnce()).method("setJMSDestination");
    objectMessageMock.expects(atMostOnce()).method("getJMSDestination");
    messageProducerMock.expects(once()).method("send")
      .with(eq(objectMessageMock.proxy()),
        eq(testWriteConnector.getDeliveryMode()),
        eq(testWriteConnector.getPriority()),
        eq(testWriteConnector.getTimeToLive()));
    objectMessageMock.expects(once()).method("getJMSMessageID").will(returnValue(testMessageID));

    Object[] returnedMessagedID = (Object[])testWriteConnector.deliver(new Object[] { testMessage } );
    assertEquals("Expected returned messageID to match expected one", testMessageID, returnedMessagedID[0]);

  }
  
  public void testDeliverWithMetadata() {
    
    setupConnectExpectations();
    
    Map testMetadata = new HashMap();
    testMetadata.put("StringValue", "TestString");
    testMetadata.put("IntegerValue", new Integer(1));
    
    testWriteConnector.setPropagateMetadata(true);
    testWriteConnector.setMetadata(testMetadata);
    
    testWriteConnector.connect();

    Object testMessage = new ArrayList();
    Mock objectMessageMock = new Mock(ObjectMessage.class);
    String testMessageID = "Test ID";
    sessionMock.expects(once()).method("createObjectMessage").will(returnValue(objectMessageMock.proxy()));
    objectMessageMock.expects(once()).method("setObject").with(eq(testMessage));
    //messageProducerMock.expects(atLeastOnce()).method("getDestination");
    objectMessageMock.expects(atMostOnce()).method("setJMSDestination");
    objectMessageMock.expects(atMostOnce()).method("getJMSDestination");
    
    objectMessageMock.expects(atLeastOnce()).method("setObjectProperty");
    
    messageProducerMock.expects(once()).method("send")
      .with(eq(objectMessageMock.proxy()),
        eq(testWriteConnector.getDeliveryMode()),
        eq(testWriteConnector.getPriority()),
        eq(testWriteConnector.getTimeToLive()));
    objectMessageMock.expects(once()).method("getJMSMessageID").will(returnValue(testMessageID));

    Object[] returnedMessagedID = (Object[])testWriteConnector.deliver(new Object[] { testMessage } );
    assertEquals("Expected returned messageID to match expected one", testMessageID, returnedMessagedID[0]);
  }
  
  public void testDeliverWithMetadataDefinedDestination() {
    
    setupConnectExpectations();
    
    Map testMetadata = new HashMap();
    testMetadata.put("Destination", DESTINATION_NAME);
    testMetadata.put("IntegerValue", new Integer(1));
    
    testWriteConnector.setPropagateMetadata(false);
    testWriteConnector.setMetadata(testMetadata);
    testWriteConnector.setDestinationFromMetadata("Destination");
    testWriteConnector.setDestinationName(null);
    
    testWriteConnector.connect();

    Object testMessage = new ArrayList();
    Mock objectMessageMock = new Mock(ObjectMessage.class);
    String testMessageID = "Test ID";
    sessionMock.expects(once()).method("createObjectMessage").will(returnValue(objectMessageMock.proxy()));
    objectMessageMock.expects(once()).method("setObject").with(eq(testMessage));
    //messageProducerMock.expects(atLeastOnce()).method("getDestination");
    objectMessageMock.expects(atMostOnce()).method("setJMSDestination");
    objectMessageMock.expects(atMostOnce()).method("getJMSDestination");
    
    objectMessageMock.expects(atLeastOnce()).method("setObjectProperty");
    //dirContextMock.expects(once()).method("lookup").with(eq("TestDestination")).will(returnValue(destinationMock.proxy()));
    
    messageProducerMock.expects(once()).method("send")
      .with(eq(objectMessageMock.proxy()),
        eq(testWriteConnector.getDeliveryMode()),
        eq(testWriteConnector.getPriority()),
        eq(testWriteConnector.getTimeToLive()));
    objectMessageMock.expects(once()).method("getJMSMessageID").will(returnValue(testMessageID));

    Object[] returnedMessagedID = (Object[])testWriteConnector.deliver(new Object[] { testMessage } );
    assertEquals("Expected returned messageID to match expected one", testMessageID, returnedMessagedID[0]);
  }  
  
  public void testDeliverDisconnected() {
    try {
      Object testMessage = new Object();
      testWriteConnector.deliver(new Object[] { testMessage } );
      fail("Expected a ConnectionException to be thrown");
    }
    catch (ConnectionException ce) { /* Expected */ }
    catch (Exception e) {fail("Unexpected Exception: " + e); }
  }

  public void testDeliverWithJMSException() {
    setupConnectExpectations();
    testWriteConnector.connect();

    Exception testException = new JMSException("This is a test");
    Object testMessage = new ArrayList();
    Mock objectMessageMock = new Mock(ObjectMessage.class);
    sessionMock.expects(once()).method("createObjectMessage").will(returnValue(objectMessageMock.proxy()));
    objectMessageMock.expects(once()).method("setObject").with(eq(testMessage));
    messageProducerMock.expects(once()).method("send").will(throwException(testException));
    //messageProducerMock.expects(atLeastOnce()).method("getDestination");
    objectMessageMock.expects(atMostOnce()).method("setJMSDestination");
    objectMessageMock.expects(once()).method("getJMSDestination");    
    try {
      testWriteConnector.deliver(new Object[] { testMessage } );
      fail("Expected ConnectionException");
    } catch (ConnectionException e) {
      // expected this to be thrown.
    }
    catch (Exception e) { fail("Unexpected exception: " + e); }
  }

  // Support methods

  protected void setupConnectExpectations() {
    mockJMSConnection.setMockSession((Session)sessionMock.proxy());
    dirContextMock.expects(once()).method("lookup").with(eq(DESTINATION_NAME)).will(returnValue(destinationMock.proxy()));
    sessionMock.expects(once()).method("createProducer")
      .with(eq(destinationMock.proxy()))
      .will(returnValue(messageProducerMock.proxy()));
  }


  // Inner Mocks

  class MockJNDIConnection extends JNDIConnection {

    private DirContext dirContext;

    public DirContext connect() throws NamingException {
      if (dirContext == null) {
        throw new NamingException("No DirContext set");
      }
      return dirContext;
    }

    public void setContext(DirContext dirContext) {
      this.dirContext = dirContext;
    }
  }
}
