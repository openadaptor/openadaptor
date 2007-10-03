/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in the
 Software without restriction, including without limitation the rights to use, copy,
 modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 and to permit persons to whom the Software is furnished to do so, subject to the
 following conditions:

 The above copyright notice and this permission notice shall be included in all 
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Nothing in this notice shall be deemed to grant any rights to trademarks, copyrights,
 patents, trade secrets or any other intellectual property of the licensor or any
 contributor except as expressly stated herein. No patent license is granted separate
 from the Software, for code that you delete from the Software, or for combinations
 of the Software with other software or hardware.
*/
package org.openadaptor.auxil.connector.jms;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

import javax.jms.*;
import java.io.Serializable;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Apr 27, 2007 by oa3 Core Team
 */

/**
 * All implementations of IMessagegenerator should be tested by a concrete implementation of this class.
 */
public abstract class AbstractMessageGeneratorTests extends MockObjectTestCase {

  protected IMessageGenerator testInstance;

  protected String messageText = "Test Text";
  private TestObject messageObject = new TestObject();


  protected void setUp() throws Exception {
    super.setUp();
    testInstance = createTestInstance();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  abstract protected IMessageGenerator createTestInstance();

  public void testSendText() {
    Mock sessionMock = new Mock(Session.class);
    Mock textMessageMock = new Mock(TextMessage.class);

    TextMessage message = (TextMessage)textMessageMock.proxy();
    sessionMock.expects(once()).method("createTextMessage").will(returnValue(message));
    textMessageMock.expects(once()).method("setText").with(eq(messageText));

    Message generatedMessage = null;

    try {
      generatedMessage = testInstance.createMessage(messageText, (Session)sessionMock.proxy());
    } catch (JMSException e) {
      fail("Unexpected JMSException: " + e );
    }

    assertEquals("Didn't return the expected TextMessage.", message, generatedMessage );
  }

  public void testSendObject() {
    Mock sessionMock = new Mock(Session.class);
    Mock objectMessageMock = new Mock(ObjectMessage.class);

    ObjectMessage message = (ObjectMessage)objectMessageMock.proxy();
    sessionMock.expects(once()).method("createObjectMessage").will(returnValue(message));
    objectMessageMock.expects(once()).method("setObject").with(eq(messageObject));

    Message generatedMessage = null;

    try {
      generatedMessage = testInstance.createMessage(messageObject, (Session) sessionMock.proxy());
    } catch (JMSException e) {
      fail("Unexpected JMSException: " + e );
    }
    assertEquals("Didn't return the expected ObjectMessage.", message, generatedMessage );
  }

  protected class TestObject implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;}
}
