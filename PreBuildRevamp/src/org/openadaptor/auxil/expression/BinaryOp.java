/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.auxil.expression;

import java.util.Date;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of basic binary postfix functions.
 * <p>
 * It expects two non-null operand Objects on the stack, and will present the result back on the stack.
 * <UL>
 * <LI>Null operands, or operands operator combinations which the class cannot handle will result in MathExceptions.
 * <LI>The class of the result depends on the operator and the incoming operand types</LI>
 * </UL>
 * 
 * @author Eddy Higgins
 */
public class BinaryOp implements IPostfixFunction {
  public static final Log log = LogFactory.getLog(BinaryOp.class);

  // Operator symbol - for debug output only
  protected final char op;

  /**
   * Return the operator corresponding to this operation. See ExpressionToken for a full list of known operators.
   * 
   * @return char representing the operator.
   */
  protected char getOp() {
    return op;
  }

  /**
   * Return the name of this function.
   * 
   * @return String version of the operator.
   */
  public String getName() {
    return new String(new char[] { getOp() });
  }

  public int getArgCount() {
    return 2; // Duh! It's a binary operator
  }

  public BinaryOp(int op) {
    this.op = (char) op;
  }

  /**
   * Executes this binary operation by taking two operands from the stack and pushing the result
   * 
   * @param stack
   *          A stack containing at least the two input operands (in reverse order)
   * @throws ExpressionException
   *           if less than two operands are supplied on the stack, or if an inappropriate operator/operand combination
   *           occurs.
   */
  public void execute(Stack stack) throws ExpressionException {
    if (stack == null) {
      throw new ExpressionException("<null> stack not permitted");
    }
    if (stack.size() > 1) {
      Object operand2 = stack.pop(); // Reverse order on the stack
      Object operand1 = stack.pop();
      Object result = operate(operand1, operand2);
      stack.push(result);
    } else {
      throw new ExpressionException("2 operands required for " + getName() + " but only " + stack.size() + " available");
    }
  }

  /**
   * Implementation of postfix function.
   * 
   * <UL>
   * <LI>Operands should be non-null Number Objects or a MathException will result.
   * <LI>The result will be either a Double or Long depending on the incoming operand types.</LI>
   * </UL>
   * 
   * @param arg1
   *          Number
   * @param arg2
   *          Number
   * @return Object with result (one of Double or Long)
   * @throws ExpressionException
   */

  protected Object operate(Object arg1, Object arg2) throws ExpressionException {
    if (arg1 == null || arg2 == null) {
      return nullOp(arg1, arg2);
      // throw new ExpressionException("Null arguments not permitted");
    }
    // Convert dates into longs for operations. It's hack-tastic!
    if (arg1 instanceof Date) { // Get the millis value.
      arg1 = new Long(((Date) arg1).getTime());
    }
    if (arg2 instanceof Date) { // Get the millis value.
      arg2 = new Long(((Date) arg2).getTime());
    }

    if ((arg1 instanceof Number) && (arg2 instanceof Number)) {// Numeric add
      if ((arg1 instanceof Double) || (arg1 instanceof Float) || (arg2 instanceof Double) || (arg2 instanceof Float)) { // Result
                                                                                                                        // will
                                                                                                                        // be a
                                                                                                                        // double
        return doubleOp(((Number) arg1).doubleValue(), ((Number) arg2).doubleValue());
      } else {// Non floating point. Treat 'em all as Longs, for now.
        // ToDO: Optimisation: Allow Integer results.
        return longOp(((Number) arg1).longValue(), ((Number) arg2).longValue());
      }
    } else { // Not numeric
      if ((arg1 instanceof Boolean) && (arg2 instanceof Boolean)) {
        return boolOp(((Boolean) arg1).booleanValue(), ((Boolean) arg2).booleanValue());
      } else {
        // if (((arg1 instanceof Date) && (arg2 instanceof Date))){
        // return dateOp((Date)arg1,(Date)arg2);
        // }
        // else { //Hmm try string
        return stringOp(arg1.toString(), arg2.toString());
        // }
      }
    }
  }

  protected Object doubleOp(double d1, double d2) throws ExpressionException {
    Object result;
    switch (op) {
    case ExpressionToken.OP_PLUS:
      result = new Double(d1 + d2);
      break;
    case ExpressionToken.OP_MINUS:
      result = new Double(d1 - d2);
      break;
    case ExpressionToken.OP_MUL:
      result = new Double(d1 * d2);
      break;
    case ExpressionToken.OP_DIV:
      if (d2 == 0) {
        throw new ExpressionException("Attempted division by zero");
      }
      result = new Double(d1 / d2);
      break;

    case ExpressionToken.OP_EQ:
      result = new Boolean(d1 == d2);
      break;
    case ExpressionToken.OP_NE:
      result = new Boolean(d1 != d2);
      break;

    case ExpressionToken.OP_GT:
      result = new Boolean(d1 > d2);
      break;

    case ExpressionToken.OP_LT:
      result = new Boolean(d1 < d2);
      break;

    case ExpressionToken.OP_LE:
      result = new Boolean(d1 <= d2);
      break;

    case ExpressionToken.OP_GE:
      result = new Boolean(d1 >= d2);
      break;

    default:
      throw new ExpressionException("Attempted unrecognised expression operation: " + op);
      // break;
    }
    return result;
  }

  protected Object longOp(long l1, long l2) throws ExpressionException {
    Object result;
    switch (op) {
    case ExpressionToken.OP_PLUS:
      result = new Long(l1 + l2);
      break;
    case ExpressionToken.OP_MINUS:
      result = new Long(l1 - l2);
      break;
    case ExpressionToken.OP_MUL:
      result = new Long(l1 * l2);
      break;
    case ExpressionToken.OP_DIV:
      if (l2 == 0) {
        throw new ExpressionException("Attempted division by zero");
      }
      result = new Long(l1 / l2);
      break;

    case ExpressionToken.OP_EQ:
      result = new Boolean(l1 == l2);
      break;
    case ExpressionToken.OP_NE:
      result = new Boolean(l1 != l2);
      break;

    case ExpressionToken.OP_GT:
      result = new Boolean(l1 > l2);
      break;

    case ExpressionToken.OP_LT:
      result = new Boolean(l1 < l2);
      break;

    case ExpressionToken.OP_LE:
      result = new Boolean(l1 <= l2);
      break;

    case ExpressionToken.OP_GE:
      result = new Boolean(l1 >= l2);
      break;

    default:
      throw new ExpressionException("Attempted unrecognised expression operation: " + op);
      // break;
    }
    return result;
  }

  protected Object boolOp(boolean b1, boolean b2) throws ExpressionException {
    boolean result;
    switch (op) {
    case ExpressionToken.OP_AND:
      result = b1 && b2;
      break;
    case ExpressionToken.OP_OR:
      result = b1 || b2;
      break;
    case ExpressionToken.OP_EQ:
      result = b1 == b2;
      break;
    case ExpressionToken.OP_NE: // Note - it's integer division.
      result = b1 != b2;
      break;
    default:
      throw new ExpressionException("Attempted unrecognised expression operation: " + op);
      // break;
    }
    return new Boolean(result);
  }

  protected Object nullOp(Object b1, Object b2) throws ExpressionException {
    boolean result;
    switch (op) {
    case ExpressionToken.OP_EQ:
      result = b1 == b2;
      break;
    case ExpressionToken.OP_NE:
      result = b1 != b2;
      break;
    default:
      throw new ExpressionException("Illegal operation for null operand: " + op);
      // break;
    }
    return new Boolean(result);
  }

  protected Object stringOp(String s1, String s2) throws ExpressionException {
    Object result;
    switch (op) {
    case ExpressionToken.OP_PLUS:
      result = s1 + s2;
      break;
    case ExpressionToken.OP_EQ:
      result = new Boolean(s1.equals(s2));
      break;
    case ExpressionToken.OP_NE:
      result = new Boolean(!s1.equals(s2));
      break;

    case ExpressionToken.OP_GT:
      result = new Boolean(s1.compareTo(s2) > 0);
      break;

    case ExpressionToken.OP_LT:
      result = new Boolean(s1.compareTo(s2) < 0);
      break;

    case ExpressionToken.OP_LE:
      result = new Boolean(s1.compareTo(s2) <= 0);
      break;

    case ExpressionToken.OP_GE:
      result = new Boolean(s1.compareTo(s2) >= 0);
      break;

    default:
      throw new ExpressionException("Unable to perform <String> " + op + " <String>");
      // break;
    }
    return result;
  }

  protected Object dateOp(Date d1, Date d2) throws ExpressionException {
    Object result;
    switch (op) {

    case ExpressionToken.OP_PLUS:
      result = new Long(d1.getTime() + d2.getTime());
      break;
    case ExpressionToken.OP_MINUS:
      result = new Long(d1.getTime() - d2.getTime());
      break;

    case ExpressionToken.OP_EQ:
      result = new Boolean(d1.equals(d2));
      break;
    case ExpressionToken.OP_NE:
      result = new Boolean(!(d1.equals(d2)));
      break;

    case ExpressionToken.OP_GT:
      result = new Boolean(d1.compareTo(d2) > 0);
      break;

    case ExpressionToken.OP_LT:
      result = new Boolean(d1.compareTo(d2) < 0);
      break;

    case ExpressionToken.OP_LE:
      result = new Boolean(d1.compareTo(d2) <= 0);
      break;

    case ExpressionToken.OP_GE:
      result = new Boolean(d1.compareTo(d2) >= 0);
      break;
    default:
      throw new ExpressionException("Attempted unrecognised expression operation: " + op);
      // break;
    }
    return result;
  }

  protected void fail(String operandType) throws ExpressionException {
    throw new ExpressionException("Operation " + getOp() + "not supported with " + operandType + " operand types");
  }
}
