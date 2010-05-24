/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.auxil.connector.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.openadaptor.core.Component;
import org.openadaptor.core.exception.ValidationException;
import org.openadaptor.core.transaction.ITransactional;

/**
 * Abstract class for code common to both {@link org.openadaptor.auxil.connector.jdbc.reader.JDBCReadConnector JDBCReadConnector} 
 * and {@link org.openadaptor.auxil.connector.jdbc.writer.JDBCWriteConnector JDBCWriteConnector}.
 * 
 * @author Kris Lachor
 * @author Eddy Higgins
 */
public abstract class AbstractJDBCConnector extends Component implements ITransactional{

  protected JDBCConnection jdbcConnection;
  
  /* 
   * SQL that, if set, will get executed right after the connection is established and right before the
   * disconnection, respectively.
   */
  protected String afterConnectSql=null;
  protected String beforeDisconnectSql=null;
  
  /**
   * Default constructor.
   */
  public AbstractJDBCConnector() {
    super();
  }

  /**
   * Constructor.
   * 
   * @param id component id.
   */
  public AbstractJDBCConnector(String id) {
    super(id);
  }

  /**
   * Associate this connector with a JDBCConnection instance.
   * <br>
   * This property is manadatory
   *
   * @param connection the connection details for the database.
   */
  public void setJdbcConnection(final JDBCConnection connection) {
    jdbcConnection = connection;
  }
  
  /**
   * Optional SQL to be executed right after the connector has extablished physical connection.
   * @param sql SQL statement
   */
  public void setAfterConnectSql(String sql) {
    this.afterConnectSql=sql;
  }

  /**
   * Optional SQL to be executed before connector disconnects.
   * @param sql SQL statement
   */
  public void setBeforeDisconnectSql(String sql) {
    this.beforeDisconnectSql=sql;
  }
  
  /**
   * Optional.
   * @param sql SQL statement
   * @deprecated use {@link #setAfterConnectSql(String)} instead.
   */
  public void setPreambleSQL(String sql) {
    this.afterConnectSql=sql;
  }

  /**
   * Optional.
   * @param sql SQL statement
   * @deprecated use {@link #setBeforeDisconnectSql(String)} instead.
   */
  public void setPostambleSQL(String sql) {
    this.beforeDisconnectSql=sql;
  }
  
  /**
   * Just a utility method to execute an SQL statement.
   * 
   * @param sql - an SQL statement
   * @param connection an established conenction
   */
  protected void executePrePostambleSQL(String sql, Connection connection) {
    try {
      PreparedStatement ps=connection.prepareStatement(sql);
      ps.execute();
      ps.close();
    } catch (SQLException e) {
      jdbcConnection.handleException(e, "Failed to execute sql: "+sql);
    }
  }
  
  /**
   * @return the transaction resource if the connection is transacted or null otherwise
   * @see ITransactional#getResource()
   * @see JDBCConnection#getTransactionalResource()
   */
  public Object getResource() {
    if(jdbcConnection != null && jdbcConnection.isTransacted()) {
      return jdbcConnection.getTransactionalResource();
    }
    return null;
  }
  
  /**
   * Checks that the mandatory properties have been set.
   * 
   * @param exceptions list of exceptions that any validation errors will be appended to
   */
  public void validate(List exceptions) {
    if (jdbcConnection == null) {
      exceptions.add(new ValidationException("[jdbcConnection] property not set. " 
          + "Please supply an instance of " + JDBCConnection.class.getName(), this));   
    } else {
      jdbcConnection.validate(exceptions);
    }
  }
  
}
