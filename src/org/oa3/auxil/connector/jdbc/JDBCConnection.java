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
 * File: $Header: /cvs/oa3/src/org/oa3/connector/jdbc/JDBCConnection.java,v 1.11 2006/10/18 14:31:02 ottalk Exp $
 * Rev:  $Revision: 1.11 $
 * Created Oct 20, 2005 by Eddy Higgins
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oa3.core.Component;

import javax.sql.XAConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


/**
 * This is a bean which provides access to a JDBC connection.
 * It is little more than a thin wrapper around a java.sql.Connection.
 * @author Eddy Higgins.
 */
public class JDBCConnection extends Component {

  private static final Log log = LogFactory.getLog(JDBCConnection.class.getName());

  private String driver;
  private String url;
  private String username;
  private String password;
  private Connection connection = null;
  private boolean isTransacted = false;
  /** transactional resource, can either be XAResource or ITransactionalResource */
  private Object transactionalResource = null;
  /**
   * The JDBCConnection class can be initialised with all required resources
   * @param driver
   * @param url
   * @param username
   * @param password
   */
  public JDBCConnection(String driver, String url, String username, String password) {
    this();
    setDriver(driver);
    setUrl(url);
    setUsername(username);
    setPassword(password);
  }

  /**
   * Default constructor
   */
  public JDBCConnection() {} //No-arg constructor for beans

  /**
   * Set JDBC driver string
   */
  public void setDriver(String driver) {this.driver = driver;}

  /**
   * Set the URL string for the database server, generally includes server name/ip address, port number and database name
   */
  public void setUrl(String url) {this.url = url;}

  /**
   * Set database username credential
   */
  public void setUsername(String username) {this.username = username;}

  /**
   * Set database password credential
   */
  public void setPassword(String password) {this.password = password;}

  /**
   * Return JDBC driver string
   *
   * @return JDBC driver string
   */
  public String getDriver() { return driver; }

  /**
   * Return the URL string for the database server, generally includes server name/ip address, port number and database name
   *
   * @return url string
   */
  public String getUrl() { return url; }

  /**
   * Return database username credential
   *
   * @return database username credential
   */
  public String getUsername() { return username; }

  /**
   * Return database password credential
   *
   * @return database password credential
   */
  public String getPassword() { return password; }

  /**
   * Is JDBC Connection transacted
   *
   * @return boolean
   */
  public boolean isTransacted() {
    return isTransacted;
  }

  /**
   * Set connection to be transacted
   */
  public void setTransacted(boolean transacted) {
    this.isTransacted = transacted;
  }

  /**
   * returns transactional resource, can either be XAResource or ITransactionalResource
   *
   * @return transactionalResource
   */
  public Object getTransactionalResource() {
    return transactionalResource;
  }

  /**
   * Instantiate the JDBC driver and set up connection to database using credentials
   *
   * @return jdbc connection
   * @throws SQLException
   */
  public Connection connect() throws SQLException {
    
    //Attempt to load jdbc driver class
    try {
      Class.forName(driver);
    }
    catch (ClassNotFoundException cnfe) { //Rebrand error as an SQL one.
      throw new SQLException("Unable to instantiate JDBC driver: " + driver);
    }

    //Set up properties for use with DriverManager
    Properties props = new Properties();
    props.put("user", username);
    props.put("password", password);

    log.info("Connecting to " + url + " as " + username);
    connection = DriverManager.getConnection(url, props);

    if (connection instanceof XAConnection) {
      transactionalResource = ((XAConnection)connection).getXAResource();
    }

    return connection;
  }

  protected void beginTransaction() throws SQLException {
    connection.setAutoCommit(false);
  }

  protected void commitTransaction() throws SQLException {
    connection.commit();
  }

  protected void rollbackTransaction() throws SQLException {
    connection.rollback();
  }
}
