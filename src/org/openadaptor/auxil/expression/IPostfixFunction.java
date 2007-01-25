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

package org.openadaptor.auxil.expression;

import java.util.Stack;

/**
 * Interface for PostFix functions (which are used by expressions).
 * <p>
 * Implementing classes should be able to perform postfix operations using operands from a (supplied) stack. The
 * resulting value will be available on the top of the stack.
 * 
 * @author Eddy Higgins
 */
public interface IPostfixFunction {
  /**
   * perform the postfix function, using arguments provided on the supplied stack.
   * 
   * @param stack
   *          Stack which contains function operands
   * @throws ExpressionException
   *           if execution fails for any reason
   */
  public void execute(Stack stack) throws ExpressionException;

  /**
   * Get the name of this function.
   * 
   * @return String containing the name of the function. Should not be <tt>null</tt> and should be unique.
   */
  public String getName();

  /**
   * Return the number of arguments expected by this function
   * 
   * @return int containing the number of arguments expected
   */
  public int getArgCount();
}
