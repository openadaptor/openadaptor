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

package org.openadaptor.auxil.connector.jdbc.reader;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.util.JDBCUtil;


/**
 * Connects to a database and runs a fixed query. The fixed query is run the
 * first time next is called and each subsequent time next() is called it
 * returns a single row of the ResultSet converted by the ResultSetConverter.
 * Becomes "dry" when ResultSet is finished.
 * 
 * @see AbstractResultSetConverter
 * @author Eddy Higgins
 * @author perryj
 * @deprecated use JDBCPollingReadConnector instead.
 * @todo candidate for deletion (replaced with JDBCPollingReadConnector.)
 */

public class OldJDBCReadConnector extends AbstractJDBCReadConnector {

//  private static final Log log = LogFactory.getLog(JDBCReadConnector.class.getName());

  protected String sql;
  protected Statement statement = null;
  protected ResultSet rs = null;
  protected ResultSetMetaData rsmd = null;
  protected boolean dry = false;


  /**
   * Default constructor
   */
  public OldJDBCReadConnector() {
    super();
  }
  
  public OldJDBCReadConnector(String id) {
    super(id);
  }

  /**
   * Set sql statement to be executed
   *
   * @param sql
   */
  public void setSql(final String sql) { 
    this.sql = sql;
  }

  /**
   * Set up connection to database
   *
   */
  public void connect() {
    super.connect();
    try {
      statement = createStatement();
    } catch (SQLException e) {
      handleException(e, "failed to create JDBC statement");
    }
  }

  /**
   * Disconnect JDBC connection
   *
   * @throws ComponentException
   */
  public void disconnect() throws ComponentException {
    JDBCUtil.closeNoThrow(statement);
    super.disconnect();
  }

  /**
   * Inpoint has no more data
   *
   * @return boolean  true if there is no more input data
   */
  public boolean isDry() {
    return dry;
  }

  /**
   * Returns array of objects extracted from resultset. Executes the fixed
   * query the first time it is called.
   *
   * @param timeoutMs Ignored as this implementation is non-blocking.
   * @return Object[] array of objects from resultset
   * @throws ComponentException
   */
  public Object[] next(long timeoutMs) throws ComponentException {
    try {
      if (rs == null) { rs = statement.executeQuery(sql); }
      Object data = convertNext(rs);
      if (data != null) {
        return new Object[] {data};
      } else {
        JDBCUtil.closeNoThrow(rs);
        rs = null;
        dry = true;
       }
    }
    catch (SQLException e) {
      handleException(e);
    }
    return null;
  }

}
