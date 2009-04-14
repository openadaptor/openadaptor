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
/*
 * File: $Header: $
 * Rev:  $Revision: $
 * Created Sep 24, 2007 by oa3 Core Team
 */

/**
 * Base class extended by all openadaptor unchecked exceptions.
 *
 * <p>RuntimeExceptions raised by openadaptor components should always be an
 * instance of this class or it's subclasses. This allows the Exception
 * Management System to easily distinguish openadaptor specific exceptions.</p>
 *
 * <p>As this extends RuntimeException this exception can be used to wrap
 * other exceptions. It is strongly reccomended that all third party exceptions
 * raised by Connectors or DataProcessors are wrapped by the appropriate
 * OAException subclass. This makes it much easier to configure openadaptor's
 * exception management correctly.</p>
 *
 * <p>OAException extends RuntimeException to add the ability to record the
 * IComponent that raised the exception. This is an optional feature. There
 * is no need to supply this if the exception is not being raised by an
 * IComponent implementation.</p>
 *
 * @author Kevin Scully
 */
public class OAException extends RuntimeException {

  private static final long serialVersionUID = 1L;
  protected IComponent component = null;

  // Constructors

  public OAException() {
    super();
  }

  /**
   * Supply just the Exception Message.
   *
   * @param msg   The error message.
   */
  public OAException(String msg) {
    super(msg);
  }

  /**
   * Include both message and original cause.
   *
   * @param msg     The error message.
   * @param cause   Original exception thrown.
   */
  public OAException(String msg, Throwable cause) {
    super(msg, cause);
  }

  /**
   * Include both message and original Component that raised the exception.
   *
   * @param msg The error message.
   * @param c   Component that raised the exception.
   */
  public OAException(String msg, IComponent c) {
    super(msg);
    component = c;
  }

  /**
   * Include the message, cause and component.
   *
   * @param msg   The error message.
   * @param cause Original exception thrown.
   * @param c     Component that raised the exception.
   */
  public OAException(String msg, Throwable cause, IComponent c) {
    super(msg, cause);
    component = c;
  }

  /**
   * Accessor for component that raised the exception.
   *
   * @return IComponent
   */
  public IComponent getComponent() {
    return component;
  }

  /**
   * Return the error message. If the component is populated then the message
   * will be prefixed with the component id.
   *
   * @return String   The error message.
   */
  public String getMessage() {
    return (component != null ? component.getId() : "") + " : " + super.getMessage();
  }
}
