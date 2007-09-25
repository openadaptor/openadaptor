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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.openadaptor.auxil.connector.jndi.JNDIConnection;
import org.openadaptor.core.exception.ConnectionException;

import javax.jms.*;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import java.util.ArrayList;
import java.util.List;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Jan 18, 2007 by oa3 Core Team
 */

public class JMSReadConnectorTestCase extends MockObjectTestCase {

  private static final Log log = LogFactory.getLog(JMSReadConnectorTestCase.class);

  protected static String CONNECTION_FACTORY_LOOKUP_NAME = "TestTopicConnectionFactory";
  protected static String DESTINATION_NAME = "testTopic";

  private JMSReadConnector testReadConnector;
  private MockJMSConnection mockJMSConnection;

  //private DirContext dirContext;

  // JMock mocks
  private Mock dirContextMock;
  private Mock sessionMock;
  private Mock destinationMock;
  private Mock messageConsumerMock;

  protected void setUp() throws Exception {
    super.setUp();

    dirContextMock = new Mock(DirContext.class);
    sessionMock = new Mock(Session.class);
    messageConsumerMock = new Mock(MessageConsumer.class);
    destinationMock = new Mock(Topic.class);

    // Mock of openadaptor3's JNDIConnection
    MockJNDIConnection jndiConnection = new MockJNDIConnection();
    jndiConnection.setContext((DirContext)dirContextMock.proxy());

    mockJMSConnection = new MockJMSConnection();
    mockJMSConnection.setJndiConnection(jndiConnection);

    testReadConnector = new JMSReadConnector();
    testReadConnector.setId("Test Read Connector");
    testReadConnector.setJmsConnection(mockJMSConnection);
    testReadConnector.setDestinationName(DESTINATION_NAME);
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    testReadConnector = null;
    mockJMSConnection = null;
    sessionMock = null;
    messageConsumerMock = null;
    destinationMock = null;
  }

  public void testValidate() {
    // Test instance should be set up to validate correctly
    List validateExceptions = new ArrayList();
    testReadConnector.validate(validateExceptions);
    assertTrue("Didn't validate when it should have.", validateExceptions.size() == 0);
  }

  public void testFailValidate() {
    mockJMSConnection.setPassValidate(false);
    List validateExceptions = new ArrayList();
    testReadConnector.validate(validateExceptions);
    assertTrue("Should have failed validate.", validateExceptions.size() > 0);
  }

  public void testConnect() {
    setupConnectExpectations();
    mockJMSConnection.installAsExceptionListener(testReadConnector);
    try {
      testReadConnector.connect();
      assertTrue("Should be connected.", testReadConnector.isConnected());
      assertEquals("Test Connector hould have been installed as an Exception Listener ", testReadConnector, mockJMSConnection.getListener());
    }
    catch (Exception e) {
      fail("Unexpected exception." + e);
    }
  }

  public void testConnectFailureComponentException() {
    mockJMSConnection.setMockSession((Session)sessionMock.proxy());
    mockJMSConnection.setThrowConnectionExceptionOnConnect(true);
    try {
      testReadConnector.connect();
      fail("Expected a ConnectionException to be thrown.");
    }
    catch (ConnectionException ce) { /* This is expected */ }
    catch (Exception e) {
      fail("Unexpected exception: " + e);
    }
  }

  public void testConnectFailureNPE() {
    mockJMSConnection.setMockSession((Session)sessionMock.proxy());
    mockJMSConnection.setThrowNPEOnConnect(true);
    try {
      testReadConnector.connect();
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
    messageConsumerMock.expects(once()).method("close");
    try {
      testReadConnector.connect();
      testReadConnector.disconnect();
      assertFalse("Should be disconnected. ", testReadConnector.isConnected());
    } catch (Exception e) {
      fail("Unexpected Exception: " + e);
    }
  }

  public void testDisconnectFailureNPE() {
    mockJMSConnection.setThrowNPEOnDisconnect(true);

    setupConnectExpectations();

    sessionMock.expects(once()).method("close");
    messageConsumerMock.expects(once()).method("close");
    try {
      testReadConnector.connect();
      testReadConnector.disconnect();
      fail("Expected a NullPointerException to be thrown.");
    } catch (NullPointerException ce) {
    }
    catch (Exception e) {
      fail("Unexpected Exception: " + e);
    }
  }

  public void testDisconnectFailureCE() {
    mockJMSConnection.setThrowConnectionExceptionOnDisconnect(true);

    setupConnectExpectations();

    sessionMock.expects(once()).method("close");
    messageConsumerMock.expects(once()).method("close");
    try {
      testReadConnector.connect();
      testReadConnector.disconnect();
      fail("Expected a ConnectionException to be thrown.");
    } catch (ConnectionException ce) {
    }
    catch (Exception e) {
      fail("Unexpected Exception: " + e);
    }
  }

  public void testNext() {
    Mock mockObjectMessage = new Mock(ObjectMessage.class);
    Mock mockTextMessage = new Mock(TextMessage.class);

    setupConnectExpectations();
    testReadConnector.connect();

    messageConsumerMock.expects(once()).method("receive").will(returnValue(null));
    assertTrue("Expected null message", testReadConnector.next(10) == null);

    messageConsumerMock.expects(once()).method("receive").will(returnValue(mockObjectMessage.proxy()));
    mockObjectMessage.expects(once()).method("getObject").will(returnValue(null));
    mockObjectMessage.expects(once()).method("getJMSMessageID").will(returnValue("ID for Null contents"));
    Object nullNext = testReadConnector.next(10);
    assertTrue("Expected null message contents", nullNext == null);

    Object testNextMessage = new ArrayList();
    messageConsumerMock.expects(once()).method("receive").will(returnValue(mockObjectMessage.proxy()));
    mockObjectMessage.expects(once()).method("getJMSMessageID").will(returnValue("TestMessageID Object Message"));
    mockObjectMessage.expects(once()).method("getObject").will(returnValue(testNextMessage));

    Object[] nextObjectArray = testReadConnector.next(10);
    assertTrue("Expected test object", nextObjectArray[0] == testNextMessage);

    String testTextMessage = "hello World";
    messageConsumerMock.expects(once()).method("receive").will(returnValue(mockTextMessage.proxy()));
    mockTextMessage.expects(once()).method("getJMSMessageID").will(returnValue("TestMessageID Text Message"));
    mockTextMessage.expects(once()).method("getText").will(returnValue(testTextMessage));

    nextObjectArray = testReadConnector.next(10);
    assertTrue("Expected test object", nextObjectArray[0] == testTextMessage);
  }

  public void testNextDisconnected() {
    try {
      testReadConnector.next(10);
      fail("Expected a ConnectionException to be thrown");
    }
    catch (ConnectionException ce) {
      log.debug("Expected ConnectionException raised." + ce);
    }
    catch (Exception e) {fail("Unexpected Exception: " + e); }
  }

  public void testNextJMSException() {
    setupConnectExpectations();

    JMSException testJmsException = new JMSException("I am a test exception.");
    messageConsumerMock.expects(once()).method("receive").will(throwException(testJmsException));

    testReadConnector.connect();

    try {
      testReadConnector.next(10);
      fail("Expected ConnectionException");
    } catch (ConnectionException e) {
      log.debug("Expected ConnectionException raised." + e);
    }
    catch (Exception e) {
      fail("Unexpected exception: " + e);
    }
  }


  // Support methods

  protected void setupConnectExpectations() {
    mockJMSConnection.setMockSession((Session)sessionMock.proxy());
    dirContextMock.expects(once()).method("lookup").with(eq(DESTINATION_NAME)).will(returnValue(destinationMock.proxy()));
    sessionMock.expects(once()).method("createConsumer")
      .with(eq(destinationMock.proxy()), eq(testReadConnector.getMessageSelector()), eq(testReadConnector.isNoLocal()))
      .will(returnValue(messageConsumerMock.proxy()));
  }

    // My Inner Mocks
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
