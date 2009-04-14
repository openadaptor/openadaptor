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

package org.openadaptor.core.transaction;

import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.IWriteConnector;

/**
 * Should be implemented by {@link IReadConnector}s and {@link IWriteConnector}s that
 * are capable of taking part in a transaction.
 * 
 * @author perryj
 *
 */
public interface ITransactional {
  
  /**
   * If the implementation wants to take part in the current transaction then it should either
   * return an instance of {@link org.openadaptor.core.transaction.ITransactionalResource} or and instance of {@link javax.transaction.xa.XAResource}.
   * It is assumed that the configured transaction manager that creates the actual transaction
   * will match type of resource return. Return null if the component is not configured to be
   * transactional.
   * @return the output
   */
  public Object getResource();
}
