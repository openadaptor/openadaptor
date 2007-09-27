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
package org.openadaptor.auxil.connector.smtp;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

/**
 * Unit tests for {@link SMTPConnection}.
 * 
 * @author Kris Lachor
 */
public class SMTPConnectionUnitTestCase extends MockObjectTestCase  {

  private static String TEST_BODY = "test body";
  
  private static String TEST_MIME_CONTENT_TYPE = "testMimeType";
  
  SMTPConnection smtpConnection = new SMTPConnection();
  
  Mock mockMessage;
    
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    mockMessage = mock(Message.class, "mockMessage");
    smtpConnection.setMessage((Message)mockMessage.proxy());
  }

  /**
   * Test method for {@link org.openadaptor.auxil.connector.smtp.SMTPConnection
   * #generateMessageBody(java.lang.String)}.
   * recordsAsAttachment == false, mimeContentType == null
   * @throws MessagingException 
   */
  public void testGenerateMessageBody1() throws MessagingException {
    smtpConnection.setRecordsAsAttachment(false);
    mockMessage.expects(once()).method("setText").with(eq("\n\n" + TEST_BODY));
    smtpConnection.generateMessageBody(TEST_BODY);
  }
  
  /**
   * Test method for {@link org.openadaptor.auxil.connector.smtp.SMTPConnection
   * #generateMessageBody(java.lang.String)}.
   * recordsAsAttachment == true, mimeContentType == null
   * @throws MessagingException 
   */
  public void testGenerateMessageBody2() throws MessagingException {
    smtpConnection.setRecordsAsAttachment(true);
    mockMessage.expects(once()).method("setContent");
    smtpConnection.generateMessageBody(TEST_BODY);
  }
  
  /**
   * Test method for {@link org.openadaptor.auxil.connector.smtp.SMTPConnection
   * #generateMessageBody(java.lang.String)}.
   * recordsAsAttachment == false, mimeContentType != null
   * @throws MessagingException 
   */
  public void testGenerateMessageBody3() throws MessagingException {
    smtpConnection.setRecordsAsAttachment(false);
    smtpConnection.setMimeContentType(TEST_MIME_CONTENT_TYPE);
    mockMessage.expects(once()).method("setContent").with(eq("\n\n" + TEST_BODY), eq(TEST_MIME_CONTENT_TYPE));
    smtpConnection.generateMessageBody(TEST_BODY);
  }
  
  /**
   * Test method for {@link org.openadaptor.auxil.connector.smtp.SMTPConnection
   * #generateMessageBody(java.lang.String)}.
   * recordsAsAttachment == true, mimeContentType != null
   * @throws MessagingException 
   */
  public void testGenerateMessageBody4() throws MessagingException {
    smtpConnection.setRecordsAsAttachment(false);
    smtpConnection.setMimeContentType(TEST_MIME_CONTENT_TYPE);
    mockMessage.expects(once()).method("setContent");
    smtpConnection.generateMessageBody(TEST_BODY);
  }

}
