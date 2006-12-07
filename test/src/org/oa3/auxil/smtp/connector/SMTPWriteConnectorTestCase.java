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

package org.oa3.auxil.smtp.connector;

import junit.framework.TestCase;
import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import org.oa3.auxil.smtp.SMTPWriteConnector;
import org.oa3.auxil.smtp.SMTPConnection;

import java.util.Iterator;

/**
* File: $Header: $
* Rev: $Revision: $
* Created Dec 7, 2006 by oa3 Core Team
*/

public class SMTPWriteConnectorTestCase extends TestCase {
  //Set an smtp port which will not interfere with a server running the smtp proccess
  private static final String SMTP_TEST_HOST="localhost";
  private static final int SMTP_TEST_PORT=2599;
  private static final String RECIPIENT="recipient@there.com";
  private static final String FROM="sender@here.com";
  private static final String TEST_SUBJECT="Test Subject";
  private static final String TEST_BODY="Test Body";

  private SMTPConnection smtpConnection;
  private SMTPWriteConnector smtpWriteConnector;
  private SimpleSmtpServer smtpServer;

  protected void setUp()  throws Exception {
    super.setUp();
    smtpServer = SimpleSmtpServer.start(SMTPWriteConnectorTestCase.SMTP_TEST_PORT);

    smtpConnection = new SMTPConnection();
    smtpConnection.setMailHost(SMTPWriteConnectorTestCase.SMTP_TEST_HOST);
    smtpConnection.setMailHostPort(SMTPWriteConnectorTestCase.SMTP_TEST_PORT + "");
    smtpConnection.setTo(SMTPWriteConnectorTestCase.RECIPIENT);
    smtpConnection.setFrom(SMTPWriteConnectorTestCase.FROM);
    smtpConnection.setSubject(SMTPWriteConnectorTestCase.TEST_SUBJECT);
    smtpConnection.setBodyPreface(SMTPWriteConnectorTestCase.TEST_BODY);
    smtpConnection.setRecordsAsAttachment(true);

    smtpWriteConnector = new SMTPWriteConnector();
    smtpWriteConnector.setSmtpConnection(smtpConnection);
    smtpWriteConnector.connect();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    smtpWriteConnector.disconnect();
    smtpConnection.disconnect();
    smtpServer.stop();
  }

  public void testConnect() {
    assertTrue("smtpWriteConnector not connected",smtpWriteConnector.isConnected());
  }

  public void testDeliver() {
    String[] recordArray = { "TestRecord1" };
    smtpWriteConnector.deliver(recordArray);
    // Stop smtp server to ensure there are no timing issues with processing of messages and unit testing
    smtpServer.stop();

    assertTrue("SMTP Server did not recieve any email messages", smtpServer.getReceivedEmailSize() == 1);

    Iterator emailIter = smtpServer.getReceivedEmail();
    SmtpMessage email = (SmtpMessage) emailIter.next();
    assertTrue("Unexpected email subject" + email.getHeaderValue("Subject"), email.getHeaderValue("Subject").equals(TEST_SUBJECT));
    // Does email body contain "TestRecord1"
    assertTrue("Unexpected email body" + email.getBody(), email.getBody().indexOf("TestRecord1") > 0);
  }

  public void testDisconnect() {
    smtpWriteConnector.disconnect();
    assertFalse("smtp write connector not disconnected",smtpWriteConnector.isConnected());
  }


}
