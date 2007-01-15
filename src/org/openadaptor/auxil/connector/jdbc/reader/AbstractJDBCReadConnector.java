package org.openadaptor.auxil.connector.jdbc.reader;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.logging.Log;
import org.openadaptor.auxil.connector.jdbc.JDBCConnection;
import org.openadaptor.core.Component;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.core.transaction.ITransactional;

public abstract class AbstractJDBCReadConnector extends Component implements IReadConnector, ITransactional {

  private static ResultSetConverter DEFAULT_CONVERTER = new ResultSetOrderedMapConverter();
  
  private JDBCConnection jdbcConnection;
  private ResultSetConverter resultSetConverter = DEFAULT_CONVERTER;

  public AbstractJDBCReadConnector() {
    super();
  }

  public AbstractJDBCReadConnector(String id) {
    super(id);
  }

  public void setJdbcConnection(JDBCConnection connection) {
    jdbcConnection = connection;
  }

  public void setResultSetConverter(ResultSetConverter resultSetConverter) {
    this.resultSetConverter = resultSetConverter;
  }

  public void connect() {
    try {
      jdbcConnection.connect();
    } catch (SQLException sqle) {
      throw new ComponentException("Failed to establish JDBC connection - " + sqle.toString(), sqle, this);
    }
  }

  public void disconnect() throws ComponentException {
    try {
      jdbcConnection.disconnect();
    } catch (SQLException sqle) {
      throw new ComponentException("Failed to disconnect JDBC connection - " + sqle.toString(), sqle, this);
    }
  }

  public boolean isDry() {
    return false;
  }

  public Object getReaderContext() {
    return null;
  }

  public void validate(List exceptions) {
  }
  
  public Object getResource() {
    if (jdbcConnection.isTransacted()) {
      return jdbcConnection.getTransactionalResource();
    } else {
      return null;
    }
  }

  protected Object convertNext(ResultSet rs) throws SQLException {
    return resultSetConverter.convertNext(rs);
  }
  
  protected Object[] convertAll(ResultSet rs) throws SQLException {
    return resultSetConverter.convertAll(rs);
  }
  
  protected CallableStatement prepareCall(String sql) throws SQLException {
    return jdbcConnection.getConnection().prepareCall(sql);
  }
  
  protected Statement createStatement() throws SQLException {
    return jdbcConnection.getConnection().createStatement();
  }
  
  protected void checkWarnings(Log log, ResultSet rs) throws SQLException {
    SQLWarning warn = rs.getWarnings();
    if (warn != null) {
      checkWarnings(log, warn);
    }
  }
  
  protected void checkWarnings(Log log, Statement s) throws SQLException {
    SQLWarning warn = s.getWarnings();
    if (warn != null) {
      checkWarnings(log, warn);
    }
  }
  
  private  void checkWarnings(Log log, SQLWarning warning) {
    while (warning != null) {
      log.warn("SQLWarning Message: " + warning.getMessage()
        + " SQLState: " + warning.getSQLState()
        + " Vendor error code: " + warning.getErrorCode());
      warning = warning.getNextWarning();
    }
  }
}
