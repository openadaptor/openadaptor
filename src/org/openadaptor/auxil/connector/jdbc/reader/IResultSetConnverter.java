package org.openadaptor.auxil.connector.jdbc.reader;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface IResultSetConnverter {

  public Object convertNext(ResultSet rs) throws SQLException;
  public Object[] convertAll(ResultSet rs) throws SQLException;

}
