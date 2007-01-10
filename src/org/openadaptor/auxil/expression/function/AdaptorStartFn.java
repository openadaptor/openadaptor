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
package org.openadaptor.auxil.expression.function;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.expression.ExpressionException;
import org.openadaptor.util.Application;

/**
 * Function to provide access to the Adaptor start timestamp as a <code>Date</code>.
 * <p>
 * This amounts to a convenience function to retrieve System property <i>RunAdaptor.ADAPTOR_START_TIMESTAMP_PROPERTY</i>
 * and parse it as a <code>Date</code> using the format from <i>RunAdaptor.ADAPTOR_START_TIMESTAMP_FORMAT</i>
 * 
 * @author Eddy Higgins
 */
public class AdaptorStartFn extends SystemPropertyFn {
  public static final String NAME = "adaptorstart";

  private static final Log log = LogFactory.getLog(AdaptorStartFn.class);

  private SimpleDateFormat sdf;

  Date startTimestamp = null;

  public AdaptorStartFn() {
    super(AdaptorStartFn.NAME, 0);
    sdf = new SimpleDateFormat(Application.START_TIMESTAMP_FORMAT);
  }

  /**
   * Retrieve the adaptor start timestamp as a <code>Date</code> object.
   * <P>
   * This assumes the supplied argument contains the name of a System property to retrieve.
   * <P>
   * It is essentially a wrapper around System.getProperties(arg[0]).
   * 
   * @param args
   *          Object array which is ignored
   * @return Object containing a String with the value of the names system property.
   */
  protected Object operate(Object[] args) throws ExpressionException {
    Date result = startTimestamp;
    if (result == null) { // Not already set. Look it up.
      String startTimestamp = System.getProperty(Application.PROPERTY_START_TIMESTAMP);
      if (startTimestamp != null) {
        try {
          result = sdf.parse(startTimestamp);
        } catch (ParseException pe) {
          log.warn("Failed to get " + Application.PROPERTY_START_TIMESTAMP);
        }
      }
    }
    return result;
  }
}
