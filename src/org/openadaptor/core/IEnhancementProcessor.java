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
package org.openadaptor.core;

import org.openadaptor.auxil.orderedmap.IOrderedMap;

/**
 * Interface that represents an enhancement processor. An enhancement processor 
 * has access to a read connector that will query an external resource for 
 * data that enhancement processor will then use to enrich its input data.
 * 
 * @author Kris Lachor
 * @since Post 3.3
 */
public interface IEnhancementProcessor  {
  
  /**
   * Underlying read connector.
   * 
   * @return the underlying read connector.
   */
  IEnhancementReadConnector getReadConnector();
 
  /**
   * Builds a map with data that IEnhancementReadConnector will use to parameterise 
   * the query.
   *
   * @return data to parameterise IEnhancementReadConnector's query
   */
  IOrderedMap prepareParameters(Object input);
  
  /**
   * Merges original input with additional data obtained from the underlying 
   * IEnhancementReadConnector.
   * 
   * @param input original input record used to parametrise query in
   *        IEnhancementReadConnector
   * @param additionalData data returned by IEnhancementReadConnector
   */
  Object [] enhance(Object input, Object [] additionalData);
 
}
