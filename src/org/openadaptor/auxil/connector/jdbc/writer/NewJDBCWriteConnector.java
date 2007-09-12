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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.jdbc.JDBCConnection;
import org.openadaptor.core.connector.AbstractWriteConnector;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.core.exception.ValidationException;
import org.openadaptor.core.transaction.ITransactional;

/**
 * This connector writes output records to JDBC databases.
 * <br>
 * It delegates the actual writing to ISQLWriter instances.
 *
 *
 * @author higginse
 *
 * @see NewJDBCConnection
 */
public class NewJDBCWriteConnector extends AbstractWriteConnector implements ITransactional {
  private static final Log log = LogFactory.getLog(NewJDBCWriteConnector.class);

  //Provide a default writer delegate.
  private ISQLWriter sqlWriter = new RawSQLWriter();
  private JDBCConnection jdbcConnection;

  private String preambleSQL=null;
  private String postambleSQL=null;


  public NewJDBCWriteConnector() {
    super();
  }

  public NewJDBCWriteConnector(String id) {
    super(id);
  }


  /**
   * Associate this connector with a JDBCConnection instance.
   * <br>
   * This property is manadatory
   *
   * @param jdbcConnection the connection details for the database.
   */
  public void setJdbcConnection(final JDBCConnection jdbcConnection) {
    this.jdbcConnection = jdbcConnection;
  }

  /**
   * Delegate which will actually perform the writes.
   * <br>
   * Optional - defaults to an instance of SQLWriter()
   * <br>
   * This delegate will be used to write each batch of records received
   * by this connector.
   *
   * @param Writer class which converts the incoming data into a PreparedStatement.
   */
  public void setWriter(final ISQLWriter sqlWriter) {
    this.sqlWriter = sqlWriter;
  }

  /**
   * Optional SQL to be executed before connector processes messages.
   * @param sql SQL statement
   */
  public void setPreambleSQL(String sql) {
    this.preambleSQL=sql;
  }

  /**
   * Optional SQL to be executed before connector disconnects.
   * @param sql SQL statement
   */
  public void setPostambleSQL(String sql) {
    this.postambleSQL=sql;
  }

  /**
   * Checks that all mandatory properties have been set. Calls validate() on the statement
   * converter to ensure that it's properties are correctly set as well. 
   *
   * @param exceptions list of exceptions that any validation errors will be appended to
   */
  public void validate(List exceptions) {
    if ( jdbcConnection == null ) {
      exceptions.add(new ValidationException("jdbcConnection property not set", this));
    }
    sqlWriter.validate(exceptions, this);
  }

  /**
   * Write incoming array of records to a database.
   * It will use the configured ISQLWriter delegate to perform the
   * writes.
   *
   * @param data Object[] of records to be written.
   *
   * @return null
   *
   * @throws ComponentException just a wrapper around any SQLExceptions that may be thrown
   * or if the jdbcConnection details have not been set
   */
  public Object deliver(Object[] data) throws ComponentException {
    try {
      sqlWriter.writeBatch(data);
    } catch (SQLException e) {
      jdbcConnection.handleException(e, null);
    }
    return null;
  }



  /**
   * Creates a connection to the database. 
   * Reuses existing connection if one already exists. 
   *
   * @throws ComponentException just a wrapper around a SQLException or thrown if the
   * jdbcConnection property has not been set
   */
  public void connect() throws ComponentException {
    log.debug("Connector: [" + getId() + "] connecting ....");

    if (!jdbcConnection.isConnected() ) {
      try {
        jdbcConnection.connect();
      } catch (SQLException e) {
        jdbcConnection.handleException(e, "Failed to establish JDBC connection");
      }
      //Execute preamble sql if it exists...
      if (preambleSQL!=null) {
        log.debug("Executing preamble SQL: "+preambleSQL);
        executePrePostambleSQL(preambleSQL, jdbcConnection.getConnection());
      }
    }

    sqlWriter.initialise(jdbcConnection.getConnection());
    connected = true;
    log.info("Connector: [" + getId() + "] successfully connected.");
  }

  private void executePrePostambleSQL(String sql, Connection connection) {
    try {
      PreparedStatement ps=connection.prepareStatement(sql);
      ps.execute();
      ps.close();
    } catch (SQLException e) {
      jdbcConnection.handleException(e, "Failed to execute sql: "+sql);
    }
  }


  /**
   * Closes the connection to the database.
   *
   * @throws ComponentException just a wrapper around any SQLexception that may be thrown
   */
  public void disconnect() throws ComponentException {
    log.debug("Connector: [" + getId() + "] disconnecting ....");

    if ( jdbcConnection == null ) {
      log.info("Connection already closed");
      return;
    }

    //Execute postamble sql if it exists...
    if (postambleSQL!=null) {
      log.debug("Executing postamble SQL: "+postambleSQL);
      executePrePostambleSQL(postambleSQL, jdbcConnection.getConnection());
    }

    try {
      jdbcConnection.disconnect();
    } catch (SQLException e) {
      jdbcConnection.handleException(e, "Failed to disconnect JDBC connection");
    }

    connected = false;
    log.info("Connector: [" + getId() + "] disconnected");
  }


  /**
   * @return the transaction resource if the connection is transacted or null otherwise
   */
  public Object getResource() {
    if ( jdbcConnection != null && jdbcConnection.isTransacted()) {
      return jdbcConnection.getTransactionalResource();
    }

    return null;
  }
}
