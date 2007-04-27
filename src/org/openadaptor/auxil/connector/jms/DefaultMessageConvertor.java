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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.exception.RecordFormatException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Apr 24, 2007 by oa3 Core Team
 */

/**
 * Default implementation of IMessageConvertor. This implementation supports JMS's TextMessage
 * and ObjectMessage only. Only the message contents are returned, all properties are ignored.
 */
public class DefaultMessageConvertor implements IMessageConvertor {

  private static final Log log = LogFactory.getLog(DefaultMessageConvertor.class);

  /** Extract the payload from the message */
  public Object unpackMessage(Message message) throws JMSException {
    Object msgContents;
    if (message instanceof TextMessage) {
      log.debug("Handling TextMessage");
      msgContents = ((TextMessage) message).getText();
    } else {
      if (message instanceof ObjectMessage) {
        log.debug("Handling ObjectMessage");
        msgContents = ((ObjectMessage) message).getObject();
      } else {
        throw new RecordFormatException("Unsupported JMS Message Type.");
      }
    }
    return msgContents;
  }
}
