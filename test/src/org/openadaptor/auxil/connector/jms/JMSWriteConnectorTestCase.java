/*
 * [[
 * Copyright (C) 2001 - 2006 The Software Conservancy as Trustee. All rights
 * reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Nothing in this notice shall be deemed to grant any rights to
 * trademarks, copyrights, patents, trade secrets or any other intellectual
 * property of the licensor or any contributor except as expressly stated
 * herein. No patent license is granted separate from the Software, for
 * code that you delete from the Software, or for combinations of the
 * Software with other software or hardware.
 * ]]
 */
package org.openadaptor.auxil.connector.jms;

import org.jmock.MockObjectTestCase;
import org.openadaptor.auxil.connector.jms.mock.MockJMSConnection;
import org.openadaptor.core.exception.ComponentException;

import java.util.List;
import java.util.ArrayList;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Jan 19, 2007 by oa3 Core Team
 */

public class JMSWriteConnectorTestCase extends MockObjectTestCase {
  private JMSWriteConnector testWriteConnector;
  private MockJMSConnection mockJMSConnection;

  protected void setUp() throws Exception {
    super.setUp();
    mockJMSConnection = new MockJMSConnection();
    testWriteConnector = new JMSWriteConnector();
    testWriteConnector.setJmsConnection(mockJMSConnection);
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

  public void testFailValidate() {
    mockJMSConnection.setPassValidate(false);
    List validateExceptions = new ArrayList();
    testWriteConnector.validate(validateExceptions);
    assertTrue("Should have failed validate.", validateExceptions.size() > 0);
  }

  public void testFailValidateNoConnection() {
    testWriteConnector.setJmsConnection(null);
    List validateExceptions = new ArrayList();
    testWriteConnector.validate(validateExceptions);
    assertTrue("Should have failed validate.", validateExceptions.size() > 0);
  }

  public void testConnect() {
    try {
      testWriteConnector.connect();
      assertTrue("Should be connected.", testWriteConnector.isConnected());
    }
    catch (Exception e) {
      fail("Unexpected exception." + e);
    }
  }

  public void testConnectFailureComponentException() {
    mockJMSConnection.setThrowComponentExceptionOnConnect(true);
    try {
      testWriteConnector.connect();
      fail("Expected a ComponentException to be thrown.");
    }
    catch (ComponentException ce) { /* This is expected */ }
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
    try {
      testWriteConnector.connect();
      testWriteConnector.disconnect();
      assertFalse("Should be disconnected. ", testWriteConnector.isConnected());
    } catch (Exception e) {
      fail("Unexpected Exception: " + e);
    }
  }

  public void testDisconnectFailureNPE() {
    mockJMSConnection.setThrowNPEOnDisconnect(true);
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
    mockJMSConnection.setThrowComponentExceptionOnDisconnect(true);
    try {
      testWriteConnector.connect();
      testWriteConnector.disconnect();
      fail("Expected a ComponentException to be thrown.");
    } catch (ComponentException ce) {
    }
    catch (Exception e) {
      fail("Unexpected Exception: " + e);
    }
  }

  public void testDeliver() {
    testWriteConnector.connect();
    mockJMSConnection.clearLastDelivery();
    Object testMessage = new Object();
    testWriteConnector.deliver(new Object[] { testMessage } );
    assertEquals("Expected delivered message to match test message", mockJMSConnection.getLastDelivery(), testMessage);

  }

  public void testDeliverDisconnected() {
    try {
      mockJMSConnection.clearLastDelivery();
      Object testMessage = new Object();
      testWriteConnector.deliver(new Object[] { testMessage } );
      fail("Expected a ComponentException to be thrown");
    }
    catch (ComponentException ce) { /* Expected */ }
    catch (Exception e) {fail("Unexpected Exception: " + e); }
  }

  public void testDeliverCE() {
    testWriteConnector.connect();
    mockJMSConnection.clearLastDelivery();
    Object testMessage = new Object();
    mockJMSConnection.setThrowComponentExceptionOnDeliver(true);
    try {
      testWriteConnector.deliver(new Object[] { testMessage } );
      fail("Expected ComponentException");
    } catch (ComponentException e) {
      // expected this to be thrown.
    }
    catch (Exception e) { fail("Unexpected exception: " + e); }
  }

  public void testDeliverNPE() {
    testWriteConnector.connect();
    mockJMSConnection.clearLastDelivery();
    Object testMessage = new Object();
    mockJMSConnection.setThrowNPEOnDeliver(true);
    try {
      testWriteConnector.deliver(new Object[] { testMessage } );
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // expected this to be thrown.
    }
    catch (Exception e) { fail("Unexpected exception: " + e); }
  }

}
