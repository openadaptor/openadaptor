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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.jdbc.JDBCConnection;
import org.openadaptor.core.connector.AbstractWriteConnector;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.ValidationException;
import org.openadaptor.core.transaction.ITransactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Processes data by executing PreparedStatements against a database.
 * <p/>
 *
 * The conversion of the data to a prepared statement is delegate to an IStatementConverter, the
 * default is a StatementConverter which calls toString() on data (i.e. it assumes that an upstream
 * component has converted the data to valid SQL).
 *
 * @author J Perry, Russ Fennell
 *
 * @see IStatementConverter
 * @see JDBCConnection
 */
public class JDBCWriteConnector extends AbstractWriteConnector implements ITransactional {

  private static final Log log = LogFactory.getLog(JDBCWriteConnector.class.getName());


  private IStatementConverter statementConverter = new StatementConverter();
  private JDBCConnection jdbcConnection;


  public JDBCWriteConnector() {
    super();
  }

  public JDBCWriteConnector(String id) {
    super(id);
  }


  /**
   * Mandatory
   *
   * @param jdbcConnection the connection details for the database.
   */
  public void setJdbcConnection(final JDBCConnection jdbcConnection) {
    this.jdbcConnection = jdbcConnection;
  }


  /**
   * Optional. Defaults to a StatementConverter
   *
   * @param statementConverter the class that converts the incoming data into a PreparedStatement.
   */
  public void setStatementConverter(final IStatementConverter statementConverter) {
    this.statementConverter = statementConverter;
  }


  /**
   * Checks that all mandatory properties have been set. Calls validate() on the statement
   * converter to ensure that it's properties are correctly set as well. 
   *
   * @param exceptions list of exceptions that any validation errors will be appended to
   */
  public void validate(List exceptions) {
    if ( jdbcConnection == null )
      exceptions.add(new ValidationException("You must supply values for the [jdbcConnection] property", this));

    statementConverter.validate(exceptions, this);
  }


  /**
   * Executes the prepared statement for each of the records in the array.
   *
   * @param data the source of the data for the prepared statement
   *
   * @return null
   *
   * @throws ComponentException just a wrapper around any SQLExceptions that may be thrown
   * or if the jdbcConnection details have not been set
   */
  public Object deliver(Object[] data) throws ComponentException {
    if ( jdbcConnection == null )
      throw new ConnectionException("No connection details defined. You must supply values for the [jdbcConnection] property", this);

    boolean sucess = false;
    while (!sucess) {
      try {
        for (int i = 0; i < data.length; i++) {
          PreparedStatement s = statementConverter.convert(data[i], jdbcConnection.getConnection());
          s.executeUpdate();
          s.close();
        }
        sucess = true;
      } catch (SQLException e) {
        jdbcConnection.handleException(e, null);
      }
    }

    return null;
  }


  /**
   * Creates a connection to the database. If a connection already exists then we use it
   * rather than creating a new one.
   *
   * @throws ComponentException just a wrapper around a SQLException or thrown if the
   * jdbcConnection property has not been set
   */
  public void connect() throws ComponentException {
    log.debug("Connector: [" + getId() + "] connecting ....");

    if ( jdbcConnection == null )
      throw new ConnectionException("No connection details defined. You must supply values for the [jdbcConnection] property", this);

    if (!jdbcConnection.isConnected() ) {
      try {
        jdbcConnection.connect();
      } catch (SQLException e) {
        jdbcConnection.handleException(e, "Failed to establish JDBC connection");
      }
    }

    statementConverter.initialise(jdbcConnection.getConnection());

    connected = true;
    log.info("Connector: [" + getId() + "] successfully connected.");
  }


  /**
   * Closes the connectionh to the dattabase
   *
   * @throws ComponentException just a wrapper around any SQLexception that may be thrown
   */
  public void disconnect() throws ComponentException {
    log.debug("Connector: [" + getId() + "] disconnecting ....");

    if ( jdbcConnection == null ) {
      log.info("Connection already closed");
      return;
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
    if ( jdbcConnection != null && jdbcConnection.isTransacted() )
      return jdbcConnection.getTransactionalResource();

    return null;
  }
}
