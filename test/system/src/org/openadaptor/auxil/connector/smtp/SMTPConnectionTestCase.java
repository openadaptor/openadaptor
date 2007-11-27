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

package org.openadaptor.auxil.connector.smtp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dumbster.smtp.SimpleSmtpServer;
import junit.framework.TestCase;

public class SMTPConnectionTestCase extends TestCase {
  public static final Log log=LogFactory.getLog(SMTPConnectionTestCase.class);
  
  //Set an smtp port which will not interfere with a server running the smtp proccess
  private static final String SMTP_TEST_HOST="localhost";
  private static final int SMTP_TEST_PORT=2599;
  private static final String RECIPIENT="recipient@there.com";
  private static final String FROM="sender@here.com";
  private static final String TEST_SUBJECT="Test Subject";
  private static final String TEST_BODY="Test Body";

  private SMTPConnection smtpConnection;
  private SimpleSmtpServer smtpServer;

  protected void setUp()  throws Exception {
    super.setUp();
    smtpServer = SimpleSmtpServer.start(SMTP_TEST_PORT);
    smtpConnection = new SMTPConnection();
    smtpConnection.setMailHost(SMTP_TEST_HOST);
    smtpConnection.setMailHostPort(SMTP_TEST_PORT + "");
    smtpConnection.setTo(RECIPIENT);
    smtpConnection.setFrom(FROM);
    smtpConnection.setSubject(TEST_SUBJECT);
    smtpConnection.setBodyPreface(TEST_BODY);
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    smtpConnection.disconnect();
    //I haven't a clue how it works, but the cruise build sometimes gets
    //a NPE on the call to smtpServer.stop() here, which we don't really
    //care about. Hence sleep + try/catch.
    Thread.sleep(100);
    try {
      smtpServer.stop();
    }
    catch (Throwable t) {
      log.warn("FYI: tearDown() threw : "+t.toString());
    }
  }

  public void testConnect() {
    smtpConnection.connect();
    assertTrue("smtpConnection not connected",smtpConnection.isConnected());
  }

  public void testDisconnect() {
    smtpConnection.disconnect();
    assertFalse("smtpConnection not disconnected",smtpConnection.isConnected());
  }
}


