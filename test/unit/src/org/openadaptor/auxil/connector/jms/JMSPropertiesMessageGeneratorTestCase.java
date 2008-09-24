/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.jmock.Mock;
import org.openadaptor.core.exception.ProcessingException;
import org.openadaptor.core.exception.RecordFormatException;

/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Sep 24, 2008 by oa3 Core Team
 */
public class JMSPropertiesMessageGeneratorTestCase extends
    AbstractMessageGeneratorTests {

  private static final String DEFAULT_MESSAGE_TEXT = "<root><field1>test</field1></root>";

  protected IMessageGenerator createTestInstance() {
    return new JMSPropertiesMessageGenerator();
  }

  protected void setUp() throws Exception {
    super.setUp();
    messageText = DEFAULT_MESSAGE_TEXT;
  }

  /**
   * Tests that sending a random object produces that appropriate exception.
   */
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
    } catch (ProcessingException pe) {
      return;
    }
    
    assertEquals("Didn't return the expected ObjectMessage.", message, generatedMessage );
  }
  
  /**
   * Ensure that the MessageGenerator's properties can be set. 
   */
  public void testSetProperties() {
    Map testMap = new HashMap();
    testMap.put("key1", "//field1");
    ((JMSPropertiesMessageGenerator)testInstance).setProperties(testMap);
    assertEquals(testMap, ((JMSPropertiesMessageGenerator)testInstance).getProperties());    
  }
  
  /**
   * Ensure that the JMS Message Properties are set when valid properties and message contents are used.
   */
  public void testSetMessageProperties() {
    Map testMap = new HashMap();
    testMap.put("key1", "//field1");
    ((JMSPropertiesMessageGenerator)testInstance).setProperties(testMap);
    
    Mock sessionMock = new Mock(Session.class);
    Mock textMessageMock = new Mock(TextMessage.class);

    TextMessage message = (TextMessage)textMessageMock.proxy();
    sessionMock.expects(once()).method("createTextMessage").will(returnValue(message));
    textMessageMock.expects(once()).method("setText").with(eq(messageText));
    textMessageMock.expects(once()).method("setStringProperty").with(eq("key1"), eq("test"));
    Message generatedMessage = null;

    try {
      generatedMessage = testInstance.createMessage(messageText, (Session)sessionMock.proxy());
    } catch (JMSException e) {
      fail("Unexpected JMSException: " + e );
    }

    assertEquals("Didn't return the expected TextMessage.", message, generatedMessage );
  }
  
  /**
   * Ensure that a Dom4j Document can be used.
   */
  public void testSendDocument() {
    Document doc = null;
    try {
      doc = DocumentHelper.parseText(messageText);
    } catch (DocumentException e) {
      fail("Test text isn't valid xml");
    }
    
    Mock sessionMock = new Mock(Session.class);
    Mock textMessageMock = new Mock(TextMessage.class);

    TextMessage message = (TextMessage)textMessageMock.proxy();
    sessionMock.expects(once()).method("createTextMessage").will(returnValue(message));
    textMessageMock.expects(once()).method("setText").with(eq(doc.asXML()));

    Message generatedMessage = null;

    try {
      generatedMessage = testInstance.createMessage(doc, (Session)sessionMock.proxy());
    } catch (JMSException e) {
      fail("Unexpected JMSException: " + e );
    }

    assertEquals("Didn't return the expected TextMessage.", message, generatedMessage );
  }
  
  /**
   * Ensure that a RecordFormatException is thrown when a string that is not valid XML is sent.
   */
  public void testSendInvalidXML() {
    String invalidText = "This is not XML";
    
    Mock sessionMock = new Mock(Session.class);
    Mock textMessageMock = new Mock(TextMessage.class);

    TextMessage message = (TextMessage)textMessageMock.proxy();
    sessionMock.expects(once()).method("createTextMessage").will(returnValue(message));
    textMessageMock.expects(once()).method("setText").with(eq(invalidText));

    try {
      testInstance.createMessage(invalidText, (Session)sessionMock.proxy());
    } catch (JMSException e) {
      fail("Unexpected JMSException: " + e );
    } catch (RecordFormatException rfe) {return;}

    fail("Didn't throw expected RecordFormatException" );
  }
  
  /**
   * Test that you get a sensible exception when the path to the property value in the xml is invalid.
   */
  public void testInvalidMessageProperty() {
    Map testMap = new HashMap();
    testMap.put("key1", "//InvalidPath");
    ((JMSPropertiesMessageGenerator)testInstance).setProperties(testMap);
    
    Mock sessionMock = new Mock(Session.class);
    Mock textMessageMock = new Mock(TextMessage.class);

    TextMessage message = (TextMessage)textMessageMock.proxy();
    sessionMock.expects(once()).method("createTextMessage").will(returnValue(message));
    textMessageMock.expects(once()).method("setText").with(eq(messageText));
    textMessageMock.expects(once()).method("setStringProperty").with(eq("key1"), eq("test"));
    Message generatedMessage = null;

    try {
      generatedMessage = testInstance.createMessage(messageText, (Session)sessionMock.proxy());
    } catch (JMSException e) {
      fail("Unexpected JMSException: " + e );
    } catch (ProcessingException pe) {return;}

    fail("Didn't throw expected ProcessingException" );
  }
   
  
}
