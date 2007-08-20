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

import org.openadaptor.core.IReadConnector;

/*
* Interface for polling strategies. A polling strategy will define an algorithm 
* for polling the underlying resources by read connectors. The simplest strategy might be for
* the read connector to execute one call for data and exit, a more complex one might involve
* multimple calls with a specified time interval in between, with the interval perhaps 
* different for when the previous call returned data and when it did not return.
*/

/**
 * Interface for a read connector that polls. 
 * 
 * @author Kris Lachor
 */
public interface IPollingReadConnector extends IReadConnector {
  
  /**
   * @return a polling strategy
   */
  IReadConnector getDelegate();
  
//  /**
//   * Allows an implementation to provide some context that may be used to 
//   * fetch or enrich the data the reader is providing. This is typically 
//   * used by readers or polling strategies that wrap the protocol specific readers.
//   * 
//   * @param some data that relates can be used by the connector to construct queries
//   *        or enrich returned data.
//   */
//  void setReaderConext(Object context);
  
  //
  // might need a variable (enum) that states if we need the all result set in one call, 
  // only one row - or perhaps something custom
  //
  int CONVERT_NEXT_ONLY = 0;
  int CONVERT_ALL = 1;
  int CONVERT_CUSTOM = 2;
  
  /**
   * @return the underlying polling read connector.
   */
  IReadConnector getReadConnector();
  
  void setDelegate(IReadConnector readConnector);
    
  // will return one of the convert enum values at the top.
  int getConvertMode();

}
