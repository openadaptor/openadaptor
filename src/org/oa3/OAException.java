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
package org.oa3;

/*
 * File: $Header: /cvs/oa3/src/org/oa3/control/OAException.java,v 1.4 2006/10/19 11:45:45 fennelr Exp $
 * Rev:  $Revision: 1.4 $
 * Created Sep 28, 2005 by Kevin Scully
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * General purpose openadptor exception implementation.
 * <p/>
 *
 * Has two main features:
 * <ul>
 *      <li>It extends the Apache Commons Lang NestedRuntimeException. This allows
 *      it to retain the originating exception (if any).</li>
 *      <li>It is unchecked. Unless there is very good reason otherwise, all
 *      framework exceptions should be unchecked</li>
 * </ul>
 *
 * @author Kevin Scully
 */

public class OAException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private static final Log log = LogFactory.getLog(OAException.class);

  private Throwable nestedException;

  private IComponent component = null;

  public OAException(String msg, Throwable cause, IComponent c) {
    component = c;
    nestedException = cause;
    log.error(msg + " : " + (nestedException != null ? nestedException.getMessage() : ""));
  }

  public OAException(String msg, Throwable cause) {
    this(msg, cause, null);
  }

  public OAException(String msg, IComponent c) {
    this(msg, null, c);
  }

  public OAException(String msg) {
    this(msg, null, null);
  }

  public IComponent getComponent() {
    return component;
  }
}
