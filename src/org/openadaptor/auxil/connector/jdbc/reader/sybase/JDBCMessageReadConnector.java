package org.openadaptor.auxil.connector.jdbc.reader.sybase;

import java.sql.SQLException;

public class JDBCMessageReadConnector extends org.openadaptor.auxil.connector.jdbc.reader.JDBCMessageReadConnector {

  public JDBCMessageReadConnector() {
    super();
  }

  public JDBCMessageReadConnector(String id) {
    super(id);
  }

  protected boolean isDeadlockException(SQLException e) {
    return e.toString().indexOf("deadlock victim") >= 0;
  }

  protected boolean ignoreException(SQLException e) {
    return "JZ0R2".equals(e.getSQLState());
  }
}
