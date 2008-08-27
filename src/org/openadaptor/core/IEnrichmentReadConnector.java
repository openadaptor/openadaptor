/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.core;

import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.exception.ConnectionException;

/**
 * Interface of an IReadConnector that can be plugged-in to an {@link IEnrichmentProcessor}.
 * Essentially, the only difference between a plain {@link IReadConnector} is this 
 * connector should also be able to use parameters passed from the {@link IEnrichmentProcessor}.
 * 
 * @author Kris Lachor
 * @since 3.4
 * @see IEnrichmentProcessor
 */
public interface IEnrichmentReadConnector extends IReadConnector {
  
  /**
   * Queries the underlying resource using additional parameters. 
   * Exceptions should be thrown as RuntimeExceptions.
   * Implementation that are {@link IComponent}s should throw {@link ConnectionException}s.
   * 
   * @param inputParameters that this connector can use to adjust the query to the underlying
   *        resource
   * @param timeout the maximum time in milli-seconds to wait for data is none is
   *          available immediately
   * @return null or an array of data with one or more element.
   * @see IReadConnector#next(long)
   * @see IEnrichmentProcessor
   */
   Object[] next(IOrderedMap inputParameters, long timeout);

}
