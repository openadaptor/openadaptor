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
 * File: $Header: /cvs/oa3/src/org/oa3/expression/function/SubstringFn.java,v 1.1 2006/11/02 14:19:58 higginse Exp $
 * Rev: $Revision: 1.1 $ Created Sep 28 2006 by Eddy Higgins
 */
import org.oa3.auxil.expression.ExpressionException;

/**
 * implements a String.substring() function.
 * <p>
 * The only substring variant it implements is substring(int,int).
 * 
 */
public class SubstringFn extends AbstractFunction {
  public static final String NAME = "substring";

  // private static final Log log = LogFactory.getLog(SubstringFn.class);

  public SubstringFn() {
    super(NAME, 3);
  }

  /**
   * Evaluates String.substring(int,int) against a <code>String</code>.
   * <p>
   * Note: This does not validate substring i
   * 
   * @param args
   *          Object[] which should contain a String, and two int arguments
   * @return String containing result of applying the substring function.
   * @throws org.oa3.expression.ExpressionException
   *           If arguments cannot be cast appropriately
   */
  protected Object operate(Object[] args) throws ExpressionException {
    String arg = getArgAsString(args[0], null);
    validateNotNull(arg, 0);
    try {
      return (arg.substring(getArgAsNumber(args, 1).intValue(), getArgAsNumber(args, 2).intValue()));
    } catch (IndexOutOfBoundsException ioobe) {
      throw new ExpressionException("Bad call to substring - " + ioobe.getMessage());
    }
  }

}
