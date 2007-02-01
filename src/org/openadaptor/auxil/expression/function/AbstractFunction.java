/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved. 
                                                                                     
 Permission is hereby granted, free of charge, to any person obtaining a             
 copy of this software and associated documentation files (the                       
 "Software"), to deal in the Software without restriction, including               
 without limitation the rights to use, copy, modify, merge, publish,                 
 distribute, sublicense, and/or sell copies of the Software, and to                  
 permit persons to whom the Software is furnished to do so, subject to               
 the following conditions:                                                           
                                                                                     
 The above copyright notice and this permission notice shall be included             
 in all copies or substantial portions of the Software.                              
                                                                                     
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS           
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                          
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                               
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE              
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION              
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION               
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                     
                                                                                     
 Nothing in this notice shall be deemed to grant any rights to                       
 trademarks, copyrights, patents, trade secrets or any other intellectual            
 property of the licensor or any contributor except as expressly stated              
 herein. No patent license is granted separate from the Software, for                
 code that you delete from the Software, or for combinations of the                  
 Software with other software or hardware.                                           
*/

package org.openadaptor.auxil.expression.function;

import java.util.Stack;

import org.openadaptor.auxil.expression.ExpressionException;
import org.openadaptor.auxil.expression.IPostfixFunction;

/**
 * Abstract base class for function implementations.
 * <p>
 * This base class will take the required number of arguments from the stack, execute the functions (operate method) and
 * put the result back on the stack.
 * 
 * @author Eddy Higgins
 */
public abstract class AbstractFunction implements IPostfixFunction {

  // private static final Log log = LogFactory.getLog(AbstractFunction.class);

  protected final String name;

  protected final int argCount;

  /**
   * Return the operator corresponding to this operation. See ExpressionToken for a full list of known operators.
   * 
   * @return char representing the operator.
   */
  public String getName() {
    return name;
  }

  public int getArgCount() {
    return argCount;
  }

  public AbstractFunction(String name, int argCount) {
    this.name = name;
    this.argCount = argCount;
  }

  // ToDo: Catch Casting exceptions here as common code perhaps - currently most subclases are overly optimistic.
  /**
   * Execute a function, taking the required number of arguments from the stack, and pushing the result back onto the
   * stack.
   * 
   * @param stack
   *          <code>Stack</code> containing at least the required number of input arguments (in reverse order).
   * @throws org.openadaptor.expression.ExpressionException
   *           if incorrect number of arguments are supplied on the stack.
   */
  public void execute(Stack stack) throws ExpressionException {
    if (stack == null) {
      throw new ExpressionException("<null> stack not permitted");
    }
    Object[] args = new Object[argCount];
    if (stack.size() >= argCount) {
      for (int i = argCount - 1; i >= 0; i--) {
        args[i] = stack.pop(); // Reverse order on the stack
      }
      Object result = operate(args);
      stack.push(result);
    } else {
      throw new ExpressionException(argCount + " operands required but only " + stack.size() + " available");
    }
  }

  /**
   * Execute the sub-class function implementation.
   * <p>
   * 
   * @param args
   *          Object[] containing the arguments to the function.
   * @return Object containing the result of the function.
   * @throws ExpressionException
   *           if the function cannot be executes for some reason.
   */
  protected abstract Object operate(Object[] args) throws ExpressionException;

  /**
   * Extract a <code>String</code> value from an <code>Object</code>.
   * <p>
   * This will cast the Object to a <code>String</code> if possible. If not, it will return the result of calling
   * toString() on it, unless it is <tt>null</tt> in which case the valueIfNull value is returned.
   * 
   * @param arg
   *          Object containing the value
   * @param valueIfNull
   *          value to return if Object is null.
   * @return String with value or default if arg was null.
   */
  protected static String getArgAsString(Object arg, String valueIfNull) {
    String result = valueIfNull;
    if (arg != null) {
      result = (arg instanceof String) ? (String) arg : arg.toString();
    }
    return result;
  }

  protected void validateArg(boolean passCondition, int argIndex, String reason) throws ExpressionException {
    if (!passCondition) {
      throw new ExpressionException("Function " + getName() + "failed on argument " + argIndex + ": " + reason);
    }
  }

  protected void validateNotNull(Object o, int argIndex) throws ExpressionException {
    validateArg(o != null, argIndex, "Argument may not be null");
  }

  /**
   * Extract a numeric argument.
   * <p>
   * 
   * Note: Does not check array boundary.
   * 
   * @param args
   *          Object array containing arguments
   * @param index
   *          The index of the argument to be extracted
   * @return <code>Number</code> containing the argument as a <code>Number</code>
   * @throws ExpressionException
   *           if argument is not an instance of <code>Number</code>
   */
  protected Number getArgAsNumber(Object[] args, int index) throws ExpressionException {
    Object arg = args[index];
    if ((arg != null) && (arg instanceof Number)) {
      return (Number) arg;
    }
    throw new ExpressionException("Function " + getName() + "failed on argument " + index
        + ": Argument must be numeric");
  }
}
