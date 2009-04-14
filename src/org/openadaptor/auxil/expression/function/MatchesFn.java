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

import org.openadaptor.auxil.expression.ExpressionException;

/**
 * Function to implement String.matches().
 * 
 * @author Eddy higgins
 * @deprecated ScriptProcessor or ScriptFilterProcessor may be used in place of Expressions
 */
public class MatchesFn extends AbstractFunction {
  public static final String NAME = "matches";

  // private static final Log log = LogFactory.getLog(MatchesFn.class);

  public MatchesFn() {
    super(MatchesFn.NAME, 2);
  }

  /**
   * Evaluates String.matches() against a <code>String</code> regular expression argument.
   * <p>
   * It will call toString() on the first argument if it is not already a <code>String</code> If the second argument
   * is not a String, an ExpressionException will be thrown.
   * 
   * Note: This method assumes that args[] is not null and has the correct number of arguments supplied - execute()
   * should already have verified this.
   * 
   * @param args
   *          Object[] which should contain a <code>String</code> argument, followed by a <code>String</code>
   *          Regular expression.
   * @return <code>Boolean</code> indicating if the String matches the supplied expression.
   * @throws ExpressionException
   *           If argument is <tt>null</tt> or not a <code>String</code>.
   */
  protected Object operate(Object[] args) throws ExpressionException {
    String arg = getArgAsString(args[0], null);
    validateNotNull(arg, 0);

    String regex = getArgAsString(args[1], null);
    validateNotNull(regex, 1);

    return new Boolean(arg.matches(regex));
  }
}
