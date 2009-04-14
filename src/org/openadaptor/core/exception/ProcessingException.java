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

package org.openadaptor.core.exception;

import org.openadaptor.core.IComponent;

/**
 * OAException relating to processing problems.
 * <br>
 * These exceptions should be data specific, and not for example relate to transport
 * or resource issues.
 *
 * <p>ProcessingExceptions are most commonly raised by DataProcessor implementations
 * but can be raised by any component that manipulates data or requires a
 * specific data format.</p>
 *
 * @author perryj
 * @author higginse
 */
public class ProcessingException extends OAException {

  private static final long serialVersionUID = 1L;

  /** Default Constructor */
  public ProcessingException() {
    super();
  }

  /**
   * Supply just the Exception Message.
   *
   * @param msg   The error message.
   */
  public ProcessingException(String msg) {
    super(msg);
  }

  /**
   * Include both message and original cause.
   *
   * @param msg     The error message.
   * @param cause   Original exception thrown.
   */
  public ProcessingException(String msg, Throwable cause) {
    super(msg, cause);
  }

  /**
   * Include both message and original Component that raised the exception.
   *
   * @param msg The error message.
   * @param c   Component that raised the exception.
   */
  public ProcessingException(String msg, IComponent c) {
    super(msg, c);
  }

  /**
   * Include the message, cause and component.
   *
   * @param msg   The error message.
   * @param cause Original exception thrown.
   * @param c     Component that raised the exception.
   */
  public ProcessingException(String msg, Throwable cause, IComponent c) {
    super(msg, cause, c);
  }
}
