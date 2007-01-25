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

import org.openadaptor.auxil.expression.ExpressionException;

/**
 * Evaluate String.endsWith()
 * 
 * @author Kevin Scully
 */
public class EndsWithFn extends AbstractFunction {
  public static final String NAME = "endswith";

  // private static final Log log = LogFactory.getLog(EndsWithFn.class);

  public EndsWithFn() {
    super(NAME, 2);
  }

  /**
   * Test if first argument is a <code>String</code> which ends with the second argument, which should also be a
   * <code>String</code> ((String)args[0]).endsWith((String)args[1]) Return a Boolean Object with the result.
   * 
   * @param args
   *          Object array which is expected to hold two Strings
   * @return Boolean object with value as defined above.
   */
  protected Object operate(Object[] args) throws ExpressionException {
    String arg0 = getArgAsString(args[0], null);
    validateNotNull(arg0, 0);
    validateArg(args[1] instanceof String, 1, "Argument must be a non-null String");
    return new Boolean(arg0.endsWith((String) args[1]));
  }

}
