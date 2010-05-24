/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

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
 * Function to implement Math.round().
 * @deprecated ScriptProcessor or ScriptFilterProcessor may be used in place of Expressions
 */
public class RoundFn extends AbstractFunction {
  public static final String NAME = "round";

  // private static final Log log = LogFactory.getLog(RoundFn.class);

  public RoundFn() {
    super(NAME, 1);
  }

  /**
   * Evaluates Math.round() against a numeric argument.
   * 
   * @param args
   *          Object[] which should contain a single numeric argument
   * @return Double containing result of applying Math.floor() to argument.
   * @throws ExpressionException
   *           If argument is null or non-numeric.
   */
  protected Object operate(Object[] args) throws ExpressionException {
    return new Long(Math.round(getArgAsNumber(args, 0).doubleValue()));
  }
}
