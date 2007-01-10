package org.oa3.auxil.connector.jdbc.reader;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.oa3.auxil.connector.jdbc.JDBCConnection;
import org.oa3.core.Component;
import org.oa3.core.IReadConnector;
import org.oa3.core.exception.ComponentException;
import org.oa3.core.transaction.ITransactional;

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
}
