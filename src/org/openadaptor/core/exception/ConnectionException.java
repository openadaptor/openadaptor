/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.core.exception;

import org.openadaptor.core.IComponent;

/**
 * OAException for external resource problems.
 * <br>
 * These exceptions are not data specific but transport or resource specific
 * (e.g. Database Server is down etc.)
 *
 * <p>ConnectionException's are most commonly raised by Connector implementations
 * but can be raised by any component that accesses external resources.</p>
 *
 * @author higginse, scullyk
 */
public class ConnectionException extends OAException {

  private static final long serialVersionUID = 1;

  // Constructors

  /**
   * Supply just the Exception Message.
   *
   * @param msg   The error message.
   */
  public ConnectionException(String msg) {
    super(msg);
  }

  /**
   * Include both message and original cause.
   *
   * @param msg     The error message.
   * @param cause   Original exception thrown.
   */
  public ConnectionException(String msg, Throwable cause) {
    super(msg, cause);
  }
  /**
   * Include both message and original Component that raised the exception.
   *
   * @param msg The error message.
   * @param c   Component that raised the exception.
   */
  public ConnectionException(String msg, IComponent c) {
    super(msg, c);
  }

  /**
   * Include the message, cause and component.
   *
   * @param msg   The error message.
   * @param cause Original exception thrown.
   * @param c     Component that raised the exception.
   */
  public ConnectionException(String msg, Throwable cause, IComponent c) {
    super(msg, cause, c);
  }

}
