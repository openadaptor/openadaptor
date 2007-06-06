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

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.logging.Log;
import org.openadaptor.auxil.connector.jdbc.JDBCConnection;
import org.openadaptor.auxil.connector.jdbc.reader.orderedmap.ResultSetConverter;
import org.openadaptor.core.Component;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.core.transaction.ITransactional;

/**
 * Abstract readConnector for JDBC.
 * It associates a ResultSetConvertor with the connector.
 * By default, this is DEFAULT_CONVERTOR.
 * 
 * @see AbstractResultSetConverter
 * @author Eddy Higgins
 * @author perryj
 */

/**
 * 
 * @author higginse
 *
 */
public abstract class AbstractJDBCReadConnector extends Component implements IReadConnector, ITransactional {

  private static AbstractResultSetConverter DEFAULT_CONVERTER = new ResultSetConverter();
  
  private JDBCConnection jdbcConnection;
  private AbstractResultSetConverter resultSetConverter = DEFAULT_CONVERTER;

  public AbstractJDBCReadConnector() {
    super();
  }

  public AbstractJDBCReadConnector(String id) {
    super(id);
  }

  public void setJdbcConnection(JDBCConnection connection) {
    jdbcConnection = connection;
  }

  public void setResultSetConverter(AbstractResultSetConverter resultSetConverter) {
    this.resultSetConverter = resultSetConverter;
  }

  public void connect() {
    try {
      jdbcConnection.connect();
    } catch (SQLException e) {
      handleException(e, "Failed to establish JDBC connection");
    }
  }

  public void disconnect() throws ComponentException {
    try {
      jdbcConnection.disconnect();
    } catch (SQLException e) {
      handleException(e, "Failed to disconnect JDBC connection");
    }
  }

  public boolean isDry() {
    return false;
  }

  public Object getReaderContext() {
    return null;
  }

  public void validate(List exceptions) {
  }
  
  public Object getResource() {
    if (jdbcConnection.isTransacted()) {
      return jdbcConnection.getTransactionalResource();
    } else {
      return null;
    }
  }

  protected Object convertNext(ResultSet rs) throws SQLException {
    return resultSetConverter.convertNext(rs);
  }
  
  protected Object[] convertAll(ResultSet rs) throws SQLException {
    return resultSetConverter.convertAll(rs);
  }
  
  protected CallableStatement prepareCall(String sql) throws SQLException {
    return jdbcConnection.getConnection().prepareCall(sql);
  }
  
  protected Statement createStatement() throws SQLException {
    return jdbcConnection.getConnection().createStatement();
  }
  
  protected void checkWarnings(Log log, ResultSet rs) throws SQLException {
    SQLWarning warn = rs.getWarnings();
    if (warn != null) {
      checkWarnings(log, warn);
    }
  }
  
  protected void checkWarnings(Log log, Statement s) throws SQLException {
    SQLWarning warn = s.getWarnings();
    if (warn != null) {
      checkWarnings(log, warn);
    }
  }
  
  private  void checkWarnings(Log log, SQLWarning warning) {
    while (warning != null) {
      log.warn("SQLWarning Message: " + warning.getMessage()
        + " SQLState: " + warning.getSQLState()
        + " Vendor error code: " + warning.getErrorCode());
      warning = warning.getNextWarning();
    }
  }
  
  protected void handleException(SQLException e, String message) {
    jdbcConnection.handleException(e, message);
  }

  protected void handleException(SQLException e) {
    jdbcConnection.handleException(e, null);
  }


}
