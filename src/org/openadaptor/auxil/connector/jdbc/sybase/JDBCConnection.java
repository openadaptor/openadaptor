package org.openadaptor.auxil.connector.jdbc.sybase;

import java.sql.SQLException;

public class JDBCConnection extends org.openadaptor.auxil.connector.jdbc.JDBCConnection {

  public boolean isDeadlockException(SQLException e) {
    return e.getErrorCode() == 1205;
  }

  public boolean ignoreException(SQLException e) {
    return e.getSQLState().equals("JZ0R2");
  }

}
