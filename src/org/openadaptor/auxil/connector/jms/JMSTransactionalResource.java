/*
 Copyright (C) 2001 - 2009 The Software Conservancy as Trustee. All rights reserved.

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

import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.transaction.ITransactionalResource;

public class JMSTransactionalResource implements ITransactionalResource {

  private static final Log log = LogFactory.getLog(JMSTransactionalResource.class);

  //private JMSConnection connection;
  private Session session;

  public JMSTransactionalResource(final Session session) {
    this.session = session;
  }

  public void begin() {
    try {
      if (!session.getTransacted()) {
        throw new RuntimeException("Attempt to start transaction using an untransacted JMS Session");
      }
    } catch (JMSException e) {
      throw new RuntimeException("JMS Exception on attempt to start transaction using a JMS Session", e);
    }
    log.debug("JMSTransaction begun");
  }

  public void commit() {
    try {
      if (!session.getTransacted()) {
        throw new RuntimeException("Attempt to commit a transaction using an untransacted JMS Session");
      } else {
        session.commit();
        log.debug("JMSTransaction committed");
      }
    } catch (JMSException e) {
      throw new RuntimeException("JMS Exception on attempt to commit a transaction using a JMS Session", e);
    }
  }

  public void rollback(Throwable t) {
    try {
      if (!session.getTransacted()) {
        throw new RuntimeException("Attempt to rollback a transaction using an untransacted JMS Session");
      } else {
        log.debug("JMSTransaction rolled back");
        session.rollback();
      }
    } catch (JMSException e) {
      throw new RuntimeException("JMS Exception on attempt to rollback a transaction using a JMS Session", e);
    }
  }

}
