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
package org.openadaptor.auxil.connector.jms.mock;

import org.openadaptor.auxil.connector.jms.JMSConnection;
import org.openadaptor.core.exception.ComponentException;

import java.util.List;
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Jan 19, 2007 by oa3 Core Team
 */

public class MockJMSConnection extends JMSConnection {

  private boolean throwComponentExceptionOnConnect = false;
  private boolean throwNPEOnConnect = false;
  private boolean passValidate = true;
  private boolean throwComponentExceptionOnDisconnect = false;
  private boolean throwNPEOnDisconnect = false;
  private boolean throwComponentExceptionOnNext = false;
  private boolean throwNPEOnNext = false;
  private boolean throwComponentExceptionOnDeliver = false;
  private boolean throwNPEOnDeliver = false;
  private boolean isConnected = false;

  private Object lastDelivery = null;
  private Object nextMessage;
  private int deliveryCount = 0;

  // Mocked methods

  public void connectForReader() {
    if (throwNPEOnConnect) throw new NullPointerException();
    if (throwComponentExceptionOnConnect) throw new ComponentException("Test Exception", this);
    isConnected = true;
  }

  public void connectForWriter() {
    if (throwNPEOnConnect) throw new NullPointerException();
    if (throwComponentExceptionOnConnect) throw new ComponentException("Test Exception", this);
    isConnected = true;
  }

  /**
   * Close the mockJMSConnection.
   */
  public void disconnect() {
    try {
      if (throwComponentExceptionOnDisconnect)
        throw new ComponentException("Test disconnect ComponentException.", this);
      if (throwNPEOnDisconnect) throw new NullPointerException();
    }
    finally { isConnected = false; }
  }

  /**
   * True if there is an existing session.
   *
   * @return boolean
   */
  public boolean isConnected() {
    return isConnected;
  }

  /**
   * Deliver the parameter to the confgured destination.
   *
   * @param message
   * @return String The JMS Message ID.
   */
  public String deliver(Object message) {
    if (throwNPEOnDeliver) throw new NullPointerException();
    if (throwComponentExceptionOnDeliver) throw new ComponentException("Test Exception", this);
    lastDelivery = message;
    deliveryCount++;
    return "MessageId: " + deliveryCount;
  }

  /**
   * Receive a message from the configured destination. This is a blocking receive which will time out based
   * on the value of the <b>timout</b> property.
   *
   * @return Object  The contents of the received message.
   */
  public Object receive(long timeoutMs) {
    if (throwNPEOnNext) throw new NullPointerException();
    if (throwComponentExceptionOnNext) throw new ComponentException("Test Exception", this);
    if (nextMessage == null) {
      try {
        Thread.sleep(timeoutMs);
      } catch (InterruptedException e) {}
    }
    return nextMessage;
  }

  public void validate(List exceptions) {
    if (!passValidate) exceptions.add(new ComponentException("failing validate for test purposes", this));
  }

  // Mock Support methods

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

  public void setThrowComponentExceptionOnNext(boolean throwComponentExceptionOnNext) {
    this.throwComponentExceptionOnNext = throwComponentExceptionOnNext;
  }

  public void setThrowNPEOnNext(boolean throwNPEOnNext) {
    this.throwNPEOnNext = throwNPEOnNext;
  }

  public void setThrowComponentExceptionOnDeliver(boolean throwComponentExceptionOnDeliver) {
    this.throwComponentExceptionOnDeliver = throwComponentExceptionOnDeliver;
  }

  public void setThrowNPEOnDeliver(boolean throwNPEOnDeliver) {
    this.throwNPEOnDeliver = throwNPEOnDeliver;
  }

  public Object getLastDelivery() {
    return lastDelivery;
  }

  public void clearLastDelivery() {
    lastDelivery = null;
  }

  public void setNextMessage(Object nextMessage) {
    this.nextMessage = nextMessage;
  }

}