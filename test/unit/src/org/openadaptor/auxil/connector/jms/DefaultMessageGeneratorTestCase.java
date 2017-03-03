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

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.jmock.Mock;
import org.openadaptor.core.IMetadataAware;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Apr 27, 2007 by oa3 Core Team
 */

/**
 * Test DefaultMessageGenerator's implementation of the IMessageConvertor interface.
 */
public class DefaultMessageGeneratorTestCase extends AbstractMessageGeneratorTests{
  protected IMetadataAware metaAwareTestInstance;

  protected void setUp() throws Exception {
    // TODO Auto-generated method stub
    super.setUp();
    messageText = "Test Text";
  }

  protected IMessageGenerator createTestInstance() {
    DefaultMessageGenerator testee = new DefaultMessageGenerator();
    metaAwareTestInstance = (IMetadataAware)testee;
    return testee;
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
  
  public void testWithMetadata() {
    
    Map testMetadata = new HashMap();
    testMetadata.put("StringValue", "TestString");
    testMetadata.put("IntegerValue", new Integer(1));
    
    Mock sessionMock = new Mock(Session.class);
    Mock textMessageMock = new Mock(TextMessage.class);

    TextMessage message = (TextMessage)textMessageMock.proxy();
    sessionMock.expects(once()).method("createTextMessage").will(returnValue(message));
    textMessageMock.expects(once()).method("setText").with(eq(messageText));
    textMessageMock.expects(atLeastOnce()).method("setObjectProperty");
    
    Message generatedMessage = null;
    
    try {
      metaAwareTestInstance.setMetadata(testMetadata);
      generatedMessage = testInstance.createMessage(messageText, (Session) sessionMock.proxy());
    } catch (JMSException e) {
      fail("Unexpected JMSException: " + e );
    }
    assertEquals("Didn't return the expected TextMessage.", message, generatedMessage );
  }
  
  public void testWithNullMetadata() {
    
    Map testMetadata = new HashMap();
    
    Mock sessionMock = new Mock(Session.class);
    Mock textMessageMock = new Mock(TextMessage.class);

    TextMessage message = (TextMessage)textMessageMock.proxy();
    sessionMock.expects(once()).method("createTextMessage").will(returnValue(message));
    textMessageMock.expects(once()).method("setText").with(eq(messageText));
    textMessageMock.expects(never()).method("setObjectProperty");
    
    Message generatedMessage = null;
    
    try {
      metaAwareTestInstance.setMetadata(testMetadata);
      generatedMessage = testInstance.createMessage(messageText, (Session) sessionMock.proxy());
    } catch (JMSException e) {
      fail("Unexpected JMSException: " + e );
    }
    assertEquals("Didn't return the expected TextMessage.", message, generatedMessage );
  }  
  
  public void testWithEmptyMetadata() {
    
    Map testMetadata = null;
    
    Mock sessionMock = new Mock(Session.class);
    Mock textMessageMock = new Mock(TextMessage.class);

    TextMessage message = (TextMessage)textMessageMock.proxy();
    sessionMock.expects(once()).method("createTextMessage").will(returnValue(message));
    textMessageMock.expects(once()).method("setText").with(eq(messageText));
    textMessageMock.expects(never()).method("setObjectProperty");
    
    Message generatedMessage = null;
    
    try {
      metaAwareTestInstance.setMetadata(testMetadata);
      generatedMessage = testInstance.createMessage(messageText, (Session) sessionMock.proxy());
    } catch (JMSException e) {
      fail("Unexpected JMSException: " + e );
    }
    assertEquals("Didn't return the expected TextMessage.", message, generatedMessage );
  }    
 
  public void testWithInvalidMetadata() {
    
    String exceptionMessage = "Test Exception";
    
    Map testMetadata = new HashMap();
    testMetadata.put("test1", new Object());
    
    Mock sessionMock = new Mock(Session.class);
    Mock textMessageMock = new Mock(TextMessage.class);

    TextMessage message = (TextMessage)textMessageMock.proxy();
    sessionMock.expects(once()).method("createTextMessage").will(returnValue(message));
    textMessageMock.expects(once()).method("setText").with(eq(messageText));
    textMessageMock.expects(once()).method("setObjectProperty").will(throwException(new JMSException(exceptionMessage)));
    
    try {
      metaAwareTestInstance.setMetadata(testMetadata);
      testInstance.createMessage(messageText, (Session) sessionMock.proxy());
    } catch (JMSException e) {
      assertEquals("Raised jms exception not the expected one", e.getMessage(), (exceptionMessage));
    } catch (Exception e) {
      fail("Unexpected Exception");
    }
  }    
  
}
