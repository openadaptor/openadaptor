/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.jndi.JNDIConnection;
import org.openadaptor.core.Component;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.ValidationException;


/**
 * This is a bean which provides access to a JDBC connection.
 * It is little more than a thin wrapper around a java.sql.Connection.
 * <br><br>
 * It will attempt to make a connection in one of four ways depending
 * on which configuration properties have been supplied. These are an XADataSource,
 * a DataSource, a jndiConnection plus DataSource name or driver. Note that these
 * configuration options are mutually exclusive and validation will fail if conflicting
 * config options are supplied.
 * 
 * @author Eddy Higgins.
 */
public class JDBCConnection extends Component {

  private static final Log log = LogFactory.getLog(JDBCConnection.class);

  private String driver;
  
  private String url;
  
  private String username;
  
  private String password;
  
  private Connection connection = null;
  
  /* Attributes for obtaining connection from DataSource looked up in JNDI */
  private DataSource dataSource = null;
  private String dataSourceName;
  private JNDIConnection jndiConnection;
  private Context ctx;
  
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
   * Non-arg constructor.
   *
   */
  public JDBCConnection() {
  }
  
  /**
   * Constructor.
   * 
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
   * Constructor.
   * 
   * create JDBC connection based on an XADataSource
   * @param xaDataSource
   */
  public JDBCConnection(XADataSource xaDataSource) {
    this();
    this.xaDataSource = xaDataSource;
  }
  
  /**
   * Constructor.
   * 
   * create JDBC connection based on a DataSource
   * @param dataSource
   */
  public JDBCConnection(DataSource dataSource) {
    this();
    this.dataSource = dataSource;
  }  
  
  /**
   * Constructor.
   * 
   * create JDBC connection based on an JNDIConnection and DataSource name
   * @param jndiConnection
   * @param dataSourceName 
   */
  public JDBCConnection(JNDIConnection jndiConnection, String dataSourceName) {
    this();
    this.jndiConnection = jndiConnection;
    this.dataSourceName = dataSourceName;
  }
  
  public void setDriver(String driver) {this.driver = driver;}

  public void setUrl(String url) {this.url = url;}

  public void setUsername(String username) {this.username = username;}

  public void setPassword(String password) {this.password = password;}
  
  public void setJndiConnection(JNDIConnection jndiConnection) {
    this.jndiConnection = jndiConnection;
  }
  
  public void setXaDataSource(XADataSource xaDataSource) {
    this.xaDataSource = xaDataSource;
  }  
  
  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  public void setDataSourceName(String dataSourceName) {
    this.dataSourceName = dataSourceName;
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
   * or get a connection from DataSource, or create a connection from using an XADataSource. 
   * If the component is transacted then this will set the transaction resource.
   * 
   * @throws SQLException
   */
  public void connect() throws SQLException {
    if (xaDataSource != null) { 
      connectViaXADataSource();      
    } else if (dataSource != null) {
      connectViaDataSource();
    } else if ((jndiConnection != null) && (dataSourceName != null)) {
      connectViaDataSourceName();
    } else { // last Option
      connectDirectly();
    }
    if (log.isDebugEnabled()) {
      String connectionClass=(connection==null)?"<null>":connection.getClass().getName();
      log.debug("Connected (connection class is "+connectionClass);
    }
  }
  
  /**
   * Looks up DataSource in JNDI. Gets connection from DataSource.
   * 
   * @throws SQLException
   */
  private void connectViaDataSourceName() throws SQLException {
    dataSource = lookupDataSource(dataSourceName);
    log.debug("DataSource looked up in JNDI: " + dataSource);
    connectViaDataSource();
  }

  private void connectViaDataSource() throws SQLException {
    connection = dataSource.getConnection();
    log.debug("Connection obtained from DataSource: " + connection);
    connection.setAutoCommit(false);
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

  private Context getCtx() throws NamingException {
    if (ctx == null) {
      ctx = jndiConnection.connect();
    }
    return ctx;
  }
  
  private DataSource lookupDataSource( String dataSourceName ) {
    try {
      Object o = getCtx().lookup(dataSourceName);   
      return (DataSource) o;
    } catch (NamingException e) {
      String msg = "Unable to resolve DataSource for [" + dataSourceName + "]";
      log.error(msg, e);
      throw new ConnectionException(msg, e, this);
    } catch (ClassCastException cce) {
      String msg = "Object looked up at [" + dataSourceName + "] is not a DataSource. ";
      log.error(msg, cce);
      throw new ConnectionException(msg, cce, this);
    }
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }
  
  /**
   * This method checks that the current state of an implementation is
   * "meaningful".
   * 
   * @param exceptions
   *          collection to which exceptions should be added
   */
  public void validate(List exceptions) {
    // First off if an XADataSource or a DataSource is set then we are ok as
    // the other config options will not be used. For completeness we should 
    // probably require absence of or warn about the existence of other properties 
    // but leaving that for the future.
    
    // OK not proud of the following code
    
    int configFormats = 0;
    if (isConfiguredWithXaDataSource()) {configFormats = configFormats + 1;}
    if (isConfiguredWithDataSource()) {configFormats = configFormats + 1;}
    if (isConfiguredToLookupDataSource()) {configFormats = configFormats + 1;}
    if (isConfiguredForRawConnection()) {configFormats = configFormats + 1;}
    
    if (configFormats == 0) {
      exceptions.add(new ValidationException("No connection parameters have been supplied.", this));
    } else if(configFormats > 1) { 
      exceptions.add(new ValidationException("Multiple connection parameter formats have been supplied. Please choose one only", this));
    } else { // only one of the configuration formats has been supplied
      // We can ignore the xaDataSource and dataSource variants as they can't be validated any further
      if (isConfiguredToLookupDataSource()) {
        if ((jndiConnection == null) || (dataSourceName == null)) {
          exceptions.add(new ValidationException("Connection configured to lookup DataSource by name. You must supply both jndiConnection and dataSourceName.", this));
        }
      }
      if (isConfiguredForRawConnection()) {
        if (driver == null) {exceptions.add(new ValidationException("The driver must be set when connecting directly", this));}
        if (url == null) {exceptions.add(new ValidationException("The url must be set when connecting directly", this));}
        if (username == null) {exceptions.add(new ValidationException("The username must be set when connecting directly", this));}
        if (password == null) {exceptions.add(new ValidationException("The password must be set when connecting directly", this));}           
      }
    }
  }
  
  private boolean isConfiguredWithXaDataSource() {
    return xaDataSource != null;
  }
  
  private boolean isConfiguredWithDataSource() {
    return dataSource != null;
  }
  
  private boolean isConfiguredToLookupDataSource() {
    return ((jndiConnection != null) || (dataSourceName != null));
  }
  
  private boolean isConfiguredForRawConnection() {
    return ((driver != null) || (url != null) || (username != null) || (password != null));
  }
  

}
