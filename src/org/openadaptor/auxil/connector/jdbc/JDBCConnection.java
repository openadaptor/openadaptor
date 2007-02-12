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

package org.openadaptor.auxil.connector.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.XAConnection;
import javax.sql.XADataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.exception.ConnectionException;


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
  private XADataSource xaDataSource = null;
  private boolean isTransacted = false;
  private Properties properties;
  private int deadlockCount;
  private int deadlockLimit = 3;
  private int refCount = 0;

  /** 
   * transactional resource, can either be XAResource or ITransactionalResource 
   */
  private Object transactionalResource = null;

  /**
   * create JDBC connection based on the following parameters
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
   * create JDBC connection based on an XADataSource
   * @param xaDataSource
   */
  public JDBCConnection(XADataSource xaDataSource) {
    this();
    this.xaDataSource = xaDataSource;
  }
  
  public JDBCConnection() {
  }

  public void setDriver(String driver) {this.driver = driver;}

  public void setUrl(String url) {this.url = url;}

  public void setUsername(String username) {this.username = username;}

  public void setPassword(String password) {this.password = password;}

  public void setXaDataSource(XADataSource xaDataSource) {
    this.xaDataSource = xaDataSource;
  }
  
  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  public String getDriver() { return driver; }

  public String getUrl() { return url; }

  public String getUsername() { return username; }

  public String getPassword() { return password; }

  public Properties getProperties() {
    return properties;
  }

  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  public Connection getConnection() {
    return connection;
  }

  public boolean isTransacted() {
    return isTransacted;
  }

  public void setTransacted(boolean transacted) {
    this.isTransacted = transacted;
  }

  public Object getTransactionalResource() {
    return transactionalResource;
  }

  /**
   * depending on how this object/bean as been constructed/initialise this will
   * either create a connection directly from the jdbc connection parameters, or
   * create a connection from using an XADataSource. If the component is
   * transacted then this will set the transaction resource.
   * 
   * @throws SQLException
   */
  public void connect() throws SQLException {
    if (xaDataSource != null) {
      connectViaXADataSource();
    } else {
      connectDirectly();
    }
  }
  
  /**
   * Create connection and set transactional resource from XADataSource
   * @throws SQLException
   */
  private void connectViaXADataSource() throws SQLException {
    XAConnection xaConnection = xaDataSource.getXAConnection();
    setConnection(xaConnection.getConnection());
    transactionalResource = xaConnection.getXAResource();
    connection.setAutoCommit(false);
  }
  
  /**
   * Instantiate the JDBC driver and set up connection to database using credentials
   *
   * @throws SQLException
   */
  private void connectDirectly() throws SQLException {
    if (connection == null) {

      if (driver == null) {
        throw new RuntimeException("driver not set");
      }
      if (url == null) {
        throw new RuntimeException("url not set");
      }
      if (username == null) {
        throw new RuntimeException("username not set");
      }
      if (password == null) {
        throw new RuntimeException("password not set");
      }

      //Attempt to load jdbc driver class
      try {
        Class.forName(driver);
      }
      catch (ClassNotFoundException e) {
        throw new RuntimeException("Unable to instantiate JDBC driver, " + driver + ", " + e.getMessage(), e);
      }

      //Set up properties for use with DriverManager
      Properties props;
      if (properties != null) {
        props = (Properties) properties.clone();
      } else {
        props = new Properties();
      }
      props.put("user", username);
      props.put("password", password);

      log.info("Connecting to " + url + " as " + username);
      setConnection( DriverManager.getConnection(url, props) );

      if (isTransacted()) {
        connection.setAutoCommit(false);
        transactionalResource = new JDBCTransactionalResource(this);
      }
    }
  }

  /**
   * Close the connection.
   */
  public void disconnect() throws SQLException{
    if (connection != null) {
      connection.close();
      setConnection( null );
    }
  }

  /**
   * True if there is an existing connection.
   *
   * @return boolean
   */
  public boolean isConnected() {
    return (connection != null);
  }

  /**
   * Begin database transaction by taking control of commits and rollbacks
   *
   * @throws SQLException
   */
  protected void beginTransaction() throws SQLException {
    deadlockCount = 0;
    refCount++;
  }

  /**
   * Commit database transaction
   *
   * @throws SQLException
   */
  protected void commitTransaction() throws SQLException {
    connection.commit();
    refCount--;
  }

  /**
   * Roll back database transaction
   *
   * @throws SQLException
   */
  protected void rollbackTransaction() throws SQLException {
    connection.rollback();
    refCount--;
  }

  public boolean isDeadlockException(SQLException e) {
    return false;
  }

  public boolean ignoreException(SQLException e) {
    return false;
  }

  public void setDeadlockLimit(int deadlockLimit) {
    this.deadlockLimit = deadlockLimit;
  }

  /**
   * increments deadlock count
   * @return remaining retries
   */
  protected int incrementDeadlockCount() {
    deadlockCount++;
    return getDeadlockRetriesRemaining();
  }

  /**
   * get number of deadlock retries remaining,
   * 
   * @return returns zero if no more retries are allowed also checks that it is
   *         safe to retry, returns zero if not.
   */
  protected int getDeadlockRetriesRemaining() {
    int retries = Math.max(deadlockLimit - deadlockCount, 0);
    if (retries > 1 && refCount > 1) {
      log.warn("JDBC Connection is referenced by more than one component, therefore retry is not possible");
      return 0;
    } else {
      return retries;
    }
  }
  
  /**
   * interprets SQLException, this make the distinction between
   * ConnectionExceptions (db server is unavilable) and ProcessingException (SQL
   * failed)
   * 
   * @param e
   *          the SQLException
   * @param message
   *          an additonal message to append to the RuntimeException
   */
  public void handleException(SQLException e, String message) {
    throw new ConnectionException((message != null ? message + ", " : "")
          + ", SQLException, " + e.getMessage() 
          + ", Error Code = " + e.getErrorCode()
          + ", State = " + e.getSQLState(), e, this);
  }

}
