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

import org.oa3.core.connector.AbstractReadConnector;
import org.oa3.core.exception.OAException;
import org.oa3.auxil.connector.jdbc.JDBCConnection;
import org.oa3.auxil.connector.jdbc.JDBCXAConnection;
import org.oa3.auxil.orderedmap.IOrderedMap;
import org.oa3.auxil.orderedmap.OrderedHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;
/*
 * File: $Header: /cvs/oa3/src/org/oa3/connector/jdbc/JDBCListener.java,v 1.24 2006/10/18 14:31:02 ottalk Exp $
 * Rev:  $Revision: 1.24 $
 * Created Oct 20, 2005 by Eddy Higgins
 */


/**
 * This class will establish database connections via jdbc, and
 * provide records to the node to which it is attached.
 * @author Eddy Higgins
 */
public class JDBCListener extends AbstractReadConnector {

  private static final Log log = LogFactory.getLog(JDBCListener.class.getName());

  protected String sql;
  protected JDBCConnection jdbcConnection;
  protected Connection connection;
  protected ResultSet rs = null;
  protected ResultSetMetaData rsmd=null;
  protected boolean rowAvailable=false;

  protected boolean queryHasExecuted;
  protected boolean istransacted=false;
  //protected ITransactionSpec transactionspec;

  //BEGIN Bean getters/setters

  /**
   *  Set JDBC connection
   */
  public void setJdbcConnection(JDBCConnection connection) { jdbcConnection = connection; }

  /**
   *  Set sql statement to be executed
   */
  public void setSql(String sql) { this.sql = sql;}

  /**
   * Is this connector transacted
   */
  public void setIsTransacted(boolean transacted) { istransacted = transacted; }

  /**
   * Returns sql statement to be executed
   *
   * @return sql statement
   */
  public String getSql() {return sql;}

  /**
   * Returns JDBC connection
   *
   * @return JDBC connection
   */
  public JDBCConnection getJdbcConnection()     { return jdbcConnection; }

  /**
   * @return boolean, is this connector transacted
   */
  public boolean getIsTransacted() { return istransacted; }

  /**
   * Returns transaction spec
   *
   * @return transaction spec
   */
  //public ITransactionSpec getTransactionSpec() { return transactionspec; }

  //END   Bean getters/setters


  /**
   * Establish a connection to external message transport. If already
   * connected then do nothing.
   *
   * @throws OAException
   */
  public void connect() {
    log.debug("Connector: [" + getId() + "] connecting ....");
    try {
      connection = jdbcConnection.connect();
      //Attempt to set up transaction spec
      //transactionspec = createITransactionSpec();
    }
    catch (SQLException sqle) {
      throw new OAException("Failed to establish JDBC connection - " + sqle.toString(), sqle);
    }
    connected = true;
    log.info("Connector: [" + getId() + "] successfully connected.");
  }

  /**
   * Disconnect from the external message transport. If already disconnected then do nothing.
   *
   * @throws OAException
   */
  public void disconnect() {
    log.debug("Connector: [" + getId() + "] disconnecting ....");
    if (rs != null) {
      try {
        rs.close();
        connection.close();
      }
      catch (SQLException sqle) {
        log.warn(sqle.getMessage());
      }
    }
    connected = false;
    log.info("Connector: [" + getId() + "] disconnected");
  }

  //TODO: Implement
  public boolean isDry() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  //TODO: Implement
  public Object getReaderContext() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  /**
   * Get the next record.
   *
   * //ToDo: Make this capable of batching the records.
   *
   * @return  an array containing the next record to be processed.
   * @throws OAException
   */
  public Object[] nextRecord(long timeoutMs) throws OAException {
    Object[] result=null;
    try {
      //Guarantee that query has executed at least once
      if (!queryHasExecuted) {
        refreshData();
        queryHasExecuted=true;
      }
      if (rowAvailable) {
        int count = rsmd.getColumnCount();
        IOrderedMap record =new OrderedHashMap(count);
        for (int i = 1; i <= count; i++) {
          record.put(rsmd.getColumnName(i),rs.getObject(i));
        }
        result=new Object[] {record};
        rowAvailable=rs.next();//Move on.
      }
      else {
        log.debug("No more result data available");
      }
    }
    catch (SQLException sqle) {
      throw new OAException(sqle.getMessage(), sqle);
    }
    return result;
  }

  /**
   * Method called by inpoint to re-run query to fetch more data
   *
   * @throws OAException
   */
  public void refreshData() throws OAException {
    log.info("Refreshing data - executing query");
    try {
      runQuery();
    }
    catch (SQLException sqle) {
      throw new OAException(sqle.getMessage(), sqle);
    }

  }

  /**
   * Run query
   *
   * @throws SQLException
   */
  protected void runQuery() throws SQLException {
    rs = getResults(sql);
    rsmd= rs.getMetaData();

    debugDumpInfo(rsmd);
    rowAvailable=rs.next();//Have we got data?
  }

  /**
   * A debug method for outputting resultset information
   *
   * @param rsmd
   */
  protected void debugDumpInfo(ResultSetMetaData rsmd) {
    try {
      int cols = rsmd.getColumnCount();
      StringBuffer sb = new StringBuffer();
      for (int i = 1; i <= cols; i++) {
        sb.append(rsmd.getColumnClassName(i)).append(",");
      }
      log.debug(sb.deleteCharAt(sb.length() - 1).toString());
    }
    catch (SQLException sqle) {
      sqle.printStackTrace();
    }

  }

  /**
   * Create JDBC statement, call execute and return resultset
   *
   * @param sql
   * @return resultset
   * @throws SQLException
   */
  private ResultSet getResults(String sql) throws SQLException {
    Statement s = connection.createStatement();
    log.info("Executing SQL: " + sql);
    return (s.executeQuery(sql));
  }

  /**
   * If istransacted property is set, attempt to get a transaction spec from JDBC connection
   *
   * @return Transaction spec
   * @throws SQLException
   */

  //TODO: Reimplement transaction
  /*
  protected ITransactionSpec createITransactionSpec() throws SQLException {
    ITransactionSpec spec = null;
    if ( istransacted) {
      spec = new JtaTransactionSpec();
      try {
        spec.setResource(((JDBCXAConnection) getJdbcConnection()).getXaDataSource().getXAConnection().getXAResource());
      } catch (SQLException se) {
        throw new SQLException("Failed to get JTA transaction spec from JDBC connection - " + se);
      }
      spec.setTimeout(getTimeout());

    } else {
      spec = new DefaultTransactionSpec();
      spec.setTimeout(getTimeout());
    }
    return spec;

  }
  */
}
