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

package org.oa3.auxil.connector.smtp;
/*
* File: $Header: /cvs/oa3/src/org/oa3/connector/smtp/SMTPWriteConnector.java,v 1.5 2006/11/01 09:10:11 fennelr Exp $
* Rev: $Revision: 1.5 $
* Created Oct 5, 2006 by oa3 Core Team
*/

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.core.exception.OAException;
import org.oa3.core.connector.AbstractWriteConnector;
import javax.mail.MessagingException;

/**
 * This class will send messages via smtp (email) to destinations specified in properties.
 * Records can be included as part of the body or as attachments
 *
 * @author Kuldip Ottal
 *
 */

public class SMTPWriteConnector extends AbstractWriteConnector {

  private static final Log log = LogFactory.getLog(SMTPWriteConnector.class.getName());

  private SMTPConnection smtpConnection;

  /**
   * Return smtp connection
   *
   * @return SMTPConnection
   */
  public SMTPConnection getSmtpConnection() {
    return smtpConnection;
  }

  /**
   * Set smtp connection
   */
  public void setSmtpConnection(SMTPConnection smtpConnection) {
    this.smtpConnection = smtpConnection;
  }

  /**
   * Deliver a batch of records.
   *
   * @param records - an Array of records to be processed.
   * @return result information if any. May well be null.
   * @throws org.oa3.core.exception.OAException
   */
  public Object deliver(Object[] records) throws OAException {
    String result=null;
    String body="";

    int size=records.length;
    for (int recordIndex =0;recordIndex <size;recordIndex++) {
      Object record =records[recordIndex];
      try {
        if (record != null) {
          body += record.toString();
          //Generate body of mail message
          smtpConnection.generateMessageBody(body);
          //Send message
          smtpConnection.send();
        } else {
          throw new OAException("Malformed data for smtp write connector - record has null value");
        }
      } catch (MessagingException me) {
        throw new OAException(me.getMessage(), me);
      }
    }

    result= "Email message sent successfully, contained " + size + " records";
    log.info(result);
    return result;

  }


  /**
   * Establish a connection to external message transport without starting the externalconnector. If already connected
   * then do nothing.
   */
  public void connect() {
    if (!isConnected()) {
      smtpConnection.connect();
      connected=smtpConnection.isConnected();
    }
  }

  /**
   * Disconnect from the external message transport. If already disconnected then do nothing.
   */
  public void disconnect() {
    if (isConnected()) {
      smtpConnection.disconnect();
      connected=smtpConnection.isConnected();
    }
  }


}
