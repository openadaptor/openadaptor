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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.connector.AbstractWriteConnector;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.OAException;
import org.openadaptor.core.exception.ProcessingException;

import javax.mail.MessagingException;

/**
 * This Write Connector will deliver data by sending it as an email using the SMTP protocol.
 * It needs to be configured with an {@link SMTPConnection}.
 * 
 * @author Kuldip Ottal
 *
 */

public class SMTPWriteConnector extends AbstractWriteConnector {

  private static final Log log = LogFactory.getLog(SMTPWriteConnector.class);

  private SMTPConnection smtpConnection;

  public void setSmtpConnection(final SMTPConnection smtpConnection) {
    this.smtpConnection = smtpConnection;
  }

  /**
   * Creates and sends email containing the data. The body of the message is a concatentation of
   * calls to {@link #toString()} on each element in the data array.
   *
   * @param data an array of objects to be processed.
   * @return result string confirming that the email was sucessfully sent.
   * @throws OAException
   */
  public Object deliver(Object[] data) throws OAException {
    String result=null;
    String body="";

    int size=data.length;
    for (int recordIndex =0;recordIndex <size;recordIndex++) {
      Object record =data[recordIndex];
      try {
        if (record != null) {
          body += record.toString();
          smtpConnection.generateMessageBody(body);
          smtpConnection.send();
        } else {
          throw new ProcessingException("Malformed data for smtp write connector - record has null value", this);
        }
      } catch (MessagingException me) {
        throw new ConnectionException(me.getMessage(), me, this);
      }
    }

    result= "Email message sent successfully, contained " + size + " records";
    log.info(result);
    return result;

  }

  /**
   * creates smtp connection
   */
  public void connect() {
    if (!isConnected()) {
      smtpConnection.connect();
      connected=smtpConnection.isConnected();
    }
  }

  /**
   * disposes of smtp connection
   */
  public void disconnect() {
    if (isConnected()) {
      smtpConnection.disconnect();
      connected=smtpConnection.isConnected();
    }
  }


}
