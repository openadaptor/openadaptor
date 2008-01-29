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

package org.openadaptor.auxil.connector.jdbc.writer.deprecated;

import org.openadaptor.core.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * trivial implementation of IStatementConverter, calls toString() on data and
 * creates a PreparedStatement with that String.
 * @author perryj
 * @deprecated Use RawSQLWriter instead
 */
public class StatementConverter implements IStatementConverter {

  public PreparedStatement convert(Object data, Connection connection) {
    try {
      return connection.prepareStatement(data.toString());
    } catch (Exception e) {
      throw new RuntimeException("failed to convert data to PreparedStatement, " + e.getMessage(), e);
    }
  }

  public void initialise(Connection connection) {
  }


  /**
   * Checks that the properties for the statement converter are valid. If any problems are found
   * then an exception is raised and added to the supplied list.
   * <p/>
   *
   * In this case, there is nothing to check as there are no properties
   *
   * @param exceptions list of exceptions that any validation errors will be appended to
   * @param comp       the component that this converter is connected to
   */
  public void validate(List exceptions, Component comp) {
  }
  
  public boolean preparedStatementIsReusable() {
    return false;
  }

}
