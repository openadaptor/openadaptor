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

package org.openadaptor.auxil.connector.jdbc.writer;

import org.openadaptor.core.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * Converts some data into a PreparedStatement.
 *
 * @author perryj
 *
 * @see JDBCWriteConnector
 */
public interface IStatementConverter {

  /**
   * Do any initialisation required. Called with a valid connection once before convert is called.
   * Calling this method again should reset and state.
   * @param connection
   */
  void initialise(Connection connection);

  /**
   * Convert data into a fully resolved PreparedStatement
   * @param data
   * @param connection
   * @return fully resolved PreparedStatement
   */
  PreparedStatement convert(Object data, Connection connection);


  /**
   * Checks that the properties for the statement converter are valid. If any problems are found
   * then an exception is raised and added to the supplied list.
   *
   * @param exceptions list of exceptions that any validation errors will be appended to
   * @param comp the component that this converter is connected to. We need to pass this as any
   * exceptions raised require it as part of their constructor!
   */
  public void validate(List exceptions, Component comp);
  
  /**
   * Indicates whether returned PreparedStatement (from convert) can be reused multiple times.
   * Default is false.
   * It should return true if, for example a prepared statement is to be reused multiple times.
   * @return false if ps must be closed each time
   */
  public boolean preparedStatementIsReusable();
}
