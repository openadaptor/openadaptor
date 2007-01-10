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
package org.oa3.auxil.expression.function;

/*
 * File: $Header: /cvs/oa3/src/org/oa3/expression/function/FormatFn.java,v 1.2 2006/11/02 17:23:54 higginse Exp $ Rev:
 * $Revision: 1.2 $ Created Oct 26 2006 by Eddy Higgins
 */
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.auxil.expression.ExpressionException;

/**
 * Formatting functions.
 * <p>
 * Currently supported functions:
 * <UL>
 * <LI>Date to String (using a supplied format String as per <code>java.text.DateFormat</code></LI>
 * </UL>
 * 
 * @author Eddy Higgins
 * @author Andrew Shire
 */
public class FormatFn extends AbstractFunction {
  public static final String NAME = "format";

  private static final Log log = LogFactory.getLog(FormatFn.class);

  DateFormat df;

  SimpleDateFormat sdf;

  public FormatFn() {
    super(NAME, 2);
    df = DateFormat.getDateTimeInstance();
    if (df instanceof SimpleDateFormat) {
      sdf = (SimpleDateFormat) df;
    } else {
      sdf = null;
      log.warn("DateFormat is not a SimpleDateFormat - format strings will be ignored");
    }
  }

  /**
   * Generate a formatted <code>String</code> object from a supplied value
   * 
   * @param args
   *          Object[] which should contain two arguments - the Object to be formatted, and the format to be applied
   *          (typically a <code>String</code>)
   * @return Object containing a <code>String</code> with the formatted value.
   * @throws org.oa3.expression.ExpressionException
   *           If arguments cannot be cast appropriately
   */
  protected Object operate(Object[] args) throws ExpressionException {
    try {
      Object arg0 = args[0];
      if (arg0 instanceof Date) { // Format a Date as a String, using the supplied Format.
        return format((Date) arg0, getArgAsString(args[1], ""));
      }
      String badClass = (arg0 == null) ? "<null>" : arg0.getClass().getName();
      throw new ExpressionException("Unable to format " + badClass);

    } catch (ClassCastException cce) {
      throw new ExpressionException("Invalid argument(s) to " + getName() + ". " + cce.getMessage());
    }
  }

  /**
   * Format the supplied <code>Date</code> according to the supplied format.
   * 
   * @param date
   *          The <code>Date</code> to be formatted.
   * @param format
   *          <code>String</code> containing format to be applied. Ignored if empty. Must not be <tt>null</tt>.
   * @return Object containing a String representation of the supplied Date, according to the supplied format.
   */
  protected Object format(Date date, String format) {
    String result;
    if (sdf == null) {
      if (format != null) {
        log.warn("Formatter is not a SimpleDateFormat, unable to apply format " + format);
      }
      result = df.format(date);
    } else { // We have a SimpleDateFormat
      if ((format.length() > 0) && (sdf.toPattern() != format)) {
        try {
          sdf.applyPattern(format);
        } catch (IllegalArgumentException iae) {
          log.warn("SimpleDateFormat is ignoring illegal format String: " + format);
        }
      }
      result = sdf.format(date);
    }
    return result;
  }
}
