/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a
 copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be included
 in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Nothing in this notice shall be deemed to grant any rights to
 trademarks, copyrights, patents, trade secrets or any other intellectual
 property of the licensor or any contributor except as expressly stated
 herein. No patent license is granted separate from the Software, for
 code that you delete from the Software, or for combinations of the
 Software with other software or hardware.
*/
package org.openadaptor.auxil.connector.jms;

import java.util.List;

import javax.jms.ExceptionListener;
import javax.jms.Session;

import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.ValidationException;

public class MockJMSConnection extends JMSConnection {

  private boolean throwComponentExceptionOnConnect = false;
  private boolean throwNPEOnConnect = false;
  private boolean passValidate = true;
  private boolean throwComponentExceptionOnDisconnect = false;
  private boolean throwNPEOnDisconnect = false;

  private Session mockSession;
  private ExceptionListener listener;

  // Mocked methods


  protected void installAsExceptionListener(ExceptionListener listener) {
    this.listener = listener;
  }


  /**
   * Close the mockJMSConnection.
   */
  public void disconnect() {
    try {
      if (throwComponentExceptionOnDisconnect)
        throw new ConnectionException("Test disconnect ComponentException.", this);
      if (throwNPEOnDisconnect) throw new NullPointerException();
    }
    finally { setConnection(null); }
  }

  public void validate(List exceptions) {
    if (!passValidate) exceptions.add(new ValidationException("failing validate for test purposes", this));
  }

  // Overridden Mocks

  /**
   * Optionally create and return a JMS Session. If a TransactionManager is referenced and the session is
   * transacted then register a TransactionSpec with that TransactionManager.
   *
   * This is mocked up as we are testing the connector not the connection
   *
   * @return Session A JMS Session
   */
  public Session createSessionFor(JMSReadConnector connector) {
    if (throwNPEOnConnect) throw new NullPointerException();
    if (throwComponentExceptionOnConnect) throw new ConnectionException("Test Exception", this);
    return mockSession;
  }

  /**
   * Optionally create and return a JMS Session. If a TransactionManager is referenced and the session is
   * transacted then register a TransactionSpec with that TransactionManager.
   *
   * @return Session A JMS Session
   */
  public Session createSessionFor(JMSWriteConnector connector) {
    if (throwNPEOnConnect) throw new NullPointerException();
    if (throwComponentExceptionOnConnect) throw new ConnectionException("Test Exception", this);
    return mockSession;
  }

  // Mock Support methods

  public void setMockSession(Session session) {
    mockSession = session;
  }

  public void setThrowComponentExceptionOnConnect(boolean throwComponentExceptionOnConnect) {
    this.throwComponentExceptionOnConnect = throwComponentExceptionOnConnect;
  }

  public void setThrowNPEOnConnect(boolean throwNPEOnConnect) {
    this.throwNPEOnConnect = throwNPEOnConnect;
  }

  public void setPassValidate(boolean passValidate) {
    this.passValidate = passValidate;
  }

  public void setThrowComponentExceptionOnDisconnect(boolean throwComponentExceptionOnDisconnect) {
    this.throwComponentExceptionOnDisconnect = throwComponentExceptionOnDisconnect;
  }

  public void setThrowNPEOnDisconnect(boolean throwNPEOnDisconnect) {
    this.throwNPEOnDisconnect = throwNPEOnDisconnect;
  }

  // Extra getters

  public ExceptionListener getListener() {
    return listener;
  }

}