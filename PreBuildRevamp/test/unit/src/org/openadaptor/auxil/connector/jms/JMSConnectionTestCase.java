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
import org.openadaptor.core.exception.ComponentException;

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

public class JMSConnectionTestCase extends MockObjectTestCase {

  private static final Log log = LogFactory.getLog(JMSConnectionTestCase.class);

  protected static String CONNECTION_FACTORY_LOOKUP_NAME = "TestTopicConnectionFactory";
  protected static String DESTINATION_NAME = "testTopic";

  private JMSConnection testConnection;

  private Mock connectionFactoryMock;
  private Mock connectionMock;
  private Mock sessionMock;
  private Mock destinationMock;
  private JNDIConnection jndiConnection;
  private Mock dirContextMock;
  private DirContext dirContext;

  protected void setUp() throws Exception {
    super.setUp();

    dirContextMock = new Mock(DirContext.class);
    dirContext = (DirContext) dirContextMock.proxy();

    connectionFactoryMock = new Mock(ConnectionFactory.class);
    connectionMock = new Mock(Connection.class);
    sessionMock = new Mock(Session.class);
    destinationMock = new Mock(Topic.class);

    // Mock of openadaptor3's JNDIConnection
    jndiConnection = new MockJNDIConnection();
    ((MockJNDIConnection) jndiConnection).setContext(dirContext);

    testConnection = new JMSConnection();
    testConnection.setConnectionFactoryName(CONNECTION_FACTORY_LOOKUP_NAME);
    testConnection.setJndiConnection(jndiConnection);
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    testConnection = null;
    dirContextMock = null;
    dirContext = null;
    connectionFactoryMock = null;
    connectionMock = null;
    sessionMock = null;
    destinationMock = null;
    jndiConnection = null;
  }

  public void testValidate() {
    log.debug("Running Test: validate");
    // Test instance should be set up to pass Validate correctly
    List validateExceptions = new ArrayList();
    testConnection.validate(validateExceptions);
    assertTrue("Didn't validate when it should have." + validateExceptions, validateExceptions.size() == 0);
  }

  public void testValidateNoConnectionFactorySet() {
    log.debug("Running Test: ValidateNoConnectionFactorySet");
    testConnection.setConnectionFactory(null);
    testConnection.setConnectionFactoryName(null);
    List validateExceptions = new ArrayList();
    testConnection.validate(validateExceptions);
    assertTrue("Validated when it shouldn't have." + validateExceptions, validateExceptions.size() > 0);
  }

  public void testValidateNoJndiConnectionSet() {
    log.debug("Running Test: ValidateNoJndiConnectionSet");
    testConnection.setJndiConnection(null);
    List validateExceptions = new ArrayList();
    testConnection.validate(validateExceptions);
    assertTrue("Validated when it shouldn't have." + validateExceptions, validateExceptions.size() > 0);
  }

  public void testValidateNothingSet() {
    log.debug("Running Test: ValidateNothingSet");
    testConnection.setConnectionFactory(null);
    testConnection.setConnectionFactoryName(null);
    testConnection.setJndiConnection(null);
    List validateExceptions = new ArrayList();
    testConnection.validate(validateExceptions);
    assertTrue("Expected three validation exceptions. Got " + validateExceptions.size() + "." + validateExceptions, validateExceptions.size() == 2);
  }

  public void testCreateConnection() {
    log.debug("Running Test: ConnectReader");
    // Setup Connect expectations
    connectionFactoryMock.expects(once()).method("createConnection").will(returnValue(connectionMock.proxy()));
    dirContextMock.expects(once()).method("lookup").with(eq(CONNECTION_FACTORY_LOOKUP_NAME)).will(returnValue(connectionFactoryMock.proxy()));
    try {
      testConnection.createConnection();
    }
    catch (Exception e) {
      fail("Unexpected Exception. " + e);
    }
  }

  public void testCreateConnectionException() {
    log.debug("Running Test: ConnectReaderException");
    connectionFactoryMock.expects(once()).method("createConnection").will(throwException(new JMSException("test")));

    connectionMock.expects(never()).method("createSession");
    connectionMock.expects(never()).method("start");
    sessionMock.expects(never()).method("createConsumer");

    dirContextMock.expects(once()).method("lookup").with(eq(CONNECTION_FACTORY_LOOKUP_NAME)).will(returnValue(connectionFactoryMock.proxy()));
    dirContextMock.expects(never()).method("lookup").with(eq(DESTINATION_NAME)).will(returnValue(destinationMock.proxy()));

    try {
      testConnection.createConnection();
      fail("Expected ComponentException not thrown.");
    } catch (ComponentException e) {
      // expected
    } catch (Exception e) {
      fail("Unexpected exception: " + e);
    }
  }

  public void testDisconnect() {
    log.debug("Running Test: Disconnect");
    // Setup Connect expectations
    connectionFactoryMock.expects(once()).method("createConnection").will(returnValue(connectionMock.proxy()));
    dirContextMock.expects(once()).method("lookup").with(eq(CONNECTION_FACTORY_LOOKUP_NAME)).will(returnValue(connectionFactoryMock.proxy()));

    testConnection.createConnection();

    // Setup disconnect expectations
    connectionMock.expects(once()).method("close");

    testConnection.disconnect();
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
