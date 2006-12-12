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
package org.oa3.auxil.connector.jdbc;
/*
 * File: $Header: /cvs/oa3/src/org/oa3/connector/jdbc/JDBCWriteConnector.java,v 1.6 2006/11/16 17:01:51 ottalk Exp $
 * Rev:  $Revision: 1.6 $
 * Created Oct 22, 2006 by Kuldip Ottal
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.core.connector.AbstractWriteConnector;
import org.oa3.core.exception.ComponentException;
import org.oa3.auxil.orderedmap.IOrderedMap;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.List;

/**
 * This class has three methods of writing data
 * to a database:
 * 1 Directly to database table
 * 2 Using a stored procedure
 * 3 Using a custom sql statement
 *
 * The mechanism required is set as a property within the Spring bean configuration.
 * The class then uses a factory to provide the appropriate object and passes it the Ordered Map
 * containing the data to be written to the database.
 */
public class JDBCWriteConnector extends AbstractWriteConnector implements IJDBCConstants {

  private static final Log log = LogFactory.getLog(JDBCWriteConnector.class.getName());

  private String tableName = "";
  private String storedProcName = "";
  private String sqlStatement = "";
  private String delimiter = "$";
  private IJDBCStatement jdbcStatement;
  private JDBCConnection jdbcConnection;
  private Connection connection;
  private Map omKeyToDBTableColumnMapping;
  private List omKeyToStoredProcParameterMapping;

  //BEGIN Bean getters/setters

  /**
   * @return database table name where data will be written
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * Set database table name where data will be written
   */
  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  /**
   * @return stored procedure which will be used to write data to database
   */
  public String getStoredProcName() {
    return storedProcName;
  }

  /**
   * Set stored procedure which will be used to write data to database
   */
  public void setStoredProcName(String storedProcName) {
    this.storedProcName = storedProcName;
  }

  /**
   * @return Sql statement which will be used to write data to database
   */
  public String getSqlStatement() {
    return sqlStatement;
  }

  /**
   * Set Sql statement which will be used to write data to database
   */
  public void setSqlStatement(String sqlStatement) {
    this.sqlStatement = sqlStatement;
  }

  /**
   * @return JDBCConnection
   */
  public JDBCConnection getJdbcConnection() {
    return jdbcConnection;
  }

  /**
   * Set JDBCConnection
   */
  public void setJdbcConnection(JDBCConnection jdbcConnection) {
    this.jdbcConnection = jdbcConnection;
  }

  /**
   * @return JDBC Connection
   */
  public Connection getConnection() {
    return connection;
  }

  /**
   * Set JDBC Connection
   */
  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  /**
   * @return Delimiter character for sql statement placeholder variables
   */
  public String getDelimiter() {
    return delimiter;
  }

  /**
   * Set delimiter character for sql statement placeholder variables
   */
  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }

  /**
   * @return Map Ordered map keys to database table column mappings
   */
  public Map getOmKeyToDBTableColumnMapping() {
    return omKeyToDBTableColumnMapping;
  }

  /**
   * set ordered map keys to database table column mappings
   */
  public void setOmKeyToDBTableColumnMapping(Map omKeyToDBTableColumnMapping) {
    this.omKeyToDBTableColumnMapping = omKeyToDBTableColumnMapping;
  }

  /**
   * @return Map Ordered map keys to stored procedure parameters mappings
   */
  public List getOmKeyToStoredProcParameterMapping() {
    return omKeyToStoredProcParameterMapping;
  }

  /**
   * set ordered map keys to stored procedure parameters mappings
   */
  public void setOmKeyToStoredProcParameterMapping(List omKeyToStoredProcParameterMapping) {
    this.omKeyToStoredProcParameterMapping = omKeyToStoredProcParameterMapping;
  }

  //END   Bean getters/setters

  //BEGIN Implement methods of AbstractWriteConnector

  /**
   * Write ordered map record to database using IJDBCStatement instance
   *
   * @param records Ordered map
   * @return Object String with number of rows updated hopefully.
   * @throws org.oa3.core.exception.ComponentException
   */
  public Object deliver(Object[] records) throws ComponentException {
    String result=null;
    IOrderedMap om=null;
    //Todo: Rectify massive inefficiencies of creating a SQL statement even when we don't need to.
    int size=records.length;
    int updateCount=0;
    for (int recordIndex =0;recordIndex <size;recordIndex++) {
      Object record =records[recordIndex];
      if (record instanceof IOrderedMap) {
        om = (IOrderedMap) record;
        updateCount += jdbcStatement.executeStatement(om,connection);
      }
      else {
        throw new ComponentException("Malformed data for writer - not IOrderedMap", this);
      }
    }
    result= updateCount + " rows updated";
    log.info(result);
    return result;
  }

  /**
   * Set up JDBC connection and transaction spec
   */
  public void connect() throws ComponentException{
    log.debug("Connector: [" + getId() + "] connecting ....");
    try {
      connection = jdbcConnection.connect();
      //transactionspec = createITransactionSpec();
      configureWriteMechanism();
    }
    catch (SQLException sqle) {
      throw new ComponentException("Failed to establish JDBC connection - " + sqle.toString(), sqle, this);
    }
    connected = true;
    log.info("Connector: [" + getId() + "] successfully connected.");
  }

  /**
   * This method ensures only one write mechanism is configure,
   * it then requests a IJDBCStatement object from the JDBCStatement factory.
   *
   * @throws ComponentException
   */
  private void configureWriteMechanism() throws ComponentException {
    String writeMechanism="";

    if (!(tableName.equals("")) && storedProcName.equals("") && sqlStatement.equals("")) {
      writeMechanism=DATABASE_TABLE;
      jdbcStatement = JDBCStatementFactory.createStatement(writeMechanism, tableName, delimiter, omKeyToDBTableColumnMapping, connection);
    } else if (tableName.equals("") && !(storedProcName.equals("")) && sqlStatement.equals("")) {
      writeMechanism=STORED_PROCEDURE;
      jdbcStatement = JDBCStatementFactory.createStatement(writeMechanism, storedProcName, delimiter, omKeyToStoredProcParameterMapping, connection);
    } else if (tableName.equals("") && storedProcName.equals("") && !(sqlStatement.equals(""))) {
      writeMechanism=SQL_STATEMENT;
      jdbcStatement = JDBCStatementFactory.createStatement(writeMechanism, sqlStatement, delimiter, null, connection);
    } else {
      throw new ComponentException("No valid write mechanism, or more than one write mechanism configured", this);
    }
  }


  /**
   * Disconnect from the external message transport. If already disconnected then do nothing.
   *
   * @throws ComponentException
   */
  public void disconnect() throws ComponentException {
    log.debug("Connector: [" + getId() + "] disconnecting ....");
    if (connection != null) {
      try {
        connection.close();
      }
      catch (SQLException sqle) {
        log.warn(sqle.getMessage());
      }
    }
    connected = false;
    log.info("Connector: [" + getId() + "] disconnected");
  }
  //END Implement methods of AbstractWriteConnector

}
