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

package org.openadaptor.auxil.expression.function;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.expression.ExpressionException;

/**
 * Function to parse Dates supplied as <code>String</code>values.
 * <p>
 * Parses Strings as Date objects.
 * 
 * @author Andrew Shire
 * @author Eddy Higgins
 * @deprecated ScriptProcessor or ScriptFilterProcessor may be used in place of Expressions
 */

public class DateParseFn extends AbstractFunction {
  public static final String NAME = "dateparse";

  private static final Log log = LogFactory.getLog(DateParseFn.class);

  DateFormat df;

  SimpleDateFormat sdf;

  public DateParseFn() {
    super(DateParseFn.NAME, 2);
    df = DateFormat.getDateTimeInstance();
    if (df instanceof SimpleDateFormat) {
      sdf = (SimpleDateFormat) df;
    } else {
      sdf = null;
      DateParseFn.log.warn("DateFormat is not a SimpleDateFormat - format strings will be ignored");
    }
  }

  /**
   * Generate a formatted <code>String</code> object from a supplied value
   * 
   * @param args
   *          Object[] which should contain two arguments - the Object to be formatted, and the format to be applied
   *          (typically a <code>String</code>)
   * @return Object containing a <code>String</code> with the formatted value.
   * @throws ExpressionException
   *           If arguments cannot be cast appropriately
   */
  protected Object operate(Object[] args) throws ExpressionException {
    String dateString = getArgAsString(args[0], null);
    validateNotNull(dateString, 0);
    String formatString = getArgAsString(args[1], null);
    return parse(dateString, formatString);
  }

  /**
   * Parse the supplied <code>String</code> into a <code>String</code> according to the supplied format.
   * 
   * @param dateString
   *          A <code>String</code> containing a Date to be parsed.
   * @param format
   *          <code>String</code> containing format to be applied. Ignored if empty or <tt>null</tt>.
   * @return Object containing a <code>Date</code> representation of the supplied Date string.
   */
  protected Object parse(String dateString, String format) throws ExpressionException {
    try {
      Date result;
      if (sdf == null) {
        if (format != null) {
          log.warn("Formatter is not a SimpleDateFormat, unable to apply format " + format);
        }
        result = df.parse(dateString);
      } else { // We have a SimpleDateFormat
        if ((format.length() > 0) && (sdf.toPattern() != format)) {
          try {
            sdf.applyPattern(format);
          } catch (IllegalArgumentException iae) {
            DateParseFn.log.warn("SimpleDateFormat is ignoring illegal format String: " + format);
          }
        }
        result = sdf.parse(dateString);
      }
      return result;
    } catch (ParseException pe) {
      String msg = "Failed to parse supplied date :" + dateString;
      if (format != null) {
        msg += "[format= " + format + "]";
      }
      throw new ExpressionException(msg + ". Reason: " + pe.getMessage());
    }
  }
}
