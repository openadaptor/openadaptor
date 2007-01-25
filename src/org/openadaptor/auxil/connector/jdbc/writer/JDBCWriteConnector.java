/*
 * [[
 * Copyright (C) 2001 - 2006 The Software Conservancy as Trustee. All rights
 * reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Nothing in this notice shall be deemed to grant any rights to
 * trademarks, copyrights, patents, trade secrets or any other intellectual
 * property of the licensor or any contributor except as expressly stated
 * herein. No patent license is granted separate from the Software, for
 * code that you delete from the Software, or for combinations of the
 * Software with other software or hardware.
 * ]]
 */
package org.openadaptor.auxil.connector.jdbc.writer;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.jdbc.JDBCConnection;
import org.openadaptor.core.connector.AbstractWriteConnector;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.core.transaction.ITransactional;

/**
 * Processes data by executing PreparedStatements against a database. The conversion of the data to a prepared statement
 * is delegate to an IStatementConverter, the default is a StatementConverter (which calls toString() on data, i.e. it
 * assumes that an upstream component has converted the data to valid SQL.
 * 
 * @see IStatementConverter
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

  public void setJdbcConnection(final JDBCConnection jdbcConnection) {
    this.jdbcConnection = jdbcConnection;
  }

  public void setStatementConverter(final IStatementConverter statementConverter) {
    this.statementConverter = statementConverter;
  }

  public Object deliver(Object[] data) throws ComponentException {
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
        if (jdbcConnection.isDeadlockException(e) && jdbcConnection.incrementDeadlockCount() > 0) {
          log.warn("deadlock detected, " + jdbcConnection.getDeadlockRetriesRemaining() + " retries remaining");
        }
        throw new ComponentException("SQLException, " + e.getMessage() + ", Error Code = " + e.getErrorCode()
            + ", State = " + e.getSQLState(), e, this);
      }
    }
    return null;
  }

  public void connect() throws ComponentException {
    log.debug("Connector: [" + getId() + "] connecting ....");
    try {
      jdbcConnection.connect();
      statementConverter.initialise(jdbcConnection.getConnection());
    } catch (SQLException sqle) {
      throw new ComponentException("Failed to establish JDBC connection - " + sqle.toString(), sqle, this);
    }
    connected = true;
    log.info("Connector: [" + getId() + "] successfully connected.");
  }

  public void disconnect() throws ComponentException {
    log.debug("Connector: [" + getId() + "] disconnecting ....");

    try {
      jdbcConnection.disconnect();
    } catch (SQLException sqle) {
      throw new ComponentException("Failed to disconnect JDBC connection - " + sqle.toString(), sqle, this);
    }
    connected = false;
    log.info("Connector: [" + getId() + "] disconnected");
  }

  public Object getResource() {
    if (jdbcConnection.isTransacted()) {
      return jdbcConnection.getTransactionalResource();
    } else {
      return null;
    }
  }
}
