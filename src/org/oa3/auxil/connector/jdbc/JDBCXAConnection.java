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
 * File: $Header: /cvs/oa3/src/org/oa3/connector/jdbc/JDBCXAConnection.java,v 1.6 2006/10/18 14:31:02 ottalk Exp $
 * Rev:  $Revision: 1.6 $
 * Created Apr 26, 2006 by Kuldip Ottal
 */

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;

/**
 *
 * @author Kuldip Ottal
 */
public class JDBCXAConnection extends JDBCConnection{

  //private static final Log log = LogFactory.getLog(JDBCWriter.class.getName());

  private XADataSource xaDataSource;

  public JDBCXAConnection(String driver, String url, String username, String password) {
    this();
    setDriver(driver);
    setUrl(url);
    setUsername(username);
    setPassword(password);
  }

  //No-arg constructor for beans
  public JDBCXAConnection() {}

  /**
   * Returns XADataSource
   *
   * @return XADataSource
   */
  public XADataSource getXaDataSource() {
    return xaDataSource;
  }

  /**
   * set XADataSource
   */
  public void setXaDataSource(XADataSource xaDataSource) {
    this.xaDataSource = xaDataSource;
  }

  /**
   * Obtain JDBC connection from XADataSource
   *
   * @return JDBC connection
   * @throws SQLException
   */
  public Connection connect() throws SQLException {
    Connection connection = null;
    XAConnection xaConnection = getXAConnection(xaDataSource);
    connection = xaConnection.getConnection();
    return connection;
  }

  /**
   * Returns JDBC XAConnection from specified XADataSource
   *
   * @param xaDataSource
   * @return XAConnection
   * @throws SQLException
   */
  public XAConnection getXAConnection(XADataSource xaDataSource) throws SQLException {
    XAConnection  xaConnection = xaDataSource.getXAConnection();
    return xaConnection;
  }

  /**
   * Returns XAResource from specified XAConnection
   *
   * @param xaConnection
   * @return XAResource
   * @throws SQLException
   */
  public XAResource getXAResource(XAConnection xaConnection) throws SQLException {
    try {
      return xaConnection.getXAResource();
    } catch (SQLException se) {
      throw new SQLException("Unable to get XA Resource from XAConnection : " + se.getMessage());
    }

  }
}
