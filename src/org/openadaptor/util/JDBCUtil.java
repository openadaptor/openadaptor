package org.openadaptor.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;

public class JDBCUtil {

  public static void closeNoThrow(Statement s) {
    if (s != null) {
      try {
        s.close();
      } catch (SQLException e) {
      }
    }
  }

  public static void closeNoThrow(ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      } catch (SQLException e) {
      }
    }
  }

  public static void logResultSet(Log log, String msg, ResultSet rs) throws SQLException {
    if (log.isDebugEnabled()) {
      ResultSetMetaData rsmd = rs.getMetaData();
      if (msg != null) {
        log.debug(msg);
      }
      for (int i = 1; i <= rsmd.getColumnCount(); i++) {
        log.debug("  " + rsmd.getColumnName(i) + " (" + rsmd.getColumnClassName(i) + ") = " + rs.getString(i));
      }
    }
  }


}
