package org.openadaptor.auxil.connector.jdbc.sybase;

import java.sql.SQLException;

public class JDBCConnection extends org.openadaptor.auxil.connector.jdbc.JDBCConnection {

  public boolean isDeadlockException(SQLException e) {
    return e.toString().indexOf("deadlock victim") >= 0;
  }

  public boolean ignoreException(SQLException e) {
    return "JZ0R2".equals(e.getSQLState());
  }

}
