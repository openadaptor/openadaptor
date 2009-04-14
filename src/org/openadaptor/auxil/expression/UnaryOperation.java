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

package org.openadaptor.auxil.expression;

import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of several unary postfix function.
 * <p>
 * It expects one non-null operand Objects on the stack, and will present the result back on the stack.
 * <UL>
 * <LI>Null operand, or operands which the implementing class cannot handle will result in MathExceptions.
 * <LI>The class of the result depends on the incoming operand type, and the implementing class</LI>
 * </UL>
 * 
 * @author Eddy Higgins
 * @deprecated ScriptProcessor or ScriptFilterProcessor may be used in place of Expressions
 */

public class UnaryOperation implements IPostfixFunction {
  private static final Log log = LogFactory.getLog(UnaryOperation.class);

  protected final char op;

  // Operator symbol - for debug output only
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
    return 1; // Duh! it's a unary operator
  }

  /**
   * Create a unary operation for the supplied operator.
   * <p>
   * Currently recognised operators are:
   * <UL>
   * <LI>ExpressionToken.OP_NOT</LI>
   * <LI>ExpressionToken.OP_PLUS</LI>
   * <LI>ExpressionToken.OP_MINUS</LI>
   * </UL>
   * 
   * @param op
   *          int representing the operation.
   */
  public UnaryOperation(int op) {
    this.op = (char) op;
  }

  /**
   * Execute a unary operation.
   * <p>
   * This will take the top operand on the stack and call operate() with it. The result will then be pushed back on the
   * stack.
   * 
   * @param stack
   *          <code>Stack</code> containing at least one item.
   * @throws ExpressionException
   *           if the Stack doesn't have an item, or if operate() fails.
   */
  public void execute(Stack stack) throws ExpressionException {
    if (stack == null) {
      throw new ExpressionException("<null> stack not permitted");
    }
    if (stack.size() > 0) {
      Object operand1 = stack.pop(); // Reverse order on the stack
      Object result = operate(operand1);
      UnaryOperation.log.debug(getOp() + " " + operand1 + "=" + result);
      stack.push(result);
    } else {
      throw new ExpressionException("Operand required but none available");
    }
  }

  /**
   * Perform a unary operation on the supplied argument.
   * <p>
   * It will return the result of the opearation. <br>
   * If the operator is unrecognised, an ExpressionException will be raised.
   * 
   * @param arg1
   *          <code>Object</code> argument to the expression.
   * @return <code>Object</code> containing the result of the operation.
   * @throws ExpressionException
   *           if the operator is not recognised or if the operation cannot be performed.
   */
  protected Object operate(Object arg1) throws ExpressionException {
    Object result;
    switch (op) {
    case ExpressionToken.OP_NOT:
      result = not(arg1);
      break;
    case ExpressionToken.OP_PLUS:
      result = arg1;
      break;
    case ExpressionToken.OP_MINUS:
      result = unaryMinus(arg1);
      break;
    /*
     * Replaced by functions instead case ExpressionToken.OP_LOWER: result=
     * arg1==null?null:arg1.toString().toLowerCase(); break; case ExpressionToken.OP_UPPER: result=
     * arg1==null?null:arg1.toString().toUpperCase(); break;
     */
    default:
      throw new ExpressionException("Attempted unrecognised unary operation: " + op);
      // break;
    }
    return result;
  }

  /**
   * return logical 'not' on supplied argument.
   * <p>
   * Argument must be a non-null <code>Boolean</code> instance or an ExpressionException will be thrown.
   * 
   * @param arg1
   *          <code>Object</code> which should containin a <code>Boolean</code> instance
   * @return Boolean value containing the result.
   * @throws ExpressionException
   *           If argument is <tt>null</tt> or does not contain a <code>Boolean</code>
   */
  protected Boolean not(Object arg1) throws ExpressionException {
    if ((arg1 != null) && (arg1 instanceof Boolean)) {
      return new Boolean(!((Boolean) (arg1)).booleanValue());
    }
    throw new ExpressionException("argument to " + op + " must be Boolean");
  }

  /**
   * return unary minus of supplied argument.
   * <p>
   * If argument is a <code>Double</code> or <code>Float</code>, then a <code>Double</code> will be returned.
   * Otherwise a <code>Long</code> will be returned. Argument must be a non-null <code>Number</code> instance or an
   * ExpressionException will be thrown.
   * 
   * @param arg1
   *          <code>Object</code> which should containin a <code>Number</code> instance
   * @return <code>Double</code> or <code>Long</code> value containing the result.
   * @throws ExpressionException
   *           If argument is <tt>null</tt> or does not contain a <code>Number</code>
   */
  protected Object unaryMinus(Object arg1) throws ExpressionException {
    if ((arg1 != null) && (arg1 instanceof Number)) {// We are in business
      if ((arg1 instanceof Double) || (arg1 instanceof Float)) {
        return new Double(-((Number) arg1).doubleValue());
      } else {
        return new Long(-((Number) arg1).longValue());
      }
    }
    throw new ExpressionException("argument to " + op + " must be a Number");
  }
}
