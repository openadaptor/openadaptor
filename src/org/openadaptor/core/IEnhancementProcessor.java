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
 * Draft of the new interface that represents a generic
 * enhancement processor. Not quite ready for use yet.
 * 
 * @author Kris Lachor
 * @since Post 3.3
 * @TODO consider this not extending IDataProcessor (process method not needed)
 */
//public interface IEnhancementProcessor extends IDataProcessor {
  public interface IEnhancementProcessor  {
  
  /**
   * Underlying read connector.
   * 
   * @return the underlying read connector.
   */
  IEnhancementReadConnector getReadConnector();
  
  /**
   * Merges original input with additional data retrieved by the underlying IEnhancementReadConnector.
   * 
   * @param input original input record used to parametrise query in IEnhancementReadConnector
   * @param additionalData data returned by IEnhancementReadConnector
   * @TODO rename to 'process'
   */
  Object [] enhance(Object input, Object [] additionalData);

  /**
   * Builds a map with data that IEnhancementReadConnector will use to parametrise the query.
   *
   * @return data to parametrise IEnhancementReadConnector query
   */
  IOrderedMap prepareParameters(Object input);
  
}