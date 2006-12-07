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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.core.exception.OAException;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;
import java.util.Date;
/*
* File: $Header: $
* Rev: $Revision: $
* Created Dec 6, 2006 by oa3 Core Team
*/

public class SMTPConnection {

  private static final Log log = LogFactory.getLog(SMTPConnection.class.getName());

  private static final String DEFAULT_SMTP_PORT="25";
  private static final String DEFAULT_MAILER="openadaptor3";
  private String mailHost;
  private String mailHostPort=DEFAULT_SMTP_PORT;
  private String mailer=DEFAULT_MAILER;
  private String from;
  private String to;
  private String cc,bcc;
  private String subject;
  private String bodyPreface="";
  private boolean recordsAsAttachment = false;
  private Message message;
  private boolean connected=false;

  //BEGIN Bean getters/setters

  /**
   * Returns SMTP server which will route email
   *
   * @return SMTP server hostname
   */
  public String getMailHost() {
    return mailHost;
  }

  /**
   * Set SMTP server which will route email
   */
  public void setMailHost(String mailHost) {
    this.mailHost = mailHost;
  }

  public String getMailHostPort() {
    return mailHostPort;
  }

  public void setMailHostPort(String mailHostPort) {
    this.mailHostPort = mailHostPort;
  }

  /**
   * Returns mailer identifier, default is "openadaptor3"
   *
   * @return mailer identifier
   */
  public String getMailer() {
    return mailer;
  }

  /**
   * Set mailer identifier
   */
  public void setMailer(String mailer) {
    this.mailer = mailer;
  }

  /**
   * Returns senders email address
   *
   * @return email address
   */
  public String getFrom() {
    return from;
  }

  /**
   * Set senders email address
   */
  public void setFrom(String from) {
    this.from = from;
  }

  /**
   * Returns recipients email address, multiple address supplied by separating with commas
   *
   * @return recipients email address
   */
  public String getTo() {
    return to;
  }

  /**
   * Set recipients email address, multiple address supplied by separating with commas
   */
  public void setTo(String to) {
    this.to = to;
  }

  /**
   * Return cc recipients, multiple address supplied by separating with commas
   *
   * @return cc recipients
   */
  public String getCc() {
    return cc;
  }

  /**
   * Set cc recipients, multiple address supplied by separating with commas
   */
  public void setCc(String cc) {
    this.cc = cc;
  }

  /**
   * Return bcc recipients, multiple address supplied by separating with commas
   *
   * @return bcc recipients
   */
  public String getBcc() {
    return bcc;
  }

  /**
   * Set bcc recipients, multiple address supplied by separating with commas
   */
  public void setBcc(String bcc) {
    this.bcc = bcc;
  }

  /**
   * Returns subject of email
   *
   * @return subject string
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Set subject of email
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Return message object
   *
   * @return message object
   */
  public Message getMessage() {
    return message;
  }

  /**
   * Set message object
   */
  public void setMessage(Message message) {
    this.message = message;
  }

  /**
   * Return string to be prepended to email body to give context of records sent in email
   *
   * @return body preface string
   */
  public String getBodyPreface() {
    return bodyPreface;
  }

  /**
   * Set string to be prepended to email body to give context of records sent in email
   */
  public void setBodyPreface(String bodyPreface) {
    this.bodyPreface = bodyPreface;
  }

  /**
   * Determine if records are sent as attachments or in body of email
   *
   * @return boolean
   */
  public boolean getRecordAsAttachment() {
    return recordsAsAttachment;
  }

  /**
   * Determine if records are sent as attachments or in body of email
   */
  public void setRecordsAsAttachment(boolean recordsAsAttachment) {
    this.recordsAsAttachment = recordsAsAttachment;
  }

  //END   Bean getters/setters

  // Connection Support

  /**
   * Create a connection and session.
   */
  public void connect() {
    if ( !connected ) {
      createConnection();
    }
  }

  /**
   * Close the connection.
   */
  public void disconnect() {
    if (connected) {
      connected=false;
    }
  }

  /**
   * True if there is an existing session.
   *
   * @return boolean
   */
  public boolean isConnected() {
    return (connected);
  }

  // End Connection


  /**
   * Set up javamail objects required to create connection to smtp server.
   */
  protected void createConnection() throws OAException {
    Session session;

    try {
      log.debug("To: " + to);
      log.debug("Subject: " + subject);

      Properties props = System.getProperties();
      if (mailHost != null) {
        props.put("mail.smtp.host", mailHost);
        props.put("mail.smtp.port", mailHostPort);
      } else {
        throw new OAException ("FATAL: mailHost property not set");
      }
      // Get a Session object
      session = Session.getInstance(props, null);

      // construct the message
      message = new MimeMessage(session);
      if (from != null)
        message.setFrom(new InternetAddress(from));
      else
        message.setFrom();

      message.setRecipients(Message.RecipientType.TO,
        InternetAddress.parse(to, false));
      if (cc != null)
        message.setRecipients(Message.RecipientType.CC,
          InternetAddress.parse(cc, false));
      if (bcc != null)
        message.setRecipients(Message.RecipientType.BCC,
          InternetAddress.parse(bcc, false));

      message.setSubject(subject);

      message.setHeader("X-Mailer", mailer);
      message.setSentDate(new Date());

    } catch (MessagingException me) {
      throw new OAException(me.getMessage(), me);
    }
    log.debug("Successfully connected.");
    connected=true;
  }

  /**
   * Generate body of email. It will either be a mime multipart message or text. This is
   * determined by the <code>recordsAsAttachment</code> property.
   *
   * @param body
   * @throws MessagingException
   */
  public void generateMessageBody(String body) throws MessagingException{
      MimeBodyPart mbpPrefaceBody, mbpBody;
      MimeMultipart mmp;

      // Determine if records are to be send as an attachment
      if ( recordsAsAttachment ) {
        //Define mime parts
        mbpPrefaceBody = new MimeBodyPart();
        mbpPrefaceBody.setText(bodyPreface);
        mbpBody = new MimeBodyPart();
        mbpBody.setText(body);
        //Create mime message
        mmp = new MimeMultipart();
        mmp.addBodyPart(mbpPrefaceBody);
        mmp.addBodyPart(mbpBody);
        message.setContent(mmp);
      } else {
        message.setText(bodyPreface + "\n\n" + body);
      }
  }

  /**
   * Send prepared email.
   *
   * @throws MessagingException
   */
  public void send() throws MessagingException{
    Transport.send(message);
  }

}
