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
 * File: $Header: /cvs/oa3/src/org/oa3/connector/jdbc/JDBCDataSourceConnection.java,v 1.5 2006/10/18 14:31:02 ottalk Exp $
 * Rev:  $Revision: 1.5 $
 * Created May 25, 2006 by oa3 Core Team
 */

import org.oa3.auxil.connector.jdbc.JDBCConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import javax.transaction.xa.XAResource;
import java.sql.Connection;
import java.sql.SQLException;


/**
 * This bean is used to return a jdbc connection from a JDBC Datasource connection
 * @author Kuldip Ottal
 *
 */
public class JDBCDataSourceConnection extends JDBCConnection {

  private static final Log log = LogFactory.getLog(JDBCDataSourceConnection.class.getName());

  private DataSource dataSource;

  public JDBCDataSourceConnection(String driver, String url, String username, String password) {
    this();
    setDriver(driver);
    setUrl(url);
    setUsername(username);
    setPassword(password);
  }

  /**
   * Default constructor
   */
  public JDBCDataSourceConnection() {} //No-arg constructor for beans


  /**
   * Returns JDBC datasource
   *
   * @return JDBC datasource
   */
  public DataSource getDataSource() {
    return dataSource;
  }

  /**
   * set JDBC datasource
   */
  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * Get JDBC connection from JDBC datasource which has been configure within bean
   *
   * @return JDBC connection
   * @throws SQLException
   */
  public Connection connect() throws SQLException {
    return getConnection(dataSource);
  }

  /**
   * Get JDBC connection from JDBC datasource supplied as parameter
   *
   * @param dataSource
   * @return JDBC connection
   * @throws SQLException
   */
  public Connection getConnection(DataSource dataSource) throws SQLException {
    Connection  connection = dataSource.getConnection();
    return connection;
  }

  /**
   * Placeholder method
   *
   * @param connection
   * @return XAResource
   * @throws SQLException
   */
  public XAResource getXAResource(Connection connection) throws SQLException {
    //TODO: Investigate where this method is used
    return null;
  }
}
